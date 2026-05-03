package com.id.titikjemputmitra.ui.driver.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.response.DataItemDriver
import com.id.titikjemputmitra.databinding.ItemDriverBinding

class DriverAdapter(
    private val onItemClick: (DataItemDriver) -> Unit,
    private val onItemLongClick: (DataItemDriver) -> Unit
) :
    ListAdapter<DataItemDriver, DriverAdapter.DriverViewHolder>(DIFF_CALLBACK) {

    private var fullList: List<DataItemDriver> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {
        val binding = ItemDriverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DriverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setInitialList(list: List<DataItemDriver>) {
        fullList = list
        submitList(list)
    }

    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) fullList else {
            fullList.filter { it.fullname.contains(query, ignoreCase = true) }
        }
        submitList(filteredList)
    }

    inner class DriverViewHolder(private val binding: ItemDriverBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(driver: DataItemDriver) {
            binding.apply {
                tvDriverName.text = driver.fullname
                tvDriverPhone.text = driver.phone // Sesuai ID XML baru

                // Logika Status
                if (driver.isActive == "1") {
                    tvDriverStatus.text = "Aktif"
                    tvDriverStatus.setTextColor(ContextCompat.getColor(root.context, R.color.colorPrimary))
                } else {
                    tvDriverStatus.text = "Non-Aktif"
                    tvDriverStatus.setTextColor(ContextCompat.getColor(root.context, R.color.gray_text))
                }

                // Load Foto Avatar
                Glide.with(root.context)
                    .load(driver.ktpPhotoUrl)
                    .placeholder(R.drawable.ic_user) // Sesuai src XML Anda
                    .circleCrop()
                    .into(ivDriverAvatar)

                root.setOnClickListener { onItemClick(driver) }
                root.setOnLongClickListener {
                    onItemLongClick(driver)
                    true // true artinya event telah dikonsumsi
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataItemDriver>() {
            override fun areItemsTheSame(oldItem: DataItemDriver, newItem: DataItemDriver) = oldItem.userId == newItem.userId
            override fun areContentsTheSame(oldItem: DataItemDriver, newItem: DataItemDriver) = oldItem == newItem
        }
    }
}