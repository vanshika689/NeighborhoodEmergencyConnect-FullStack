package com.example.neighborhoodemergencyconnect.activities
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityAddAlertBinding
import com.example.neighborhoodemergencyconnect.models.AddAlertReq
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddAlert : AppCompatActivity() {
    private var fullAddress: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    var selectedImageUri: Uri? = null
    var uploadedImageUrl : String? = null
    private lateinit var binding: ActivityAddAlertBinding
    private val locationPickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                val data = result.data

                 latitude =
                    data?.getDoubleExtra("latitude", 0.0)

                 longitude =
                    data?.getDoubleExtra("longitude", 0.0)

                 val Shortaddress = data?.getStringExtra("Shortaddress")
                fullAddress = data?.getStringExtra("fullAddress")

                binding.etLocation.setText(
                    Shortaddress
                )
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {

     val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
                uri ->           ///opens gallery
            if(uri != null){
                selectedImageUri = uri
                lifecycleScope.launch {

                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()

                        if (bytes != null) {
                            val requestBody = bytes.toRequestBody(
                                "image/*".toMediaTypeOrNull()
                            )

                            val imagePart = MultipartBody.Part.createFormData(
                                "image",
                                "upload.jpg",
                                requestBody
                            )

                            val response =
                                RetrofitInstance.api.uploadImage(imagePart)

                            if (response.isSuccessful) {
                                uploadedImageUrl = response.body()?.imageUrl
                                binding.ImageText.setText("Image Selected Successfully")


                            } else {

                                Toast.makeText(this@AddAlert, "Upload failed", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    } catch (e: Exception) {

                        Toast.makeText(this@AddAlert, e.message, Toast.LENGTH_SHORT
                        ).show()
                    }
                }


            }
        }
        binding = ActivityAddAlertBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.etLocation.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            locationPickerLauncher.launch(intent)

        }
        binding.btnUploadImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        binding.layoutFire.setOnClickListener {
            selectCategory(binding.layoutFire, "Fire")
            binding.etTitle.setText("Fire")
        }

        binding.layoutAccident.setOnClickListener {
            selectCategory(binding.layoutAccident, "Accident")
            binding.etTitle.setText("Accident")
        }

        binding.layoutDisaster.setOnClickListener {
            selectCategory(binding.layoutDisaster, "Disaster")
            binding.etTitle.setText("Disaster")
        }

        binding.layoutCrime.setOnClickListener {
            selectCategory(binding.layoutCrime, "Crime")
            binding.etTitle.setText("Crime")
        }

        binding.layoutOther.setOnClickListener {
            selectCategory(binding.layoutOther, "Other")
            binding.etTitle.setText("Other")
        }
        binding.Cancel.setOnClickListener {
            finish()
        }
        binding .btnSubmitAlert.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()

            if (title.isEmpty()) {
               Toast.makeText(this, "Please select Category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                binding.etDescription.error = "Description is required"
                return@setOnClickListener
            }
            if (location.isEmpty()) {
                binding.etLocation.error = "Location is required"
                return@setOnClickListener
            }
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (uploadedImageUrl == null) {
                Toast.makeText(
                    this,
                    "Please wait for image upload to complete",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }


            val request = AddAlertReq(
                title = title,
                description = description,
                shortAddress = binding.etLocation.text.toString().trim(),
                fullAddress = fullAddress ?: "",
                latitude = latitude ?: 0.0,
                longitude = longitude ?: 0.0,
                imageUrl = uploadedImageUrl ?: ""
            )

            val sharedPreferences = getSharedPreferences("NEC_APP", MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            if (token == null) {
                Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.AddAlert("$token",request)
                    if (response.isSuccessful) {
                        val alertResponse = response.body()
                        Toast.makeText(
                            this@AddAlert,
                            alertResponse?.message ?: "Alert Created Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.etTitle.text?.clear()
                        binding.etDescription.text?.clear()
                        binding.etLocation.text?.clear()
                        finish()
                    } else {
                        Toast.makeText(this@AddAlert, "Failed to create alert", Toast.LENGTH_SHORT).show()

                    }
                } catch(e: Exception) {
                    Toast.makeText(this@AddAlert, e.message, Toast.LENGTH_SHORT).show()
                }
            }

        }

    }


    fun resetSelection() {
        binding.layoutFire.setBackgroundResource(R.drawable.category_normal)
        binding.layoutAccident.setBackgroundResource(R.drawable.category_normal)
        binding.layoutDisaster.setBackgroundResource(R.drawable.category_normal)
        binding.layoutCrime.setBackgroundResource(R.drawable.category_normal)
        binding.layoutOther.setBackgroundResource(R.drawable.category_normal)
    }
    private fun selectCategory(
        view: LinearLayout,
        category: String
    ) {

        resetSelection()
        view.setBackgroundResource(
            R.drawable.titleselected
        )

        binding.etTitle.setText(category)
    }
}