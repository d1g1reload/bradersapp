package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class WalletResponse(

    @field:SerializedName("data")
    val data: DataWallet? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Boolean? = null
)

@Parcelize
data class DataWallet(

    @field:SerializedName("wallet_id")
    val walletId: String? = null,

    @field:SerializedName("last_updated")
    val lastUpdated: String? = null,

    @field:SerializedName("balance")
    val balance: String? = null,

    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("status")
    val status: String? = null
): Parcelable