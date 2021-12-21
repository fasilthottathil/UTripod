package com.aitechnologies.utripod.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp


data class Groups(
    var roomId: String = System.currentTimeMillis().toString(),
    var name: String = "",
    var imageUrl: String = "",
    var description: String = "",
    var admins: String = "",
    var members: Int = 0,
    var message: String = "",
    var count: Int = 0,
    var timestamp: Timestamp = Timestamp.now()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readParcelable(Timestamp::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(roomId)
        parcel.writeString(name)
        parcel.writeString(imageUrl)
        parcel.writeString(description)
        parcel.writeString(admins)
        parcel.writeInt(members)
        parcel.writeString(message)
        parcel.writeInt(count)
        parcel.writeParcelable(timestamp, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Groups> {
        override fun createFromParcel(parcel: Parcel): Groups {
            return Groups(parcel)
        }

        override fun newArray(size: Int): Array<Groups?> {
            return arrayOfNulls(size)
        }
    }
}