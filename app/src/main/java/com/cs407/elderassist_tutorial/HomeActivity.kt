package com.cs407.elderassist_tutorial

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Weather TextView
        weatherTextView = findViewById(R.id.weatherTextView)

        //  UserViewModel
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Set up button navigation
        setupButtonNavigation()

        // Fetch weather data
        fetchWeather()

        // Import CSV data
        importCSVData()

        //initialize
        initializeUserViewModel()

    }



    private fun setupButtonNavigation() {
        // Find buttons by ID as ImageView
        val tutorialButton = findViewById<ImageView>(R.id.tutorialButton)
        val mapButton = findViewById<ImageView>(R.id.mapButton)
        val scanButton = findViewById<ImageView>(R.id.scanButton)
        val chatButton = findViewById<ImageView>(R.id.chatButton)
        val meButton = findViewById<ImageView>(R.id.meButton)
        val playTetrisButton=findViewById<Button>(R.id.playTetrisButton)

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
            //navigateToActivity(LoginMainActivity::class.java)
            handleMeButtonClick()
        }

        playTetrisButton.setOnClickListener {
            navigateToActivity(TetrisGameActivity::class.java)
        }
    }

    private fun <T> navigateToActivity(activity: Class<T>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }

    private fun handleMeButtonClick() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)

        if (isLoggedIn) {
            val userState = userViewModel.userState.value
            Log.d("homeFragment", "user: ${userState}")
            if (userState == null || userState.name.isEmpty()) {
                // 尝试从 SharedPreferences 加载数据
                val username = sharedPreferences.getString("NAME", null)
                val userId = sharedPreferences.getString("ID", null)
                val password = sharedPreferences.getString("PASSWORD", null)
                val info = sharedPreferences.getString("INFO", null)
                Log.d("homeFragment", "id: ${username}")
                Log.d("homeFragment", "userName: ${userId}")
                Log.d("homeFragment", "passwd: ${password}")
                Log.d("homeFragment", "randomInfo: ${info}")
                if (username != null && userId != null) {
                    userViewModel.setUser(UserState(userId.toInt(), username, password ?: "", info ?: ""))
                } else {
                    Toast.makeText(this, "Failed to restore user state", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            // 跳转到 NoteListFragment
            val intent = Intent(this, LoginMainActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, LoginMainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun initializeUserViewModel() {
        if (!::userViewModel.isInitialized) { // 确保只初始化一次
            userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
            val userName = sharedPreferences.getString("NAME", null)
            val userId = sharedPreferences.getString("ID", null)
            val password = sharedPreferences.getString("PASSWORD", null)
            val randomInfo = sharedPreferences.getString("INFO", null)

            if (isLoggedIn && userName != null && userId != null) {
                userViewModel.setUser(UserState(userId.toInt(), userName, password ?: "", randomInfo?:""))
            } else {
                userViewModel.setUser(UserState()) // 设置默认空状态
            }
        }
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