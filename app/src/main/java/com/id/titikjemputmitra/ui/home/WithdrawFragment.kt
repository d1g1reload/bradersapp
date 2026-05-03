package com.id.titikjemputmitra.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.response.DataBank
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentWithdrawBinding
import java.text.NumberFormat
import java.util.Locale

class WithdrawFragment : Fragment() {
    private var _binding: FragmentWithdrawBinding? = null
    private val binding get() = _binding!!

    // Menggunakan viewModels() agar siklus hidup dikelola mandiri
    private val viewModel: HomeViewModel by viewModels()

    // Variabel untuk menyimpan ID Rekening yang dipilih
    private var selectedAccountId: String = ""

    // Variabel untuk menyimpan daftar semua rekening di memori
    private var cachedBankList: List<DataBank> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWithdrawBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiConfig.getApiService(requireContext())

        // 1. Tarik saldo
        viewModel.fetchUserBalance(apiService)
        // 2. Tarik SEMUA daftar rekening mitra
        viewModel.fetchListBank(apiService)

        observeViewModel()

        // AKSI TOMBOL UBAH REKENING DENGAN POP-UP DIALOG
        binding.btnChangeBank.setOnClickListener {
            if (cachedBankList.isEmpty()) {
                showErrorDialog("Perhatian", "Anda belum memiliki rekening. Silakan tambahkan rekening bank di menu Profil terlebih dahulu.")
                return@setOnClickListener
            }
            showBankSelectionDialog()
        }

        // AKSI SUBMIT PENARIKAN
        binding.btnSubmitWithdraw.setOnClickListener {
            val amountStr = binding.edtWithdrawAmount.text.toString()

            // 1. Validasi Rekening Bank
            if (selectedAccountId.isEmpty()) {
                showErrorDialog("Rekening Belum Diatur", "Anda belum memilih rekening tujuan pencairan dana.")
                return@setOnClickListener
            }

            // 2. Validasi Kosong
            if (amountStr.isEmpty()) {
                binding.edtWithdrawAmount.error = "Masukkan nominal penarikan"
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull() ?: 0.0

            // 3. Validasi Minimal Penarikan
            if (amount < 50000) {
                binding.edtWithdrawAmount.error = "Minimal penarikan adalah Rp 50.000"
                return@setOnClickListener
            }

            // 4. Ambil saldo asli langsung dari ViewModel
            val actualBalanceStr = viewModel.balance.value
            val currentBalance = actualBalanceStr?.toDoubleOrNull() ?: 0.0

            // 5. Validasi Saldo Cukup
            if (amount > currentBalance) {
                binding.edtWithdrawAmount.error = "Saldo tidak mencukupi"
                return@setOnClickListener
            }

            // Bersihkan error jika lolos
            binding.edtWithdrawAmount.error = null

            // 6. Munculkan BottomSheet PIN (Kirim amount sebagai String)
            showPinBottomSheet(amountStr)
        }
    }

    // =========================================================================
    // FUNGSI BARU: POP-UP PILIHAN BANK
    // =========================================================================
    private fun showBankSelectionDialog() {
        // Format daftar nama bank untuk ditampilkan di RadioButton Dialog
        val bankNamesArray = cachedBankList.map {
            "${it.bankName ?: "-"} - ${it.accountNumber ?: "-"}"
        }.toTypedArray()

        // Cari posisi index bank yang saat ini sedang terpilih
        var selectedIndex = cachedBankList.indexOfFirst { it.accountId == selectedAccountId }
        if (selectedIndex == -1) selectedIndex = 0

        // Munculkan Material Dialog bawaan Android
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pilih Rekening Tujuan")
            .setSingleChoiceItems(bankNamesArray, selectedIndex) { dialog, which ->
                // Update UI ketika pengguna memilih salah satu rekening
                val selectedBank = cachedBankList[which]
                updateBankUI(selectedBank)
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Fungsi untuk memperbarui text di dalam Card UI
    private fun updateBankUI(bank: DataBank) {
        selectedAccountId = bank.accountId ?: ""
        binding.tvBankInfo.text = "${bank.bankName ?: "-"} - ${bank.accountNumber ?: "-"}"
        binding.tvAccountHolder.text = bank.accountName ?: "-"
    }
    // =========================================================================

    private fun showPinBottomSheet(amountStr: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_bottom_sheet_pin, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // Binding komponen di dalam BottomSheet
        val tvAmountInfo = bottomSheetView.findViewById<TextView>(R.id.tvWithdrawAmountInfo)
        val edtPin = bottomSheetView.findViewById<TextInputEditText>(R.id.edtConfirmPin)
        val btnSubmitPin = bottomSheetView.findViewById<MaterialButton>(R.id.btnSubmitPin)

        // Tampilkan info nominal di BottomSheet
        val amountDouble = amountStr.toDoubleOrNull() ?: 0.0
        tvAmountInfo.text = "Penarikan: ${formatRupiah(amountDouble)}"

        btnSubmitPin.setOnClickListener {
            val pin = edtPin.text.toString()

            if (pin.length < 6) {
                edtPin.error = "PIN harus 6 digit"
                return@setOnClickListener
            }

            // Tutup BottomSheet lalu panggil API Withdraw dengan ID Rekening
            bottomSheetDialog.dismiss()
            val apiService = ApiConfig.getApiService(requireContext())
            viewModel.withdrawBalance(apiService, amountStr, pin, selectedAccountId)
        }

        bottomSheetDialog.show()
    }

    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Mengerti") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        // Pantau Saldo
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            if (!balance.isNullOrEmpty()) {
                binding.tvAvailableBalance.text = formatRupiah(balance.toDoubleOrNull() ?: 0.0)
            } else {
                binding.tvAvailableBalance.text = "Rp 0"
            }
        }

        // Pantau List Rekening Bank
        viewModel.listBank.observe(viewLifecycleOwner) { list ->
            cachedBankList = list ?: emptyList()

            if (cachedBankList.isNotEmpty()) {
                // Default: Pilih rekening yang memiliki status isPrimary = "1"
                // Jika tidak ada yang "1", otomatis pilih rekening urutan teratas (index 0)
                val defaultBank = cachedBankList.find { it.isPrimary == "1" } ?: cachedBankList[0]
                updateBankUI(defaultBank)
            } else {
                // Jika tidak ada rekening sama sekali
                selectedAccountId = ""
                binding.tvBankInfo.text = "Belum ada rekening"
                binding.tvAccountHolder.text = "Tambahkan melalui profil"
            }
        }

        // Pantau Hasil Penarikan
        viewModel.withdrawResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                if (response.status == true) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Penarikan Berhasil")
                        .setMessage(response.message ?: "Penarikan berhasil diajukan dan sedang diproses.")
                        .setCancelable(false)
                        .setPositiveButton("Mengerti") { dialog, _ ->
                            dialog.dismiss()
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                        .show()

                } else {
                    showErrorDialog("Penarikan Gagal", response.message ?: "PIN salah atau terjadi kesalahan.")
                }
            }
        }

        // Pantau Indikator Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSubmitWithdraw.isEnabled = !isLoading
            binding.edtWithdrawAmount.isEnabled = !isLoading
            binding.btnChangeBank.isEnabled = !isLoading // Disable tombol Ubah saat loading
        }

        // Pantau Error Jaringan/Sistem
        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatRupiah(number: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(number).replace(",00", "").replace("Rp", "Rp ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}