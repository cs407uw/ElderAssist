package com.cs407.elderassist_tutorial

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cs407.elderassist_tutorial.data.NoteDatabase
import com.cs407.elderassist_tutorial.data.SavedLocation
import com.cs407.elderassist_tutorial.utils.CSVimport
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var selectedLocation: LatLng? = null
    private var userLocation: LatLng? = null
    private val database by lazy { NoteDatabase.getDatabase(this) }
    private val savedLocationDao by lazy { database.savedLocationDao() }


    private var isOriginalMarkerLoaded = false

    // Store destination for navigation if needed (not used here since we removed the prompt)
    private var currentDestination: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        checkAndRequestPermissions()

        lifecycleScope.launch {
            try {
                val csvReader = CSVReader(InputStreamReader(assets.open("pharmacy_data.csv")))
                CSVimport.importPharmacyData(csvReader, this@MapActivity)

                val csvReader2 = CSVReader(InputStreamReader(assets.open("pharmacy_data.csv")))
                CSVimport.importMedicationData(csvReader2, this@MapActivity)

                val csvReader3 = CSVReader(InputStreamReader(assets.open("pharmacy_data.csv")))
                CSVimport.linkPharmacyAndMedications(csvReader3, this@MapActivity)

                Log.d("CSVImport", "Pharmacy data imported successfully")

                // If there's any userinfo from intent
                val searchType = intent.getStringExtra("SEARCH_TYPE")
                val searchName = intent.getStringExtra("SEARCH_NAME")
                val searchInput = findViewById<EditText>(R.id.searchInput)

                if (!searchName.isNullOrEmpty()) {
                    try {
                        searchInput.setText(searchName)
                        when (searchType) {
                            "medicine" -> searchPharmaciesWithMedicine(searchInput.text.toString().trim())
                            "pharmacy" -> searchPharmacyByName(searchInput.text.toString().trim())
                        }
                    } catch (e: Exception) {
                        Log.e("Maperror", "Error in searchPharmacyByName: ${e.message}")
                        Toast.makeText(this@MapActivity, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("CSVImport", "Error importing pharmacy data: ${e.message}")
            }
        }

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val searchButton = findViewById<ImageView>(R.id.searchPharmacyButton)
        val searchInput = findViewById<EditText>(R.id.searchInput)
        searchButton.setOnClickListener {
            val medicineName = searchInput.text.toString().trim()
            if (medicineName.isNotEmpty()) {
                searchPharmaciesWithMedicine(medicineName)
            } else {
                Toast.makeText(this, "Please enter a medicine name", Toast.LENGTH_SHORT).show()
            }
        }

        val nearbyButton = findViewById<Button>(R.id.nearbyPharmaciesButton)
        nearbyButton.setOnClickListener {
            showNearbyPharmacies()
        }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        requestLocationPermission()

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val saveButton = findViewById<ImageView>(R.id.saveLocationButton)
        saveButton.setOnClickListener {
            selectedLocation?.let {
                saveLocation(it)
            } ?: Toast.makeText(this, "No location selected", Toast.LENGTH_SHORT).show()
        }

        val showSavedLocationsButton = findViewById<Button>(R.id.showSavedLocationsButton)
        showSavedLocationsButton.setOnClickListener {
            showSavedLocationsPanel()
        }
        // If there's any userinfo from intent
        val searchType = intent.getStringExtra("SEARCH_TYPE")
        val searchName = intent.getStringExtra("SEARCH_NAME")

        if (!searchName.isNullOrEmpty()) {
            try {
                searchInput.setText(searchName)
                when (searchType) {
                    "medicine" -> searchPharmaciesWithMedicine(searchInput.text.toString().trim())
                    "pharmacy" -> searchPharmacyByName(searchInput.text.toString().trim())
                }
            } catch (e: Exception) {
                Log.e("Maperror", "Error in searchPharmacyByName: ${e.message}")
                Toast.makeText(this@MapActivity, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fetchUserLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0f,
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    userLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLocation ?: LatLng(0.0, 0.0), 15f
                        )
                    )
                    locationManager.removeUpdates(this)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fetchUserLocation() // Fetch location as soon as we know we have permission.
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

// 加载 Original Markers
        lifecycleScope.launch {
            val pharmacies = database.pharmacyDao().getAllPharmacies()
            pharmacies.forEach { pharmacy ->
                val latLng = getLatLngFromAddress(pharmacy.address)
                if (latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(pharmacy.pharmacyName)
                            .snippet("Original Marker")
                    )
                }
            }

            // Original Markers 加载完成，设置标志位
            isOriginalMarkerLoaded = true
            Log.d("MapActivity", "Original markers loaded")
        }

        // Map click to set selected location
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            selectedLocation = latLng
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showNearbyPharmacies() {
        val pharmacyDao = database.pharmacyDao()

        lifecycleScope.launch {
            val pharmacies = pharmacyDao.getAllPharmacies()

            if (pharmacies.isEmpty()) {
                Toast.makeText(
                    this@MapActivity,
                    "No pharmacies found in the database",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            mMap.clear()
            pharmacies.forEach { pharmacy ->
                val latLng = getLatLngFromAddress(pharmacy.address)
                if (latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(pharmacy.pharmacyName)
                            .snippet("Hours: ${pharmacy.operatingHours ?: "Unknown"}")
                    )
                }
            }

            val firstLatLng = pharmacies.firstOrNull()?.let { getLatLngFromAddress(it.address) }
            val centerLatLng = firstLatLng ?: userLocation ?: LatLng(0.0, 0.0)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 12f))
        }
    }

    private fun searchPharmaciesWithMedicine(medicineName: String) {
        val pharmacyMedicationDao = database.pharmacyMedicationDao()
        val medicationDao = database.medicationDao()

        lifecycleScope.launch {
            // 等待 Original Markers 加载完成
            while (!isOriginalMarkerLoaded) {
                Log.d("MapActivity", "Waiting for original markers to load...")
                kotlinx.coroutines.delay(100) // 每 100ms 检查一次
            }

            val medication = medicationDao.getMedicationByName(medicineName)
            if (medication == null) {
                Toast.makeText(this@MapActivity, "Medicine not found", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val pharmacies = pharmacyMedicationDao.getPharmaciesByMedication(medication.medicationId)
            if (pharmacies.isEmpty()) {
                Toast.makeText(
                    this@MapActivity,
                    "No pharmacies found for this medicine",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val userLatLng = userLocation ?: LatLng(0.0, 0.0)
            val closestPharmacy = pharmacies.minByOrNull { pharmacy ->
                val pharmacyLatLng = getLatLngFromAddress(pharmacy.address)
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    userLatLng.latitude, userLatLng.longitude,
                    pharmacyLatLng.latitude, pharmacyLatLng.longitude,
                    results
                )
                results[0]
            }

            if (closestPharmacy != null) {
                val closestLatLng = getLatLngFromAddress(closestPharmacy.address)
                currentDestination = closestLatLng
                mMap.clear()
                mMap.addMarker(
                    MarkerOptions()
                        .position(closestLatLng)
                        .title("Closest Pharmacy: ${closestPharmacy.pharmacyName}")
                        .snippet("Address: ${closestPharmacy.address}")
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(closestLatLng, 15f))
                // Removed the AlertDialog navigation prompt
                // Now it just shows the closest pharmacy.
            } else {
                Toast.makeText(this@MapActivity, "No pharmacies found nearby", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Additional functions for route fetching and parsing if you still need them for other functionalities
    // (Not triggered automatically from this code since we removed the dialog)
    // You can have another button or menu option to start navigation.

    private fun getLatLngFromAddress(address: String): LatLng {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val location = geocoder.getFromLocationName(address, 1)?.firstOrNull()
            if (location != null) LatLng(location.latitude, location.longitude) else LatLng(0.0, 0.0)
        } catch (e: Exception) {
            LatLng(0.0, 0.0)
        }
    }

    private fun saveLocation(location: LatLng) {
        lifecycleScope.launch {
            savedLocationDao.insertD(SavedLocation(latitude = location.latitude, longitude = location.longitude))
            Toast.makeText(this@MapActivity, "Location saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSavedLocationsPanel() {
        val showSavedLocationsButton = findViewById<Button>(R.id.showSavedLocationsButton)
        showSavedLocationsButton.setOnClickListener {
            val intent = Intent(this, SavedLocationsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //userinfo
    private fun searchPharmacyByName(pharmacyName: String) {
        val database = NoteDatabase.getDatabase(this)
        val pharmacyDao = database.pharmacyDao()

        lifecycleScope.launch {

            while (!isOriginalMarkerLoaded) {
                Log.d("MapActivity", "Waiting for original markers to load...")
                kotlinx.coroutines.delay(100) // 每 100ms 检查一次
            }

            val pharmacies = withContext(Dispatchers.IO) {
                listOfNotNull(pharmacyDao.getPharmacyByName(pharmacyName))
            }

            if (pharmacies.isEmpty()) {
                Toast.makeText(this@MapActivity, "No pharmacies found for this name", Toast.LENGTH_SHORT).show()
                return@launch
            }

            mMap.clear()

            pharmacies.forEach { pharmacy ->
                val pharmacyLatLng = getLatLngFromAddress(pharmacy.address ?: "")
                if (pharmacyLatLng.latitude != 0.0 && pharmacyLatLng.longitude != 0.0) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(pharmacyLatLng)
                            .title(pharmacy.pharmacyName)
                            .snippet("Address: ${pharmacy.address}\nPhone: ${pharmacy.phone}")
                    )
                } else {
                    Log.e("MapActivity", "Invalid coordinates for pharmacy: ${pharmacy.pharmacyName}")
                }
            }

            val userLatLng = userLocation ?: LatLng(0.0, 0.0)
            val closestPharmacy = pharmacies.minByOrNull { pharmacy ->
                val pharmacyLatLng = getLatLngFromAddress(pharmacy.address ?: "")
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    userLatLng.latitude, userLatLng.longitude,
                    pharmacyLatLng.latitude, pharmacyLatLng.longitude,
                    results
                )
                results[0]
            }

            closestPharmacy?.let { pharmacy ->
                val closestLatLng = getLatLngFromAddress(pharmacy.address ?: "")
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(closestLatLng, 15f))
            }
        }
    }

}

