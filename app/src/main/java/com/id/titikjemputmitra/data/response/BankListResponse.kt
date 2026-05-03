package com.id.titikjemputmitra.data.response

import com.google.gson.annotations.SerializedName

data class BankListResponse(
    @field:SerializedName("data")
    val data: List<DataBank>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Boolean? = null
)