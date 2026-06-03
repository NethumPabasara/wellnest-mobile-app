package com.example.wellnest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Onboarding2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding2)

        val nextBtn = findViewById<Button>(R.id.btnNext2)

        nextBtn.setOnClickListener {
            startActivity(Intent(this, Onboarding3Activity::class.java))
            finish()
        }
    }
}
