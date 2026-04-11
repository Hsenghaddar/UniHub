package com.example.unihub

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.unihub.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: UserDatabaseHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let {
                try {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: SecurityException) {
                    // Handle case where permission cannot be persisted (e.g. some third party apps)
                }
                binding.ivProfilePicture.setImageURI(it)
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let {
                selectedImageUri = it
                binding.ivProfilePicture.setImageURI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = UserDatabaseHelper(this)
        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(this)

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

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Profile Picture")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> startCamera()
                1 -> startGallery()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun startCamera() {
        try {
            val photoFile = createImageFile()
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "com.example.unihub.fileprovider",
                photoFile
            )
            takePhotoLauncher.launch(cameraImageUri)
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating file for camera", Toast.LENGTH_SHORT).show()
        }
    }

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

    private fun loadUserData() {
        val firebaseUser = auth.currentUser ?: return
        val localUser = db.getUserByFirebaseUid(firebaseUser.uid) ?: return

        binding.etProfileFullName.setText(localUser.fullName)
        binding.etProfileEmail.setText(localUser.email)

        localUser.imageUri?.let {
            binding.ivProfilePicture.setImageURI(Uri.parse(it))
        }

        val universityName = db.getUniversityNameById(localUser.universityId)
        val universities = db.getAllUniversities()
        val position = universities.indexOf(universityName)

        if (position >= 0) {
            binding.spinnerUniversityProfile.setSelection(position)
        }
    }

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
}
