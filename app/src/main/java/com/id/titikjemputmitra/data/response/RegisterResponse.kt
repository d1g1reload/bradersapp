package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class RegisterResponse(

    @field:SerializedName("data")
    val data: DataRegister,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Boolean
)

@Parcelize
data class DataRegister(

    @field:SerializedName("note")
    val note: String,

    @field:SerializedName("email")
    val email: String,

    @field:SerializedName("token")
    val token: String
): Parcelable