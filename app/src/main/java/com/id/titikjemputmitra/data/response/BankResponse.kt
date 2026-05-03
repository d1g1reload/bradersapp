package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class BankResponse(

    @field:SerializedName("data")
    val data: DataBank? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Boolean? = null
)

@Parcelize
data class DataBank(

    @field:SerializedName("account_number")
    val accountNumber: String? = null,

    @field:SerializedName("account_id")
    val accountId: String? = null,

    @field:SerializedName("is_primary")
    val isPrimary: String? = null,

    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("account_name")
    val accountName: String? = null,

    @field:SerializedName("bank_name")
    val bankName: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null
): Parcelable