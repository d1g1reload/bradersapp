package com.id.titikjemputmitra.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.UserPreference
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentProfileBinding
import com.id.titikjemputmitra.ui.auth.LoginActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var userPref: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPref = UserPreference(requireContext())

        populateUserData()
        setupAction()
        setupObservers()
    }

    private fun populateUserData() {
        val user = userPref.getUser()
        // Mengisi nama dari data session lokal.
        // Jika Anda memiliki field store_name di UserPreference, gunakan itu untuk tvStoreName.
        // Sebagai fallback, kita isi dengan nama lengkap.
        binding.tvStoreName.text = user.fullname ?: "Mitra Titik Jemput"
        binding.tvOwnerName.text = user.email ?: "Email tidak tersedia"
    }

    private fun setupAction() {
        // Aksi Logout
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Aksi Menu Pengaturan
        binding.btnAddress.setOnClickListener {
            Toast.makeText(requireContext(), "Menuju menu Alamat Mitra", Toast.LENGTH_SHORT).show()
            // TODO: Integrasikan dengan Navigation Component ke halaman Alamat
        }

        // AKSI MENU BARU: REKENING BANK
        binding.btnBankAccount.setOnClickListener {
            findNavController().navigate(R.id.navigation_bank_account)
        }

        binding.btnHelp.setOnClickListener {
            Toast.makeText(requireContext(), "Menuju Pusat Bantuan", Toast.LENGTH_SHORT).show()
            // TODO: Integrasikan dengan Navigation Component ke halaman Bantuan
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Kunci tombol logout agar tidak di-spam klik saat proses API berjalan
            binding.btnLogout.isEnabled = !isLoading
            binding.btnLogout.text = if (isLoading) "MEMPROSES..." else "KELUAR (LOGOUT)"
        }

        viewModel.logoutStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    clearLocalDataAndRedirect()
                }
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun ini?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Keluar") { _, _ ->
                performLogout()
            }
            .show()
    }

    private fun performLogout() {
        val apikey = userPref.getUser().apikey

        if (apikey.isNullOrEmpty()) {
            clearLocalDataAndRedirect()
            return
        }

        // Panggil API Logout melalui ViewModel
        val apiService = ApiConfig.getApiService(requireContext())
        viewModel.logout(apiService)
    }

    private fun clearLocalDataAndRedirect() {
        userPref.clearUser()
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}