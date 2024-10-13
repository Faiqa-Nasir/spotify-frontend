import android.os.Parcel
import android.os.Parcelable

data class Song(
    val songId: String, // Changed to String to match JSON
    val songName: String,
    val artistName: String,
    val songPicture: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // Reading String
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(songId) // Writing String
        parcel.writeString(songName)
        parcel.writeString(artistName)
        parcel.writeString(songPicture)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}
