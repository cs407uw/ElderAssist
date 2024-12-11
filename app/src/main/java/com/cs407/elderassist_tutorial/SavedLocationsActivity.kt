package com.cs407.elderassist_tutorial

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cs407.elderassist_tutorial.data.Note
import com.cs407.elderassist_tutorial.data.NoteDatabase
import com.cs407.elderassist_tutorial.data.SavedLocation
import kotlinx.coroutines.launch
import java.util.*
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cs407.elderassist_tutorial.data.Medication
import com.cs407.elderassist_tutorial.utils.CSVimport
import com.opencsv.CSVReader
import java.io.InputStreamReader

class SavedLocationsActivity : AppCompatActivity() {

    private lateinit var adapter: SavedLocationsAdapter
    private val database by lazy { NoteDatabase.getDatabase(this) }
    private val savedLocationDao by lazy { database.savedLocationDao() }
    private val medicationDao by lazy { database.medicationDao() }
    private val noteDao by lazy { database.noteDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_locations)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.savedLocationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SavedLocationsAdapter(
            onAddNoteClicked = { location -> showAddNoteDialog(location) },
            onViewNotesClicked = { location -> showNotesForLocation(location) }
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            val csvReaderPharmacy = CSVReader(InputStreamReader(assets.open("pharmacy_data.csv")))
            CSVimport.importPharmacyData(csvReaderPharmacy, this@SavedLocationsActivity)

            // Import medication data
            val csvReaderMedication = CSVReader(InputStreamReader(assets.open("pharmacy_data.csv")))
            CSVimport.importMedicationData(csvReaderMedication, this@SavedLocationsActivity)

            // Link pharmacy and medications
            val csvReaderLink = CSVReader(InputStreamReader(assets.open("pharmacy_data.csv")))
            CSVimport.linkPharmacyAndMedications(csvReaderLink, this@SavedLocationsActivity)

            Log.d("CSVImport", "Data imported successfully.")
            val savedLocations = savedLocationDao.getAllLocations()
            val geocoder = Geocoder(this@SavedLocationsActivity, Locale.getDefault())
            val locationItems = savedLocations.map { loc ->
                val address = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                val addressStr = if (!address.isNullOrEmpty()) {
                    address[0].getAddressLine(0)
                } else {
                    "Lat: ${loc.latitude}, Lng: ${loc.longitude}"
                }
                LocationItem(loc, addressStr)
            }
            adapter.submitList(locationItems)
        }
    }

    private fun showAddNoteDialog(locationItem: LocationItem) {
        val editText = EditText(this)
        editText.hint = "Enter medicine name"
        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val medicineName = editText.text.toString().trim()
                if (medicineName.isNotEmpty()) {
                    addNoteForLocation(locationItem.location, medicineName)
                } else {
                    Toast.makeText(this, "Please enter a medicine name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNoteForLocation(location: SavedLocation, medicineName: String) {
        lifecycleScope.launch {
            val medication = medicationDao.getMedicationByName(medicineName)
            if (medication == null) {
                Toast.makeText(
                    this@SavedLocationsActivity,
                    "Medicine not found in DB",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Create a note
            val note = Note(
                locationId = location.id,
                medicineName = medication.medicineName,
                medicationDescription = medication.medicationDescription
            )
            noteDao.insertNote(note)
            Toast.makeText(this@SavedLocationsActivity, "Note added!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotesForLocation(locationItem: LocationItem) {
        lifecycleScope.launch {
            val notes = noteDao.getNotesForLocation(locationItem.location.id)
            if (notes.isEmpty()) {
                Toast.makeText(
                    this@SavedLocationsActivity,
                    "No notes for this location",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            val notesStrings = notes.map { n ->
                "Medicine: ${n.medicineName}\nDescription: ${n.medicationDescription}\n"
            }

            AlertDialog.Builder(this@SavedLocationsActivity)
                .setTitle("Notes for ${locationItem.address}")
                .setItems(notesStrings.toTypedArray(), null)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    data class LocationItem(val location: SavedLocation, val address: String)
}

class SavedLocationsAdapter(
    val onAddNoteClicked: (SavedLocationsActivity.LocationItem) -> Unit,
    val onViewNotesClicked: (SavedLocationsActivity.LocationItem) -> Unit
) : RecyclerView.Adapter<SavedLocationsAdapter.ViewHolder>() {

    private val items = mutableListOf<SavedLocationsActivity.LocationItem>()

    fun submitList(newItems: List<SavedLocationsActivity.LocationItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val addressTextView: TextView = view.findViewById(R.id.locationAddressTextView)
        val addNoteButton: Button = view.findViewById(R.id.addNoteButton)
        val viewNotesButton: Button = view.findViewById(R.id.viewNotesButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_location, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.addressTextView.text = item.address
        holder.addNoteButton.setOnClickListener {
            onAddNoteClicked(item)
        }
        holder.viewNotesButton.setOnClickListener {
            onViewNotesClicked(item)
        }
    }
}