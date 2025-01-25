package com.example.servify

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog

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
import java.io.InputStream

class EditServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditServiceBinding

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

        // Enable EditText on right drawable click


        // Set upload button to open image picker


        // Set save button click listener
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveDetails()
            }
        }

        // Fetch and display user data
        fetchData()
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val businessTitle = binding.etBusinessName.text.toString().trim()
        val businessDescription = binding.etBusinessDescription.text.toString().trim()
        val price=binding.etPrice.text.toString().trim()

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
        if(price.isEmpty() || price.length<2){
            binding.etPrice.error="Price Shold be grator than 10/-"
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
        val price=binding.etPrice.text.toString()

        // Show progress dialog while saving data
        progressDialog.setMessage("Saving details...")
        progressDialog.show()

        // Create a map with the updated values
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "mobile" to mobile,
            "address" to address,
            "password" to password,
            "businessTitle" to businessTitle,
            "businessDescription" to businessDescription,
            "price" to price
        )

        // Check if a new image is selected; otherwise, do not update the image field


        // Update data in Firebase without overwriting the existing image field
        database.child(mobile).updateChildren(updates)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Details updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to update details: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun encodeImageToBase64(uri: Uri): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        inputStream?.copyTo(byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)
        return encodedImage
    }

    private fun fetchData() {
        progressDialog.show()

        val mobile = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_MOBILE, null)
        if (mobile.isNullOrEmpty()) {
            progressDialog.dismiss()
            Toast.makeText(this, "No mobile number found. Please log in again.", Toast.LENGTH_SHORT)
                .show()
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
                    Toast.makeText(
                        this@EditServiceActivity,
                        "No data found for this user.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(
                    this@EditServiceActivity,
                    "Failed to load data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
        binding.etPrice.setText(userData.price)

        // Set image (Base64 to Bitmap)
//        try {
//            val decodedBytes = android.util.Base64.decode(userData.businessImage, android.util.Base64.DEFAULT)
//            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
//
//            if (bitmap != null) {
//                binding.etBusinessImage.setImageBitmap(bitmap)
//            } else {
//                binding.etBusinessImage.setImageResource(R.drawable.servifylofo)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            binding.etBusinessImage.setImageResource(R.drawable.servifylofo)
//        }
    }

    // Handle permission result

}

data class UserData(
    val name: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val address: String? = null,
    val password: String? = null,
    val businessTitle: String? = null,
    val businessDescription: String? = null,
    val price:String?=null

)
