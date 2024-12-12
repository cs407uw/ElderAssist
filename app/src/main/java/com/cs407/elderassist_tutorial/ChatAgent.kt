package com.cs407.elderassist_tutorial

import android.content.Context
import okhttp3.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException

object ChatAgent {

    private const val WIT_AI_ACCESS_TOKEN = "OJOIRNWKXWOEFUC65U6OIAKVGOTHW66G"
    private const val WIT_AI_URL = "https://api.wit.ai/message?v=20241127&q="

    private val client = OkHttpClient()
    private var followUpIntent: String? = null
    private lateinit var medicineInformation: Map<String, String> // Map of Medicine Name to Description
    private lateinit var travelData: List<Map<String, String>>

    interface ChatAgentCallback {
        fun onResponse(response: String)
        fun onError(error: String)
    }

    fun processMessage(userInput: String, context: Context, callback: ChatAgentCallback) {
        // Load medicine data from the new CSV file if not already loaded
        if (!::medicineInformation.isInitialized) {
            medicineInformation = try {
                loadMedicineInformation(context, "medicine_information.csv")
            } catch (e: IOException) {
                callback.onError("Failed to load medication information: ${e.message}")
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
                        handleIntent(intent, userInput, callback)
                    } ?: callback.onError("Empty response from ChatAgent.")
                } else {
                    callback.onError("Error from ChatAgent: ${response.message}")
                }
            }
        })
    }

    private fun parseIntent(response: String): String? {
        val jsonResponse = JSONObject(response)
        val intents = jsonResponse.optJSONArray("intents")
        return intents?.optJSONObject(0)?.getString("name")
    }

    private fun handleIntent(intent: String?, userInput: String, callback: ChatAgentCallback) {
        when (intent) {
            "Medication_Scan" -> {
                followUpIntent = "Medication_Scan"
                val availableMedications = medicineInformation.keys
                if (availableMedications.isNotEmpty()) {
                    callback.onResponse("Which medication information would you like to know? Available medications:\n${availableMedications.joinToString(", ")}")
                } else {
                    callback.onResponse("No medications are available in the database.")
                }
            }
            "Customer_Support" -> {
                followUpIntent = null
                callback.onResponse("You can reach customer support via phone at 1-800-555-5555 or email at support@elderassist.com.")
            }
            "Travel_Information" -> {
                followUpIntent = "Travel_Information"
                val availableDestinations = travelData.map { it["Destination"] ?: "Unknown" }
                callback.onResponse("Which destination would you like to know about? Available destinations:\n${availableDestinations.joinToString(", ")}")
            }
            else -> callback.onResponse("Sorry, I couldn't understand your request. Please try again.")
        }
    }

    private fun handleFollowUp(intent: String, userInput: String, callback: ChatAgentCallback) {
        when (intent) {
            "Medication_Scan" -> {
                val medicationDescription = medicineInformation[userInput]
                if (medicationDescription != null) {
                    callback.onResponse("Information about $userInput: $medicationDescription.")
                } else {
                    callback.onResponse("Sorry, we don't have information about this medication.")
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
                    callback.onResponse("Sorry, we don't have information about this destination yet.")
                }
            }
        }
        followUpIntent = null
    }

    // Load medicine information from the new CSV file
    private fun loadMedicineInformation(context: Context, fileName: String): Map<String, String> {
        val assetManager = context.assets
        val inputStream = assetManager.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val headers = reader.readLine().split(",") // Assumes the first row contains column names
        val nameIndex = headers.indexOf("Medicine Name")
        val descriptionIndex = headers.indexOf("Medication Description")

        val medicineMap = mutableMapOf<String, String>()

        reader.forEachLine { line ->
            val values = line.split(",").map { it.trim() }
            if (values.size > maxOf(nameIndex, descriptionIndex)) {
                val name = values[nameIndex]
                val description = values[descriptionIndex]
                if (name.isNotBlank() && description.isNotBlank()) {
                    medicineMap[name] = description // Map medicine name to description
                }
            }
        }

        reader.close()
        return medicineMap
    }

    private fun loadCSVData(context: Context, fileName: String): List<Map<String, String>> {
        val assetManager = context.assets
        val inputStream = assetManager.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val headers = reader.readLine().split(",")
        val data = mutableListOf<Map<String, String>>()

        reader.forEachLine { line ->
            val values = line.split(",").map { it.trim() }
            if (values.size == headers.size) {
                val row = headers.zip(values).toMap()
                data.add(row)
            }
        }
        reader.close()
        return data
    }

    private fun getTravelInfo(destinationName: String): Map<String, String>? {
        return travelData.find { it["Destination"]?.equals(destinationName, ignoreCase = true) == true }
    }
}
