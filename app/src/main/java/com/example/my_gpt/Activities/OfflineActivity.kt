package com.example.my_gpt.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.my_gpt.databinding.ActivityOfflineBinding

class OfflineActivity : AppCompatActivity() {

    lateinit var binding : ActivityOfflineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }


    override fun onResume() {
        super.onResume()
        binding.offlineAnime.playAnimation()
    }

    override fun onPause() {
        super.onPause()
        binding.offlineAnime.pauseAnimation()
    }

    override fun onStop() {
        super.onStop()
        binding.offlineAnime.cancelAnimation()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

}