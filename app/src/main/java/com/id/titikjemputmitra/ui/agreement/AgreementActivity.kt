package com.id.titikjemputmitra.ui.agreement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.id.titikjemputmitra.data.UserPreference
import com.id.titikjemputmitra.data.response.AgreementResponse
import com.id.titikjemputmitra.data.response.GeneralResponse
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.ActivityAgreementBinding
import com.id.titikjemputmitra.ui.MainActivity
import com.id.titikjemputmitra.ui.auth.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AgreementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgreementBinding
    private lateinit var userPref: UserPreference

    // Menyimpan ID dokumen agar bisa dikirim kembali saat klik Setuju
    private var currentAgreementId: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgreementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPref = UserPreference(this)

        // 1. Mencegah user kabur menggunakan tombol back HP (Android 13+ Support)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(
                    this@AgreementActivity,
                    "Silakan setujui atau tolak perjanjian terlebih dahulu.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        setupUI()
        setupAction()

        // 2. Langsung tarik data perjanjian dari server saat halaman dibuka
        fetchDataAgreement()
    }

    private fun setupUI() {
        // Tombol Submit hanya aktif jika Checkbox dicentang
        binding.cbAgree.setOnCheckedChangeListener { _, isChecked ->
            binding.btnSubmit.isEnabled = isChecked
        }
    }

    private fun setupAction() {
        binding.btnSubmit.setOnClickListener {
            submitAgreement()
        }

        binding.btnLogout.setOnClickListener {
            // Hapus sesi lokal karena user menolak T&C
            userPref.clearUser()

            Toast.makeText(this, "Anda menolak persyaratan kemitraan", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // ============================================================================
    // INTEGRASI API: MENGAMBIL DATA PERJANJIAN
    // ============================================================================
    private fun fetchDataAgreement() {
        setLoadingState(true)

        val apiService = ApiConfig.getApiService(this)
        apiService.getAgreement().enqueue(object : Callback<AgreementResponse> {
            override fun onResponse(call: Call<AgreementResponse>, response: Response<AgreementResponse>) {
                setLoadingState(false)

                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        currentAgreementId = data.agreementId
                        renderHtmlDataFromDatabase(data.title, data.content)
                    } else {
                        Toast.makeText(this@AgreementActivity, "Format dokumen tidak valid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AgreementActivity, "Gagal memuat dokumen: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AgreementResponse>, t: Throwable) {
                setLoadingState(false)
                Toast.makeText(this@AgreementActivity, "Koneksi terputus. Gagal memuat perjanjian.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ============================================================================
    // INTEGRASI API: MENGIRIM PERSETUJUAN
    // ============================================================================
    private fun submitAgreement() {
        setLoadingState(true)
        // Matikan tombol sementara agar tidak double-click
        binding.btnSubmit.isEnabled = false

        val apiService = ApiConfig.getApiService(this)
        apiService.approveAgreement(currentAgreementId).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                setLoadingState(false)

                if (response.isSuccessful && response.body()?.status == true) {
                    // SANGAT PENTING: Update sesi lokal agar tidak terjebak di halaman ini lagi
                    userPref.setAgreementStatus("1")

                    Toast.makeText(this@AgreementActivity, "Perjanjian disetujui!", Toast.LENGTH_SHORT).show()
                    moveToMainActivity()
                } else {
                    binding.btnSubmit.isEnabled = binding.cbAgree.isChecked
                    Toast.makeText(this@AgreementActivity, response.body()?.message ?: "Gagal menyetujui", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                setLoadingState(false)
                binding.btnSubmit.isEnabled = binding.cbAgree.isChecked
                Toast.makeText(this@AgreementActivity, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ============================================================================
    // UTILITIES
    // ============================================================================

    private fun renderHtmlDataFromDatabase(title: String, htmlContent: String) {
        binding.tvAgreementTitle.text = title
        binding.tvAgreementContent.text = HtmlCompat.fromHtml(
            htmlContent,
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        // Sembunyikan dokumen sementara saat loading
        binding.cardDocument.alpha = if (isLoading) 0.5f else 1.0f
    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}