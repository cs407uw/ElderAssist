package com.cs407.elderassist_tutorial.utils

import android.content.Context
import com.cs407.elderassist_tutorial.data.NoteDatabase
import com.cs407.elderassist_tutorial.data.Medication
import com.cs407.elderassist_tutorial.data.Pharmacy
import com.cs407.elderassist_tutorial.data.PharmacyMedication

object SearchUtils {
    suspend fun searchPharmaciesByMedication(context: Context, medicineName: String): List<Pharmacy> {
        val database = NoteDatabase.getDatabase(context)
        val medicationDao = database.medicationDao()
        val pharmacyMedicationDao = database.pharmacyMedicationDao()

        val medication = medicationDao.getMedicationByName(medicineName)
        return medication?.let {
            pharmacyMedicationDao.getPharmaciesByMedication(it.medicationId)
        } ?: emptyList()
    }
    suspend fun searchMedicationsByPharmacy(context: Context, pharmacyName: String): List<Medication> {
        val database = NoteDatabase.getDatabase(context)
        val pharmacyDao = database.pharmacyDao()
        val pharmacyMedicationDao = database.pharmacyMedicationDao()

        val pharmacy = pharmacyDao.getPharmacyByName(pharmacyName)
        return pharmacy?.let {
            pharmacyMedicationDao.getMedicationsByPharmacy(it.pharmacyId)
        } ?: emptyList()
    }
}