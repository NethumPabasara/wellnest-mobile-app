package com.example.wellnest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Onboarding1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        // Find the Next button
        val nextBtn = findViewById<Button>(R.id.btnNext1)

        // When clicked, go to Onboarding2Activity
        nextBtn.setOnClickListener {
            startActivity(Intent(this, Onboarding2Activity::class.java))
            finish() // close this activity so user can't go back
        }
    }
}
