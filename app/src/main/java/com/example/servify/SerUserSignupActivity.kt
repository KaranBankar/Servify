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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.servify.databinding.ActivitySerUserSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream

class SerUserSignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySerUserSignupBinding
    private val READ_STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    private val READ_MEDIA_IMAGES_PERMISSION = android.Manifest.permission.READ_MEDIA_IMAGES
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val REQUEST_MEDIA_IMAGES_PERMISSION = 100
    private val REQUEST_STORAGE_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySerUserSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigate to login page
        binding.tvLoginPage.setOnClickListener {
            startActivity(Intent(this@SerUserSignupActivity, LoginActivity::class.java))
            finish()
        }

        // Sign up button click event
        binding.btnSignup.setOnClickListener {
            if (validateInputs()) {
                val imageUri = binding.businessImage.tag as? Uri
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
        binding.btnUpload.setOnClickListener {
            checkAndRequestPermissions()
        }
    }

    // Check and request permissions based on Android version
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API level 33 and above)
            if (ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES_PERMISSION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_MEDIA_IMAGES_PERMISSION),
                    REQUEST_MEDIA_IMAGES_PERMISSION
                )
            } else {
                openImagePicker()
            }
        } else {
            // For Android 12 (API level 32) and below
            if (ContextCompat.checkSelfPermission(this, READ_STORAGE_PERMISSION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_STORAGE_PERMISSION),
                    REQUEST_STORAGE_PERMISSION
                )
            } else {
                openImagePicker()
            }
        }
    }

    // Handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_MEDIA_IMAGES_PERMISSION, REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    Toast.makeText(this, "Permission denied. Unable to pick an image.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Open image picker using the Storage Access Framework (SAF)
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
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

    // Display the selected image in the ImageView
    private fun displaySelectedImage(imageUri: Uri) {
        Glide.with(this).load(imageUri).into(binding.businessImage)
        binding.businessImage.tag = imageUri // Store the URI in tag for later use
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


        val user = User(name, email, mobile, address, password, encodedImage)

        val databaseRef = FirebaseDatabase.getInstance().getReference("users")

        // Check if the user already exists
        databaseRef.child(mobile).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    // User with this mobile number already exists
                    progressDialog.dismiss()
                    Toast.makeText(this, "User with this mobile number already exists.", Toast.LENGTH_SHORT).show()
                } else {
                    // Save new user data
                    databaseRef.child(mobile).setValue(user)
                        .addOnCompleteListener { saveTask ->
                            progressDialog.dismiss()
                            if (saveTask.isSuccessful) {
                                Toast.makeText(this, "User signed up successfully!", Toast.LENGTH_SHORT).show()
                                // Navigate to another activity after successful signup
                                startActivity(Intent(this@SerUserSignupActivity, LoginActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Error checking user existence. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Validate inputs for the signup form
    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

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

        return true
    }

    // Validate password strength (minimum 8 characters with 1 uppercase, 1 number, and 1 special character)
    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=]).{8,}\$")
        return password.matches(passwordPattern)
    }
}

// User data class to store the user details including image
data class User(
    val name: String,
    val email: String,
    val mobile: String,
    val address: String,
    val password: String,
    val image: String // Image URL or Base64 encoded string
)
