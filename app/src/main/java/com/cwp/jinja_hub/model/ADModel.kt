package com.cwp.jinja_hub.model

import android.os.Parcel
import android.os.Parcelable

data class ADModel(
    var posterId: String = "",
    var adId: String? = null,
    var adType: String = "",
    var description: String = "",
    var city: String = "",
    var state: String = "",
    var country: String = "",
    var amount: String = "",
    var currency: String = "",
    var phone: String = "",
    var productName: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var mediaUrl: List<String>? = emptyList(), // List of image URLs
    var posterName: String = "",
    var posterUsername: String = "",
    var posterProfileImage: String = "",
) : Parcelable {
    constructor(parcel: Parcel) : this(
        posterId = parcel.readString() ?: "",
        adId = parcel.readString(),
        adType = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        city = parcel.readString() ?: "",
        state = parcel.readString() ?: "",
        country = parcel.readString() ?: "",
        amount = parcel.readString() ?: "",
        currency = parcel.readString() ?: "",
        phone = parcel.readString() ?: "",
        productName = parcel.readString() ?: "",
        timestamp = parcel.readLong(),
        mediaUrl = parcel.createStringArrayList(), // Can be null
        posterName = parcel.readString() ?: "",
        posterUsername = parcel.readString() ?: "",
        posterProfileImage = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(posterId)
        parcel.writeString(adId)
        parcel.writeString(adType)
        parcel.writeString(description)
        parcel.writeString(city)
        parcel.writeString(state)
        parcel.writeString(country)
        parcel.writeString(amount)
        parcel.writeString(currency)
        parcel.writeString(phone)
        parcel.writeString(productName)
        parcel.writeLong(timestamp)
        parcel.writeStringList(mediaUrl)
        parcel.writeString(posterName)
        parcel.writeString(posterUsername)
        parcel.writeString(posterProfileImage)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ADModel> {
        override fun createFromParcel(parcel: Parcel): ADModel {
            return ADModel(parcel)
        }

        override fun newArray(size: Int): Array<ADModel?> = arrayOfNulls(size)
    }
}
