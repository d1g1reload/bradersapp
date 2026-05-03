package com.id.titikjemputmitra.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.response.DataItemHistory
import com.id.titikjemputmitra.databinding.ItemOrderMitraBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(private val onItemClick: (DataItemHistory) -> Unit) :
    ListAdapter<DataItemHistory, OrderAdapter.OrderViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderMitraBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, onItemClick)
    }

    class OrderViewHolder(private val binding: ItemOrderMitraBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DataItemHistory, onItemClick: (DataItemHistory) -> Unit) {
            with(binding) {
                tvTrxCode.text = item.transactionCode

                // Gunakan counterpartName karena ini adalah nama Customer dari sisi Mitra
                tvCustomerName.text = item.counterpartName

                // Di DataItemHistory tidak ada licensePlate, jadi kita tampilkan Brand & Model saja
                tvCarModel.text = "${item.brandName} ${item.modelName}"

                tvPrice.text = formatRupiah(item.totalPayment)

                // Di DataItemHistory tidak ada fullAddress.
                // Opsional: Anda bisa menyembunyikan icon lokasi di list, atau menaruh teks statis.
                // Untuk sementara kita isi dengan "Lihat detail untuk alamat"
                tvAddress.text = "Ketuk untuk melihat alamat"

                // Format Tanggal: 2026-05-11 08:00:00 -> 11 Mei
                tvRentalDate.text = "${formatDate(item.startDate)} - ${formatDate(item.endDate)}"

                // Load Thumbnail Mobil (Langsung dari carThumbnail)
                Glide.with(itemView.context)
                    .load(item.carThumbnail)
                    .placeholder(R.drawable.ic_car) // Pastikan file ic_car ada di res/drawable
                    .into(ivCarThumbnail)

                // Handling Status & Warna Badge
                tvStatusBadge.text = item.rentalStatus
                setStatusStyle(item.rentalStatus)

                // Klik Item untuk pindah ke Detail/Navigasi
                root.setOnClickListener { onItemClick(item) }
            }
        }

        private fun setStatusStyle(status: String) {
            val context = itemView.context
            when (status) {
                "PREPARING" -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_preparing)
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                "ON_GOING" -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_ongoing)
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
                }
                "WAITING_APPROVAL" -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_preparing)
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                "COMPLETED" -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_completed) // Tambahkan jika ada
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
                }
                "CANCELLED" -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_cancelled) // Tambahkan jika ada
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
                }
            }
        }

        private fun formatRupiah(number: Int): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return format.format(number).replace(",00", "").replace("Rp", "Rp ")
        }

        private fun formatDate(dateStr: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateStr
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataItemHistory>() {
            override fun areItemsTheSame(oldItem: DataItemHistory, newItem: DataItemHistory): Boolean {
                return oldItem.transactionCode == newItem.transactionCode
            }

            override fun areContentsTheSame(oldItem: DataItemHistory, newItem: DataItemHistory): Boolean {
                return oldItem == newItem
            }
        }
    }
}