package com.aitechnologies.utripod.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Chats(
    var roomId: String = System.currentTimeMillis().toString(),
    var username: String = "",
    var profileUrl: String = "",
    var message: String = "",
    var count: Int = 0,
    var timestamp: Timestamp = Timestamp.now()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readParcelable(Timestamp::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(roomId)
        parcel.writeString(username)
        parcel.writeString(profileUrl)
        parcel.writeString(message)
        parcel.writeInt(count)
        parcel.writeParcelable(timestamp, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Chats> {
        override fun createFromParcel(parcel: Parcel): Chats {
            return Chats(parcel)
        }

        override fun newArray(size: Int): Array<Chats?> {
            return arrayOfNulls(size)
        }
    }
}
