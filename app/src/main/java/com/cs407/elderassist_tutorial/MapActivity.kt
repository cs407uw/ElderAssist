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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cs407.elderassist_tutorial.data.NoteDatabase
import com.cs407.elderassist_tutorial.data.SavedLocation
import com.cs407.elderassist_tutorial.data.Medication
import com.cs407.elderassist_tutorial.data.Pharmacy
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
import java.net.URLEncoder
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

    // Store destination for navigation
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

                // Ask user if they want navigation
                AlertDialog.Builder(this@MapActivity)
                    .setTitle("Navigation")
                    .setMessage("Do you want to navigate to ${closestPharmacy.pharmacyName}?")
                    .setPositiveButton("Yes") { _, _ ->
                        startNavigation(mode = "driving")
                    }
                    .setNegativeButton("No", null)
                    .show()
            } else {
                Toast.makeText(this@MapActivity, "No pharmacies found nearby", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startNavigation(mode: String = "driving") {
        val origin = userLocation
        val dest = currentDestination
        if (origin == null || dest == null) {
            Toast.makeText(this, "Origin or destination not set", Toast.LENGTH_SHORT).show()
            return
        }
        fetchRouteAndDisplay(origin, dest, mode)
    }

    private suspend fun getDirectionsJson(origin: LatLng, destination: LatLng, mode: String): String? {
        val apiKey = "AIzaSyAGQLEb1tJGrm76jo-_wK3Lep73ZiI4r78"
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}&" +
                "destination=${destination.latitude},${destination.longitude}&" +
                "mode=$mode&key=$apiKey"

        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) response.body?.string() else null
        }
    }

    data class RouteInfo(
        val polylinePoints: List<LatLng>,
        val distanceText: String,
        val durationText: String
    )

    private fun parseDirectionsJson(json: String): RouteInfo? {
        val jsonObj = JSONObject(json)
        val routes = jsonObj.getJSONArray("routes")
        if (routes.length() == 0) return null

        val route = routes.getJSONObject(0)
        val legs = route.getJSONArray("legs")
        val leg = legs.getJSONObject(0)

        val distanceText = leg.getJSONObject("distance").getString("text")
        val durationText = leg.getJSONObject("duration").getString("text")

        val polylineObj = route.getJSONObject("overview_polyline")
        val encodedPoints = polylineObj.getString("points")
        val decodedPoints = decodePolyline(encodedPoints)

        return RouteInfo(decodedPoints, distanceText, durationText)
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val latLng = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(latLng)
        }
        return poly
    }

    private fun showRouteOnMap(route: RouteInfo) {
        mMap.clear()
        userLocation?.let { mMap.addMarker(MarkerOptions().position(it).title("Your Location")) }
        currentDestination?.let { mMap.addMarker(MarkerOptions().position(it).title("Destination")) }

        val polylineOptions = PolylineOptions()
            .color(android.graphics.Color.BLUE)
            .width(10f)
            .addAll(route.polylinePoints)
        mMap.addPolyline(polylineOptions)

        val builder = LatLngBounds.Builder()
        route.polylinePoints.forEach { builder.include(it) }
        val bounds = builder.build()
        val padding = 100
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

        Toast.makeText(
            this,
            "Distance: ${route.distanceText}, Duration: ${route.durationText}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun fetchRouteAndDisplay(origin: LatLng, destination: LatLng, mode: String) {
        lifecycleScope.launch {
            val json = getDirectionsJson(origin, destination, mode)
            if (json != null) {
                val routeInfo = parseDirectionsJson(json)
                if (routeInfo != null) {
                    showRouteOnMap(routeInfo)
                } else {
                    Toast.makeText(this@MapActivity, "Failed to parse route", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@MapActivity, "Failed to fetch directions", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        // You can add logic here to show saved locations in another activity or dialog
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
}
