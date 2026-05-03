package com.id.titikjemputmitra.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.data.UserPreference
import com.id.titikjemputmitra.data.response.LoginResponse
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.ActivityLoginBinding
import com.id.titikjemputmitra.ui.MainActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnLogin.setOnClickListener {
            val identity = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (identity.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email/No. HP dan Password wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(identity, password)
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(identity: String, password: String) {
        showLoading(true)

        // Asumsi 2 atau 3 adalah Role ID Mitra Anda
        val client = ApiConfig.getApiService(this).userLogin(identity, password, 3)

        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody != null && responseBody.status == true) {
                        val userData = responseBody.data
                        if (userData != null) {
                            val userPref = UserPreference(this@LoginActivity)

                            // SIMPAN SESI (Kecuali jika statusnya "0" / Pending)
                            if (userData.identityStatus != "0") {
                                userPref.setUser(userData)
                            }

                            // CEK STATUS IDENTITAS
                            when (userData.identityStatus) {
                                "1" -> {
                                    Toast.makeText(this@LoginActivity, "Selamat Datang, ${userData.fullname}!", Toast.LENGTH_SHORT).show()
                                    // Arahkan ke MainActivity (MainActivity akan mengecek Perjanjian otomatis)
                                    navigateToHome()
                                }
                                "0" -> {
                                    MaterialAlertDialogBuilder(this@LoginActivity)
                                        .setTitle("Verifikasi Diproses")
                                        .setMessage("Akun Anda sedang dalam proses verifikasi oleh tim Admin kami. Mohon tunggu hingga akun disetujui.")
                                        .setPositiveButton("Mengerti") { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        .setCancelable(false)
                                        .show()
                                }
                                "2" -> {
                                    MaterialAlertDialogBuilder(this@LoginActivity)
                                        .setTitle("Verifikasi Ditolak")
                                        .setMessage("Verifikasi identitas Anda ditolak. Pastikan foto KTP dan Selfie terlihat jelas, tidak terpotong, dan tidak buram.")
                                        .setPositiveButton("Lengkapi Ulang") { dialog, _ ->
                                            dialog.dismiss()
                                            navigateToIdentity()
                                        }
                                        .setCancelable(false)
                                        .show()
                                }
                                else -> {
                                    // Jika null atau kosong (Belum Isi)
                                    MaterialAlertDialogBuilder(this@LoginActivity)
                                        .setTitle("Identitas Belum Lengkap")
                                        .setMessage("Demi keamanan bersama, Anda wajib melengkapi data KTP dan Selfie sebelum menggunakan aplikasi.")
                                        .setPositiveButton("Lengkapi Sekarang") { dialog, _ ->
                                            dialog.dismiss()
                                            navigateToIdentity()
                                        }
                                        .setCancelable(false)
                                        .show()
                                }
                            }
                        }
                    } else {
                        handleLoginError(responseBody?.message, responseBody?.data?.isUnregistered == true)
                    }
                } else {
                    parseErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@LoginActivity, "Masalah koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseErrorResponse(response: Response<LoginResponse>) {
        val errorBodyString = response.errorBody()?.string()
        var errorMessage = "Gagal masuk ke sistem"
        var isUnregistered = false

        if (errorBodyString != null) {
            try {
                val jsonObject = JSONObject(errorBodyString)
                errorMessage = jsonObject.optString("message", errorMessage)

                val dataObj = jsonObject.optJSONObject("data")
                if (dataObj != null) {
                    isUnregistered = dataObj.optBoolean("is_unregistered", false)
                }
            } catch (e: Exception) {}
        }
        handleLoginError(errorMessage, isUnregistered)
    }

    private fun handleLoginError(message: String?, isUnregistered: Boolean) {
        Toast.makeText(this, message ?: "Terjadi kesalahan", Toast.LENGTH_LONG).show()
        if (isUnregistered) {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToIdentity() {
        val intent = Intent(this, IdentityActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}