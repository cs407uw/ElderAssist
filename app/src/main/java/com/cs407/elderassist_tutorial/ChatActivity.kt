package com.cs407.elderassist_tutorial

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ChatActivity : AppCompatActivity(), ChatAgent.ChatAgentCallback {

    private lateinit var chatInput: EditText
    private lateinit var chatSendButton: Button
    private lateinit var faqButton1: Button
    private lateinit var faqButton2: Button
    private lateinit var faqButton3: Button
    private lateinit var chatOutputContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatInput = findViewById(R.id.chatInput)
        chatSendButton = findViewById(R.id.chatSendButton)
        faqButton1 = findViewById(R.id.faqButton1)
        faqButton2 = findViewById(R.id.faqButton2)
        faqButton3 = findViewById(R.id.faqButton3)
        chatOutputContainer = findViewById(R.id.chatOutputContainer)

        // Welcome message
        addChatBubble(false, getString(R.string.chat_welcome_message))

        // FAQ Button Actions
        faqButton1.setOnClickListener {
            sendMessage(getString(R.string.faq_question_1))
        }

        faqButton2.setOnClickListener {
            sendMessage(getString(R.string.faq_question_2))
        }

        faqButton3.setOnClickListener {
            sendMessage(getString(R.string.faq_question_3))
        }

        // Send Button Action
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
        addChatBubble(true, message)

        // Process message through ChatAgent
        ChatAgent.processMessage(message, this)
    }

    override fun onResponse(response: String) {
        runOnUiThread {
            // Add AI response bubble
            addChatBubble(false, response)
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            // Add error message bubble
            addChatBubble(false, "Error: $error")
        }
    }

    // Function to add a chat bubble
    private fun addChatBubble(isUser: Boolean, message: String) {
        val chatBubble = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(if (isUser) R.drawable.chat_bubble_right else R.drawable.chat_bubble_left)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = if (isUser) Gravity.END else Gravity.START
                topMargin = 8 // Space between bubbles
            }
            layoutParams = params
            setPadding(16, 12, 16, 12)
        }

        val messageText = TextView(this).apply {
            text = message
            setTextColor(Color.BLACK)
            textSize = 16f
        }

        chatBubble.addView(messageText)
        chatOutputContainer.addView(chatBubble)
    }
}
