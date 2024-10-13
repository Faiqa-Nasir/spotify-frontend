package com.example.mobileproject
import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SongPageActivity : AppCompatActivity() {

    private lateinit var shareIcon: ImageView
    private lateinit var songNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var albumArtImageView: ImageView
    private lateinit var playButton: ImageButton
    private lateinit var releaseDateTextView: TextView
    private lateinit var heartIcon: ImageView
    private lateinit var backgroundImage: ImageView
    private var isPlaying = false
    private var isFirstPlay = true
    private var songId: String? = null
    private lateinit var auth: FirebaseAuth
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var plusIcon: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_page)
        auth = FirebaseAuth.getInstance()

        // Initialize share icon
        shareIcon = findViewById(R.id.shareIcon)

        // Set share functionality
        shareIcon.setOnClickListener {
            shareSong()
        }
        // Initialize UI elements
        backgroundImage = findViewById(R.id.backgroundImage)
        albumArtImageView = findViewById(R.id.albumArt)
        songNameTextView = findViewById(R.id.songName)
        artistNameTextView = findViewById(R.id.artistName)
        playButton = findViewById(R.id.playButton)
        releaseDateTextView = findViewById(R.id.releaseDate)
        heartIcon = findViewById(R.id.heartIcon)
        plusIcon = findViewById(R.id.plusIcon)


        // Retrieve the song ID passed via intent
        songId = intent.getStringExtra("songId")

        // Load song details from API
        loadSongDetails()

        // Handle play/pause button click
        playButton.setOnClickListener {
            togglePlayPause()
        }

        // Handle heart icon click for liking the song
        heartIcon.setOnClickListener {
            likeSong()
        }

        plusIcon.setOnClickListener {
            val intent = Intent(this, SelectPlaylist::class.java)
            intent.putExtra("songId", songId)
            startActivity(intent)
        }
    }
    private fun shareSong() {
        val songName = songNameTextView.text.toString()
        val artistName = artistNameTextView.text.toString()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out this song!")
            putExtra(Intent.EXTRA_TEXT, "Listen to $songName by $artistName.")
        }

        startActivity(Intent.createChooser(shareIntent, "Share song via"))
    }
    private fun loadSongDetails() {
        songId?.let {
            RetrofitClient.backendService.getSongById(it).enqueue(object : Callback<PlaySong> {
                override fun onResponse(call: Call<PlaySong>, response: Response<PlaySong>) {
                    if (response.isSuccessful) {
                        val song = response.body()
                        song?.let {
                            songNameTextView.text = it.name
                            artistNameTextView.text = it.artistName
                            releaseDateTextView.text = "Date of Release: ${it.dateOfRelease.take(10)}"
                            Glide.with(this@SongPageActivity)
                                .load(it.picture)  // Load image from URL
                                .into(backgroundImage)

                            // Load album art
                            Glide.with(this@SongPageActivity)
                                .load(it.picture)  // Load image from URL
                                .into(albumArtImageView)

                            // Sanitize the song name to create a valid resource identifier (e.g., replace spaces with underscores)
                            val sanitizedSongName = it.name.replace(" ", "_").lowercase() // Adjust sanitization as needed

                            // Check if the song resource exists in the raw directory using the sanitized song name
                            val resId = resources.getIdentifier(sanitizedSongName, "raw", packageName)
                            if (resId != 0) {
                                // Initialize MediaPlayer with the local resource
                                mediaPlayer = MediaPlayer.create(this@SongPageActivity, resId)
                                mediaPlayer?.setOnCompletionListener {
                                    // Reset to initial state on song completion
                                    playButton.setImageResource(R.drawable.ic_play)
                                    isPlaying = false
                                }
                            } else {
                                // Display toast message if the song is not available
                                Toast.makeText(this@SongPageActivity, "Song isn't available for playing", Toast.LENGTH_SHORT).show()
                                Log.e("SongPageActivity", "Song resource not found in raw directory with name: $sanitizedSongName")
                            }
                        }
                    } else {
                        Log.e("SongPageActivity", "Failed to retrieve song details: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<PlaySong>, t: Throwable) {
                    // Handle failure
                    Log.e("SongPageActivity", "Error fetching song details", t)
                }
            })
        }
    }

    private fun togglePlayPause() {
        isPlaying = !isPlaying

        if (isPlaying) {
            playButton.setImageResource(R.drawable.ic_pause)  // Change to pause icon
            if (isFirstPlay) {
                isFirstPlay = false
                updateSongOnFirstPlay()
            }
            startMusicService(songId)
            mediaPlayer?.start()  // Start playing the song
        } else {
            playButton.setImageResource(R.drawable.ic_play)  // Change back to play icon
            mediaPlayer?.pause()  // Pause the song
            stopMusicService()

        }
    }

    private fun likeSong() {
        songId?.let { id ->
            val currentUser: FirebaseUser? = auth.currentUser
            currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseToken = task.result?.token
                    if (firebaseToken != null) {
                        val requestBody = mapOf("songId" to id)
                        RetrofitClient.backendService.likeSong("Bearer $firebaseToken", requestBody).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d("SongPageActivity", "Song liked successfully")
                                    Toast.makeText(this@SongPageActivity, "Added to your liked songs", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.e("SongPageActivity", "Failed to like song: ${response.errorBody()?.string()}")
                                    Toast.makeText(this@SongPageActivity, "Song is already liked", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Log.e("SongPageActivity", "Error liking song", t)
                                Toast.makeText(this@SongPageActivity, "Song is already liked", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Log.e("SongPageActivity", "Firebase token is null")
                        Toast.makeText(this@SongPageActivity, "Not liked song", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("SongPageActivity", "Error retrieving Firebase token")
                    Toast.makeText(this@SongPageActivity, "Not liked song", Toast.LENGTH_SHORT).show()
                }
            } ?: Log.e("SongPageActivity", "User is not logged in")
        }
    }

    private fun updateSongOnFirstPlay() {
        songId?.let { id ->
            val currentUser: FirebaseUser? = auth.currentUser
            currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseToken = task.result?.token
                    if (firebaseToken != null) {
                        RetrofitClient.backendService.updateSongOnFirstPlay("Bearer $firebaseToken", id).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d("SongPageActivity", "Song updated on first play")
                                } else {
                                    Log.e("SongPageActivity", "Failed to update song on first play: ${response.errorBody()?.string()}")
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Log.e("SongPageActivity", "Error updating song on first play", t)
                            }
                        })
                    } else {
                        Log.e("SongPageActivity", "Firebase token is null")
                    }
                } else {
                    Log.e("SongPageActivity", "Error retrieving Firebase token")
                }
            } ?: Log.e("SongPageActivity", "User is not logged in")
        }
    }
    private fun startMusicService(songId: String?) {
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("songId", songId)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopMusicService() {
        val intent = Intent(this, MusicService::class.java)
        stopService(intent)
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        stopMusicService()  // Stop the service when the activity is destroyed
    }
}
