package com.cs407.alderassist_tutorial

import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: VideoAdapter
    private lateinit var recyclerView: RecyclerView
    private val videoList = listOf(
        VideoItem("How to use Google Maps on Android phone to navigate", "Xo7yywC9iPk"),
        VideoItem("How To Use Google Maps! (Complete Beginners Guide)", "tui9hq9lfsU"),
        VideoItem("How To Send A Text Message On Any Android Phone", "rfA83_bhdXw"),
        VideoItem("How to Setup Any Email on Android 2019", "KL2PsdyAXXY"),
        VideoItem("The Best How to Send an Email With an Android Phone or Tablet", "sYHxF98TU9k"),
        VideoItem("Make calls using Google Voice on Android using Google Workspace for business", "jWhnE9wUOQE"),
        VideoItem("Galaxy S23's: How to Make a Phone Call", "_-qm3BCLP1Y")
    )
    private var filteredList = videoList.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "Video list size: ${videoList.size}")

        // Initialize RecyclerView and Search Functionality
        val searchBar = findViewById<EditText>(R.id.searchBar)
        val searchButton = findViewById<Button>(R.id.searchButton)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = VideoAdapter(filteredList) { videoId ->
            val intent = Intent(this, VideoActivity::class.java)
            intent.putExtra("VIDEO_ID", videoId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        searchButton.setOnClickListener {
            val query = searchBar.text.toString().trim()
            filterVideos(query)
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                filterVideos(query)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Initialize Footer Navigation Buttons
        initializeFooterNavigation()
    }

    private fun filterVideos(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(videoList)
        } else {
            filteredList.addAll(videoList.filter { it.title.contains(query, ignoreCase = true) })
        }
        adapter.notifyDataSetChanged()
    }

    private fun initializeFooterNavigation() {
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        val videosButton = findViewById<ImageButton>(R.id.videosButton)
        val blogsButton = findViewById<ImageButton>(R.id.blogsButton)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)

        // Navigate to HomeActivity
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Stay on MainActivity (Videos Section)
        videosButton.setOnClickListener {
            // Optional: Reload videos or perform specific action
        }

        // Placeholder for Blogs Section
        blogsButton.setOnClickListener {
            // Intent for BlogsActivity (Create if needed)
            Log.d("MainActivity", "Blogs Button Clicked")
        }

        // Placeholder for Profile Section
        profileButton.setOnClickListener {
            // Intent for ProfileActivity (Create if needed)
            Log.d("MainActivity", "Profile Button Clicked")
        }

        // Placeholder for Settings Section
        settingsButton.setOnClickListener {
            // Intent for SettingsActivity (Create if needed)
            Log.d("MainActivity", "Settings Button Clicked")
        }
    }
}
