package com.id.titikjemputmitra.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.UserPreference
import com.id.titikjemputmitra.data.adapter.OrderAdapter
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentHomeBinding
import com.id.titikjemputmitra.utils.formatRupiah

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel menggunakan delegasi viewModels()
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: OrderAdapter
    private lateinit var userPref: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeData()

        // Load data pertama kali
        fetchData()

        binding.btnWithdraw.setOnClickListener {
            // Ambil status PIN terbaru dari SharedPreferences
            userPref = UserPreference(requireContext())
            val isPinSet = userPref.getUser().isPin

            if (isPinSet == "1") {

                findNavController().navigate(R.id.navigation_withdraw)
            } else {
                Toast.makeText(requireContext(), "Harap buat PIN Keamanan terlebih dahulu", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.navigation_pin)
            }
        }

        val navigateToTopupHistory = View.OnClickListener {
            try {
                findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToNavigationTransactionHistory())
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal pindah halaman: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardWallet.setOnClickListener(navigateToTopupHistory)
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(
            onItemClick = { order ->
                // Nanti kita arahkan ini ke halaman Navigasi Detail Pesanan
                Toast.makeText(requireContext(), "Pilih: ${order.transactionCode}", Toast.LENGTH_SHORT).show()

                // Contoh jika sudah ada NavGraph:
                 val action = HomeFragmentDirections.actionNavigationHomeToNavigationOrderDetail(order)
                 findNavController().navigate(action)
            }
        )

        binding.rvRecentOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
            // Mencegah RecyclerView scroll sendiri sehingga NestedScrollView yang bekerja
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchData()
        }
    }

    private fun fetchData() {
        val apiService = ApiConfig.getApiService(requireContext())
        viewModel.fetchRecentOrders(apiService)
        viewModel.fetchUserBalance(apiService)
    }

    private fun observeData() {
        viewModel.balance.observe(viewLifecycleOwner) { rawBalance ->
            // Gunakan formatRupiah utility agar tampilan cantik (Rp 50.000)
            val formatted = formatRupiah(rawBalance.toDoubleOrNull() ?: 0.0)
            binding.tvSaldo.text = formatted
        }

        // Observer Loading State (Menyamai logika di DriverFragment)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (binding.swipeRefresh.isRefreshing) {
                if (!isLoading) binding.swipeRefresh.isRefreshing = false
            } else {
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observer Data Pesanan
        viewModel.listOrder.observe(viewLifecycleOwner) { list ->
            // 1. Submit data ke Adapter (Otomatis ditangani DiffUtil)
            adapter.submitList(list)

            // 2. Kalkulasi Angka Dashboard secara Lokal (Sangat Efisien)
            val countApproval = list.count { it.rentalStatus == "WAITING_APPROVAL" }
            val countPreparing = list.count { it.rentalStatus == "PREPARING" }
            val countOnGoing = list.count { it.rentalStatus == "ON_GOING" }

            binding.tvWaitingApprovalCount.text = countApproval.toString()
            binding.tvPreparingCount.text = countPreparing.toString()
            binding.tvOnGoingCount.text = countOnGoing.toString()
        }

        // Observer Pesan Error/Notifikasi
        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}