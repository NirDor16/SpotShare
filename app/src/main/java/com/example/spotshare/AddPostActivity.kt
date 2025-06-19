package com.example.spotshare

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.spotshare.databinding.ActivityAddPostBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPostBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .into(binding.addImagePreview)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        Glide.with(this)
            .load(R.drawable.image)
            .fitCenter()
            .into(binding.addImagePreview)


        val categories = listOf("בחר קטגוריה", "חברים", "דייטים", "משפחה")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
        binding.spinnerCategory.setSelection(0)

        binding.addImageSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        binding.addBTNSave.setOnClickListener {
            savePost()
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_add_post
        BottomNavigationViewHelper.setup(binding.bottomNavigation, this)
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                currentLocation = location
            }
            .addOnFailureListener {
                Toast.makeText(this, "שגיאה בקבלת מיקום", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePost() {
        val placeName = binding.addETPlace.text.toString().trim()
        val description = binding.addETDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()

        if (placeName.isEmpty() || description.isEmpty() || category == "בחר קטגוריה") {
            Toast.makeText(this, "יש למלא את כל השדות כולל קטגוריה", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "יש לבחור תמונה לפני שמירה", Toast.LENGTH_SHORT).show()
            return
        }

        val postId = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().getReference("images/$postId.jpg")

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    uploadPost(placeName, description, category, uri.toString(), postId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "שגיאה בהעלאת התמונה", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPost(place: String, desc: String, cat: String, imageUrl: String, postId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val latitude = currentLocation?.latitude
        val longitude = currentLocation?.longitude

        val post = Post(
            placeName = place,
            description = desc,
            category = cat,
            imageUrl = imageUrl,
            userId = userId,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance().getReference("posts").child(postId)
            .setValue(post)
            .addOnSuccessListener {
                Toast.makeText(this, "הפוסט נשמר בהצלחה", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "שגיאה בשמירת הפוסט", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        }
    }
}
