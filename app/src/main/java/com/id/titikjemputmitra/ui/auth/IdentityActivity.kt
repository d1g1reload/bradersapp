package com.id.titikjemputmitra.ui.auth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.data.response.DataIdentity
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.ActivityIdentityBinding
import com.id.titikjemputmitra.utils.getImageUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class IdentityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIdentityBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var tempImageUri: Uri? = null
    private var finalKtpUri: Uri? = null
    private var finalSelfieUri: Uri? = null
    private var isScanningKtp = true

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    // =========================================================================
    // LAUNCHER: Foto Kamera
    // =========================================================================
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess && tempImageUri != null) {
                if (isScanningKtp) {
                    processKtpImage(tempImageUri!!)
                } else {
                    processSelfieImage(tempImageUri!!)
                }
            }
        }

    // =========================================================================
    // LAUNCHER: Multiple Permissions (Kamera + Lokasi)
    // =========================================================================
    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val locationGranted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false)
                    || (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false)

            when {
                !cameraGranted && !locationGranted -> showToast("Izin Kamera dan Lokasi wajib diaktifkan.")
                !cameraGranted -> showToast("Izin Kamera wajib diaktifkan.")
                !locationGranted -> showToast("Izin Lokasi wajib diaktifkan.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setFormEnabled(false)
        checkFormValidity()

        binding.btnScanKtp.setOnClickListener {
            isScanningKtp = true
            checkPermissionsAndAction { openCamera() }
        }

        binding.btnScanSelfie.setOnClickListener {
            isScanningKtp = false
            checkPermissionsAndAction { openCamera() }
        }

        binding.btnAmbilLokasi.setOnClickListener {
            checkPermissionsAndAction { fetchCurrentLocation() }
        }

        binding.btnSimpan.setOnClickListener {
            if (validateFormBeforeSubmit()) {
                submitFinalData()
            }
        }
    }

    private fun checkPermissionsAndAction(action: () -> Unit) {
        val hasCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val hasLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasCamera && hasLocation) {
            action()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("Izin Diperlukan")
                .setMessage("Aplikasi membutuhkan akses Kamera dan Lokasi untuk melanjutkan.")
                .setPositiveButton("Lanjut") { _, _ ->
                    requestMultiplePermissionsLauncher.launch(
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun openCamera() {
        tempImageUri = getImageUri(this)
        tempImageUri?.let { uri -> takePictureLauncher.launch(uri) }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        binding.btnAmbilLokasi.isEnabled = false
        binding.btnAmbilLokasi.text = "Sedang mencari sinyal GPS..."

        lifecycleScope.launch {
            try {
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).await()
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    binding.etKoordinat.setText("${location.latitude}, ${location.longitude}")
                    showToast("Lokasi berhasil ditangkap!")
                } else {
                    showToast("Gagal mendapatkan lokasi. Pastikan GPS menyala.")
                }
            } catch (e: Exception) {
                showToast("Error GPS: ${e.message}")
            } finally {
                binding.btnAmbilLokasi.isEnabled = true
                binding.btnAmbilLokasi.text = "PERBARUI LOKASI"
                checkFormValidity()
            }
        }
    }

    // =========================================================================
    // PROSES KTP — Kirim ke backend → OCR → Gemini → Isi Form
    // =========================================================================
    private fun processKtpImage(uri: Uri) {
        setKtpLoading(true)
        lifecycleScope.launch {
            try {
                val rawBitmap = withContext(Dispatchers.IO) { loadBitmap(uri) }
                val rotatedBitmap = withContext(Dispatchers.IO) { rotateIfRequired(rawBitmap, uri) }
                val multipartImage = withContext(Dispatchers.IO) { createMultipart(rotatedBitmap, "ktp_image") }

                val apiService = ApiConfig.getApiService(this@IdentityActivity)
                val response = withContext(Dispatchers.IO) { apiService.scanKtp(multipartImage) }

                if (response.status == true && response.data != null) {
                    binding.ivKtpPreview.setImageBitmap(rotatedBitmap)
                    binding.flScanGuide.visibility = View.GONE

                    setFormEnabled(true)
                    fillForm(response.data) // Memanggil fungsi fillForm dengan key bahasa Inggris

                    finalKtpUri = uri
                    binding.btnScanKtp.visibility = View.GONE
                    binding.tvInfoKtpManual.visibility = View.VISIBLE

                    showToast("KTP berhasil dibaca. Periksa dan lengkapi jika ada yang kosong.")
                } else {
                    throw Exception(response.message ?: "Gagal memproses data KTP dari server.")
                }
            } catch (e: Exception) {
                finalKtpUri = null
                showErrorDialog("Gagal Membaca KTP", e.message ?: "Pastikan pencahayaan cukup dan KTP tidak terpotong.")
            } finally {
                setKtpLoading(false)
                checkFormValidity()
            }
        }
    }

    private fun processSelfieImage(uri: Uri) {
        setSelfieLoading(true)
        lifecycleScope.launch {
            try {
                val rawBitmap = withContext(Dispatchers.IO) { loadBitmap(uri) }
                val rotatedBitmap = withContext(Dispatchers.IO) { rotateIfRequired(rawBitmap, uri) }
                val multipartImage = withContext(Dispatchers.IO) { createMultipart(rotatedBitmap, "selfie_photo") }

                val apiService = ApiConfig.getApiService(this@IdentityActivity)
                val response = withContext(Dispatchers.IO) { apiService.verifySelfie(multipartImage) }

                if (response.status == true) {
                    finalSelfieUri = uri
                    binding.ivSelfiePreview.setImageBitmap(rotatedBitmap)
                    binding.layoutSelfiePlaceholder.visibility = View.GONE
                    binding.btnScanSelfie.visibility = View.GONE
                    showToast("Selfie valid!")
                } else {
                    throw Exception(response.message ?: "Foto selfie tidak memenuhi syarat.")
                }
            } catch (e: Exception) {
                finalSelfieUri = null
                binding.ivSelfiePreview.setImageDrawable(null)
                binding.layoutSelfiePlaceholder.visibility = View.VISIBLE
                showErrorDialog("Selfie Ditolak", e.message ?: "Harap lepas kacamata hitam, masker, atau topi saat berfoto.")
            } finally {
                setSelfieLoading(false)
                checkFormValidity()
            }
        }
    }

    // =========================================================================
    // SUBMIT FINAL DATA KYC
    // =========================================================================
    private fun submitFinalData() {
        if (finalKtpUri == null || finalSelfieUri == null || currentLatitude == null || currentLongitude == null) return

        setGlobalLoading(true)
        lifecycleScope.launch {
            try {
                fun String.asBody() = this.toRequestBody("text/plain".toMediaTypeOrNull())

                val rbNik           = binding.etNik.text.toString().trim().asBody()
                val rbNama          = binding.etNama.text.toString().trim().asBody()
                val rbTtl           = binding.etTtl.text.toString().trim().asBody()
                val rbGender        = binding.etGender.text.toString().trim().asBody()
                val rbGolDarah      = binding.etGolDarah.text.toString().trim().asBody()
                val rbAgama         = binding.etAgama.text.toString().trim().asBody()
                val rbStatus        = binding.etStatus.text.toString().trim().asBody()
                val rbAlamat        = binding.etAlamat.text.toString().trim().asBody()
                val rbRtRw          = binding.etRtRw.text.toString().trim().asBody()
                val rbKelurahan     = binding.etKelurahan.text.toString().trim().asBody()
                val rbKecamatan     = binding.etKecamatan.text.toString().trim().asBody()
                val rbKota          = binding.etKota.text.toString().trim().asBody()
                val rbProvinsi      = binding.etProvinsi.text.toString().trim().asBody()
                val rbPekerjaan     = binding.etPekerjaan.text.toString().trim().asBody()
                val rbKewarganegaraan = binding.etKewarganegaraan.text.toString().trim().asBody()
                val rbBerlaku       = binding.etBerlaku.text.toString().trim().asBody()
                val rbLatitude      = currentLatitude.toString().asBody()
                val rbLongitude     = currentLongitude.toString().asBody()

                val rawKtp  = withContext(Dispatchers.IO) { loadBitmap(finalKtpUri!!) }
                val rotKtp  = withContext(Dispatchers.IO) { rotateIfRequired(rawKtp, finalKtpUri!!) }
                val ktpPart = withContext(Dispatchers.IO) { createMultipart(rotKtp, "ktp_photo") }

                val rawSelfie   = withContext(Dispatchers.IO) { loadBitmap(finalSelfieUri!!) }
                val rotSelfie   = withContext(Dispatchers.IO) { rotateIfRequired(rawSelfie, finalSelfieUri!!) }
                val selfiePart  = withContext(Dispatchers.IO) { createMultipart(rotSelfie, "selfie_photo") }

                val apiService = ApiConfig.getApiService(this@IdentityActivity)
                val response = withContext(Dispatchers.IO) {
                    apiService.submitKyc(
                        rbNik, rbNama, rbTtl, rbGender, rbGolDarah,
                        rbAgama, rbStatus, rbAlamat, rbRtRw, rbKelurahan, rbKecamatan,
                        rbKota, rbProvinsi, rbPekerjaan, rbKewarganegaraan,
                        rbBerlaku, rbLatitude, rbLongitude,
                        ktpPart, selfiePart
                    )
                }

                if (response.status == true) {
                    MaterialAlertDialogBuilder(this@IdentityActivity)
                        .setTitle("Pendaftaran Berhasil")
                        .setMessage("Data Anda telah dikirim dan sedang menunggu verifikasi Admin.")
                        .setPositiveButton("Selesai") { _, _ ->
                            val intent = Intent(this@IdentityActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    throw Exception(response.message ?: "Gagal menyimpan data ke server.")
                }
            } catch (e: Exception) {
                showToast(e.message ?: "Terjadi kesalahan saat mengirim data.")
            } finally {
                setGlobalLoading(false)
            }
        }
    }

    private fun validateFormBeforeSubmit(): Boolean {
        var isValid = true

        if (binding.etNik.text.toString().trim().length != 16) {
            binding.tilNik.error = "NIK harus 16 digit"
            isValid = false
        } else {
            binding.tilNik.error = null
        }

        if (binding.etNama.text.toString().trim().isEmpty()) {
            binding.tilNama.error = "Nama tidak boleh kosong"
            isValid = false
        } else {
            binding.tilNama.error = null
        }

        if (finalKtpUri == null || finalSelfieUri == null || currentLatitude == null || currentLongitude == null) {
            showToast("Harap selesaikan Scan KTP, Selfie, dan Lokasi.")
            isValid = false
        }

        return isValid
    }

    // =========================================================================
    // HELPER: ISI FORM DARI DATA KTP (Menggunakan Kunci DataIdentity)
    // =========================================================================
    private fun fillForm(data: DataIdentity) {
        binding.apply {
            etNik.setText(data.nik.orEmpty())
            etNama.setText(data.fullName.orEmpty())            // ✔️ DIGANTI ke fullName
            etTtl.setText(data.birthPlaceDate.orEmpty())       // ✔️ DIGANTI ke birthPlaceDate
            etGender.setText(data.gender.orEmpty())            // ✔️ DIGANTI ke gender
            etGolDarah.setText(data.bloodType.orEmpty())       // ✔️ DIGANTI ke bloodType
            etAgama.setText(data.religion.orEmpty())           // ✔️ DIGANTI ke religion
            etStatus.setText(data.maritalStatus.orEmpty())     // ✔️ DIGANTI ke maritalStatus
            etAlamat.setText(data.addressKtp.orEmpty())        // ✔️ DIGANTI ke addressKtp
            etRtRw.setText(data.rtRw.orEmpty())
            etKelurahan.setText(data.village.orEmpty())        // ✔️ DIGANTI ke village
            etKecamatan.setText(data.subdistrict.orEmpty())    // ✔️ DIGANTI ke subdistrict
            etKota.setText(data.city.orEmpty())                // ✔️ DIGANTI ke city
            etProvinsi.setText(data.province.orEmpty())        // ✔️ DIGANTI ke province
            etPekerjaan.setText(data.occupation.orEmpty())     // ✔️ DIGANTI ke occupation
            etKewarganegaraan.setText(data.nationality.orEmpty()) // ✔️ DIGANTI ke nationality
            etBerlaku.setText(data.validUntil.orEmpty())       // ✔️ DIGANTI ke validUntil
        }
    }

    private fun setFormEnabled(isEditable: Boolean) {
        binding.apply {
            etNik.isEnabled             = isEditable
            etNama.isEnabled            = isEditable
            etTtl.isEnabled             = isEditable
            etGender.isEnabled          = isEditable
            etGolDarah.isEnabled        = isEditable
            etAgama.isEnabled           = isEditable
            etStatus.isEnabled          = isEditable
            etAlamat.isEnabled          = isEditable
            etRtRw.isEnabled            = isEditable
            etKelurahan.isEnabled       = isEditable
            etKecamatan.isEnabled       = isEditable
            etKota.isEnabled            = isEditable
            etProvinsi.isEnabled        = isEditable
            etPekerjaan.isEnabled       = isEditable
            etKewarganegaraan.isEnabled = isEditable
            etBerlaku.isEnabled         = isEditable
            btnAmbilLokasi.isEnabled    = isEditable
        }
    }

    private fun checkFormValidity() {
        val isReady = finalKtpUri != null && finalSelfieUri != null && currentLatitude != null && currentLongitude != null
        binding.btnSimpan.isEnabled = isReady
        binding.btnSimpan.alpha = if (isReady) 1.0f else 0.5f
    }

    private fun createMultipart(bitmap: Bitmap, paramName: String): MultipartBody.Part {
        val scaled = if (bitmap.width > 1200) {
            Bitmap.createScaledBitmap(bitmap, 1200, (1200 * bitmap.height / bitmap.width), true)
        } else bitmap
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val requestFile = stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(paramName, "photo.jpg", requestFile)
    }

    private fun loadBitmap(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("Gagal membuka gambar")
        return BitmapFactory.decodeStream(inputStream) ?: throw Exception("Gagal mendecode gambar")
    }

    private fun rotateIfRequired(bitmap: Bitmap, uri: Uri): Bitmap {
        return try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val matrix = Matrix()
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90  -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL   -> matrix.postScale(1f, -1f)
                }
                if (matrix.isIdentity) bitmap else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } ?: bitmap
        } catch (e: Exception) { bitmap }
    }

    private fun setKtpLoading(isLoading: Boolean) {
        binding.flKtpLoadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnScanKtp.isEnabled = !isLoading
    }

    private fun setSelfieLoading(isLoading: Boolean) {
        binding.flSelfieLoadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnScanSelfie.isEnabled = !isLoading
    }

    private fun setGlobalLoading(isLoading: Boolean) {
        binding.flGlobalLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSimpan.isEnabled = !isLoading
    }

    private fun showToast(message: String) { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }

    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this).setTitle(title).setMessage(message).setPositiveButton("Mengerti", null).show()
    }
}