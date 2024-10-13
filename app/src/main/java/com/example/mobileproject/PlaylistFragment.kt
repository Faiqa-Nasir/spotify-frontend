package com.example.mobileproject
import Song
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaylistFragment : Fragment() {

    private lateinit var playlistContainer: LinearLayout
    private lateinit var addPlaylistButton: ImageView
    private lateinit var likedSongsTextView: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the layout views
        playlistContainer = view.findViewById(R.id.playlist_container)
        addPlaylistButton = view.findViewById(R.id.add_playlist_icon)
        likedSongsTextView = view.findViewById(R.id.liked_songs_title)
        auth = FirebaseAuth.getInstance()

        // Set click listeners
        likedSongsTextView.setOnClickListener {
            fetchLikedSongs()
        }

        addPlaylistButton.setOnClickListener {
            startActivity(Intent(activity, CreatePlaylistActivity::class.java))
        }

        // Fetch playlists
        fetchPlaylists()
    }

    private fun fetchLikedSongs() {
        val user = auth.currentUser
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val authToken = "Bearer ${task.result?.token}"
                    makeApiCall2(authToken)
                } else {
                    // Handle token retrieval error
                    Log.e("PlaylistFragment", "Failed to get token: ${task.exception?.message}")
                }
            }
        } else {
            // Handle user not logged in
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeApiCall2(authToken: String) {
        RetrofitClient.backendService.getLikedSongs(authToken).enqueue(object : Callback<List<Song>> {
            override fun onResponse(call: Call<List<Song>>, response: Response<List<Song>>) {
                if (response.isSuccessful) {
                    response.body()?.let { songList ->
                        val intent = Intent(context, PlayListActivity::class.java)
                        intent.putParcelableArrayListExtra("songs", ArrayList(songList))
                        startActivity(intent)
                    } ?: run {
                        Log.d("PlaylistFragment", "No liked songs found.")
                    }
                } else {
                    // Handle error response
                    Log.e("PlaylistFragment", "Error fetching liked songs: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Song>>, t: Throwable) {
                // Handle network failure
                Log.e("PlaylistFragment", "Network error: ${t.message}", t)
            }
        })
    }

    private fun fetchPlaylists() {
        val user = auth.currentUser
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val authToken = "Bearer ${task.result?.token}"
                    makeApiCall(authToken)
                } else {
                    Toast.makeText(context, "Failed to get token.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "User is not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeApiCall(authToken: String) {
        RetrofitClient.backendService.getPlaylists(authToken).enqueue(object : Callback<List<Playlist>> {
            override fun onResponse(call: Call<List<Playlist>>, response: Response<List<Playlist>>) {
                if (response.isSuccessful) {
                    response.body()?.let { playlistList ->
                        Log.d("PlaylistFragment", "Playlists fetched successfully: $playlistList")
                        displayPlaylists(playlistList)
                    } ?: run {
                        Log.d("PlaylistFragment", "No playlists found.")
                    }
                } else {
                    Log.e("PlaylistFragment", "Error fetching playlists: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Error fetching playlists.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Playlist>>, t: Throwable) {
                Log.e("PlaylistFragment", "Network error: ${t.message}", t)
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun displayPlaylists(playlists: List<Playlist>) {
        playlistContainer.removeAllViews() // Clear previous views

        for (playlist in playlists) {
            val playlistView = LayoutInflater.from(context).inflate(R.layout.item_playlist, playlistContainer, false)
            val playlistIcon = playlistView.findViewById<ImageView>(R.id.playlist_icon)
            val playlistTitle = playlistView.findViewById<TextView>(R.id.playlist_title)
            val playlistSubtitle = playlistView.findViewById<TextView>(R.id.playlist_subtitle)

            playlistTitle.text = playlist.name
            playlistSubtitle.text = "Playlist • songs"

            // Optionally load images into playlistIcon using Glide or another image loading library
            // Glide.with(this).load(playlist.iconUrl).into(playlistIcon)

            playlistView.setOnClickListener {
                fetchSongsFromPlaylist(playlist.id) // Fetch songs when playlist is clicked
            }

            playlistContainer.addView(playlistView)
        }
    }

    private fun fetchSongsFromPlaylist(playlistId: String) {
        val user = auth.currentUser
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val authToken = "Bearer ${task.result?.token}"
                    makeApiCallForPlaylistSongs(authToken, playlistId)
                } else {
                    Log.e("PlaylistFragment", "Failed to get token: ${task.exception?.message}")
                }
            }
        } else {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeApiCallForPlaylistSongs(authToken: String, playlistId: String) {
        RetrofitClient.backendService.getSongsByPlaylist(authToken, playlistId).enqueue(object : Callback<List<Song>> {
            override fun onResponse(call: Call<List<Song>>, response: Response<List<Song>>) {
                if (response.isSuccessful) {
                    response.body()?.let { songList ->
                        val intent = Intent(context, PlayListActivity::class.java)
                        intent.putParcelableArrayListExtra("songs", ArrayList(songList))
                        startActivity(intent)
                    } ?: run {
                        Log.d("PlaylistFragment", "No songs found for the playlist.")
                    }
                } else {
                    Log.e("PlaylistFragment", "Error fetching playlist songs: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Song>>, t: Throwable) {
                Log.e("PlaylistFragment", "Network error: ${t.message}", t)
            }
        })
    }

}
