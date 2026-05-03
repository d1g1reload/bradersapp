package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ProductResponse(

	@field:SerializedName("data")
	val data: List<DataItemProduct>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

@Parcelize
data class DataItemProduct(

	@field:SerializedName("images")
	val images: List<ImagesItem?>? = null,

	@field:SerializedName("price_yearly")
	val priceYearly: String? = null,

	@field:SerializedName("year")
	val year: String? = null,

	@field:SerializedName("is_negotiable")
	val isNegotiable: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("price_daily")
	val priceDaily: String? = null,

	@field:SerializedName("is_driver_included")
	val isDriverIncluded: String? = null,

	@field:SerializedName("brand_name")
	val brandName: String? = null,

	@field:SerializedName("model_id")
	val modelId: String? = null,

	@field:SerializedName("deleted_at")
	val deletedAt: String? = null,

	@field:SerializedName("price_monthly")
	val priceMonthly: String? = null,

	@field:SerializedName("license_plate")
	val licensePlate: String? = null,

	@field:SerializedName("transmission")
	val transmission: String? = null,

	@field:SerializedName("seat_count")
	val seatCount: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("model_name")
	val modelName: String? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("product_id")
	val productId: String? = null,

	@field:SerializedName("fuel_type")
	val fuelType: String? = null,

	@field:SerializedName("status")
	val status: String? = null
): Parcelable

@Parcelize
data class ImagesItem(

	@field:SerializedName("is_primary")
	val isPrimary: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("product_id")
	val productId: String? = null,

	@field:SerializedName("s3_key")
	val s3Key: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("image_id")
	val imageId: String? = null,

	@field:SerializedName("deleted_at")
	val deletedAt: String? = null
):Parcelable
