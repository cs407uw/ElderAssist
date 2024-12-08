package com.cs407.elderassist_tutorial

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TetrisGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tetris)

        val gameOverTextView = findViewById<TextView>(R.id.gameOverTextView)
        val restartButton = findViewById<Button>(R.id.restartButton)
        val backButton = findViewById<Button>(R.id.topBackButton)
        val tetrisView = findViewById<TetrisView>(R.id.tetrisView)

        backButton.setOnClickListener {
            finish()
        }

        restartButton.setOnClickListener {
            recreate()
        }

        tetrisView.setGameOverListener { score ->
            gameOverTextView.text = "Game Over! Your total score is: $score"
            gameOverTextView.visibility = TextView.VISIBLE
        }
    }
}
