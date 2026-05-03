package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class WalletHistoryResponse(

    @field:SerializedName("data")
    val data: List<DataItemWalletHistory>,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Boolean
)

@Parcelize
data class DataItemWalletHistory(

    @field:SerializedName("transaction_code")
    val transactionCode: String,

    @field:SerializedName("amount")
    val amount: Int,

    @field:SerializedName("payment_type")
    val paymentType: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("created_at")
    val createdAt: String,

    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("type")
    val type: String,

    @field:SerializedName("is_income")
    val isIncome: Boolean,

    @field:SerializedName("status")
    val status: String,
    @field:SerializedName("payment_url") val paymentUrl: String? = null,
    @field:SerializedName("expired_at") val expiredAt: String? = null
): Parcelable