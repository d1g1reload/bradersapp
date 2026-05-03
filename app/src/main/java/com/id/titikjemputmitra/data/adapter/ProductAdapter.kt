package com.id.titikjemputmitra.data.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.id.titikjemputmitra.data.response.DataItemProduct
import com.id.titikjemputmitra.databinding.ItemProductBinding
import com.id.titikjemputmitra.utils.formatRupiah // IMPORT FUNGSI UTILITAS ANDA

class ProductAdapter(private val onItemClick: (DataItemProduct) -> Unit) :
    ListAdapter<DataItemProduct, ProductAdapter.ProductViewHolder>(DIFF_CALLBACK) {

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: DataItemProduct) {
            // 1. Set Teks Utama
            binding.tvProductName.text = "${product.brandName ?: "Merk"} ${product.modelName ?: "Tipe"}"

            // 2. No Polisi
            binding.tvPlate.text = product.licensePlate?.uppercase() ?: "PLAT KOSONG"

            // 3. Spesifikasi
            binding.tvSpecs.text = "${product.year ?: "-"} • ${product.transmission ?: "-"}"

            // 4. PERBAIKAN BUG: Gunakan priceDaily sesuai update database kita
            // Jika priceDaily null, default ke 0.0
            val price = product.priceDaily?.toDoubleOrNull() ?: 0.0
            binding.tvPrice.text = formatRupiah(price)

            // 5. Logic Status Warna Badge
            if (product.status?.equals("available", ignoreCase = true) == true) {
                binding.tvStatus.text = "Tersedia"
                binding.tvStatus.setTextColor(Color.parseColor("#2E7D32")) // Hijau tua
                binding.cvStatus.setCardBackgroundColor(Color.parseColor("#E8F5E9")) // Hijau muda
            } else {
                binding.tvStatus.text = "Disewa"
                binding.tvStatus.setTextColor(Color.parseColor("#C62828")) // Merah
                binding.cvStatus.setCardBackgroundColor(Color.parseColor("#FFEBEE")) // Merah muda
            }

            // 6. Cari Foto Utama
            val imageList = product.images ?: emptyList()
            val primaryImage = imageList.find { it?.isPrimary == "1" } ?: imageList.firstOrNull()

            if (primaryImage != null && !primaryImage.imageUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(primaryImage.imageUrl)
                    .centerCrop()
                    .into(binding.ivProductImage)
            } else {
                binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_report_image)
            }

            // 7. Aksi Klik Kartu
            binding.root.setOnClickListener {
                onItemClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataItemProduct>() {
            override fun areItemsTheSame(oldItem: DataItemProduct, newItem: DataItemProduct): Boolean {
                return oldItem.productId == newItem.productId
            }

            override fun areContentsTheSame(oldItem: DataItemProduct, newItem: DataItemProduct): Boolean {
                return oldItem == newItem
            }
        }
    }
}