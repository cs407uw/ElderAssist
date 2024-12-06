package com.cs407.elderassist_tutorial

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
        val chatButton = findViewById<Button>(R.id.chatButton) // Chat Button

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
            //Toast.makeText(this, "Me Button Clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginMainActivity::class.java)
            startActivity(intent)
        }

        // Chat Button: Navigate to ChatActivity
        chatButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }
}
