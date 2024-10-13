package com.example.mobileproject

import Song
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class SearchFragment : Fragment() {


    private lateinit var auth: FirebaseAuth
    private lateinit var userApiService: UserApiService
    private lateinit var imageSlider: ViewPager2
    private val imageList = listOf(
        R.drawable.d1,R.drawable.d2 ,R.drawable.d3 ,R.drawable.d4,R.drawable.d5,R.drawable.d7,R.drawable.d6,R.drawable.d8 // Replace with actual drawables
    )
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            // Change this line inside the runnable


            val currentItem = imageSlider.currentItem
            val nextItem = (currentItem + 1) % imageList.size
            imageSlider.setCurrentItem(nextItem, true)
            handler.postDelayed(this, 2200)
        }
    }
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    userApiService = RetrofitClient.backendService
}

override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {
    return inflater.inflate(R.layout.fragment_search, container, false)
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Setup search functionality
    val searchEt = view.findViewById<EditText>(R.id.searchET)
    val searchBtn = view.findViewById<Button>(R.id.SearchBtn)
    val imageButton1 = view.findViewById<ImageView>(R.id.imageButton1)
    val imageButton2 = view.findViewById<ImageView>(R.id.imageButton2)
    val imageButton3 = view.findViewById<ImageView>(R.id.imageButton3)
    val imageButton4 = view.findViewById<ImageView>(R.id.imageButton4)

    searchBtn.setOnClickListener {
        val searchText = searchEt.text.toString()
        if (searchText.isNotEmpty()) {
            searchSongs(searchText)
        } else {
            Toast.makeText(context, "Oops! Search field is empty", Toast.LENGTH_SHORT).show()
        }
    }

    imageButton1.setOnClickListener { fetchSongsByGenre("hip-hop") }
    imageButton2.setOnClickListener { fetchSongsByGenre("desi") }
    imageButton3.setOnClickListener { fetchSongsByGenre("pop") }
    imageButton4.setOnClickListener { fetchRecommendations() }

    // Setup slider
    imageSlider = view.findViewById(R.id.imageSlider)
    setupImageSlider()
}
    // Add this adapter class inside your SearchFragment
    private class ImageSliderAdapter(private val images: List<Int>) :
        RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder>() {

        inner class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.imageView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.image_slider_item, parent, false)
            return SliderViewHolder(view)
        }

        override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
            holder.imageView.setImageResource(images[position])
        }

        override fun getItemCount(): Int = images.size
    }
    private fun setupImageSlider() {
        imageSlider.adapter = ImageSliderAdapter(imageList)
        imageSlider.setCurrentItem(0, false)
        handler.postDelayed(runnable, 1000) // Start the slider
    }

    private fun fetchRecommendations() {
        val currentUser: FirebaseUser? = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser.getIdToken(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseToken = task.result?.token

                if (firebaseToken == null) {
                    Toast.makeText(context, "Failed to get token", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                // Proceed with the Retrofit call if the token is valid
                fetchSongsWithToken(firebaseToken)
            } else {
                Toast.makeText(context, "Token retrieval failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchSongsWithToken(firebaseToken: String) {
        RetrofitClient.backendService.getRecommendations("Bearer $firebaseToken").enqueue(object : Callback<List<Song>> {
            override fun onResponse(call: Call<List<Song>>, response: Response<List<Song>>) {
                if (response.isSuccessful) {
                    val songList = response.body()
                    if (songList != null) {
                        logSongsDetails(songList)
                        openPlaylistActivity(songList)
                    } else {
                        Toast.makeText(context, "No songs found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch recommendations", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Song>>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logSongsDetails(songList: List<Song>) {
        for (song in songList) {
            Log.d(
                "SongDetails",
                "Artist: ${song.artistName}, Song: ${song.songName}, ID: ${song.songId}, Image URL: ${song.songPicture}"
            )
        }
    }

    private fun openPlaylistActivity(songList: List<Song>) {
        val intent = Intent(context, PlayListActivity::class.java)
        intent.putParcelableArrayListExtra("songs", ArrayList(songList))
        startActivity(intent)
    }

    private fun fetchSongsByGenre(genre: String) {
        userApiService.searchSongsByGenre(genre).enqueue(object : Callback<List<Song>> {
            override fun onResponse(
                call: Call<List<Song>>,
                response: Response<List<Song>>
            ) {
                if (response.isSuccessful) {
                    val songList = response.body()
                    if (songList != null) {
                        // Log each song's details
                        for (song in songList) {
                            Log.d(
                                "SongDetails",
                                "Artist: ${song.artistName}, Song: ${song.songName}, ID: ${song.songId}, Image URL: ${song.songPicture}"
                            )
                        }
                        // Pass the list of songs to PlayListActivity
                        val intent = Intent(context, PlayListActivity::class.java)
                        intent.putParcelableArrayListExtra("songs", ArrayList(songList))
                        startActivity(intent)
                    }
                } else {
                    Log.e("YourTag", "Error occurred: ${response.message()}")
                    Toast.makeText(context, "Failed to retrieve songs", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Song>>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchSongs(keyword: String) {
        userApiService.searchSongByName(keyword).enqueue(object : Callback<List<Song>> {
            override fun onResponse(call: Call<List<Song>>, response: Response<List<Song>>) {
                if (response.isSuccessful) {
                    val songList = response.body()
                    if (songList != null && songList.isNotEmpty()) {
                        // Log each song's details
                        for (song in songList) {
                            Log.d(
                                "SongDetails",
                                "Artist: ${song.artistName}, Song: ${song.songName}, ID: ${song.songId}, Image URL: ${song.songPicture}"
                            )
                        }

                        // Pass the list of songs to PlayListActivity
                        val intent = Intent(context, PlayListActivity::class.java)
                        intent.putParcelableArrayListExtra("songs", ArrayList(songList))
                        startActivity(intent)
                    } else {
                        Log.e("SearchSongsByKeyword", "No songs found for keyword: $keyword")
                        Toast.makeText(context, "No songs found for keyword: $keyword", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("SearchSongsByKeyword", "Error occurred: ${response.message()}")
                    Toast.makeText(context, "Failed to retrieve songs", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Song>>, t: Throwable) {
                Log.e("SearchSongsByKeyword", "API call failed: ${t.message}")
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
