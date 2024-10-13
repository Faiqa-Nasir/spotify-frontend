package com.example.mobileproject


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class NoDataFoundActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_data_found)

        // Find the "Go Back" button and set a click listener
        val goBackButton: Button = findViewById(R.id.BackBtn)
        goBackButton.setOnClickListener {
            // Finish the activity and return to the previous screen
            finish()
        }
    }
}
