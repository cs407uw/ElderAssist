package com.cs407.elderassist_tutorial
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.cs407.elderassist_tutorial.utils.CSVimport
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import android.util.Log
import com.opencsv.CSVReader
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

import com.cs407.elderassist_tutorial.utils.SearchUtils

class LoginMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loginmain)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 导入数据库
        //val csvFilePath = "path/to/your/csv/file.csv"
        lifecycleScope.launch {
            try {
                // 创建 Pharmacy 的 CSVReader
                val inputStreamPharmacy = assets.open("pharmacy_data.csv")
                val csvReaderPharmacy = CSVReader(InputStreamReader(inputStreamPharmacy))
                CSVimport.importPharmacyData(csvReaderPharmacy, applicationContext)

                // 创建 Medication 的 CSVReader
                val inputStreamMedication = assets.open("pharmacy_data.csv")
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


}

