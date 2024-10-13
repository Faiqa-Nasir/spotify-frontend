package com.example.mobileproject
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreatePlaylistActivity : AppCompatActivity() {

    private lateinit var closeButton: ImageView
    private lateinit var playlistNameInput: EditText
    private lateinit var createButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_playlist)

        closeButton = findViewById(R.id.close_button)
        playlistNameInput = findViewById(R.id.playlist_name_input)
        createButton = findViewById(R.id.create_button)
        auth = FirebaseAuth.getInstance()

        closeButton.setOnClickListener {
            finish() // Close the activity
        }

        createButton.setOnClickListener {
            val playlistName = playlistNameInput.text.toString().trim()
            if (playlistName.isNotEmpty()) {
                getTokenAndCreatePlaylist(playlistName)
            } else {
                Toast.makeText(this, "Please enter a playlist name.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTokenAndCreatePlaylist(playlistName: String) {
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            // Get the Firebase ID token
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseToken = task.result?.token
                    if (firebaseToken != null) {
                        // Proceed with the API call using Retrofit
                        createPlaylist(playlistName, firebaseToken)
                    } else {
                        Toast.makeText(this, "Failed to retrieve token.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error getting token: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createPlaylist(playlistName: String, token: String) {
        val playlistData = mapOf("name" to playlistName) // Creating the correct body for the API request

        RetrofitClient.backendService.createPlaylist("Bearer $token", playlistData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@CreatePlaylistActivity, "Playlist created successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity on success
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CreatePlaylistActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@CreatePlaylistActivity, "Failed to create playlist", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
