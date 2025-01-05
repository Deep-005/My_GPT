package com.example.my_gpt.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.my_gpt.R
import com.example.my_gpt.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        checkUser()

        // Set up the register button click listener
        binding.cirRegisterButton.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val mobile = binding.editTextMobile.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            // Validate user input
            if (validateInput(name, email, mobile, password)) {
                registerUser( name, email, password)
            }
        }

        binding.login.setOnClickListener {
            startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right)
            finish()
        }

        binding.backLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
            finish()
        }

    }

    // Function to validate user input
    private fun validateInput(name: String, email: String, mobile: String, password: String): Boolean {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password) || password.length < 6) {
            Toast.makeText(this, "Please enter a valid password (at least 6 characters)", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Function to handle user registration
    private fun registerUser(name: String,email: String, password: String) {
        binding.cirRegisterButton.isEnabled = false // Disable the button to prevent multiple clicks

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.cirRegisterButton.isEnabled = true // Re-enable the button

                if (task.isSuccessful) {
                    Toast.makeText(this, "User saved successfully!", Toast.LENGTH_SHORT).show()
                    saveUserName(name)
                    navigateToMainActivity(name)

                } else {
                    // If registration fails, display a message to the user.
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                binding.cirRegisterButton.isEnabled = true // Re-enable the button
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // Function to navigate to the main activity after successful registration
    private fun navigateToMainActivity(name: String) {
        val intent = Intent(this, DashActivity::class.java)
        intent.putExtra("USER_NAME", name)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun checkUser() {
        val firebaseUser = auth.currentUser
        if(firebaseUser!=null){
            startActivity(Intent(this,DashActivity::class.java))
            overridePendingTransition(0,0)
            finish()
        }else{
            auth.signOut()
        }
    }

    // Function to save user name in SharedPreferences
    private fun saveUserName(name: String) {
        val editor = sharedPreferences.edit()
        editor.putString("USER_NAME", name)
        editor.apply()  // Asynchronously save the name
    }

}