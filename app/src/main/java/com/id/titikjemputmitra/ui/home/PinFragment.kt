package com.id.titikjemputmitra.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.data.UserPreference
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentPinBinding

class PinFragment : Fragment() {
    private var _binding: FragmentPinBinding? = null
    private val binding get() = _binding!!

    // Menggunakan activityViewModels agar sinkron dengan state di Home/Wallet
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lock tombol back saat sedang proses simpan (opsional tapi disarankan)
        setupBackPressed()

        observeViewModel()

        binding.btnSavePin.setOnClickListener {
            val pin = binding.edtPin.text.toString()
            val pinConfirm = binding.edtPinConfirm.text.toString()

            // 1. Validasi Kosong
            if (pin.isEmpty() || pinConfirm.isEmpty()) {
                showInfoDialog("Perhatian", "Harap isi PIN dan konfirmasinya.")
                return@setOnClickListener
            }

            // 2. Validasi Panjang 6 Digit
            if (pin.length < 6) {
                binding.edtPin.error = "PIN harus terdiri dari 6 digit angka"
                return@setOnClickListener
            }

            // 3. Validasi Kecocokan (Client-side Validation)
            if (pin != pinConfirm) {
                binding.edtPinConfirm.error = "Konfirmasi PIN tidak cocok, silakan periksa kembali"
                return@setOnClickListener
            }

            // Bersihkan error jika sudah valid
            binding.edtPin.error = null
            binding.edtPinConfirm.error = null

            // Panggil API lewat ViewModel - ApiService didapat via ApiConfig
            val apiService = ApiConfig.getApiService(requireContext())
            viewModel.setupPin(apiService, pin, pinConfirm)
        }
    }

    private fun observeViewModel() {
        // Pantau Loading khusus PIN
        viewModel.isPinLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSavePin.isEnabled = !isLoading
            binding.edtPin.isEnabled = !isLoading
            binding.edtPinConfirm.isEnabled = !isLoading
        }

        // Pantau Hasil Pembuatan PIN
        viewModel.pinResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                if (response.status == true) {
                    // Update status lokal di UserPreference
                    val userPref = UserPreference(requireContext())
                    userPref.setPinStatus("1")

                    // Dialog sukses yang sopan
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Berhasil")
                        .setMessage("PIN Keamanan Anda telah aktif. Sekarang Anda dapat melakukan penarikan saldo dengan aman.")
                        .setPositiveButton("Lanjutkan") { _, _ ->
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }

        // Pantau Pesan/Error dari server (Gunakan Dialog sesuai permintaan Anda)
        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                showInfoDialog("Informasi", msg)
            }
        }
    }

    private fun showInfoDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Mengerti", null)
            .show()
    }

    private fun setupBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isPinLoading.value == true) {
                    showInfoDialog("Mohon Tunggu", "Sistem sedang mengamankan PIN Anda. Mohon jangan menutup aplikasi.")
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}