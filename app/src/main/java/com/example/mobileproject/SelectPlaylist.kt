package com.example.mobileproject

import Song
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectPlaylist : AppCompatActivity() {

    private lateinit var playlistContainer: LinearLayout
    private lateinit var playlistsButton: Button
    private lateinit var auth: FirebaseAuth
    private var songId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_playlist)

        // Initialize views
        playlistContainer = findViewById(R.id.playlist_container)
        playlistsButton = findViewById(R.id.playlists_button)
        auth = FirebaseAuth.getInstance()

        // Get the song ID passed from the previous activity
        songId = intent.getStringExtra("songId")

        // Set click listener for button (if needed)
        playlistsButton.setOnClickListener {
            // Handle button click, maybe to add a new playlist
            // startActivity(Intent(this, CreatePlaylistActivity::class.java))
        }

        // Fetch playlists on load
        fetchPlaylists()
    }

    private fun fetchPlaylists() {
        val user = auth.currentUser
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val authToken = "Bearer ${task.result?.token}"
                    makeApiCall(authToken)
                } else {
                    Toast.makeText(this, "Failed to get token.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeApiCall(authToken: String) {
        RetrofitClient.backendService.getPlaylists(authToken).enqueue(object : Callback<List<Playlist>> {
            override fun onResponse(call: Call<List<Playlist>>, response: Response<List<Playlist>>) {
                if (response.isSuccessful) {
                    response.body()?.let { playlistList ->
                        Log.d("SelectPlaylist", "Playlists fetched successfully: $playlistList")
                        displayPlaylists(playlistList, authToken)
                    } ?: run {
                        Log.d("SelectPlaylist", "No playlists found.")
                    }
                } else {
                    Log.e("SelectPlaylist", "Error fetching playlists: ${response.errorBody()?.string()}")
                    Toast.makeText(this@SelectPlaylist, "Error fetching playlists.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Playlist>>, t: Throwable) {
                Log.e("SelectPlaylist", "Network error: ${t.message}", t)
                Toast.makeText(this@SelectPlaylist, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayPlaylists(playlists: List<Playlist>, authToken: String) {
        playlistContainer.removeAllViews() // Clear previous views

        for (playlist in playlists) {
            val playlistView = LayoutInflater.from(this).inflate(R.layout.item_playlist, playlistContainer, false)
            val playlistIcon = playlistView.findViewById<ImageView>(R.id.playlist_icon)
            val playlistTitle = playlistView.findViewById<TextView>(R.id.playlist_title)
            val playlistSubtitle = playlistView.findViewById<TextView>(R.id.playlist_subtitle)

            playlistTitle.text = playlist.name
            playlistSubtitle.text = "Playlist • songs"

            // Optionally load images into playlistIcon using Glide or another image loading library
            // Glide.with(this).load(playlist.iconUrl).into(playlistIcon)

            playlistView.setOnClickListener {
                songId?.let { songId ->
                    addSongToPlaylist(authToken, songId, playlist.id)
                } ?: run {
                    Toast.makeText(this, "No song selected.", Toast.LENGTH_SHORT).show()
                }
            }

            playlistContainer.addView(playlistView)
        }
    }

    private fun addSongToPlaylist(authToken: String, songId: String, playlistId: String) {
        val requestBody = mapOf("songId" to songId, "playlistId" to playlistId)

        RetrofitClient.backendService.addSongToPlaylist(authToken, requestBody).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SelectPlaylist, "Added to playlist successfully.", Toast.LENGTH_SHORT).show()
                    // Go back to the previous screen
                    finish()
                } else {
                    Log.e("SelectPlaylist", "Error adding song to playlist: ${response.errorBody()?.string()}")
                    Toast.makeText(this@SelectPlaylist, "Error adding song to playlist.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("SelectPlaylist", "Network error: ${t.message}", t)
                Toast.makeText(this@SelectPlaylist, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
