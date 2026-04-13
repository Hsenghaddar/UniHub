package com.example.unihub

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.unihub.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ProfileActivity manages the user's personal profile information.
 *
 * Users can update their full name, university, and profile picture.
 * It also handles account logout and integrates with the device camera
 * and gallery for image selection.
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: UserDatabaseHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    // Launcher for selecting an image from the gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val savedUri = saveImageToInternalStorage(it)
            if (savedUri != null) {
                selectedImageUri = savedUri
                ImageUtils.loadImage(this, savedUri, binding.ivProfilePicture)
            } else {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher for taking a photo with the camera
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let {
                selectedImageUri = it
                ImageUtils.loadImage(this, it, binding.ivProfilePicture)
            }
        }
    }

    // Launcher for requesting camera permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = UserDatabaseHelper(this)
        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(this)

        // Restore state after configuration changes (e.g., rotation)
        if (savedInstanceState != null) {
            cameraImageUri = savedInstanceState.getParcelable("cameraImageUri")
            selectedImageUri = savedInstanceState.getParcelable("selectedImageUri")
            selectedImageUri?.let {
                ImageUtils.loadImage(this, it, binding.ivProfilePicture)
            }
        }

        setupUniversitySpinner()
        loadUserData()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnChangePhoto.setOnClickListener {
            showImagePickerOptions()
        }

        binding.btnSaveProfile.setOnClickListener {
            updateProfile()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            sessionManager.clearSession()

            // Return to LoginActivity and clear the activity stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * Displays a dialog with options to take a new photo or choose from the gallery.
     */
    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Profile Picture")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> checkCameraPermissionAndOpen()
                1 -> startGallery()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    /**
     * Checks if camera permission is granted before launching the camera.
     */
    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Launches the system document picker for images.
     */
    private fun startGallery() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }

    /**
     * Prepares a file to store the camera image and launches the camera app.
     */
    private fun startCamera() {
        try {
            val photoFile = createImageFile()
            val uri = FileProvider.getUriForFile(
                this,
                "com.example.unihub.fileprovider",
                photoFile
            )
            cameraImageUri = uri
            takePhotoLauncher.launch(uri)
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating file for camera", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Creates a temporary image file in the external files directory.
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    /**
     * Saves an image from a given Uri into the app's internal storage for persistence.
     */
    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val photoFile = createImageFile()
            val outputStream = FileOutputStream(photoFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            FileProvider.getUriForFile(this, "com.example.unihub.fileprovider", photoFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Populates the university selection spinner from the database.
     */
    private fun setupUniversitySpinner() {
        val universities = db.getAllUniversities()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            universities
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUniversityProfile.adapter = adapter
    }

    /**
     * Loads the current user's profile data into the UI fields.
     */
    private fun loadUserData() {
        val firebaseUser = auth.currentUser ?: return
        val localUser = db.getUserByFirebaseUid(firebaseUser.uid) ?: return

        binding.etProfileFullName.setText(localUser.fullName)
        binding.etProfileEmail.setText(localUser.email)

        if (selectedImageUri == null) {
            localUser.imageUri?.let {
                try {
                    val uri = Uri.parse(it)
                    ImageUtils.loadImage(this, uri, binding.ivProfilePicture)
                    selectedImageUri = uri
                } catch (e: Exception) {
                    binding.ivProfilePicture.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }

        val universityName = db.getUniversityNameById(localUser.universityId)
        val universities = db.getAllUniversities()
        val position = universities.indexOf(universityName)

        if (position >= 0) {
            binding.spinnerUniversityProfile.setSelection(position)
        }
    }

    /**
     * Saves the updated profile information to the local SQLite database.
     */
    private fun updateProfile() {
        val firebaseUser = auth.currentUser ?: return
        val fullName = binding.etProfileFullName.text.toString().trim()
        val selectedUniversity = binding.spinnerUniversityProfile.selectedItem.toString()

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val universityId = db.getUniversityIdByName(selectedUniversity)
        if (universityId == -1) {
            Toast.makeText(this, "Invalid university", Toast.LENGTH_SHORT).show()
            return
        }

        val updated = db.updateUser(firebaseUser.uid, fullName, universityId)
        
        selectedImageUri?.let {
            db.updateUserImage(firebaseUser.uid, it.toString())
        }

        if (updated) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Profile update failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("cameraImageUri", cameraImageUri)
        outState.putParcelable("selectedImageUri", selectedImageUri)
    }
}
