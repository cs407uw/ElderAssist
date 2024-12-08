package com.cs407.elderassist_tutorial

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var adapter1: VideoAdapter
    private lateinit var adapter2: VideoAdapter
    private lateinit var recyclerView1: RecyclerView
    private lateinit var recyclerView2: RecyclerView

//    private val videoList = listOf(
//        VideoItem("How to use Google Maps on Android phone to navigate", "Xo7yywC9iPk"),
//        VideoItem("How To Use Google Maps! (Complete Beginners Guide)", "tui9hq9lfsU"),
//        VideoItem("How To Send A Text Message On Any Android Phone", "rfA83_bhdXw"),
//        VideoItem("How to Setup Any Email on Android 2019", "KL2PsdyAXXY"),
//        VideoItem("The Best How to Send an Email With an Android Phone or Tablet", "sYHxF98TU9k"),
//        VideoItem("Make calls using Google Voice on Android using Google Workspace for business", "jWhnE9wUOQE"),
//        VideoItem("Galaxy S23's: How to Make a Phone Call", "_-qm3BCLP1Y")
//    )

    private val videoList1 = listOf(
        VideoItem("How to use Google Maps on Android phone to navigate", "Xo7yywC9iPk"),
        VideoItem("How To Use Google Maps! (Complete Beginners Guide)", "tui9hq9lfsU"),
        VideoItem("How To Send A Text Message On Any Android Phone", "rfA83_bhdXw"),
        VideoItem("How to Setup Any Email on Android 2019", "KL2PsdyAXXY")
    )

    private val videoList2 = listOf(
        VideoItem("The Best How to Send an Email With an Android Phone or Tablet", "sYHxF98TU9k"),
        VideoItem("Make calls using Google Voice on Android using Google Workspace for business", "jWhnE9wUOQE"),
        VideoItem("Galaxy S23's: How to Make a Phone Call", "_-qm3BCLP1Y")
    )

    private var filteredList1 = videoList1.toMutableList()
    private var filteredList2 = videoList2.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchBar = findViewById<EditText>(R.id.searchBar)
        val searchButton = findViewById<Button>(R.id.searchButton)

        recyclerView1 = findViewById(R.id.recyclerView1)
        recyclerView2 = findViewById(R.id.recyclerView2)

        recyclerView1.layoutManager = GridLayoutManager(this, 2)
        recyclerView2.layoutManager = GridLayoutManager(this, 2)

        adapter1 = VideoAdapter(filteredList1) { videoId ->
            val intent = Intent(this, VideoActivity::class.java)
            intent.putExtra("VIDEO_ID", videoId)
            startActivity(intent)
        }

        adapter2 = VideoAdapter(filteredList2) { videoId ->
            val intent = Intent(this, VideoActivity::class.java)
            intent.putExtra("VIDEO_ID", videoId)
            startActivity(intent)
        }

        recyclerView1.adapter=adapter1
        recyclerView2.adapter=adapter2

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

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun filterVideos(query: String) {
        val section1Title = findViewById<TextView>(R.id.section1Title)
        val section2Title = findViewById<TextView>(R.id.section2Title)

        val recyclerViewSection1 = findViewById<RecyclerView>(R.id.recyclerView1)
        val recyclerViewSection2 = findViewById<RecyclerView>(R.id.recyclerView2)

        val filteredSection1 = videoList1.filter { it.title.contains(query, ignoreCase = true) }
        val filteredSection2 = videoList2.filter { it.title.contains(query, ignoreCase = true) }

        adapter1.updateData(filteredSection1)
        adapter2.updateData(filteredSection2)

        val searchBar=findViewById<Button>(R.id.searchButton)

        if (query.isEmpty()) {
            section1Title.visibility = View.VISIBLE
            section2Title.visibility = View.VISIBLE
            resetRecyclerViewMargins(recyclerViewSection1)
            resetRecyclerViewMargins(recyclerViewSection2)
        } else {
            section1Title.visibility = View.VISIBLE
            section2Title.visibility = View.VISIBLE
            setRecyclerViewMarginToTop(recyclerViewSection1)
            setRecyclerViewMarginToTop(recyclerViewSection2)
        }
        searchBar.setOnClickListener {
            if (filteredSection1.isEmpty() && filteredSection2.isEmpty()) {
                showNoVideosDialog()
            }
        }


    }
    private fun showNoVideosDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No Videos Found")
        builder.setMessage("No videos match your search. Please try a different keyword.")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun resetRecyclerViewMargins(recyclerView: RecyclerView) {
        val params = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = resources.getDimensionPixelSize(R.dimen.default_recycler_margin_top)
        recyclerView.layoutParams = params
    }

    private fun setRecyclerViewMarginToTop(recyclerView: RecyclerView) {
        val params = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = resources.getDimensionPixelSize(R.dimen.search_result_margin_top)
        recyclerView.layoutParams = params
    }
}
