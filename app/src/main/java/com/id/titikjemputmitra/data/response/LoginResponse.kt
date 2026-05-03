package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class LoginResponse(

	@field:SerializedName("data")
	val data: DataLogin? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

@Parcelize
data class DataLogin(


	@field:SerializedName("is_agreement_approved")
	val isAgreementApproved: String? = null,

	@field:SerializedName("is_verified")
	val isVerified: Int? = null,

	@field:SerializedName("is_unregistered")
	val isUnregistered: Boolean? = null,

	@field:SerializedName("last_access")
	val lastAccess: String? = null,

	@field:SerializedName("is_active")
	val isActive: String? = null,

	@field:SerializedName("apikey")
	val apikey: String? = null,

	@field:SerializedName("identity_status")
	val identityStatus: String? = null,

	@field:SerializedName("last_login")
	val lastLogin: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("address_id")
	val addressId: String? = null,

	@field:SerializedName("cin")
	val cin: String? = null,

	@field:SerializedName("wallet_balance")
	val walletBalance: String? = null,

	@field:SerializedName("user_created")
	val userCreated: String? = null,

	@field:SerializedName("full_address")
	val fullAddress: String? = null,

	@field:SerializedName("is_parent")
	val isParent: String? = null,

	@field:SerializedName("is_pin")
	val isPin: String? = null,

	@field:SerializedName("pin")
	val pin: String? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("role_id")
	val roleId: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("fcm_token")
	val fcmToken: String? = null,

	@field:SerializedName("fullname")
	val fullname: String? = null,

	@field:SerializedName("user_deleted")
	val userDeleted: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null
): Parcelable
