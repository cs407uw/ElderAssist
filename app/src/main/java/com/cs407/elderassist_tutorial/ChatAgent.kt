package com.cs407.elderassist_tutorial

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object ChatAgent {

    private const val WIT_AI_ACCESS_TOKEN = "YGRKMKSEL5OJ7RFEM2TAMUCOLOMBCRGQ"
    private const val WIT_AI_URL = "https://api.wit.ai/message?v=20241127&q="

    private val client = OkHttpClient()
    private var followUpIntent: String? = null
    private var availableDestinations = listOf("New York", "Washington", "San Francisco", "Los Angeles", "Chicago")
    private var availableMedications = listOf(
        "Tylenol", "Paracetamol", "Aspirin", "Albuterol", "Levothyroxine", "Lisinopril", "Amlodipine",
        "Atorvastatin", "Metformin", "Metoprolol", "Gabapentin", "Ibuprofen", "Losartan", "Omeprazole"
    )

    // Callback to handle chat responses
    interface ChatAgentCallback {
        fun onResponse(response: String)
        fun onError(error: String)
    }

    // Function to process user input
    fun processMessage(userInput: String, callback: ChatAgentCallback) {
        followUpIntent?.let { handleFollowUp(it, userInput, callback); return }

        val requestUrl = WIT_AI_URL + userInput

        val request = Request.Builder()
            .url(requestUrl)
            .addHeader("Authorization", "Bearer $WIT_AI_ACCESS_TOKEN")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Failed to connect to ChatAgent: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val intent = parseIntent(responseBody)
                        callback.onResponse(intent)
                    } ?: callback.onError("Empty response from ChatAgent.")
                } else {
                    callback.onError("Error from ChatAgent: ${response.message}")
                }
            }
        })
    }

    // Function to parse the Wit.AI response
    private fun parseIntent(response: String): String {
        val jsonResponse = JSONObject(response)
        val intents = jsonResponse.optJSONArray("intents")
        val entities = jsonResponse.optJSONObject("entities")

        if (intents != null && intents.length() > 0) {
            val intentName = intents.getJSONObject(0).getString("name")

            return when (intentName) {
                "Customer_Support" -> handleCustomerSupport(entities)
                "Medication_Scan" -> handleMedicationScanFollowUp()
                "Travel_Information" -> handleTravelInformationFollowUp()
                else -> "Sorry, I couldn't understand your request. Please try again."
            }
        }

        return "Sorry, I couldn't determine your intent. Please try again."
    }

    private fun handleCustomerSupport(entities: JSONObject?): String {
        val contactMethod = entities?.optJSONArray("ContactMethod:ContactMethod")?.optJSONObject(0)
            ?.getString("value")
        return "Connecting you to customer support via ${contactMethod ?: "your preferred method"}."
    }


    private fun handleMedicationScanFollowUp(): String {
        followUpIntent = "Medication_Scan"
        return "Which medication information would you like to know? Available medications:\n${availableMedications.joinToString(", ")}"
    }

    private fun handleTravelInformationFollowUp(): String {
        followUpIntent = "Travel_Information"
        return "Which destination would you like to know about? Available destinations:\n${availableDestinations.joinToString(", ")}"
    }

    private fun handleFollowUp(intent: String, userInput: String, callback: ChatAgentCallback) {
        when (intent) {
            "Medication_Scan" -> {
                val medication = availableMedications.find { it.equals(userInput, ignoreCase = true) }
                if (medication != null) {
                    callback.onResponse("Information about $medication: It's commonly used to treat conditions. Always consult a doctor before use.")
                } else {
                    callback.onResponse("Sorry, we don't have information about this medication yet. We will update it in the future.")
                }
            }
            "Travel_Information" -> {
                val destination = availableDestinations.find { it.equals(userInput, ignoreCase = true) }
                if (destination != null) {
                    callback.onResponse("Information about $destination: It's a wonderful place to visit with many attractions.")
                } else {
                    callback.onResponse("Sorry, we don't have information about this destination yet. We will update it in the future.")
                }
            }
        }
        followUpIntent = null // Clear follow-up intent after handling
    }
}
