package com.example.servify

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.servify.databinding.ActivitySerproSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream

class SerproSignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySerproSignupBinding
    private val READ_STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    private val READ_MEDIA_IMAGES_PERMISSION = android.Manifest.permission.READ_MEDIA_IMAGES
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySerproSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigate to login page
        binding.tvLoginPage.setOnClickListener {
            startActivity(Intent(this@SerproSignupActivity, LoginActivity::class.java))
            finish()
        }

        // Sign up button click event
        binding.btnSignup.setOnClickListener {
            if (validateInputs()) {
                val imageUri = binding.etBusinessImage.tag as? Uri
                if (imageUri != null) {
                    // Show the ProgressDialog
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Saving data... Please wait.")
                    progressDialog.setCancelable(false)
                    progressDialog.show()

                    convertImageToBase64(imageUri, progressDialog)
                } else {
                    Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Upload button to select image
        binding.tvUpload.setOnClickListener {
            if (hasStoragePermission()) {
                openImagePicker()
            } else {
                requestStoragePermission()
            }
        }
    }

    // Check if the app has the appropriate storage permission
    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES_PERMISSION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, READ_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Request storage permission if not granted
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(READ_MEDIA_IMAGES_PERMISSION), 1001)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(READ_STORAGE_PERMISSION), 1001)
        }
    }

    // Open image picker to select an image
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    // Image picker activity result handler
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            if (selectedImageUri != null) {
                displaySelectedImage(selectedImageUri)
            } else {
                Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Display selected image in ImageView
    private fun displaySelectedImage(imageUri: Uri) {
        Glide.with(this).load(imageUri).into(binding.etBusinessImage)
        binding.etBusinessImage.tag = imageUri // Store URI in tag to use later
    }

    // Convert image to Base64 format and save user data
    private fun convertImageToBase64(imageUri: Uri, progressDialog: ProgressDialog) {
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        inputStream?.copyTo(byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)
        // Store the user data including Base64 encoded image
        saveUserDataToFirebase(encodedImage, progressDialog)
    }

    // Save user data (including Base64 encoded image) to Firebase Realtime Database
    private fun saveUserDataToFirebase(encodedImage: String, progressDialog: ProgressDialog) {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val business_title = binding.etBusinessName.text.toString().trim()
        val business_description = binding.etBusinessDescription.text.toString().trim()
        val price=binding.etPrice.text.toString().trim()

        val user = User1(
            name,
            email,
            mobile,
            address,
            password,
            encodedImage,
            business_title,
            business_description,
            price
        )

        val databaseRef = FirebaseDatabase.getInstance().getReference("users")

        // Check if the user already exists in the database
        databaseRef.child(mobile).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                // User already exists
                progressDialog.dismiss()
                Toast.makeText(this, "A user with this mobile number already exists.", Toast.LENGTH_SHORT).show()
            } else {
                // User does not exist, proceed to save data
                databaseRef.child(mobile).setValue(user)
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (task.isSuccessful) {
                            Toast.makeText(this, "User signed up successfully!", Toast.LENGTH_SHORT).show()
                            // Navigate to another activity after successful signup
                        } else {
                            Toast.makeText(this, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }.addOnFailureListener { exception ->
            progressDialog.dismiss()
            Toast.makeText(this, "An error occurred: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Validate inputs for the signup form
    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val price=binding.etPrice.text.toString().trim()
        val business_title = binding.etBusinessName.text.toString().trim()
        val business_description = binding.etBusinessDescription.text.toString().trim()

        if (name.isEmpty() || name.length < 2) {
            binding.etName.error = "Enter a valid name (at least 2 characters)."
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email address."
            return false
        }

        if (mobile.isEmpty() || !mobile.matches(Regex("^\\d{10}$"))) {
            binding.etMobile.error = "Enter a valid 10-digit mobile number."
            return false
        }

        if (address.isEmpty()) {
            binding.etAddress.error = "Address cannot be empty."
            return false
        }

        if (password.isEmpty() || !isValidPassword(password)) {
            binding.etPassword.error = "Password must be at least 8 characters with 1 uppercase, 1 number, and 1 special character."
            return false
        }

        if (business_title.isEmpty() || business_title.length < 5) {
            binding.etBusinessName.error = "Business Name At Least 5 chars"
            return false
        }
        if (business_description.isEmpty() || business_description.length < 5) {
            binding.etBusinessDescription.error = "Business Description At Least 5 chars"
            return false
        }
        if(price.isEmpty() || price.length<2){
            binding.etPrice.error="Price Shold be grator than 10/-"
            return false
        }

        return true
    }

    // Validate password strength (minimum 8 characters with 1 uppercase, 1 number, and 1 special character)
    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=]).{8,}\$")
        return password.matches(passwordPattern)
    }
}

// User data class to store the user details including image
data class User1(
    val name: String = "", // Provide default values
    val email: String = "",
    val mobile: String = "",
    val address: String = "",
    val password: String = "",
    val image: String = "", // Image URL or Base64 encoded string
    val businessTitle: String = "",
    val businessDescription: String = "",
    val price:String=""
)
