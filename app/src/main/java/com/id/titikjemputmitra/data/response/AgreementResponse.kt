package com.id.titikjemputmitra.data.response

import com.google.gson.annotations.SerializedName

data class AgreementResponse(

    @field:SerializedName("data")
    val data: DataItemAgreement? = null,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Boolean
)

data class DataItemAgreement(

    @field:SerializedName("agreement_id")
    val agreementId: String,

    @field:SerializedName("title")
    val title: String,

    @field:SerializedName("content")
    val content: String,

    @field:SerializedName("version")
    val version: String
)