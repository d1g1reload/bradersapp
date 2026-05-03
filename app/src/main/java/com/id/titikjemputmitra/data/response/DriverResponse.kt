package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class DriverResponse(

	@field:SerializedName("data")
	val data: List<DataItemDriver>,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Boolean
)

@Parcelize
data class DataItemDriver(

	@field:SerializedName("ktp_photo_url")
	val ktpPhotoUrl: String,

	@field:SerializedName("is_active")
	val isActive: String,

	@field:SerializedName("user_id")
	val userId: String,

	@field:SerializedName("phone")
	val phone: String,

	@field:SerializedName("fullname")
	val fullname: String,

	@field:SerializedName("is_verified")
	val isVerified: String,

	@field:SerializedName("email")
	val email: String
):Parcelable
