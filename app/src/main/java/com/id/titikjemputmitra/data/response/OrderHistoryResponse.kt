package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class OrderHistoryResponse(

	@field:SerializedName("data")
	val data: List<DataItemHistory>,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Boolean
)

@Parcelize
data class DataItemHistory(

	@field:SerializedName("end_date")
	val endDate: String,

	@field:SerializedName("transaction_code")
	val transactionCode: String,

	@field:SerializedName("car_thumbnail")
	val carThumbnail: String,

	@field:SerializedName("payment_status")
	val paymentStatus: String,

	@field:SerializedName("rental_status")
	val rentalStatus: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("brand_name")
	val brandName: String,

	@field:SerializedName("total_payment")
	val totalPayment: Int,

	@field:SerializedName("rental_type")
	val rentalType: String,

	@field:SerializedName("counterpart_phone")
	val counterpartPhone: String,

	@field:SerializedName("model_name")
	val modelName: String,

	@field:SerializedName("product_id")
	val productId: String,

	@field:SerializedName("counterpart_name")
	val counterpartName: String,

	@field:SerializedName("start_date")
	val startDate: String,

	@field:SerializedName("duration_value")
	val durationValue: Int
): Parcelable
