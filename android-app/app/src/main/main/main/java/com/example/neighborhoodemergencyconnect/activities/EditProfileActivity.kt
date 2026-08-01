package com.example.neighborhoodemergencyconnect.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityEditProfileBinding
import com.example.neighborhoodemergencyconnect.models.UpdateProfileRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class EditProfileActivity : AppCompatActivity() {
    private var uploadedImageUrl: String? = null
    private var selectedImageUri: Uri? = null
    private var isUploadingImage = false


    private lateinit var binding : ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        loadProfile()
        binding.btnSaveChanges.setOnClickListener {
            updateProfile()
        }
        binding.fabEditPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        }
    private fun updateProfile() {
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        if (isUploadingImage) {
            Toast.makeText(
                this,
                "Please wait, image upload is still in progress",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(
                this,
                "All fields are required",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        lifecycleScope.launch {
            try {


                val sharedPreferences = getSharedPreferences("NEC_APP", MODE_PRIVATE)
                val token = sharedPreferences.getString("token", null)

                binding.btnSaveChanges.isEnabled = false
                Log.d("SENDING_URL", "SENDING URL = $uploadedImageUrl")
                val response =
                    RetrofitInstance.api.updateProfile(
                        token!!,
                        UpdateProfileRequest(name, email, uploadedImageUrl)
                    )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@EditProfileActivity,
                        response.body()?.message
                            ?: "Profile Updated",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadProfile()

                    finish()

                } else {

                    Toast.makeText(
                        this@EditProfileActivity,
                        "Error ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@EditProfileActivity,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()

            }
            binding.btnSaveChanges.isEnabled = true
        }
    }

    private fun loadProfile() {
        lifecycleScope.launch {

            try {
                val prefs = getSharedPreferences("NEC_APP", MODE_PRIVATE)
                val token = prefs.getString("token", null)

                val response = RetrofitInstance.api.loadProfile(token!!)
                if (response.isSuccessful) {

                    response.body()?.let {

                        binding.etFullName.setText(it.user.name)
                        binding.etEmail.setText(it.user.email)

                        if (uploadedImageUrl.isNullOrEmpty()) {
                            uploadedImageUrl = it.user.profileImage
                        }
                        Log.d("DB_URL", "DB URL = ${it.user.profileImage}")

                    }

                } else {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@EditProfileActivity,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

    }
    private val imagePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {

                selectedImageUri = uri

                binding.ivProfileImage.setImageURI(uri)

                lifecycleScope.launch {

                    try {
                        val inputStream =
                            contentResolver.openInputStream(uri)

                        val bytes = inputStream?.readBytes()

                        if (bytes != null) {

                            val requestBody =
                                bytes.toRequestBody(
                                    "image/*".toMediaTypeOrNull()
                                )

                            val imagePart =
                                MultipartBody.Part.createFormData(
                                    "image",
                                    "profile.jpg",
                                    requestBody
                                )
                            isUploadingImage = true
                            binding.btnSaveChanges.text = "Please wait while uploading..."


                            val response =
                                RetrofitInstance.api.uploadImage(
                                    imagePart
                                )

                            if (response.isSuccessful) {
                                uploadedImageUrl =
                                    response.body()?.imageUrl
                                isUploadingImage = false
                                binding.btnSaveChanges.text = "Save Changes"




                            } else {
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Upload Failed",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }

                        }

                    } catch (e: Exception) {

                        Log.e(
                            "UPLOAD_ERROR",
                            "Upload Failed",
                            e
                        )

                        Toast.makeText(
                            this@EditProfileActivity,
                            e.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

            }

        }
}