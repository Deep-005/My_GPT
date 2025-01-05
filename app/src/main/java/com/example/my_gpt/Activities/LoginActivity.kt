package com.example.my_gpt.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.my_gpt.R
import com.example.my_gpt.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        if (isConnectedToInternet(this)) {
            checkUser()
        } else {
            val intent = Intent(this, OfflineActivity::class.java)
            startActivity(intent)
        }

        // Set up the login button click listener
        binding.LoginButton.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            // Check if email and password are not empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                // Show error message
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.newUser.setOnClickListener {
            startActivity(Intent(this@LoginActivity,RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
            finish()
        }

        binding.addUser.setOnClickListener {
            startActivity(Intent(this@LoginActivity,RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
            finish()
        }
    }

    // Function to handle user login with Firebase
    private fun loginUser(email: String, password: String) {
        binding.LoginButton.isEnabled = false // Disable the button to prevent multiple clicks

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.LoginButton.isEnabled = true // Re-enable the button

                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity,DashActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                binding.LoginButton.isEnabled = true // Re-enable the button
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
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

    // Function to check if the device is connected to the internet
    private fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

}