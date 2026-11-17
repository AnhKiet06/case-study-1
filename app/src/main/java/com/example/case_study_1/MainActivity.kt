package com.example.case_study_1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<MaterialButton>(R.id.btnStart)
        btnStart.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }
}