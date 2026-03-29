package com.example.unihub

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unihub.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: UserDatabaseHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

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

        if (updated) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Profile update failed", Toast.LENGTH_SHORT).show()
        }
    }
}