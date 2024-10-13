package com.example.mobileproject

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("playlist/add-recent-song")
    fun getRecentSongs(): Call<List<Song>> // Adjust the return type as per your response structure


    @GET("search/search-song-name")
    fun searchSongByName(@Query("keyword") keyword: String): Call<List<Song>>
}
