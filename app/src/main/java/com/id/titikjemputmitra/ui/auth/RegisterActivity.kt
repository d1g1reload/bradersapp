package com.id.titikjemputmitra.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    // Gunakan ViewModel sesuai arsitektur Titik Jemput
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        observeViewModel()

        binding.btnRegister.setOnClickListener {
            submitRegister()
        }
    }

    private fun submitRegister() {
        val fullname = binding.etFullname.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (fullname.isEmpty()) {
            binding.etFullname.error = "Nama lengkap harus diisi"
            binding.etFullname.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email harus diisi"
            binding.etEmail.requestFocus()
            return
        } else if (!isValidEmail(email)) {
            binding.etEmail.error = "Format email tidak valid"
            binding.etEmail.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "Nomor telepon harus diisi"
            binding.etPhone.requestFocus()
            return
        } else if (phone.length < 10) {
            binding.etPhone.error = "Nomor telepon minimal 10 digit"
            binding.etPhone.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password harus diisi"
            binding.etPassword.requestFocus()
            return
        } else if (password.length < 8) { // Diperbaiki menjadi < 8 sesuai error message
            binding.etPassword.error = "Password minimal 8 karakter"
            binding.etPassword.requestFocus()
            return
        }

        // Panggil API lewat ViewModel
        // Perhatikan penggunaan ApiConfig.getApiService(this) sesuai standar kita
        val apiService = ApiConfig.getApiService(this)

        // Asumsi "2" adalah role ID untuk Mitra/Penjual sesuai endpoint Anda
        viewModel.registerUser(apiService, "3", fullname, email, password, phone)
    }

    private fun observeViewModel() {
        // Pantau hasil registrasi
        viewModel.registerResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { response ->
                if (response.status == true) {
                    Toast.makeText(this, "Registrasi Berhasil. Silakan verifikasi OTP.", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, OtpActivity::class.java)
                    intent.putExtra("extra_email", binding.etEmail.text.toString().trim())
                    startActivity(intent)
                    finish()
                } else {
                    showErrorDialog("Registrasi Gagal", response.message ?: "Terjadi kesalahan")
                }
            }
        }

        // Pantau Error/Pesan dari CI3
        viewModel.message.observe(this) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                showErrorDialog("Perhatian", msg)
            }
        }

        // Pantau Indikator Loading
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false
            binding.btnRegister.text = "Memproses..."
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true
            binding.btnRegister.text = "Daftar"
        }
    }

    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Mengerti", null)
            .show()
    }

    private fun isValidEmail(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}