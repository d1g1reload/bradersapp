package com.id.titikjemputmitra.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.id.titikjemputmitra.data.response.DataBank
import com.id.titikjemputmitra.databinding.ItemBankAccountBinding

class BankAdapter(
    private val onDeleteClick: (DataBank) -> Unit // Callback untuk tombol hapus
) : RecyclerView.Adapter<BankAdapter.BankViewHolder>() {

    private val listBank = ArrayList<DataBank>()

    // Fungsi untuk memasukkan data baru dari API dan me-refresh daftar
    fun setData(newList: List<DataBank>?) {
        listBank.clear()
        if (newList != null) {
            listBank.addAll(newList)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val binding = ItemBankAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        holder.bind(listBank[position])
    }

    override fun getItemCount(): Int = listBank.size

    inner class BankViewHolder(private val binding: ItemBankAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DataBank) {
            // Sesuai dengan id di item_bank_account.xml
            binding.tvBankName.text = data.bankName ?: "-"
            binding.tvAccountNumber.text = data.accountNumber ?: "-"
            binding.tvAccountName.text = data.accountName ?: "-"

            // LOGIKA PENTING: Hanya tampilkan lencana jika ini adalah rekening utama (is_primary == "1")
            if (data.isPrimary == "1") {
                binding.tvPrimaryBadge.visibility = View.VISIBLE
            } else {
                binding.tvPrimaryBadge.visibility = View.GONE
            }

            // Aksi klik tombol tong sampah
            binding.btnDelete.setOnClickListener {
                onDeleteClick(data)
            }
        }
    }
}