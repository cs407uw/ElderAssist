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
        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        val tetrisView = findViewById<TetrisView>(R.id.tetrisView)
        tetrisView.setGameOverListener { score ->
            gameOverTextView.text = "Game Over! Your total score is: $score"
            gameOverTextView.visibility = TextView.VISIBLE
            backButton.visibility = Button.VISIBLE
        }
    }
}
