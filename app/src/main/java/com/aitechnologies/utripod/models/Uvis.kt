package com.aitechnologies.utripod.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Uvis(
    var id: String? = "",
    var username: String? = "",
    var profileUrl: String? = "",
    var url: String? = "",
    var likes: Int? = 0,
    var comments: Int? = 0,
    var shares: Int? = 0,
    var description: String? = "",
    var hashTags: String? = "",
    var tags: String? = "",
    var likesList: String? = "",
    var isPublic: Boolean? = true,
    @ServerTimestamp
    var timestamp: Timestamp? = Timestamp.now(),
    var profession:String? = "",
    var viewType: Int? = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(username)
        parcel.writeString(profileUrl)
        parcel.writeString(url)
        parcel.writeValue(likes)
        parcel.writeValue(comments)
        parcel.writeValue(shares)
        parcel.writeString(description)
        parcel.writeString(hashTags)
        parcel.writeString(tags)
        parcel.writeString(likesList)
        parcel.writeValue(isPublic)
        parcel.writeParcelable(timestamp, flags)
        parcel.writeString(profession)
        parcel.writeValue(viewType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Uvis> {
        override fun createFromParcel(parcel: Parcel): Uvis {
            return Uvis(parcel)
        }

        override fun newArray(size: Int): Array<Uvis?> {
            return arrayOfNulls(size)
        }
    }
}
