package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class IdentityResponse(
	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("data")
	val data: DataIdentity? = null // CUKUP GUNAKAN INI
)

@Parcelize
data class DataIdentity(
	@field:SerializedName("nik") val nik: String? = null,
	@field:SerializedName("full_name") val fullName: String? = null,
	@field:SerializedName("birth_place_date") val birthPlaceDate: String? = null,
	@field:SerializedName("gender") val gender: String? = null,
	@field:SerializedName("blood_type") val bloodType: String? = null,
	@field:SerializedName("address_ktp") val addressKtp: String? = null,
	@field:SerializedName("rt_rw") val rtRw: String? = null,
	@field:SerializedName("village") val village: String? = null,
	@field:SerializedName("subdistrict") val subdistrict: String? = null,
	@field:SerializedName("city") val city: String? = null,
	@field:SerializedName("province") val province: String? = null,
	@field:SerializedName("religion") val religion: String? = null,
	@field:SerializedName("marital_status") val maritalStatus: String? = null,
	@field:SerializedName("occupation") val occupation: String? = null,
	@field:SerializedName("nationality") val nationality: String? = null,
	@field:SerializedName("valid_until") val validUntil: String? = null,
	@field:SerializedName("ktp_photo_url") val ktpPhotoUrl: String? = null,
	@field:SerializedName("selfie_photo_url") val selfiePhotoUrl: String? = null,
	@field:SerializedName("is_verified") val isVerified: Int? = null
) : Parcelable