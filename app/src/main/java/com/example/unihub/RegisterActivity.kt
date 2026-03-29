package com.example.unihub

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unihub.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: UserDatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = UserDatabaseHelper(this)
        sessionManager = SessionManager(this)

        setupUniversitySpinner()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.btnGoToLogin.setOnClickListener {
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
        binding.spinnerUniversity.adapter = adapter
    }

    private fun registerUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val selectedUniversity = binding.spinnerUniversity.selectedItem.toString()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val universityId = db.getUniversityIdByName(selectedUniversity)
        if (universityId == -1) {
            Toast.makeText(this, "Invalid university selected", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.btnRegister.isEnabled = true

                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser

                    if (firebaseUser != null) {
                        val inserted = db.insertUser(
                            firebaseUid = firebaseUser.uid,
                            fullName = fullName,
                            email = email,
                            universityId = universityId
                        )

                        if (inserted) {
                            sessionManager.saveUserSession(firebaseUser.uid)

                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            Toast.makeText(this, "User saved in Firebase but failed in SQLite", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Registration failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}