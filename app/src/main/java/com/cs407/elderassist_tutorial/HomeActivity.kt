package com.cs407.elderassist_tutorial

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.cs407.elderassist_tutorial.utils.CSVimport
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import com.opencsv.CSVReader

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Footer Navigation Buttons
        val homeButton = findViewById<Button>(R.id.homeButton)
        val tutorialButton = findViewById<Button>(R.id.tutorialButton)
        val meButton = findViewById<Button>(R.id.meButton)
        val chatButton = findViewById<Button>(R.id.chatButton)
        val scanButton = findViewById<Button>(R.id.ScanButton)
        val mapButton = findViewById<Button>(R.id.MapButton)

        // Initialize Search Bar and Button
        val searchBar = findViewById<EditText>(R.id.searchBar)
        val searchButton = findViewById<Button>(R.id.searchButton)

        // Home Button: Stay on HomeActivity
        homeButton.setOnClickListener {
            // Do nothing, already on HomeActivity
        }

        // Tutorial Button: Navigate to MainActivity
        tutorialButton.setOnClickListener {
            navigateToActivity(MainActivity::class.java)
        }

        // Me Button: Navigate to Profile Page
        meButton.setOnClickListener {
            navigateToActivity(LoginMainActivity::class.java)
        }

        // Chat Button: Navigate to ChatActivity
        chatButton.setOnClickListener {
            navigateToActivity(ChatActivity::class.java)
        }

        // Scan Button: Navigate to CameraScan
        scanButton.setOnClickListener {
            navigateToActivity(CameraScan::class.java)
        }

        // Map Button: Navigate to MapActivity
        mapButton.setOnClickListener {
            navigateToActivity(MapActivity::class.java)
        }

        // Search Button Functionality
        searchButton.setOnClickListener {
            val query = searchBar.text.toString().trim().lowercase()
            when (query) {
                "tutorial" -> navigateToActivity(MainActivity::class.java)
                "map" -> navigateToActivity(MapActivity::class.java)
                "scan" -> navigateToActivity(CameraScan::class.java)
                "chat" -> navigateToActivity(ChatActivity::class.java)
                "me" -> navigateToActivity(LoginMainActivity::class.java)
                else -> Toast.makeText(
                    this,
                    "Invalid search query. Try 'tutorial', 'map', 'scan', 'chat', or 'me'.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val startGameButton = findViewById<Button>(R.id.startGameButton)
        startGameButton.setOnClickListener {
            navigateToActivity(TetrisGameActivity::class.java)
        }

        // database
        //val csvFilePath = "path/to/your/csv/file.csv"
        lifecycleScope.launch {
            try {
                // 创建 Pharmacy 的 CSVReader
                val inputStreamPharmacy = assets.open("pharmacy_data.csv")
                val csvReaderPharmacy = CSVReader(InputStreamReader(inputStreamPharmacy))
                CSVimport.importPharmacyData(csvReaderPharmacy, applicationContext)

                // 创建 Medication 的 CSVReader

                val inputStreamMedication = assets.open("medicine_information.csv")

                val csvReaderMedication = CSVReader(InputStreamReader(inputStreamMedication))
                CSVimport.importMedicationData(csvReaderMedication, applicationContext)

                // 创建 PharmacyMedication 的 CSVReader
                val inputStreamPharmacyMedication = assets.open("pharmacy_data.csv")
                val csvReaderPharmacyMedication = CSVReader(InputStreamReader(inputStreamPharmacyMedication))
                CSVimport.linkPharmacyAndMedications(csvReaderPharmacyMedication, applicationContext)

                Log.d("CSVImporter", "CSV success")
                // 查询并打印数据库内容
                logDatabaseInfo()

//                // 测试 searchPharmaciesByMedication
//                val medicineName = "Tylenol" // 替换为你的 CSV 数据中的药品名称
//                val pharmacies = withContext(Dispatchers.IO) {
//                    SearchUtils.searchPharmaciesByMedication(applicationContext, medicineName)
//                }
//                Log.d("LoginMainActivity", "Pharmacies for $medicineName: $pharmacies")
//
//                // 测试 searchMedicationsByPharmacy
//                val pharmacyName = "CVS Pharmacy" // 替换为你的 CSV 数据中的药店名称
//                val medications = withContext(Dispatchers.IO) {
//                    SearchUtils.searchMedicationsByPharmacy(applicationContext, pharmacyName)
//                }
//                Log.d("LoginMainActivity", "Medications for $pharmacyName: $medications")

            } catch (e: Exception) {
                Log.e("CSVImporter", "CSV error: ${e.message}")
            }
        }
    }

    // 查询并打印数据库内容
    private suspend fun logDatabaseInfo() {
        withContext(Dispatchers.IO) {
            val database = com.cs407.elderassist_tutorial.data.NoteDatabase.getDatabase(applicationContext)

            // 打印所有药房信息
            val pharmacies = database.pharmacyDao().getAllPharmacies()
            pharmacies.forEach { pharmacy ->
                Log.d("Database-Pharmacy", "Pharmacy: ${pharmacy.pharmacyName}, Address: ${pharmacy.address}")
            }

            // 打印所有药品信息
            val medications = database.medicationDao().getAllMedications()
            medications.forEach { medication ->
                Log.d("Database-Medication", "Medication: ${medication.medicineName}, Description: ${medication.medicationDescription}")
            }
        }
    }

    // Helper function to simplify navigation
    private fun <T> navigateToActivity(activity: Class<T>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }
}
