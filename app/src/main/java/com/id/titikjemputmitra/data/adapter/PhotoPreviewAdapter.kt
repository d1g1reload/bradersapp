package com.id.titikjemputmitra.data.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.id.titikjemputmitra.databinding.ItemPhotoPreviewBinding
import java.io.File

class PhotoPreviewAdapter(
    private val photos: MutableList<File>,
    private val onDeleteClick: (File, Int) -> Unit
) : RecyclerView.Adapter<PhotoPreviewAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(private val binding: ItemPhotoPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File, position: Int) {
            Glide.with(binding.root.context)
                .load(file) // Glide sangat cerdas, bisa langsung menerima tipe 'File'
                .apply(
                    RequestOptions()
                        .centerCrop() // Pastikan gambar memenuhi kotak tanpa distorsi
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Jangan cache foto temporary dari kamera
                        .skipMemoryCache(true)
                )
                .into(binding.ivPreview)

            // 2. LOGIKA INDIKATOR FOTO UTAMA
            // Hanya foto pada indeks ke-0 (paling kiri) yang mendapat badge "UTAMA"
            if (position == 0) {
                binding.viewPrimaryIndicator.visibility = View.VISIBLE
                binding.tvPrimaryLabel.visibility = View.VISIBLE
            } else {
                binding.viewPrimaryIndicator.visibility = View.GONE
                binding.tvPrimaryLabel.visibility = View.GONE
            }

            // 3. AKSI TOMBOL HAPUS
            binding.btnRemovePhoto.setOnClickListener {
                onDeleteClick(file, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position], position)
    }

    override fun getItemCount(): Int = photos.size
}