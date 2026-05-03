package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class OrderDetailResponse(

	@field:SerializedName("data")
	val data: DataItemOrder,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Boolean
)

@Parcelize
data class DataItemOrder(

	@field:SerializedName("end_date")
	val endDate: String,

	@field:SerializedName("latitude")
	val latitude: String,

	@field:SerializedName("rental_status")
	val rentalStatus: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("is_driver_included")
	val isDriverIncluded: String,

	@field:SerializedName("address_label")
	val addressLabel: String,

	@field:SerializedName("full_address")
	val fullAddress: String,

	@field:SerializedName("rental_type")
	val rentalType: String,

	@field:SerializedName("updated_at")
	val updatedAt: String,

	@field:SerializedName("model_name")
	val modelName: String,

	@field:SerializedName("base_price")
	val basePrice: String,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("car_id")
	val carId: String,

	@field:SerializedName("payment_method")
	val paymentMethod: String,

	@field:SerializedName("start_date")
	val startDate: String,

	@field:SerializedName("driver_fee")
	val driverFee: String,

	@field:SerializedName("longitude")
	val longitude: String,

	@field:SerializedName("transaction_code")
	val transactionCode: String,

	@field:SerializedName("car_images")
	val carImages: List<CarImagesItem>,

	@field:SerializedName("mitra_id")
	val mitraId: String,

	@field:SerializedName("payment_status")
	val paymentStatus: String,

	@field:SerializedName("address_id")
	val addressId: String,

	@field:SerializedName("customer_phone")
	val customerPhone: String,

	@field:SerializedName("agreement_document_url")
	val agreementDocumentUrl: String,

	@field:SerializedName("brand_name")
	val brandName: String,

	@field:SerializedName("customer_notes")
	val customerNotes: String,

	@field:SerializedName("total_payment")
	val totalPayment: Int,

	@field:SerializedName("platform_fee")
	val platformFee: String,

	@field:SerializedName("address_notes")
	val addressNotes: String,

	@field:SerializedName("license_plate")
	val licensePlate: String,

	@field:SerializedName("user_id")
	val userId: String,

	@field:SerializedName("customer_name")
	val customerName: String,

	@field:SerializedName("duration_value")
	val durationValue: Int
): Parcelable

@Parcelize
data class CarImagesItem(

	@field:SerializedName("is_primary")
	val isPrimary: String,

	@field:SerializedName("image_url")
	val imageUrl: String,

	@field:SerializedName("product_id")
	val productId: String,

	@field:SerializedName("s3_key")
	val s3Key: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("image_id")
	val imageId: String,

	@field:SerializedName("deleted_at")
	val deletedAt: String
): Parcelable
