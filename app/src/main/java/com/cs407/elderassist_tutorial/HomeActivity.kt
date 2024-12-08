package com.cs407.elderassist_tutorial

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.elderassist_tutorial.data.WeatherResponse
import com.cs407.elderassist_tutorial.data.WeatherService
import com.cs407.elderassist_tutorial.utils.CSVimport
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStreamReader

class HomeActivity : AppCompatActivity() {

    private lateinit var weatherTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Weather TextView
        weatherTextView = findViewById(R.id.weatherTextView)

        // Set up button navigation
        setupButtonNavigation()

        // Fetch weather data
        fetchWeather()

        // Import CSV data
        importCSVData()
    }


    private fun setupButtonNavigation() {
        // Find buttons by ID as ImageView
        val tutorialButton = findViewById<ImageView>(R.id.tutorialButton)
        val mapButton = findViewById<ImageView>(R.id.mapButton)
        val scanButton = findViewById<ImageView>(R.id.scanButton)
        val chatButton = findViewById<ImageView>(R.id.chatButton)
        val meButton = findViewById<ImageView>(R.id.meButton)

        // Set up navigation for each button
        tutorialButton.setOnClickListener {
            navigateToActivity(MainActivity::class.java)
        }

        mapButton.setOnClickListener {
            navigateToActivity(MapActivity::class.java)
        }

        scanButton.setOnClickListener {
            navigateToActivity(CameraScan::class.java)
        }

        chatButton.setOnClickListener {
            navigateToActivity(ChatActivity::class.java)
        }

        meButton.setOnClickListener {
            navigateToActivity(LoginMainActivity::class.java)
        }
    }

    private fun <T> navigateToActivity(activity: Class<T>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }

    // Import CSV Data
    private fun importCSVData() {
        lifecycleScope.launch {
            try {
                val inputStreamPharmacy = assets.open("pharmacy_data.csv")
                val csvReaderPharmacy = CSVReader(InputStreamReader(inputStreamPharmacy))
                CSVimport.importPharmacyData(csvReaderPharmacy, applicationContext)

                val inputStreamMedication = assets.open("medicine_information.csv")
                val csvReaderMedication = CSVReader(InputStreamReader(inputStreamMedication))
                CSVimport.importMedicationData(csvReaderMedication, applicationContext)

                val inputStreamPharmacyMedication = assets.open("pharmacy_data.csv")
                val csvReaderPharmacyMedication = CSVReader(InputStreamReader(inputStreamPharmacyMedication))
                CSVimport.linkPharmacyAndMedications(csvReaderPharmacyMedication, applicationContext)

                Log.d("CSVImporter", "CSV import success")
                logDatabaseInfo()
            } catch (e: Exception) {
                Log.e("CSVImporter", "CSV error: ${e.message}")
            }
        }
    }

    // Log Database Info
    private suspend fun logDatabaseInfo() {
        withContext(Dispatchers.IO) {
            val database = com.cs407.elderassist_tutorial.data.NoteDatabase.getDatabase(applicationContext)
            val pharmacies = database.pharmacyDao().getAllPharmacies()
            pharmacies.forEach { pharmacy ->
                Log.d("Database-Pharmacy", "Pharmacy: ${pharmacy.pharmacyName}, Address: ${pharmacy.address}")
            }
            val medications = database.medicationDao().getAllMedications()
            medications.forEach { medication ->
                Log.d("Database-Medication", "Medication: ${medication.medicineName}, Description: ${medication.medicationDescription}")
            }
        }
    }

    // Fetch Weather Data
    private fun fetchWeather() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherService::class.java)
        val apiKey = "f1682bfc95490e71fc2e7e2aca222825"
        val latitude = 43.0731
        val longitude = -89.4012

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    weatherService.getCurrentWeather(latitude, longitude, apiKey)
                }
                updateWeatherUI(response)
            } catch (e: Exception) {
                Log.e("WeatherError", "Failed to fetch weather: ${e.message}")
            }
        }
    }

    // Update Weather Information in UI
    private fun updateWeatherUI(weather: WeatherResponse) {
        weatherTextView.text = "Weather in ${weather.name}: ${weather.main.temp}°C, ${weather.weather[0].description}"
    }
}
