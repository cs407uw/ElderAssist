package com.cs407.alderassist_tutorial

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Footer Navigation Buttons
        val homeButton = findViewById<Button>(R.id.homeButton)
        val tutorialButton = findViewById<Button>(R.id.tutorialButton)
        val meButton = findViewById<Button>(R.id.meButton)

        // Home Button: Stay on HomeActivity
        homeButton.setOnClickListener {
            // Do nothing, already on HomeActivity
        }

        // Tutorial Button: Navigate to MainActivity
        tutorialButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Me Button: Placeholder for Profile Page (Future Implementation)
        meButton.setOnClickListener {
            // Add navigation logic for ProfileActivity when implemented
            Toast.makeText(this, "Me Button Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
