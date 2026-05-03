package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class OtpResponse(

    @field:SerializedName("data")
    val data: DataOtp? = null,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Boolean
)

@Parcelize
data class DataOtp(

    @field:SerializedName("last_access")
    val lastAccess: String,

    @field:SerializedName("is_active")
    val isActive: String,

    @field:SerializedName("apikey")
    val apikey: String,

    @field:SerializedName("last_login")
    val lastLogin: String,

    @field:SerializedName("cin")
    val cin: String,

    @field:SerializedName("user_created")
    val userCreated: String,

    @field:SerializedName("user_id")
    val userId: String,

    @field:SerializedName("role_id")
    val roleId: String,

    @field:SerializedName("phone")
    val phone: String,

    @field:SerializedName("fcm_token")
    val fcmToken: String,

    @field:SerializedName("fullname")
    val fullname: String,

    @field:SerializedName("user_deleted")
    val userDeleted: String,

    @field:SerializedName("email")
    val email: String
): Parcelable