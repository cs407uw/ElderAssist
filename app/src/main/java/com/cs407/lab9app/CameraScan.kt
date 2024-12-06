package com.cs407.lab9app

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class CameraScan : AppCompatActivity() {
    private lateinit var imageHolder: ImageView
    private lateinit var textOutput: TextView
    private lateinit var copyTextButton: Button

    private val imageCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                imageHolder.setImageBitmap(it)
                processImage(it)
            }
        }
    }

    private val imagePickLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            imageHolder.setImageBitmap(imageBitmap)
            processImage(imageBitmap)
        }
    }

//    companion object {
//        private const val REQUEST_READ_STORAGE_PERMISSION = 101
//        private const val PICK_PHOTO_REQUEST = 102
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        imageHolder = findViewById(R.id.imageHolder)
        textOutput = findViewById(R.id.textOutput)
        copyTextButton = findViewById(R.id.copyTextButton)
        copyTextButton.setOnClickListener { copyTextToClipboard() }
        copyTextButton.visibility = View.GONE  // Initially hide the copy button

//        val button = findViewById<Button>(R.id.cameraButton)
//        button.setOnClickListener {
//            checkAndRequestPermission()
//        }
    }

//    private fun checkAndRequestPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_STORAGE_PERMISSION)
//        } else {
//            openGallery()
//        }
//    }

//    private fun openGallery() {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(intent, PICK_PHOTO_REQUEST)
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_READ_STORAGE_PERMISSION) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openGallery()
//            } else {
//                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    fun launchCamera(view: View) {
        val items = arrayOf<CharSequence>("拍照", "从相册选择")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("选择图片来源")
        builder.setItems(items) { _, item ->
            when (item) {
                0 -> dispatchTakePictureIntent()  // 拍照
                1 -> imagePickLauncher.launch("image/*")  // 从图库选择
            }
        }
        builder.show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageCaptureLauncher.launch(takePictureIntent)
    }

    private fun processImage(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotEmpty()) {
                    textOutput.text = visionText.text
                    copyTextButton.visibility = View.VISIBLE  // Show the copy button
                } else {
                    // No text found, try image labeling
                    labelImage(inputImage)
                }
            }
            .addOnFailureListener {
                // Text recognition failed, try image labeling
                labelImage(inputImage)
            }
    }

    private fun labelImage(inputImage: InputImage) {
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        labeler.process(inputImage)
            .addOnSuccessListener { labels ->
                if (labels.isNotEmpty()) {
                    val labelsText = labels.joinToString("\n") { label ->
                        "${label.text}: ${String.format("%.2f", label.confidence * 100)}%"
                    }
                    textOutput.text = labelsText
                    copyTextButton.visibility = View.VISIBLE  // Show the copy button
                } else {
                    textOutput.text = "No objects detected."
                    copyTextButton.visibility = View.GONE  // Hide the copy button
                }
            }
            .addOnFailureListener {
                textOutput.text = "Error labeling image."
                copyTextButton.visibility = View.GONE  // Hide the copy button
            }
    }

    private fun copyTextToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", textOutput.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
