package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class AiDescriptionResponse(

	@field:SerializedName("data")
	val data: DataAi,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Boolean
)

@Parcelize
data class DataAi(

	@field:SerializedName("description")
	val description: String
): Parcelable
