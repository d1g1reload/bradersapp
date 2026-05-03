package com.id.titikjemputmitra.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ChatListResponse(

    @field:SerializedName("data")
    val data: List<DataItemChat>,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Boolean
)

@Parcelize
data class DataItemChat(

    @field:SerializedName("room_id")
    val roomId: String,

    @field:SerializedName("unread_count")
    val unreadCount: String,

    @field:SerializedName("updated_at")
    val updatedAt: String,

    @field:SerializedName("chat_title")
    val chatTitle: String,

    @field:SerializedName("counterpart_id")
    val counterpartId: String,

    @field:SerializedName("last_message")
    val lastMessage: String
): Parcelable