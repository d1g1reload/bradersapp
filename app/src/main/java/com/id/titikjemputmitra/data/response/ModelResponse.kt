package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ModelResponse(

	@field:SerializedName("data")
	val data: List<DataItemModel>,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Boolean
)

@Parcelize
data class DataItemModel(

	@field:SerializedName("model_name")
	val modelName: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("model_id")
	val modelId: String,

	@field:SerializedName("brand_id")
	val brandId: String
): Parcelable
