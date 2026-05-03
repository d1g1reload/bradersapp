package com.id.titikjemputmitra.data.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.id.titikjemputmitra.data.response.DataItemWalletHistory
import com.id.titikjemputmitra.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val listTransaction = ArrayList<DataItemWalletHistory>()

    fun setData(newList: List<DataItemWalletHistory>?) {
        listTransaction.clear()
        if (newList != null) {
            listTransaction.addAll(newList)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(listTransaction[position])
    }

    override fun getItemCount(): Int = listTransaction.size

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataItemWalletHistory) {
            binding.tvDescription.text = data.description
            binding.tvDate.text = formatDate(data.createdAt)

            val amountFormatted = formatRupiah(data.amount.toDouble())

            // LOGIKA WARNA DAN TANDA UANG (+ / -)
            if (data.isIncome) {
                binding.tvAmount.text = "+ $amountFormatted"
                binding.tvAmount.setTextColor(Color.parseColor("#2E7D32")) // Hijau
                binding.ivTransactionIcon.setColorFilter(Color.parseColor("#2E7D32"))
            } else {
                binding.tvAmount.text = "- $amountFormatted"
                binding.tvAmount.setTextColor(Color.parseColor("#D32F2F")) // Merah
                binding.ivTransactionIcon.setColorFilter(Color.parseColor("#D32F2F"))
            }

            // LOGIKA BADGE STATUS TRANSAKSI
            val status = data.status.uppercase(Locale.getDefault())
            binding.tvStatusBadge.text = status
            when (status) {
                "SUCCESS" -> {
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
                    binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#E8F5E9"))
                }
                "PENDING" -> {
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#E65100")) // Oranye
                    binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#FFF3E0"))
                }
                "FAILED", "REJECTED" -> {
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#D32F2F")) // Merah
                    binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#FFEBEE"))
                }
                else -> {
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#666666"))
                    binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#F5F5F5"))
                }
            }
        }
    }

    // Helper untuk mengubah "2026-04-25 11:14:59" menjadi format yang lebih ramah
    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "-"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))
            val date = inputFormat.parse(dateString)
            if (date != null) outputFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatRupiah(number: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(number).replace(",00", "").replace("Rp", "Rp ")
    }
}