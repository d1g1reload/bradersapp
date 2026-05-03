package com.id.titikjemputmitra.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.id.titikjemputmitra.data.adapter.ImageSliderAdapter
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentProductDetailBinding
import com.id.titikjemputmitra.utils.formatRupiah

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()
    private val args: ProductDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product
        val apiService = ApiConfig.getApiService(requireContext())

        binding.apply {
            // 1. Informasi Dasar
            tvDetailName.text = "${product.brandName} ${product.modelName}"
            tvDetailPlate.text = product.licensePlate?.uppercase()
            tvDetailDesc.text = product.description ?: "Tidak ada deskripsi."

            // 2. Tampilkan Spesifikasi (Tahun, BBM, Transmisi, Kursi)
            tvDetailYear.text = "Tahun: ${product.year ?: "-"}"
            tvDetailTransmission.text = "Transmisi: ${product.transmission?.replaceFirstChar { it.uppercase() } ?: "-"}"
            tvDetailFuel.text = "BBM: ${product.fuelType ?: "-"}"
            tvDetailSeats.text = "Kursi: ${product.seatCount ?: "-"} Seat"

            // 3. Logika Harga (Harian, Bulanan, Tahunan)
            tvPriceDaily.text = formatRupiah(product.priceDaily?.toDoubleOrNull() ?: 0.0)

            // Harga Bulanan
            if (!product.priceMonthly.isNullOrEmpty() && product.priceMonthly != "0") {
                layoutPriceMonthly.visibility = View.VISIBLE
                tvPriceMonthly.text = formatRupiah(product.priceMonthly.toDouble())
            } else {
                layoutPriceMonthly.visibility = View.GONE
            }

            // Harga Tahunan
            if (!product.priceYearly.isNullOrEmpty() && product.priceYearly != "0") {
                layoutPriceYearly.visibility = View.VISIBLE
                tvPriceYearly.text = formatRupiah(product.priceYearly.toDouble())
            } else {
                layoutPriceYearly.visibility = View.GONE
            }

            // 4. Setup Image Slider (Menggantikan ImageView Tunggal)
            val imagesList = product.images ?: emptyList()
            val imageUrls = imagesList.mapNotNull { it?.imageUrl }

            if (imageUrls.isNotEmpty()) {
                val sliderAdapter = ImageSliderAdapter(imageUrls)
                viewPagerImages.adapter = sliderAdapter

                // Sembunyikan titik indikator jika gambar hanya 1
                if (imageUrls.size > 1) {
                    tabLayoutDots.visibility = View.VISIBLE
                    TabLayoutMediator(tabLayoutDots, viewPagerImages) { _, _ ->
                        // Biarkan kosong, kita tidak butuh teks pada titik/dots
                    }.attach()
                } else {
                    tabLayoutDots.visibility = View.GONE
                }
            } else {
                // Fallback jika tidak ada gambar sama sekali
                viewPagerImages.adapter = ImageSliderAdapter(listOf(""))
                tabLayoutDots.visibility = View.GONE
            }

            // 5. Setup Tombol Aksi
            btnEdit.setOnClickListener {
                val action = ProductDetailFragmentDirections.actionNavigationProductDetailToNavigationAddProduct(product)
                findNavController().navigate(action)
            }

            btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Hapus Armada?")
                    .setMessage("Apakah Anda yakin ingin menghapus ${product.brandName} ${product.modelName}? Tindakan ini tidak dapat dibatalkan.")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Hapus") { _, _ ->
                        // Eksekusi hapus jika klik Hapus
                        product.productId?.let { id ->
                            viewModel.deleteProduct(apiService, id)
                        }
                    }
                    .show()
            }
        }
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.isSaving.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Pantau pesan dari ViewModel
        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                if (msg == "Produk berhasil dihapus") {
                    // Kembali ke Dashboard setelah sukses
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}