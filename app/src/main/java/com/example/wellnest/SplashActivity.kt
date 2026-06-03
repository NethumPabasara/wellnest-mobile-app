package com.example.wellnest

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Access SharedPreferences
        val sharedPref: SharedPreferences = getSharedPreferences("WellNestPrefs", MODE_PRIVATE)
        val isFirstLaunch = sharedPref.getBoolean("isFirstLaunch", true)

        // Delay splash for 2.5 seconds
        Handler(Looper.getMainLooper()).postDelayed({

            if (isFirstLaunch) {
                // First launch → go to Onboarding
                val editor = sharedPref.edit()
                editor.putBoolean("isFirstLaunch", false) // mark as completed
                editor.apply()

                startActivity(Intent(this, Onboarding1Activity::class.java))
            } else {
                // Not first time → go to Home
                startActivity(Intent(this, MainActivity::class.java))
            }

            finish() // close splash so user can't go back here
        }, 2500) // 2500 ms = 2.5 seconds
    }
}
