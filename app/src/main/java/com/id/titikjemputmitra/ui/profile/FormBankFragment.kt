package com.id.titikjemputmitra.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentFormBankBinding

class FormBankFragment : Fragment() {
    private var _binding: FragmentFormBankBinding? = null
    private val binding get() = _binding!!

    // Gunakan ViewModel yang sesuai (Misal: ProfileViewModel)
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormBankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        binding.btnSaveBank.setOnClickListener {
            val bankName = binding.edtBankName.text.toString().trim()
            val accountNumber = binding.edtAccountNumber.text.toString().trim()
            val accountName = binding.edtAccountName.text.toString().trim()

            // Konversi boolean dari switch menjadi 1 atau 0
            val isPrimary = if (binding.switchPrimary.isChecked) 1 else 0

            // Reset Error
            binding.tilBankName.error = null
            binding.tilAccountNumber.error = null
            binding.tilAccountName.error = null

            // Validasi Input Kosong
            var isValid = true

            if (bankName.isEmpty()) {
                binding.tilBankName.error = "Nama bank wajib diisi"
                isValid = false
            }

            if (accountNumber.isEmpty()) {
                binding.tilAccountNumber.error = "Nomor rekening wajib diisi"
                isValid = false
            } else if (accountNumber.length < 8) {
                // Rata-rata nomor rekening minimal 8 digit
                binding.tilAccountNumber.error = "Nomor rekening tidak valid"
                isValid = false
            }

            if (accountName.isEmpty()) {
                binding.tilAccountName.error = "Nama pemilik wajib diisi"
                isValid = false
            }

            // Jika semua valid, tembak API
            if (isValid) {
                val apiService = ApiConfig.getApiService(requireContext())
                viewModel.addBankAccount(apiService, bankName, accountNumber, accountName, isPrimary)
            }
        }
    }

    private fun observeViewModel() {
        // Pantau hasil sukses
        viewModel.addBankResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                if (response.status == true) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Berhasil")
                        .setMessage(response.message ?: "Rekening berhasil ditambahkan.")
                        .setPositiveButton("Selesai") { _, _ ->
                            // Kembali ke halaman daftar bank
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    showErrorDialog(response.message ?: "Gagal menambahkan rekening.")
                }
            }
        }

        // Pantau pesan error dari server
        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                showErrorDialog(msg)
            }
        }

        // Pantau Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveBank.isEnabled = !isLoading

            // Kunci input saat loading agar tidak bisa diubah-ubah
            binding.edtBankName.isEnabled = !isLoading
            binding.edtAccountNumber.isEnabled = !isLoading
            binding.edtAccountName.isEnabled = !isLoading
            binding.switchPrimary.isEnabled = !isLoading
        }
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Perhatian")
            .setMessage(message)
            .setPositiveButton("Mengerti", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}