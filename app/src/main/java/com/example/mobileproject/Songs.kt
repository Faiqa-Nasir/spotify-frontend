package com.example.mobileproject

data class Songs(
    val id: String,
    val name: String,
    val artist: String, // Ensure this matches your API response
    val imageUrl: String // If there's an image associated with the song
)
