package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class UnreadChatResponse(

    @field:SerializedName("data")
    val data: DataUnreadChat? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Boolean? = null
)

@Parcelize
data class DataUnreadChat(

    @field:SerializedName("unread_count")
    val unreadCount: Int? = null
): Parcelable