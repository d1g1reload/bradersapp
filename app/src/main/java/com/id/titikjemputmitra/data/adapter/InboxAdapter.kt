package com.id.titikjemputmitra.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.id.titikjemputmitra.data.response.DataItemChat
import com.id.titikjemputmitra.databinding.ItemInboxBinding
import java.text.SimpleDateFormat
import java.util.Locale

class InboxAdapter(private val onItemClick: (DataItemChat) -> Unit) :
    ListAdapter<DataItemChat, InboxAdapter.InboxViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val binding = ItemInboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InboxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InboxViewHolder(private val binding: ItemInboxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: DataItemChat) {
            binding.tvChatTitle.text = chat.chatTitle
            binding.tvLastMessage.text = chat.lastMessage ?: "Pesan gambar atau belum ada pesan"

            // Tampilkan badge unread jika > 0
            val unread = chat.unreadCount.toIntOrNull() ?: 0
            if (unread > 0) {
                binding.tvUnreadCount.visibility = View.VISIBLE
                binding.tvUnreadCount.text = unread.toString()
                binding.tvChatTitle.setTypeface(null, android.graphics.Typeface.BOLD)
                binding.tvLastMessage.setTypeface(null, android.graphics.Typeface.BOLD)
                binding.tvLastMessage.setTextColor(android.graphics.Color.parseColor("#1A1A2E"))
            } else {
                binding.tvUnreadCount.visibility = View.GONE
                binding.tvChatTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
                binding.tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL)
                binding.tvLastMessage.setTextColor(android.graphics.Color.parseColor("#64748B"))
            }

            // Sederhanakan format jam (Opsional)
            try {
                val sdfIn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val sdfOut = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = sdfIn.parse(chat.updatedAt)
                binding.tvTime.text = if (date != null) sdfOut.format(date) else chat.updatedAt
            } catch (e: Exception) {
                binding.tvTime.text = chat.updatedAt
            }

            binding.root.setOnClickListener {
                onItemClick(chat)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataItemChat>() {
            override fun areItemsTheSame(oldItem: DataItemChat, newItem: DataItemChat): Boolean {
                return oldItem.roomId == newItem.roomId
            }

            override fun areContentsTheSame(oldItem: DataItemChat, newItem: DataItemChat): Boolean {
                return oldItem == newItem
            }
        }
    }
}