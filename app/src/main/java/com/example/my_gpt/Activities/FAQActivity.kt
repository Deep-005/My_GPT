package com.example.my_gpt.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.my_gpt.databinding.ActivityFaqBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class FAQActivity : AppCompatActivity() {

    lateinit var binding: ActivityFaqBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "FAQ's"

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        editTextListener()
        cardViewClickListeners()
        submitButtonClickListener()
    }

    // Setup listener for EditText to show/hide the submit button
    private fun editTextListener() {
        binding.input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.submit.visibility = View.GONE
                } else {
                    binding.submit.visibility = View.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed here
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed here
            }
        })
    }

    // Setup listeners for each CardView click to update the EditText
    private fun cardViewClickListeners() {
        // List of CardView elements to set click listeners on
        val cardViews = listOf(binding.q1, binding.q2, binding.q3, binding.q4, binding.q5, binding.q6, binding.q7, binding.q8, binding.q9, binding.q10)
            for (cardView in cardViews) {
                cardView.setOnClickListener {
                    // Since each CardView only contains one TextView, we can get the first child
                    val textView = cardView.getChildAt(0) as? TextView
                    textView?.let {
                        // Set the text from TextView to the EditText
                        binding.input.setText(it.text.toString())
                    }
                }
            }
    }

    // Setup submit button click listener to send data to the database
    private fun submitButtonClickListener() {
        binding.submit.setOnClickListener {
            val userInput = binding.input.text.toString()
            if (userInput.isNotEmpty()) {
                sendDataToDatabase(userInput)
            }
        }
    }

    private fun sendDataToDatabase(question: String) {
        val db = FirebaseFirestore.getInstance()
        val faqCollection = db.collection("FAQ")

        val questionData = hashMapOf(
            "question" to question
        )

        faqCollection.add(questionData)
            .addOnSuccessListener {
                Toast.makeText(this, "We will reach out to u with your answer soon.", Toast.LENGTH_SHORT).show()
                binding.input.text.clear() // Clear input after sending
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error sending question: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        binding.lottie.playAnimation()
    }

    override fun onPause() {
        super.onPause()
        binding.lottie.pauseAnimation()
    }

    override fun onStop() {
        super.onStop()
        binding.lottie.cancelAnimation()
    }
}
