package com.example.spotshare

data class Post(
    var placeName: String? = "",
    var description: String? = "",
    var category: String? = "",
    var imageUrl: String? = "",
    var userId: String? = "",
    var likedBy: List<String> = listOf(),
    var latitude: Double? = null,
    var longitude: Double? = null,
    var timestamp: Long = 0L
)
