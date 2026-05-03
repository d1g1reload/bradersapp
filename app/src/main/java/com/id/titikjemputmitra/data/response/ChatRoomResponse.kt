package com.id.titikjemputmitra.data.response

import com.google.gson.annotations.SerializedName

data class ChatInitResponse(
    @field:SerializedName("status")
    val status: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("data")
    val data: ChatRoomData?
)

data class ChatRoomData(
    @field:SerializedName("room_id")
    val roomId: String,

    @field:SerializedName("messages")
    val messages: List<MessageItem>
)

// 2. Response saat berhasil mengirim pesan (Send)
data class ChatSendResponse(
    @field:SerializedName("status")
    val status: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("data")
    val data: MessageItem?
)

// 3. Inti dari Pesan (Digunakan oleh Init, Send, dan Pusher)
data class MessageItem(
    @field:SerializedName("message_id")
    val messageId: String? = null,

    @field:SerializedName("room_id")
    val roomId: String? = null,

    @field:SerializedName("sender_type")
    val senderType: String,

    @field:SerializedName("sender_id")
    val senderId: String,

    @field:SerializedName("message_text")
    val messageText: String,

    @field:SerializedName("product_name")
    val productName: String? = null,

    @field:SerializedName("product_image")
    val productImage: String? = null,

    @field:SerializedName("is_read")
    val isRead: String? = "0", // Bisa berupa Int atau String dari backend

    @field:SerializedName("created_at")
    val createdAt: String
)