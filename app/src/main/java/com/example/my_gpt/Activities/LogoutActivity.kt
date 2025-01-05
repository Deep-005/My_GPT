package com.example.my_gpt.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.my_gpt.R
import com.google.firebase.auth.FirebaseAuth

class LogoutActivity : AppCompatActivity() {

    private lateinit var back: ImageView
    private lateinit var logout: Button
    private lateinit var mail: TextView
    private lateinit var uName: TextView
    private lateinit var profile: ImageView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("USER_NAME", "Guest")
        uName = findViewById(R.id.user_name)
        uName.text = "Username: ${userName}"

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize views
        back = findViewById(R.id.back)
        logout = findViewById(R.id.logout)
        mail = findViewById(R.id.user_mail)
        profile = findViewById(R.id.user_profile)

        // Set onClickListeners
        back.setOnClickListener {
            startActivity(Intent(this, DashActivity::class.java))
            finish()
        }

        logout.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Update UI with current user's email
        updateData()
    }

    override fun onResume() {
        super.onResume()
        updateData()
    }

    @SuppressLint("SetTextI18n")
    private fun updateData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            mail.text = "${currentUser.email}"
        } else {
            mail.text = getString(R.string.no_email)
        }
    }


}