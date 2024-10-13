package com.example.mobileproject

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArtistViewActivity : AppCompatActivity() {

    private lateinit var albumsLayout: LinearLayout
    private lateinit var genresTextView: TextView
    private lateinit var journeyTitleTextView: TextView
    private lateinit var journeyParagraphTextView: TextView
    private lateinit var followButton: Button
    private lateinit var artistImageView: ImageView
    private lateinit var artistNameTextView: TextView
    private lateinit var monthlyListenersTextView: TextView

    private var artistDetail: ArtistDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.artist_view)

        // Initialize views
        albumsLayout = findViewById(R.id.albumsLayout)
        genresTextView = findViewById(R.id.genresTextView)
        journeyTitleTextView = findViewById(R.id.journeyTitleTextView)
        journeyParagraphTextView = findViewById(R.id.journeyParagraphTextView)
        followButton = findViewById(R.id.followButton)
        artistImageView = findViewById(R.id.artistImageView)
        artistNameTextView = findViewById(R.id.artistNameTextView)
        monthlyListenersTextView = findViewById(R.id.monthlyListenersCountTextView)

        // Get the artist ID and follow button visibility from the intent
        val artistId = intent.getStringExtra("ARTIST_ID") ?: return
        val showFollowButton = intent.getBooleanExtra("SHOW_FOLLOW_BUTTON", true)

        // Show or hide the follow button based on the flag
        if (!showFollowButton) {
            followButton.visibility = Button.GONE
        }

        // Set up follow button click listener
        followButton.setOnClickListener {
            followArtist(artistId)
        }

        // Fetch artist details
        fetchArtistDetails(artistId)
        fetchArtistAlbums(artistId)  // Fetch albums
    }

    private fun fetchArtistAlbums(artistId: String) {
        RetrofitClient.backendService.getArtistAlbums(artistId).enqueue(object : Callback<List<Album>> {
            override fun onResponse(call: Call<List<Album>>, response: Response<List<Album>>) {
                if (response.isSuccessful) {
                    response.body()?.let { albums ->
                        // Remove existing views to avoid duplication
                        albumsLayout.removeAllViews()

                        for (album in albums) {
                            val albumView = layoutInflater.inflate(R.layout.album_item, albumsLayout, false)

                            val albumImageView: ImageView = albumView.findViewById(R.id.albumImageView)
                            val albumNameTextView: TextView = albumView.findViewById(R.id.albumNameTextView)
                            val albumReleaseDateTextView: TextView = albumView.findViewById(R.id.albumReleaseDateTextView)

                            Glide.with(this@ArtistViewActivity)
                                .load(album.picture)
                                .into(albumImageView)
                            albumNameTextView.text = album.name
                            // Display only the first 10 characters of the release date
                            albumReleaseDateTextView.text = album.releaseDate.take(10)

                            albumsLayout.addView(albumView)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Album>>, t: Throwable) {
                // Handle the failure (e.g., show a Toast or log the error)
                Toast.makeText(this@ArtistViewActivity, "Failed to load albums", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchArtistDetails(artistId: String) {
        RetrofitClient.backendService.getArtistById(artistId).enqueue(object : Callback<ArtistDetail> {
            override fun onResponse(call: Call<ArtistDetail>, response: Response<ArtistDetail>) {
                if (response.isSuccessful) {
                    response.body()?.let { artistDetail ->
                        this@ArtistViewActivity.artistDetail = artistDetail
                        displayArtistDetails(artistDetail)
                    }
                }
            }

            override fun onFailure(call: Call<ArtistDetail>, t: Throwable) {
                // Handle the failure (e.g., show a Toast or log the error)
                Toast.makeText(this@ArtistViewActivity, "Failed to load artist details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayArtistDetails(artistDetail: ArtistDetail) {
        // Display artist picture
        Glide.with(this)
            .load(artistDetail.picture)
            .into(artistImageView)

        // Display artist name
        artistNameTextView.text = artistDetail.artistPick

        // Display monthly listeners
        monthlyListenersTextView.text = "${artistDetail.followersNumber}"

        // Display journey paragraph with genres
        journeyParagraphTextView.text = "Discover the sonic journey of this artist, blending diverse genres to create a truly unique soundscape. From energetic pop beats to soulful ballads, each track reflects their artistic evolution. Immerse yourself in their musical story and experience the depth of their creativity."

        // Update genresTextView with genres from artistDetail
        genresTextView.text = artistDetail.description

        // Modify follow button if necessary
        followButton.text = "Follow ${artistDetail.artistPick}"
    }

    private fun followArtist(artistId: String) {
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Get the Firebase ID token
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseToken = task.result?.token
                    if (firebaseToken != null) {
                        // Create the request body
                        val requestBody = FollowArtistRequest(artistId)

                        // Call the API
                        RetrofitClient.backendService.followArtist("Bearer $firebaseToken", requestBody).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@ArtistViewActivity, "Artist followed", Toast.LENGTH_SHORT).show()
                                    followButton.isEnabled = false
                                    followButton.text = "Artist Followed"
                                    followButton.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                                } else {
                                    Log.e("ArtistViewActivity", "Failed to followed artist: ${response.code()} ${response.message()}")
                                    Toast.makeText(this@ArtistViewActivity, "followed artist", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Log.e("ArtistViewActivity", "Failed to follow artist", t)
                                Toast.makeText(this@ArtistViewActivity, "Failed to follow artist", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Log.e("ArtistViewActivity", "Failed to retrieve authentication token")
                        Toast.makeText(this@ArtistViewActivity, "Failed to retrieve authentication token", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ArtistViewActivity", "Failed to retrieve authentication token", task.exception)
                    Toast.makeText(this@ArtistViewActivity, "Failed to retrieve authentication token", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.e("ArtistViewActivity", "User not authenticated")
            Toast.makeText(this@ArtistViewActivity, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}
