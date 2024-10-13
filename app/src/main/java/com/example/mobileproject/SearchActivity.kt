package com.example.mobileproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val searchEditText = findViewById<EditText>(R.id.searchET)
        val searchButton = findViewById<Button>(R.id.SearchBtn)

        searchButton.setOnClickListener {
            val keyword = searchEditText.text.toString().trim()
            if (keyword.isNotEmpty()) {
            /*    val intent = Intent(this, PlaylistActivity::class.java)
                intent.putExtra("SEARCH_KEYWORD", keyword)
                startActivity(intent)
            */}
        }
    }    }