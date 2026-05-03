package com.id.titikjemputmitra.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.adapter.BankAdapter
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentBankAccountBinding

class BankAccountFragment : Fragment() {
    private var _binding: FragmentBankAccountBinding? = null
    private val binding get() = _binding!!

    // Inisiasi ViewModel
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var bankAdapter: BankAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBankAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // Panggil API saat halaman dibuka
        fetchBankData()

        // Aksi Tombol Tambah Rekening (FAB)
        binding.fabAddBank.setOnClickListener {
            findNavController().navigate(R.id.navigation_form_bank)
        }
    }

    private fun fetchBankData() {
        val apiService = ApiConfig.getApiService(requireContext())
        viewModel.getListBank(apiService)
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan aksi hapus
        bankAdapter = BankAdapter { bank ->
            showDeleteConfirmDialog(bank.accountId, bank.bankName, bank.accountNumber)
        }

        binding.rvBankAccounts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bankAdapter
        }
    }

    private fun observeViewModel() {
        // Pantau Daftar Bank
        viewModel.listBank.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvBankAccounts.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvBankAccounts.visibility = View.VISIBLE
                bankAdapter.setData(list)
            }
        }

        // Pantau Hasil Hapus Rekening
        viewModel.deleteBankResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                if (response.status == true) {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    // Refresh data setelah berhasil dihapus
                    fetchBankData()
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Pantau Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Pantau Error Umum
        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmDialog(accountId: String?, bankName: String?, accountNumber: String?) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Rekening")
            .setMessage("Apakah Anda yakin ingin menghapus rekening $bankName - $accountNumber ini?")
            .setPositiveButton("Hapus") { _, _ ->
                if (!accountId.isNullOrEmpty()) {
                    val apiService = ApiConfig.getApiService(requireContext())
                    viewModel.deleteBank(apiService, accountId)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}