package com.example.mobileproject

import Song
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

data class AddUserRequest(
    val id : String,
    val email: String,
    val username: String,
    val password: String
)

interface UserApiService : ApiInterface {
    @POST("account/create")
    fun createUser(@Body request: AddUserRequest): Call<Void>

    @GET("search/search-song-name") // Update to match your endpoint
    override fun searchSongByName(@Query("keyword") keyword: String): Call<List<Song>>

    @GET("playlist/user/followed-artists")
    fun getFollowedArtists(@Header("Authorization") authHeader: String): Call<List<Artist>>
    @GET("artists")
    fun getNewArtists(@Header("Authorization") authHeader: String): Call<List<Artist>>

    @GET("search/artist/{id}")
    fun getArtistById(@Path("id") artistId: String): Call<ArtistDetail>

    @POST("account/follow-artist")
    fun followArtist(
        @Header("Authorization") authHeader: String,
        @Body requestBody: FollowArtistRequest
    ): Call<Void>

    @GET("playlist/recent-songs")
    fun getRecentSongs(@Header("Authorization") token: String): Call<List<Song>>

    @GET("playlist/liked-songs")
    fun getLikedSongs(@Header("Authorization") authHeader: String): Call<List<Song>>

    @GET("playlist/get-song/{id}")
    fun getSongById(@Path("id") id: String): Call<PlaySong>

    @GET("search/songs-by-genres/{genre}")
    fun searchSongsByGenre(
        @Path("genre") genre: String
    ): Call<List<Song>>

    @GET("playlist/albums/{artistId}")
    fun getArtistAlbums(@Path("artistId") artistId: String): Call<List<Album>>

    @GET("artist/genres/{artistId}")
    fun getArtistGenres(@Path("artistId") artistId: String): Call<List<String>>

    @POST("playlist/like-song")
    fun likeSong(
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Call<Void>

    @POST("playlist/update-song/{songId}")
    fun updateSongOnFirstPlay(
        @Header("Authorization") token: String,
        @Path("songId") songId: String
    ): Call<Void>

        @POST("playlist/create-playlist")
        fun createPlaylist(
            @Header("Authorization") authHeader: String,
            @Body playlistData: Map<String, String>
        ): Call<Void>
    @GET("playlist/get-playlists")
    fun getPlaylists(@Header("Authorization") token: String): Call<List<Playlist>>


    @GET("top-played-songs")
    fun getTopPlayedSongs(): Call<List<Song>>

    @GET("/playlist/playlist-songs/{playlistId}/songs")
    fun getSongsByPlaylist(
        @Header("Authorization") authToken: String,
        @Path("playlistId") playlistId: String
    ): Call<List<Song>>


    @POST("/playlist/add-song-to-playlist")
    fun addSongToPlaylist(
        @Header("Authorization") authToken: String,
        @Body requestBody: Map<String, String>
    ): Call<Void>


    @GET("/recommend")
    fun getRecommendations(@Header("Authorization") token: String): Call<List<Song>>

}
