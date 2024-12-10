package com.cs407.elderassist_tutorial

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class CameraScan : AppCompatActivity() {
    private lateinit var imageHolder: ImageView
    private lateinit var textOutput: TextView
    private lateinit var copyTextButton: Button
    private lateinit var searchInMapButton: Button

    private var photoUri: Uri? = null

    private val imageCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val cameraButton: Button = findViewById(R.id.cameraButton)
            cameraButton.isEnabled = true
            if (result.resultCode == Activity.RESULT_OK && photoUri != null) {
                try {
                    val imageBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri!!))
                    imageHolder.setImageBitmap(imageBitmap)
                    processImage(imageBitmap)
                } catch (e: Exception) {
                    Log.e("CameraScan", "Error processing captured image: ${e.message}")
                    Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val imagePickLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
                imageHolder.setImageBitmap(imageBitmap)
                processImage(imageBitmap)
            } catch (e: Exception) {
                Log.e("CameraScan", "Error processing selected image: ${e.message}")
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        imageHolder = findViewById(R.id.imageHolder)
        textOutput = findViewById(R.id.textOutput)
        copyTextButton = findViewById(R.id.copyTextButton)
        val backButton = findViewById<ImageView>(R.id.backButton)
        searchInMapButton = findViewById(R.id.searchInMapButton)

        copyTextButton.setOnClickListener { copyTextToClipboard() }
        copyTextButton.visibility = View.GONE
        searchInMapButton.isEnabled = false

        backButton.setOnClickListener {
            finish()
        }

        checkAndRequestPermissions()
        showImageSourceDialog()

        val searchInMapButton: Button = findViewById(R.id.searchInMapButton)
        searchInMapButton.setOnClickListener { searchInMap(it) }

    }

    private fun showImageSourceDialog() {
        val items = arrayOf<CharSequence>("Take Photos", "Add from Camera Roll")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photos")
        builder.setItems(items) { _, item ->
            when (item) {
                0 -> dispatchTakePictureIntent() // 拍照
                1 -> imagePickLauncher.launch("image/*") // 从图库选择
            }
        }
        builder.show()
    }


    private fun checkAndRequestPermissions() {
        val requiredPermissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 100) {
//            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
//                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    fun launchCamera(view: View) {
        val cameraButton: Button = findViewById(R.id.cameraButton)
        cameraButton.isEnabled = false

        val items = arrayOf<CharSequence>("Take Photos", "Add from Camera Roll")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photos")
        builder.setItems(items) { _, item ->
            when (item) {
                0 -> dispatchTakePictureIntent()
                1 -> imagePickLauncher.launch("image/*")
            }
        }
        builder.setOnDismissListener {
            cameraButton.isEnabled = true
        }
        builder.show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            createImageFile()
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        imageCaptureLauncher.launch(takePictureIntent)
    }

    private fun createImageFile(): File {
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    private fun processImage(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotEmpty()) {
                    textOutput.text = visionText.text
                    copyTextButton.visibility = View.VISIBLE
                    searchInMapButton.isEnabled = true
                } else {
                    labelImage(inputImage)
                }
                photoUri = null
            }
            .addOnFailureListener {
                labelImage(inputImage)
                photoUri = null
            }
    }


    private fun labelImage(inputImage: InputImage) {
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        labeler.process(inputImage)
            .addOnSuccessListener { labels ->
                if (labels.isNotEmpty()) {
                    val labelsText = "I guess it is a: " + labels.joinToString(", ") { label ->
                        "${label.text} (${String.format("%.2f", label.confidence * 100)}%)"
                    }
                    textOutput.text = labelsText
                    copyTextButton.visibility = View.VISIBLE
                    searchInMapButton.isEnabled = false
                } else {
                    textOutput.text = "No objects detected."
                    copyTextButton.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                textOutput.text = "Error labeling image."
                copyTextButton.visibility = View.GONE
                searchInMapButton.isEnabled = false
            }
    }

    private fun copyTextToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", textOutput.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun loadPharmacyData(): List<Map<String, String>> {
        val assetManager = assets
        val inputStream = assetManager.open("pharmacy_data.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))

        val headers = reader.readLine().split(",") // Assumes the first row contains column names
        val data = mutableListOf<Map<String, String>>()

        reader.forEachLine { line ->
            val values = line.split(",")
            val row = headers.zip(values).toMap()
            data.add(row)
        }

        reader.close()
        return data
    }

    private fun searchInMap(view: View) {
        val recognizedText = textOutput.text.toString().lowercase().trim()
        if (recognizedText.isNotEmpty()) {
            val pharmacyData = loadPharmacyData()
            val matchingPharmacies = pharmacyData.filter { row ->
                val medications = row["MedicationName"]?.lowercase()?.split("/") ?: emptyList()
                medications.any { it.contains(recognizedText) || recognizedText.contains(it) }
            }.filter { row ->
                !row["PharmacyName"].isNullOrEmpty() &&
                        !row["Address"].isNullOrEmpty()
            }

            if (matchingPharmacies.isNotEmpty()) {
                val resultMessage = matchingPharmacies.joinToString("\n\n") { row ->
                    """
                Pharmacy: ${row["PharmacyName"]?.cleanValue()}
                Address: ${row["Address"]?.cleanValue()}
                Website: ${row["Website"]?.cleanValue() ?: "Not Available"}
                Operating Hours: ${row["OperatingHours"]?.cleanValue() ?: "Not Available"}
                Phone: ${row["Phone"]?.cleanValue() ?: "Not Available"}
                Rating: ${row["Rating"]?.cleanValue() ?: "Not Rated"}
                """.trimIndent()
                }
                showResultDialog(resultMessage)
                Log.d("SearchInMap", "Matching Pharmacies: $matchingPharmacies")
            } else {
                Toast.makeText(this, "No matching pharmacies found.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No text available for search.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun String.cleanValue(): String {
        return this.replace("\"", "").trim()
    }

    private fun showResultDialog(resultMessage: String) {
        AlertDialog.Builder(this)
            .setTitle("Pharmacy Results")
            .setMessage(resultMessage)
            .setPositiveButton("OK", null)
            .show()
    }

}
