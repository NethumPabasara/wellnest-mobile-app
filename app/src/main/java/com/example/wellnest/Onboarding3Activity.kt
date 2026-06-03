package com.example.wellnest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Onboarding3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding3)

        val getStartedBtn = findViewById<Button>(R.id.btnGetStarted)

        getStartedBtn.setOnClickListener {
            // After onboarding, go to Home (MainActivity)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
