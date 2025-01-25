package com.example.servify

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.servify.databinding.ActivityEditServiceBinding

class EditServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditServiceBinding
    private val READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        if (name.isEmpty()) {
            binding.etName.error = "Name cannot be empty"
            binding.etName.requestFocus()
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
        val imageUri = selectedImageUri.toString()

        // Handle saving logic here (e.g., save to database or send to server)
        Toast.makeText(this, "Details saved successfully!\nName: $name\nImage: $imageUri", Toast.LENGTH_LONG).show()
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
