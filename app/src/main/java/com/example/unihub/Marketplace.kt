package com.example.unihub

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.unihub.databinding.ActivityMarketplaceBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Marketplace : AppCompatActivity() {

    private lateinit var binding: ActivityMarketplaceBinding
    private lateinit var db: UserDatabaseHelper
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivSelectedImage.setImageURI(it)
            binding.layoutPickHint.visibility = View.GONE
            
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {}
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedImageUri = cameraImageUri
            binding.ivSelectedImage.setImageURI(selectedImageUri)
            binding.layoutPickHint.visibility = View.GONE
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarketplaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = UserDatabaseHelper(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.cardImagePicker.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnAddItem.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser == null) {
                Toast.makeText(this, "Please log in to post items", Toast.LENGTH_LONG).show()
            } else {
                addItem()
            }
        }

        binding.btnBrowseMarketplace.setOnClickListener {
            startActivity(Intent(this, Marketplace_Display_Items::class.java))
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> pickImageLauncher.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "com.example.unihub.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(cameraImageUri)
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun addItem() {
        val title = binding.etItemTitle.text.toString().trim()
        val description = binding.etItemDescription.text.toString().trim()
        val category = binding.etItemCategory.text.toString().trim()
        val priceStr = binding.etItemPrice.text.toString().trim()
        val stockStr = binding.etItemStock.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || category.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()

        if (price == null || stock == null) {
            Toast.makeText(this, "Invalid price or stock", Toast.LENGTH_SHORT).show()
            return
        }

        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val success = db.insertMarketplaceItem(
            title, description, category, price, stock, userUid,
            selectedImageUri?.toString()
        )

        if (success) {
            Toast.makeText(this, "Item posted successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to post item", Toast.LENGTH_SHORT).show()
        }
    }
}
