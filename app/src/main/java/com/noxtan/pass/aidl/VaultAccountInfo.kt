package com.noxtan.pass.aidl

import android.os.Parcel
import android.os.Parcelable

data class VaultAccountInfo(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val password: String = "",
    val totp: String = "",
    val url: String = "",
    val note: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(username)
        parcel.writeString(password)
        parcel.writeString(totp)
        parcel.writeString(url)
        parcel.writeString(note)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VaultAccountInfo> {
        override fun createFromParcel(parcel: Parcel): VaultAccountInfo = VaultAccountInfo(parcel)
        override fun newArray(size: Int): Array<VaultAccountInfo?> = arrayOfNulls(size)
    }
}