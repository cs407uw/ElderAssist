package com.cs407.elderassist_tutorial

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ChatActivity : AppCompatActivity() {

    private lateinit var chatInput: EditText
    private lateinit var chatSendButton: Button
    private lateinit var faqButton1: Button
    private lateinit var faqButton2: Button
    private lateinit var faqButton3: Button
    private lateinit var chatOutputContainer: LinearLayout
    private lateinit var backButton: Button
    private lateinit var chatOutputScrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize UI components
        initializeUIComponents()

        // Set button click listeners
        setupClickListeners()
    }

    private fun initializeUIComponents() {
        chatInput = findViewById(R.id.chatInput)
        chatSendButton = findViewById(R.id.chatSendButton)
        faqButton1 = findViewById(R.id.faqButton1)
        faqButton2 = findViewById(R.id.faqButton2)
        faqButton3 = findViewById(R.id.faqButton3)
        chatOutputContainer = findViewById(R.id.chatOutputContainer)
        backButton = findViewById(R.id.backButton)
        chatOutputScrollView = findViewById(R.id.chatOutputScrollView)
    }

    private fun setupClickListeners() {
        // Back button to finish the activity
        backButton.setOnClickListener { finish() }

        // FAQ Buttons
        faqButton1.setOnClickListener { sendMessage(getString(R.string.faq_question_1)) }
        faqButton2.setOnClickListener { sendMessage(getString(R.string.faq_question_2)) }
        faqButton3.setOnClickListener { sendMessage(getString(R.string.faq_question_3)) }

        // Send button for user input
        chatSendButton.setOnClickListener {
            val userInput = chatInput.text.toString().trim()
            if (userInput.isNotBlank()) {
                sendMessage(userInput)
                chatInput.text.clear()
            }
        }
    }

    private fun sendMessage(message: String) {
        // Add user message bubble
        addChatBubble(isUser = true, message)

        // Process message through ChatAgent
        ChatAgent.processMessage(message, this, object : ChatAgent.ChatAgentCallback {
            override fun onResponse(response: String) {
                runOnUiThread {
                    // Add AI response bubble
                    addChatBubble(isUser = false, response)
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    // Add error message bubble
                    addChatBubble(isUser = false, "Error: $error")
                }
            }
        })
    }

    private fun addChatBubble(isUser: Boolean, message: String) {
        val chatBubble = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(if (isUser) R.drawable.chat_bubble_right else R.drawable.chat_bubble_left)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = if (isUser) Gravity.END else Gravity.START
                topMargin = 8 // Add space between bubbles
            }
            setPadding(16, 12, 16, 12)
        }

        val messageText = TextView(this).apply {
            text = message
            setTextColor(Color.BLACK)
            textSize = 16f
        }

        chatBubble.addView(messageText)
        chatOutputContainer.addView(chatBubble)

        // Scroll to the bottom of the chat
        chatOutputScrollView.post {
            chatOutputScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}
