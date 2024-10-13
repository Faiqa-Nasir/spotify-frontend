package com.example.mobileproject

import Song
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class PlayListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val songList = intent.getParcelableArrayListExtra<Song>("songs") ?: emptyList()
        songAdapter = SongAdapter(songList) { songId ->
            // Start SongPageActivity with the clicked song ID
            val intent = Intent(this, SongPageActivity::class.java).apply {
                putExtra("songId", songId)
            }
            startActivity(intent)
        }
        recyclerView.adapter = songAdapter
    }
}
