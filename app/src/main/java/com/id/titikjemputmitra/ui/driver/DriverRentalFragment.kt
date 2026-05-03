package com.id.titikjemputmitra.ui.driver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.data.response.DataItemDriver
import com.id.titikjemputmitra.databinding.FragmentDriverRentalBinding
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.ui.driver.adapter.DriverAdapter

class DriverRentalFragment : Fragment() {
    private var _binding: FragmentDriverRentalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DriverViewModel by viewModels()
    private lateinit var adapter: DriverAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverRentalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupSwipeRefresh()
        setOnClickListener()
        observeData()

        // Load data pertama kali
        fetchData()
    }

    private fun setupRecyclerView() {
        adapter = DriverAdapter(
            onItemClick = { driver ->
                // Opsi: Navigasi ke detail atau chat driver
                Toast.makeText(requireContext(), "Info: ${driver.fullname}", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { driver ->
                // Panggil Dialog Konfirmasi Hapus
                showDeleteConfirmation(driver)
            }
        )
        binding.rvDrivers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DriverRentalFragment.adapter
        }
    }

    private fun showDeleteConfirmation(driver: DataItemDriver) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Driver?")
            .setMessage("Apakah Anda yakin ingin menghapus ${driver.fullname}? \n\nData login akan dinonaktifkan dan data identitas KTP akan dihapus secara permanen dari server.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                val apiService = ApiConfig.getApiService(requireContext())
                // Panggil fungsi hapus di ViewModel
                viewModel.deleteDriver(apiService, driver.userId)
            }
            .show()
    }

    private fun setupSearch() {
        binding.etSearchDriver.doOnTextChanged { text, _, _, _ ->
            adapter.filter(text.toString())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchData()
        }
    }

    private fun fetchData() {
        val apiService = ApiConfig.getApiService(requireContext())
        viewModel.getDrivers(apiService)
    }

    private fun observeData() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // LOGIKA: Jika SwipeRefresh sedang aktif (berputar), jangan tampilkan ProgressBar tengah
            if (binding.swipeRefresh.isRefreshing) {
                if (!isLoading) binding.swipeRefresh.isRefreshing = false
            } else {
                // Jika bukan dari swipe (misal load pertama kali), gunakan ProgressBar
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observer Data List
        viewModel.listDriver.observe(viewLifecycleOwner) { list ->
            // Pastikan saat data datang, semua loading mati
            binding.swipeRefresh.isRefreshing = false
            binding.progressBar.visibility = View.GONE

            if (list.isNullOrEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvDrivers.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvDrivers.visibility = View.VISIBLE
                adapter.setInitialList(list)
            }
        }

        // Observer Pesan Error
        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setOnClickListener() {
        binding.fabAddDriver.setOnClickListener {
            // Menggunakan Directions yang sudah Anda siapkan
            findNavController().navigate(
                DriverRentalFragmentDirections.actionNavigationDriverToNavigationAddDriver()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}