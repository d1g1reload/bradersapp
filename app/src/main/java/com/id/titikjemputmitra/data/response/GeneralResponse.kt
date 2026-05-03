package com.id.titikjemputmitra.data.response

import com.google.gson.annotations.SerializedName

data class GeneralResponse(

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Boolean
)
