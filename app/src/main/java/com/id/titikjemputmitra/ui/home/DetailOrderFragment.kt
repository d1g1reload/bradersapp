package com.id.titikjemputmitra.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.response.DataItemOrder
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentDetailOrderBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class DetailOrderFragment : Fragment() {
    private var _binding: FragmentDetailOrderBinding? = null
    private val binding get() = _binding!!

    // MENGGUNAKAN activityViewModels() AGAR SHARE INSTANCE DENGAN HOMEFRAGMENT
    private val viewModel: HomeViewModel by activityViewModels()

    // MENGAMBIL ARGUMEN DARI NAVGRAPH
    private val args: DetailOrderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Ambil data transaksi dari argument (yang diklik dari Home)
        val transactionCode = args.order?.transactionCode

        // 2. Panggil API untuk mendapatkan Detail Lengkap
        if (transactionCode != null) {
            val apiService = ApiConfig.getApiService(requireContext())
            viewModel.fetchOrderDetail(apiService, transactionCode)
        } else {
            Toast.makeText(requireContext(), "Kode Transaksi tidak valid", Toast.LENGTH_SHORT).show()
        }

        observeData()
    }

    private fun observeData() {
        // Observe Loading State
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe Message/Error
        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                showInfoDialog("Informasi Pesanan", msg)
            }
        }

        // Observe Data Detail Pesanan
        viewModel.orderDetail.observe(viewLifecycleOwner) { order ->
            order?.let { populateUI(it) }
        }

        viewModel.updateStatusResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    // JIKA SUKSES, TARIK ULANG DATA DETAIL TERBARU DARI SERVER
                    val transactionCode = args.order?.transactionCode
                    if (transactionCode != null) {
                        viewModel.fetchOrderDetail(ApiConfig.getApiService(requireContext()), transactionCode)
                    }
                }
            }
        }
    }

    private fun showInfoDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Mengerti") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false) // Mencegah dialog tertutup jika user klik area luar
            .show()
    }

    private fun populateUI(item: DataItemOrder) {
        with(binding) {
            tvTransactionCode.text = item.transactionCode

            // --- Customer & Location Info ---
            tvCustomerName.text = item.customerName
            tvCustomerPhone.text = item.customerPhone
            tvAddressLabel.text = item.addressLabel
            tvFullAddress.text = item.fullAddress
            tvAddressNotes.text = if (!item.addressNotes.isNullOrEmpty()) "Catatan: ${item.addressNotes}" else "Tidak ada catatan"

            // --- Aksi Cepat (Telepon & Maps) ---
            btnCall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.customerPhone}"))
                startActivity(intent)
            }

            btnOpenMap.setOnClickListener {
                // Rencana A: Buka aplikasi Google Maps dalam mode Navigasi
                val gmmIntentUri = Uri.parse("google.navigation:q=${item.latitude},${item.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                try {
                    startActivity(mapIntent)
                } catch (e: Exception) { // Menangkap ActivityNotFoundException
                    // Rencana B: Jika gagal/tidak ada aplikasi, buka rute via Browser Web
                    Toast.makeText(requireContext(), "Membuka Maps via Browser...", Toast.LENGTH_SHORT).show()
                    val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${item.latitude},${item.longitude}")
                    val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                    startActivity(browserIntent)
                }
            }

            // --- Car Info ---
            tvCarName.text = "${item.brandName} ${item.modelName}"
            tvLicensePlate.text = item.licensePlate

            val rentalLabel = if (item.rentalType == "DAILY") "Harian" else if (item.rentalType == "MONTHLY") "Bulanan" else "Tahunan"
            tvRentalType.text = "• Sewa $rentalLabel (${item.durationValue} $rentalLabel)"

            tvStartDate.text = formatDateTime(item.startDate)
            tvEndDate.text = formatDateTime(item.endDate)

            val imageUrl = item.carImages.firstOrNull { it.isPrimary == "1" }?.imageUrl
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_car) // Ganti dengan placeholder mobil Anda
                .into(ivCar)

            // --- Payment Info ---
            tvBasePrice.text = formatRupiah(item.basePrice.toDoubleOrNull() ?: 0.0)
            tvPlatformFee.text = formatRupiah(item.platformFee.toDoubleOrNull() ?: 0.0)
            tvTotalPayment.text = formatRupiah(item.totalPayment.toDouble())
            tvPaymentStatus.text = "Status: ${item.paymentStatus}"

            // --- Status & Bottom Button ---
            tvStatusBadge.text = item.rentalStatus
            setupStatusAndButton(item)
        }
    }

    private fun setupStatusAndButton(item: DataItemOrder) {
        val context = requireContext()
        val btn = binding.btnUpdateStatus
        val btnCancel = binding.btnCancelOrder // Ambil tombol batal
        val badge = binding.tvStatusBadge
        var nextStatus = ""

        // Reset visibility tombol batal
        btnCancel.visibility = View.GONE

        when (item.rentalStatus) {
            "WAITING_APPROVAL" -> {
                badge.setBackgroundResource(R.drawable.bg_status_warning)
                badge.setTextColor(ContextCompat.getColor(context, R.color.white))
                btn.visibility = View.VISIBLE
                btn.text = "Tinjau & Setujui Pesanan"
                nextStatus = "PREPARING"

                // Bisa dibatalkan (Tolak/Reject)
                btnCancel.visibility = View.VISIBLE
            }
            "PREPARING" -> {
                badge.setBackgroundResource(R.drawable.bg_status_preparing)
                badge.setTextColor(ContextCompat.getColor(context, R.color.black))
                btn.visibility = View.VISIBLE
                btn.text = "Serahkan Kendaraan (ON GOING)"
                nextStatus = "ON_GOING"

                // Mitra bisa membatalkan saat preparing
                btnCancel.visibility = View.VISIBLE
            }
            "ON_GOING" -> {
                badge.setBackgroundResource(R.drawable.bg_status_ongoing)
                badge.setTextColor(ContextCompat.getColor(context, R.color.white))
                btn.visibility = View.VISIBLE
                btn.text = "Selesaikan Pesanan"
                nextStatus = "COMPLETED"
            }
            "COMPLETED", "CANCELLED" -> {
                // Set warna badge...
                btn.visibility = View.GONE
            }
        }

        // AKSI KLIK TOMBOL UPDATE STATUS (NORMAL)
        btn.setOnClickListener {
            if (nextStatus.isNotEmpty()) {
                val apiService = ApiConfig.getApiService(context)
                viewModel.updateOrderStatus(apiService, item.transactionCode, nextStatus)
            }
        }

        // AKSI KLIK TOMBOL BATAL (MEMBUKA DIALOG ALASAN)
        btnCancel.setOnClickListener {
            showCancelDialog(item.transactionCode)
        }
    }

    private fun showCancelDialog(transactionCode: String) {
        val input = EditText(requireContext())
        input.hint = "Contoh: Mobil mendadak rusak / Mesin mogok"
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = layoutParams

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Batalkan Pesanan")
            .setMessage("Silakan masukkan alasan pembatalan. Dana akan dikembalikan ke saldo customer.")
            .setView(input)
            .setNegativeButton("Kembali", null)
            .setPositiveButton("Batalkan Pesanan") { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isNotEmpty()) {
                    val apiService = ApiConfig.getApiService(requireContext())
                    // Kirim status 'CANCELLED' beserta alasannya
                    viewModel.updateOrderStatus(apiService, transactionCode, "CANCELLED", reason)
                } else {
                    Toast.makeText(requireContext(), "Alasan pembatalan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun formatRupiah(number: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(number).replace(",00", "").replace("Rp", "Rp ")
    }

    private fun formatDateTime(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy\nHH:mm WIB", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}