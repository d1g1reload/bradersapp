package com.id.titikjemputmitra.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.id.titikjemputmitra.data.adapter.TransactionAdapter
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentTransactionHistoryBinding

class TransactionHistoryFragment : Fragment() {
    private var _binding: FragmentTransactionHistoryBinding? = null
    private val binding get() = _binding!!

    // Sesuai aturan: Tetap gunakan viewModels() secara mandiri
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Tombol Back di Toolbar
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        observeViewModel()

        // 1. Eksekusi Pemanggilan API Pertama Kali
        fetchData()

        // 2. Setup Aksi Swipe to Refresh
        binding.swipeRefresh.setOnRefreshListener {
            fetchData()
        }
    }

    private fun fetchData() {
        val apiService = ApiConfig.getApiService(requireContext())
        viewModel.fetchWalletHistory(apiService)
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.rvTransactionHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun observeViewModel() {
        // Pantau Data Histori
        viewModel.walletHistory.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
                transactionAdapter.setData(list)
            }
        }

        // Pantau Loading (Menangani ProgressBar & SwipeRefresh)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isLoading) {
                // Matikan animasi loading jika sudah selesai
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
            } else {
                // Jika sedang loading, cek apakah user melakukan swipe?
                // Jika TIDAK sedang swipe (berarti loading pertama kali), munculkan ProgressBar tengah
                if (!binding.swipeRefresh.isRefreshing) {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        // Pantau Error/Pesan Umum
        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk menampilkan/menyembunyikan UI Empty State
    private fun showEmptyState(isEmpty: Boolean) {
        // PENTING: Kita HANYA mengatur visibilitas layoutEmptyState.
        // rvTransactionHistory tidak di-hide (GONE) agar layar tetap bisa di-swipe walau data kosong!
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}