package com.id.titikjemputmitra.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.id.titikjemputmitra.data.response.MessageItem
import com.id.titikjemputmitra.databinding.ItemChatMeBinding
import com.id.titikjemputmitra.databinding.ItemChatOtherBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ChatRoomAdapter(private val currentUserId: String) :
    ListAdapter<MessageItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    // Konstanta untuk menentukan tipe tampilan
    companion object {
        private const val VIEW_TYPE_ME = 1
        private const val VIEW_TYPE_OTHER = 2

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessageItem>() {
            override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
                // message_id bisa null jika pesan baru saja dikirim secara lokal
                return oldItem.messageId == newItem.messageId || oldItem.createdAt == newItem.createdAt
            }

            override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    // Menentukan layout mana yang akan dipakai berdasarkan sender_id
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item.senderId == currentUserId) VIEW_TYPE_ME else VIEW_TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ME) {
            val binding = ItemChatMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MeViewHolder(binding)
        } else {
            val binding = ItemChatOtherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            OtherViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is MeViewHolder) {
            holder.bind(message)
        } else if (holder is OtherViewHolder) {
            holder.bind(message)
        }
    }

    // --- ViewHolder untuk Pesan Saya (Kanan) ---
    inner class MeViewHolder(private val binding: ItemChatMeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageItem) {
            binding.tvMessage.text = message.messageText
            binding.tvTime.text = formatTime(message.createdAt)
        }
    }

    // --- ViewHolder untuk Pesan Lawan Bicara (Kiri) ---
    inner class OtherViewHolder(private val binding: ItemChatOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageItem) {
            binding.tvMessage.text = message.messageText
            binding.tvTime.text = formatTime(message.createdAt)
        }
    }

    // Helper untuk merapikan tampilan waktu (yyyy-MM-dd HH:mm:ss -> HH:mm)
    private fun formatTime(dateStr: String): String {
        return try {
            val sdfIn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val sdfOut = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdfIn.parse(dateStr)
            if (date != null) sdfOut.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}