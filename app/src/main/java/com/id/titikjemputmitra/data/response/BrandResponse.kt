package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class BrandResponse(

	@field:SerializedName("data")
	val data: List<DataItemBrand>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

@Parcelize
data class DataItemBrand(

	@field:SerializedName("logo_url")
	val logoUrl: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("brand_name")
	val brandName: String? = null,

	@field:SerializedName("brand_id")
	val brandId: String
): Parcelable
