package com.example.spotshare

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.spotshare.databinding.ActivityEditPostBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostBinding
    private lateinit var postId: String
    private var selectedImageUri: Uri? = null
    private var existingImageUrl: String = ""

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .into(binding.editImagePreview)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.selectedItemId = R.id.nav_add_post
        BottomNavigationViewHelper.setup(binding.bottomNavigation, this)

        val categories = listOf("בחר קטגוריה", "חברים", "דייטים", "משפחה")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        postId = intent.getStringExtra("postId") ?: return
        val placeName = intent.getStringExtra("placeName") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        existingImageUrl = intent.getStringExtra("imageUrl") ?: ""

        binding.editETPlace.setText(placeName)
        binding.editETDescription.setText(description)
        binding.spinnerCategory.setSelection(categories.indexOf(category).coerceAtLeast(0))


        Glide.with(this)
            .load(if (existingImageUrl.isNotEmpty()) existingImageUrl else R.drawable.image)
            .fitCenter()
            .into(binding.editImagePreview)


        binding.editImageSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        binding.editBTNSave.setOnClickListener {
            if (selectedImageUri != null) {
                uploadNewImageAndUpdatePost()
            } else {
                updatePost(existingImageUrl)
            }
        }
    }

    private fun uploadNewImageAndUpdatePost() {
        val storageRef = FirebaseStorage.getInstance().getReference("images/${postId}.jpg")
        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updatePost(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "שגיאה בהעלאת התמונה", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePost(imageUrl: String) {
        val newPlace = binding.editETPlace.text.toString().trim()
        val newDesc = binding.editETDescription.text.toString().trim()
        val newCat = binding.spinnerCategory.selectedItem.toString()

        if (newPlace.isEmpty() || newDesc.isEmpty() || newCat == "בחר קטגוריה") {
            Toast.makeText(this, "יש למלא את כל השדות כולל קטגוריה", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "placeName" to newPlace,
            "description" to newDesc,
            "category" to newCat,
            "imageUrl" to imageUrl
        )

        FirebaseDatabase.getInstance().getReference("posts").child(postId)
            .updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "הפוסט עודכן בהצלחה", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "שגיאה בעדכון הפוסט", Toast.LENGTH_SHORT).show()
            }
    }
}
