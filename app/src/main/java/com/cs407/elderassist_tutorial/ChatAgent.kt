package com.cs407.elderassist_tutorial

import android.content.Context
import okhttp3.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException

object ChatAgent {

    private const val WIT_AI_ACCESS_TOKEN = "YGRKMKSEL5OJ7RFEM2TAMUCOLOMBCRGQ"
    private const val WIT_AI_URL = "https://api.wit.ai/message?v=20241127&q="

    private val client = OkHttpClient()
    private var followUpIntent: String? = null
    private lateinit var medicationData: List<Map<String, String>>
    private lateinit var travelData: List<Map<String, String>>

    // Callback to handle chat responses
    interface ChatAgentCallback {
        fun onResponse(response: String)
        fun onError(error: String)
    }

    // Function to process user input
    fun processMessage(userInput: String, context: Context, callback: ChatAgentCallback) {
        // Load data from CSV if not already loaded
        if (!::medicationData.isInitialized) {
            medicationData = try {
                loadCSVData(context, "pharmacy_data.csv")
            } catch (e: IOException) {
                callback.onError("Failed to load pharmacy data: ${e.message}")
                return
            }
        }
        if (!::travelData.isInitialized) {
            travelData = try {
                loadCSVData(context, "travel_information.csv")
            } catch (e: IOException) {
                callback.onError("Failed to load travel data: ${e.message}")
                return
            }
        }

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
                        // Handle follow-up or new intent
                        handleIntent(intent, userInput, callback)
                    } ?: callback.onError("Empty response from ChatAgent.")
                } else {
                    callback.onError("Error from ChatAgent: ${response.message}")
                }
            }
        })
    }

    // Parse the Wit.AI response and extract the intent
    private fun parseIntent(response: String): String? {
        val jsonResponse = JSONObject(response)
        val intents = jsonResponse.optJSONArray("intents")
        return if (intents != null && intents.length() > 0) {
            intents.getJSONObject(0).getString("name")
        } else {
            null
        }
    }

    // Handle the extracted intent
    private fun handleIntent(intent: String?, userInput: String, callback: ChatAgentCallback) {
        when (intent) {
            "Medication_Scan" -> {
                followUpIntent = "Medication_Scan" // Set follow-up intent
                val availableMedications = medicationData.map { it["Medicine Name"] ?: "Unknown" }
                callback.onResponse("Which medication information would you like to know? Available medications:\n${availableMedications.joinToString(", ")}")
            }
            "Customer_Support" -> {
                followUpIntent = null // No follow-up needed
                callback.onResponse("You can reach customer support via phone at 1-800-555-5555 or email at support@elderassist.com.")
            }
            "Travel_Information" -> {
                followUpIntent = "Travel_Information" // Set follow-up intent
                val availableDestinations = travelData.map { it["Destination"] ?: "Unknown" }
                callback.onResponse("Which destination would you like to know about? Available destinations:\n${availableDestinations.joinToString(", ")}")
            }
            else -> callback.onResponse("Sorry, I couldn't understand your request. Please try again.")
        }
    }

    // Handle follow-up based on previous intent
    private fun handleFollowUp(intent: String, userInput: String, callback: ChatAgentCallback) {
        when (intent) {
            "Medication_Scan" -> {
                val medicationInfo = getMedicationInfo(userInput)
                if (medicationInfo != null) {
                    callback.onResponse("Information about ${medicationInfo["Medicine Name"]}: ${medicationInfo["Medication Description"]}.")
                } else {
                    callback.onResponse("Sorry, we don't have information about this medication yet. We will update it in the future.")
                }
            }
            "Travel_Information" -> {
                val destinationInfo = getTravelInfo(userInput)
                if (destinationInfo != null) {
                    callback.onResponse(
                        "Information about ${destinationInfo["Destination"]}:\n" +
                                "Description: ${destinationInfo["Description"]}\n" +
                                "Attractions: ${destinationInfo["Attractions"]}\n" +
                                "Best Time to Visit: ${destinationInfo["Best Time to Visit"]}\n" +
                                "Transport Options: ${destinationInfo["Transport Options"]}"
                    )
                } else {
                    callback.onResponse("Sorry, we don't have information about this destination yet. We will update it in the future.")
                }
            }
        }
        followUpIntent = null // Reset follow-up intent after handling
    }

    // Load data from a CSV file
    private fun loadCSVData(context: Context, fileName: String): List<Map<String, String>> {
        val assetManager = context.assets
        val inputStream = assetManager.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val headers = reader.readLine().split(",") // Assumes the first row contains column names
        val data = mutableListOf<Map<String, String>>()

        reader.forEachLine { line ->
            val values = line.split(",")
            val row = headers.zip(values).toMap()
            data.add(row)
        }

        reader.close()
        return data
    }

    // Fetch specific medication information
    private fun getMedicationInfo(medicationName: String): Map<String, String>? {
        return medicationData.find { it["Medicine Name"]?.equals(medicationName, ignoreCase = true) == true }
    }

    // Fetch specific travel information
    private fun getTravelInfo(destinationName: String): Map<String, String>? {
        return travelData.find { it["Destination"]?.equals(destinationName, ignoreCase = true) == true }
    }
}
