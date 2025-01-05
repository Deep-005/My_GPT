package com.example.my_gpt.Application

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class Global : Application() {

    var textCount: Int = 0
    var imgCount: Int = 0
    var summCount: Int = 0

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("global_prefs", Context.MODE_PRIVATE)

        // Load saved values
        textCount = sharedPreferences.getInt("text_count", 0)
        imgCount = sharedPreferences.getInt("img_count", 0)
        summCount = sharedPreferences.getInt("summ_count", 0)
    }

    // Save the current values into SharedPreferences
    fun saveCounts() {
        val editor = sharedPreferences.edit()
        editor.putInt("text_count", textCount)
        editor.putInt("img_count", imgCount)
        editor.putInt("summ_count", summCount)
        editor.apply()
    }
}
