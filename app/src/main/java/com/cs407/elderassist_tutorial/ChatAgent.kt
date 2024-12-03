package com.cs407.elderassist_tutorial

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object ChatAgent {

    private const val WIT_AI_ACCESS_TOKEN = "YGRKMKSEL5OJ7RFEM2TAMUCOLOMBCRGQ"
    private const val WIT_AI_URL = "https://api.wit.ai/message?v=20241127&q="

    private val client = OkHttpClient()

    // Callback to handle chat responses
    interface ChatAgentCallback {
        fun onResponse(response: String)
        fun onError(error: String)
    }

    // Function to process user input
    fun processMessage(userInput: String, callback: ChatAgentCallback) {
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
                "Emergency_Assistance" -> handleEmergencyAssistance(entities)
                "Get_Help" -> "Here are some common requests you can make:\n- Learn a tutorial\n- Scan a medication\n- Request travel information."
                "Medication_Scan" -> handleMedicationScan(entities)
                "Travel_Information" -> handleTravelInformation(entities)
                else -> "Sorry, I couldn't understand your request, please say 'help' if you need more information."
            }
        }

        return "Sorry, I couldn't determine your intent. Please say 'help' if you need more information."
    }

    // Handler functions for each intent
    private fun handleCustomerSupport(entities: JSONObject?): String {
        val contactMethod = entities?.optJSONArray("ContactMethod:ContactMethod")?.optJSONObject(0)?.getString("value")
        return "Connecting you to customer support via ${contactMethod ?: "your preferred method"}."
    }

    private fun handleEmergencyAssistance(entities: JSONObject?): String {
        val emergencyType = entities?.optJSONArray("EmergencyType:EmergencyType")?.optJSONObject(0)?.getString("value")
        val destination = entities?.optJSONArray("Destination:Destination")?.optJSONObject(0)?.getString("value")
        return "Emergency assistance for $emergencyType has been requested at $destination."
    }

    private fun handleMedicationScan(entities: JSONObject?): String {
        val medicationName = entities?.optJSONArray("MedicationName:MedicationName")?.optJSONObject(0)?.getString("value")
        return if (medicationName != null) {
            "Searching for pharmacies near you that have $medicationName."
        } else {
            "Please provide the name of the medication you'd like to scan."
        }
    }

    private fun handleTravelInformation(entities: JSONObject?): String {
        val location = entities?.optJSONArray("location:location")?.optJSONObject(0)?.getString("value")
        return if (location != null) {
            "Fetching travel information for $location."
        } else {
            "Please specify the destination you'd like travel information for."
        }
    }
}
