package com.cs407.elderassist_tutorial

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cs407.elderassist_tutorial.data.NoteDatabase
import com.cs407.elderassist_tutorial.data.Pharmacy
import com.cs407.elderassist_tutorial.data.SavedLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var selectedLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Back Button
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Map Fragment Initialization
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Save Location Button
        val saveButton = findViewById<Button>(R.id.saveLocationButton)
        saveButton.setOnClickListener {
            selectedLocation?.let {
                saveLocation(it)
            } ?: Toast.makeText(this, "No location selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Check Location Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Display pharmacies on the map
        addPharmaciesToMap()

        // Map Click Listener
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            selectedLocation = latLng
        }
    }

    private fun addPharmaciesToMap() {
        val database = NoteDatabase.getDatabase(this)
        val pharmacyDao = database.pharmacyDao()

        lifecycleScope.launch {
            val pharmacies = pharmacyDao.getAllPharmacies()
            pharmacies.forEach { pharmacy ->
                val latLng = getLatLngFromAddress(pharmacy.address)
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(pharmacy.pharmacyName)
                        .snippet("Hours: ${pharmacy.operatingHours ?: "Unknown"}")
                )
            }

            // Zoom to the first pharmacy if available
            if (pharmacies.isNotEmpty()) {
                val firstLatLng = getLatLngFromAddress(pharmacies[0].address)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 12f))
            }
        }
    }

    private fun getLatLngFromAddress(address: String): LatLng {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val location = geocoder.getFromLocationName(address, 1)?.firstOrNull()
            if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                LatLng(0.0, 0.0) // Default location if the address can't be resolved
            }
        } catch (e: Exception) {
            LatLng(0.0, 0.0) // Default location in case of an error
        }
    }

    private fun saveLocation(location: LatLng) {
        val database = NoteDatabase.getDatabase(this)
        val savedLocationDao = database.savedLocationDao()

        lifecycleScope.launch {
            savedLocationDao.insertD(
                SavedLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
            Toast.makeText(this@MapActivity, "Location saved!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
