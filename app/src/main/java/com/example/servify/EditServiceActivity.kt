package com.example.servify

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.servify.databinding.ActivityEditServiceBinding
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream

class EditServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditServiceBinding
    private val READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    private var selectedImageUri: Uri? = null
    private lateinit var database: DatabaseReference
    private lateinit var progressDialog: ProgressDialog

    companion object {
        private const val PREF_NAME = "LoginPreferences"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_TYPE = "userType"
        private const val KEY_MOBILE = "mobile"
        private const val KEY_PASSWORD = "password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("users")
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading data...")
        progressDialog.setCancelable(false)

        // Initially disable EditText
        binding.etName.isEnabled = false

        // Enable EditText on right drawable click
        binding.etName.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.etName.compoundDrawablesRelative[2]
                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.bounds.width()
                    if (event.rawX >= (binding.etName.right - drawableWidth - binding.etName.paddingEnd)) {
                        binding.etName.isEnabled = true
                        binding.etName.requestFocus()
                        binding.etName.setSelection(binding.etName.text.length) // Place cursor at the end
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        // Set upload button to open image picker
        binding.tvUpload.setOnClickListener {
            handleImagePicker()
        }

        // Set save button click listener
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveDetails()
            }
        }

        // Fetch and display user data
        fetchData()
    }

    private fun handleImagePicker() {
        if (ContextCompat.checkSelfPermission(this, READ_STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(READ_STORAGE_PERMISSION), 100)
        } else {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data!!.data
                displaySelectedImage()
            }
        }

    private fun displaySelectedImage() {
        if (selectedImageUri != null) {
            binding.etBusinessImage.setImageURI(selectedImageUri)
            Toast.makeText(this, "Image displayed successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to display image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val businessTitle = binding.etBusinessName.text.toString().trim()
        val businessDescription = binding.etBusinessDescription.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name cannot be empty"
            binding.etName.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            binding.etEmail.requestFocus()
            return false
        }

        if (mobile.isEmpty() || mobile.length != 10) {
            binding.etMobile.error = "Enter a valid 10-digit mobile number"
            binding.etMobile.requestFocus()
            return false
        }

        if (address.isEmpty()) {
            binding.etAddress.error = "Address cannot be empty"
            binding.etAddress.requestFocus()
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return false
        }

        if (businessTitle.isEmpty()) {
            binding.etBusinessName.error = "Business title cannot be empty"
            binding.etBusinessName.requestFocus()
            return false
        }

        if (businessDescription.isEmpty()) {
            binding.etBusinessDescription.error = "Business description cannot be empty"
            binding.etBusinessDescription.requestFocus()
            return false
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveDetails() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val mobile = binding.etMobile.text.toString()
        val address = binding.etAddress.text.toString()
        val password = binding.etPassword.text.toString()
        val businessTitle = binding.etBusinessName.text.toString()
        val businessDescription = binding.etBusinessDescription.text.toString()

        // Show progress dialog while saving data
        progressDialog.setMessage("Saving details...")
        progressDialog.show()

        // Convert image to Base64
        val base64Image = selectedImageUri?.let { encodeImageToBase64(it) } ?: ""

        val userData = UserData(name, email, mobile, address, password, businessTitle, businessDescription, base64Image)

        // Save data to Firebase
        database.child(mobile).setValue(userData)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Details updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun encodeImageToBase64(uri: Uri): String {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun fetchData() {
        progressDialog.show()

        val mobile = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_MOBILE, null)
        if (mobile.isNullOrEmpty()) {
            progressDialog.dismiss()
            Toast.makeText(this, "No mobile number found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(mobile).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()
                if (snapshot.exists()) {
                    val userData = snapshot.getValue(UserData::class.java)
                    if (userData != null) {
                        setDataToFields(userData)
                    }
                } else {
                    Toast.makeText(this@EditServiceActivity, "No data found for this user.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@EditServiceActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setDataToFields(userData: UserData) {
        binding.etName.setText(userData.name)
        binding.etEmail.setText(userData.email)
        binding.etMobile.setText(userData.mobile)
        binding.etAddress.setText(userData.address)
        binding.etPassword.setText(userData.password)
        binding.etBusinessName.setText(userData.businessTitle)
        binding.etBusinessDescription.setText(userData.businessDescription)

        // Set image (Base64 to Bitmap)
        try {
            val decodedBytes = android.util.Base64.decode(userData?.businessImage, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap != null) {
                binding.etBusinessImage.setImageBitmap(bitmap) // Correctly set Bitmap to ImageView
            } else {
                binding.etBusinessImage.setImageResource(R.drawable.servifylofo) // Default image if null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.etBusinessImage.setImageResource(R.drawable.servifylofo) // Default image on error
        }
//        if (!userData.businessImage.isNullOrEmpty()) {
//            val decodedImage = decodeBase64ToBitmap(userData.businessImage)
//            binding.etBusinessImage.setImageBitmap(decodedImage)
//        }
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied to read storage", Toast.LENGTH_SHORT).show()
        }
    }
}

data class UserData(
    val name: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val address: String? = null,
    val password: String? = null,
    val businessTitle: String? = null,
    val businessDescription: String? = null,
    val businessImage: String? = null
)
