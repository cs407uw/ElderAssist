package com.cs407.elderassist_tutorial

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Footer Navigation Buttons
        val homeButton = findViewById<Button>(R.id.homeButton)
        val tutorialButton = findViewById<Button>(R.id.tutorialButton)
        val meButton = findViewById<Button>(R.id.meButton)
        val chatButton = findViewById<Button>(R.id.chatButton)
        val scanButton = findViewById<Button>(R.id.ScanButton)
        val mapButton = findViewById<Button>(R.id.MapButton)

        // Initialize Search Bar and Button
        val searchBar = findViewById<EditText>(R.id.searchBar)
        val searchButton = findViewById<Button>(R.id.searchButton)

        // Home Button: Stay on HomeActivity
        homeButton.setOnClickListener {
            // Do nothing, already on HomeActivity
        }

        // Tutorial Button: Navigate to MainActivity
        tutorialButton.setOnClickListener {
            navigateToActivity(MainActivity::class.java)
        }

        // Me Button: Navigate to Profile Page
        meButton.setOnClickListener {
            navigateToActivity(LoginMainActivity::class.java)
        }

        // Chat Button: Navigate to ChatActivity
        chatButton.setOnClickListener {
            navigateToActivity(ChatActivity::class.java)
        }

        // Scan Button: Navigate to CameraScan
        scanButton.setOnClickListener {
            navigateToActivity(CameraScan::class.java)
        }

        // Map Button: Navigate to MapActivity
        mapButton.setOnClickListener {
            navigateToActivity(MapActivity::class.java)
        }

        // Search Button Functionality
        searchButton.setOnClickListener {
            val query = searchBar.text.toString().trim().lowercase()
            when (query) {
                "tutorial" -> navigateToActivity(MainActivity::class.java)
                "map" -> navigateToActivity(MapActivity::class.java)
                "scan" -> navigateToActivity(CameraScan::class.java)
                "chat" -> navigateToActivity(ChatActivity::class.java)
                "me" -> navigateToActivity(LoginMainActivity::class.java)
                else -> Toast.makeText(
                    this,
                    "Invalid search query. Try 'tutorial', 'map', 'scan', 'chat', or 'me'.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Helper function to simplify navigation
    private fun <T> navigateToActivity(activity: Class<T>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }
}
