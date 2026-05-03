package com.id.titikjemputmitra.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.ActivityOtpBinding

class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding

    // Panggil AuthViewModel
    private val viewModel: AuthViewModel by viewModels()

    private var userEmail: String? = null
    private var countDownTimer: CountDownTimer? = null
    private val timeDuration = 30000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        userEmail = intent.getStringExtra("extra_email")
        binding.tvUserContact.text = userEmail ?: "User"

        observeViewModel()
        startResendTimer()

        binding.btnVerify.setOnClickListener {
            val otpCode = binding.etOtp.text.toString().trim()

            if (otpCode.length < 6) {
                Toast.makeText(this, "Masukkan 6 digit kode OTP", Toast.LENGTH_SHORT).show()
            } else {
                val email = userEmail ?: ""
                val apiService = ApiConfig.getApiService(this)
                viewModel.verifyOtp(apiService, email, otpCode)
            }
        }

        binding.tvResend.setOnClickListener {
            val email = userEmail ?: return@setOnClickListener
            val apiService = ApiConfig.getApiService(this)
            viewModel.resendOtp(apiService, email)
        }
    }

    private fun observeViewModel() {
        // Pantau hasil Verifikasi OTP
        viewModel.otpVerifyResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { response ->
                if (response.status == true) {
                    Toast.makeText(this, "Verifikasi Berhasil! Silakan Masuk.", Toast.LENGTH_SHORT).show()
                    navigateToHome() // Atau navigateToLogin sesuai flow aplikasi Anda
                } else {
                    showErrorDialog("Verifikasi Gagal", response.message ?: "OTP tidak valid")
                    binding.etOtp.setText("") // Kosongkan field
                }
            }
        }

        // Pantau hasil Kirim Ulang OTP
        viewModel.resendResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { response ->
                if (response.status == true) {
                    Toast.makeText(this, "OTP berhasil dikirim ulang ke email Anda", Toast.LENGTH_SHORT).show()
                    startResendTimer() // Mulai ulang timer jika sukses
                } else {
                    showErrorDialog("Gagal Kirim Ulang", response.message ?: "Silakan coba lagi nanti")
                }
            }
        }

        // Pantau pesan Error Umum dari Server (CodeIgniter)
        viewModel.message.observe(this) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                showErrorDialog("Perhatian", msg)
                binding.etOtp.setText("")
            }
        }

        // Pantau Indikator Loading
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnVerify.isEnabled = !isLoading
            binding.tvResend.isEnabled = !isLoading // Disable resend saat loading
        }
    }

    private fun startResendTimer() {
        binding.tvResend.isEnabled = false
        binding.tvResend.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))

        countDownTimer?.cancel() // Batalkan timer lama jika ada

        countDownTimer = object : CountDownTimer(timeDuration, 1000) {
            override fun onFinish() {
                binding.tvResend.isEnabled = true
                binding.tvResend.text = "Kirim Ulang"
                // Asumsi warna biru Anda (sesuaikan dengan warna tema aplikasi Anda)
                binding.tvResend.setTextColor(ContextCompat.getColor(this@OtpActivity, android.R.color.holo_blue_dark))
            }

            override fun onTick(millisUntilFinished: Long) {
                val detik = millisUntilFinished / 1000
                binding.tvResend.text = "Kirim Ulang ($detik)"
            }
        }.start()
    }

    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Mengerti", null)
            .show()
    }

    private fun navigateToHome() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // PENTING: Mencegah memory leak saat Activity dihancurkan
        countDownTimer?.cancel()
    }
}