package com.example.mobileproject

import Song
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Intent
import android.widget.HorizontalScrollView

class HomeFragment : Fragment() {

    private lateinit var followedArtistsLayout: LinearLayout
    private lateinit var newArtistsLayout: LinearLayout
    private lateinit var trendingSongsLayout: LinearLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var horizontalScrollViewTrending: HorizontalScrollView
    private lateinit var madeForYouLayout: LinearLayout
    private lateinit var horizontalScrollViewMadeForYou: HorizontalScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize the layouts
        followedArtistsLayout = view.findViewById(R.id.followed_artists_layout)
        newArtistsLayout = view.findViewById(R.id.new_artists_layout)
        trendingSongsLayout = view.findViewById(R.id.trending_songs_layout)
        madeForYouLayout = view.findViewById(R.id.made_for_you_layout)
        horizontalScrollViewMadeForYou = view.findViewById(R.id.horizontal_scroll_view_made_for_you)

        // Fetch data
        fetchFollowedArtists()
        fetchNewArtists()
        fetchRecentSongs()
        fetchTrendingSongs()
        fetchRecommendations()  // Add this line

        return view
    }

    private fun fetchRecommendations() {
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseToken = task.result?.token
                    if (firebaseToken != null) {
                        RetrofitClient.backendService.getRecommendations("Bearer $firebaseToken").enqueue(object : Callback<List<Song>> {
                            override fun onResponse(call: Call<List<Song>>, response: Response<List<Song>>) {
                                if (response.isSuccessful) {
                                    response.body()?.let { songs ->
                                        displayRecommendations(songs)
                                    }
                                } else {
                                    // Handle unsuccessful response
                                }
                            }

                            override fun onFailure(call: Call<List<Song>>, t: Throwable) {
                                // Handle failure
                            }
                        })
                    }
                }
            }
        }
    }

    private fun displayRecommendations(songs: List<Song>) {
        for (song in songs) {
            val songView = LayoutInflater.from(context).inflate(R.layout.song_item, madeForYouLayout, false)

            val imageView: ImageView = songView.findViewById(R.id.artist_image)
            val nameTextView: TextView = songView.findViewById(R.id.artist_name)

            Glide.with(this)
                .load(song.songPicture)
                .into(imageView)

            nameTextView.text = song.songName

            songView.setOnClickListener {
                val intent = Intent(context, SongPageActivity::class.java)
                intent.putExtra("songId", song.songId)
                context?.startActivity(intent)
            }

            madeForYouLayout.addView(songView)
        }
    }
    private fun fetchTrendingSongs() {
        // Fetch trending songs from the API
        RetrofitClient.backendService.getTopPlayedSongs().enqueue(object : Callback<List<Song>> {
            override fun onResponse(call: Call<List<Song>>, response: Response<List<Song>>) {
                if (response.isSuccessful) {
                    response.body()?.let { songs ->
                        displayTrendingSongs(songs)
                    }
                } else {
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<List<Song>>, t: Throwable) {
                // Handle failure
            }
        })
    }




    private fun fetchFollowedArtists() {
        // Get the current user
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            // Get the Firebase ID token
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseToken = task.result?.token
                    if (firebaseToken != null) {
                        // Proceed with the API call
                        RetrofitClient.backendService.getFollowedArtists("Bearer $firebaseToken").enqueue(object : Callback<List<Artist>> {
                            override fun onResponse(call: Call<List<Artist>>, response: Response<List<Artist>>) {
                                if (response.isSuccessful) {
                                    response.body()?.let { artists ->
                                        displayFollowedArtists(artists)
                                    }
                                }
                            }

                            override fun onFailure(call: Call<List<Artist>>, t: Throwable) {
                                // Handle failure
                            }
                        })
                    } else {
                        // Handle the case where the token is null
                    }
                } else {
                    // Handle error getting the token
                }
            }
        } else {
            // Handle the case where the user is not logged in
        }
    }
    private fun fetchNewArtists() {
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            // Get the Firebase ID token
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseToken = task.result?.token
                    if (firebaseToken != null) {
                        // Proceed with the API call
                        RetrofitClient.backendService.getNewArtists("Bearer $firebaseToken").enqueue(object : Callback<List<Artist>> {
                            override fun onResponse(call: Call<List<Artist>>, response: Response<List<Artist>>) {
                                if (response.isSuccessful) {
                                    response.body()?.let { artists ->
                                        displayNewArtists(artists)
                                    }
                                } else {
                                    // Handle the case where the response is not successful
                                    val intent = Intent(requireContext(), NoDataFoundActivity::class.java)
                                    startActivity(intent)
                                }
                            }

                            override fun onFailure(call: Call<List<Artist>>, t: Throwable) {
                                // Handle failure by starting NoDataFoundActivity
                                val intent = Intent(requireContext(), NoDataFoundActivity::class.java)
                                startActivity(intent)
                            }
                        })
                    } else {
                        // Handle the case where the token is null
                    }
                } else {
                    // Handle error getting the token
                }
            }
        } else {
            // Handle the case where the user is not logged in
        }
    }

    // HomeFragment.kt

    private fun displayFollowedArtists(artists: List<Artist>) {
        for (artist in artists) {
            val artistView = LayoutInflater.from(context).inflate(R.layout.artist_item, followedArtistsLayout, false)

            val imageView: ImageView = artistView.findViewById(R.id.artist_image)
            val nameTextView: TextView = artistView.findViewById(R.id.artist_name)

            // Load artist image using Glide
            Glide.with(this)
                .load(artist.picture)
                .circleCrop()
                .into(imageView)

            nameTextView.text = artist.artistPick

            // Set click listener to show artist details
            imageView.setOnClickListener {
                val intent = Intent(requireContext(), ArtistViewActivity::class.java)
                intent.putExtra("ARTIST_ID", artist.id)
                intent.putExtra("SHOW_FOLLOW_BUTTON", false) // Hide follow button
                startActivity(intent)
            }

            followedArtistsLayout.addView(artistView)
        }
    }

    private fun displayNewArtists(artists: List<Artist>) {
        for (artist in artists) {
            val artistView = LayoutInflater.from(context).inflate(R.layout.artist_item, newArtistsLayout, false)

            val imageView: ImageView = artistView.findViewById(R.id.artist_image)
            val nameTextView: TextView = artistView.findViewById(R.id.artist_name)

            // Load artist image using Glide
            Glide.with(this)
                .load(artist.picture)
                .circleCrop()
                .into(imageView)

            nameTextView.text = artist.artistPick

            // Set click listener to show artist details
            imageView.setOnClickListener {
                val intent = Intent(requireContext(), ArtistViewActivity::class.java)
                intent.putExtra("ARTIST_ID", artist.id)
                intent.putExtra("SHOW_FOLLOW_BUTTON", true) // Show follow button
                startActivity(intent)
            }

            newArtistsLayout.addView(artistView)
        }
    }
    private fun fetchRecentSongs() {
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            // Get the Firebase ID token
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseToken = task.result?.token
                    if (firebaseToken != null) {
                        // Proceed with the API call
                        RetrofitClient.backendService.getRecentSongs("Bearer $firebaseToken").enqueue(object : Callback<List<Song>> {
                            override fun onResponse(call: Call<List<Song>>, response: Response<List<Song>>) {
                                if (response.isSuccessful) {
                                    response.body()?.let { songs ->
                                        displayRecentSongs(songs)
                                    }
                                } else {
                                    // Handle unsuccessful response
                                }
                            }

                            override fun onFailure(call: Call<List<Song>>, t: Throwable) {
                                // Handle failure
                            }
                        })
                    } else {
                        // Handle the case where the token is null
                    }
                } else {
                    // Handle error getting the token
                }
            }
        } else {
            // Handle the case where the user is not logged in
        }
    }

    private fun displayRecentSongs(songs: List<Song>) {
        val recentSongsLayout: LinearLayout = view?.findViewById(R.id.recent_songs_layout) ?: return

        for (song in songs) {
            val songView = LayoutInflater.from(context).inflate(R.layout.song_item, recentSongsLayout, false)

            val imageView: ImageView = songView.findViewById(R.id.artist_image)
            val nameTextView: TextView = songView.findViewById(R.id.artist_name)

            // Load song image using Glide
            Glide.with(this)
                .load(song.songPicture)
                .into(imageView)

            nameTextView.text = song.songName

            // Set click listener to open SongPageActivity with the selected song ID
            songView.setOnClickListener {
                val intent = Intent(context, SongPageActivity::class.java)
                intent.putExtra("songId", song.songId)  // Pass the song ID to SongPageActivity
                context?.startActivity(intent)
            }

            recentSongsLayout.addView(songView)
        }
    }
    private fun displayTrendingSongs(songs: List<Song>) {
        for (song in songs) {
            val songView = LayoutInflater.from(context).inflate(R.layout.song_item, trendingSongsLayout, false)

            val imageView: ImageView = songView.findViewById(R.id.artist_image)
            val nameTextView: TextView = songView.findViewById(R.id.artist_name)

            // Load song image using Glide
            Glide.with(this)
                .load(song.songPicture)
                .into(imageView)

            nameTextView.text = song.songName

            // Set click listener to open SongPageActivity with the selected song ID
            songView.setOnClickListener {
                val intent = Intent(context, SongPageActivity::class.java)
                intent.putExtra("songId", song.songId)  // Pass the song ID to SongPageActivity
                context?.startActivity(intent)
            }

            trendingSongsLayout.addView(songView)
        }
    }

}
