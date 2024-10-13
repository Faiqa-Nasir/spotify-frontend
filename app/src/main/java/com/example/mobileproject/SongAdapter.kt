package com.example.mobileproject

import Song
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
class SongAdapter(
    private val songs: List<Song>,
    private val onItemClick: (String) -> Unit // Click listener function
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
        holder.itemView.setOnClickListener {
            onItemClick(song.songId) // Use the click listener to pass song ID
        }
    }

    override fun getItemCount(): Int = songs.size

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songCoverImageView: ImageView = itemView.findViewById(R.id.songCoverImageView)
        private val songNameTextView: TextView = itemView.findViewById(R.id.songNameTextView)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.artistNameTextView)

        fun bind(song: Song) {
            songNameTextView.text = song.songName
            artistNameTextView.text = song.artistName

            Glide.with(itemView.context)
                .load(song.songPicture)
                .placeholder(R.drawable.ic_liked_songs)
                .into(songCoverImageView)
        }
    }
}
