package com.cs407.elderassist_tutorial.utils

import android.content.Context
import com.cs407.elderassist_tutorial.data.NoteDatabase
import com.cs407.elderassist_tutorial.data.Medication
import com.cs407.elderassist_tutorial.data.Pharmacy
import com.cs407.elderassist_tutorial.data.PharmacyMedication
import com.opencsv.CSVReader
import java.io.FileReader
import android.util.Log

object CSVimport {
    suspend fun importPharmacyData(csvReader: CSVReader, context: Context) {
        val database = NoteDatabase.getDatabase(context)
        val pharmacyDao = database.pharmacyDao()

        csvReader.readAll().drop(1).forEach { row ->
            if (row.any { it.isBlank() }) {
                // 跳过有空字段的行
                return@forEach
            }
            val pharmacyId = row[0].toInt()
            val pharmacyName = row[1]
            val address = row[2]
            val website = row[3]
            val operatingHours = row[4]
            val phone = row[5]
            val rating = row[6]
            val insuranceLink = row[7]

            val pharmacy = Pharmacy(
                pharmacyId = pharmacyId,
                pharmacyName = pharmacyName,
                address = address,
                website = website,
                operatingHours = operatingHours,
                phone = phone,
                rating = rating,
                insuranceLink = insuranceLink
            )
            pharmacyDao.insertPharmacy(pharmacy)
        }
    }

    suspend fun importMedicationData(csvReader: CSVReader, context: Context) {
        val database = NoteDatabase.getDatabase(context)
        val medicationDao = database.medicationDao()

        csvReader.readAll().drop(1).forEach { row ->
            val medicineName = row[9]
            val description = row[10]

            val medication = Medication(
                medicineName = medicineName,
                medicationDescription = description
            )
            medicationDao.insertMedication(medication)
        }
    }

    suspend fun linkPharmacyAndMedications(csvReader: CSVReader, context: Context) {
        val database = NoteDatabase.getDatabase(context)
        val pharmacyMedicationDao = database.pharmacyMedicationDao()
        val medicationDao = database.medicationDao()

        csvReader.readAll().drop(1).forEach { row ->
            if (row.any { it.isBlank() }) {
                // 跳过有空字段的行
                return@forEach
            }

            val pharmacyId = row[0].toInt()
            val medications = row[8].split(",").map { it.trim() }
            Log.d("CSVImporter", "Pharmacy ID: $pharmacyId, Medications: $medications")

            medications.forEach { medicineName ->
                val medication = medicationDao.getMedicationByName(medicineName)
                if (medication == null) {
                    Log.d("CSVImporter", "Medication not found: $medicineName")
                } else {
                    pharmacyMedicationDao.insertPharmacyMedication(
                        PharmacyMedication(pharmacyId = pharmacyId, medicationId = medication.medicationId)
                    )
                    Log.d(
                        "CSVImporter",
                        "Inserted PharmacyMedication -> Pharmacy ID: $pharmacyId, Medication ID: ${medication.medicationId}"
                    )
                }
            }
        }
    }
//测试
//    suspend fun logPharmacyMedicationData(context: Context) {
//        val database = NoteDatabase.getDatabase(context)
//        val pharmacyMedicationDao = database.pharmacyMedicationDao()
//
//        val data = pharmacyMedicationDao.getAllPharmacyMedications()
//        if (data.isEmpty()) {
//            Log.d("PharmacyMedication", "No data found in PharmacyMedication table.")
//        } else {
//            data.forEach {
//                Log.d(
//                    "PharmacyMedication",
//                    "Pharmacy ID: ${it.pharmacyId}, Medication ID: ${it.medicationId}"
//                )
//            }
//        }
//    }
}