package com.id.titikjemputmitra.ui.driver

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.id.titikjemputmitra.data.response.DataIdentity
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentFormAddDriverBinding
import com.id.titikjemputmitra.utils.getImageUri
import com.id.titikjemputmitra.utils.reduceFileImage
import com.id.titikjemputmitra.utils.uriToFile
import java.io.File

class FormAddDriverFragment : Fragment() {

    private var _binding: FragmentFormAddDriverBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DriverViewModel by viewModels()

    private var tempImageUri: Uri? = null
    private var ktpFile: File? = null
    private var selfieFile: File? = null // ✅ TAMBAHAN: File untuk Selfie
    private var isScanningKtp = true     // ✅ TAMBAHAN: Flag penentu kamera

    // 1 Launcher Kamera untuk 2 Kebutuhan (KTP & Selfie)
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess && tempImageUri != null) {
            if (isScanningKtp) {
                processKtpImage(tempImageUri!!)
            } else {
                processSelfieImage(tempImageUri!!)
            }
        } else {
            Toast.makeText(requireContext(), "Batal mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Izin Kamera wajib diaktifkan.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormAddDriverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionListeners()
        setupObservers()
        setupFormValidation()
    }

    private fun setupActionListeners() {
        binding.btnScanKtp.setOnClickListener {
            isScanningKtp = true
            checkCameraPermissionAndOpen()
        }

        // ✅ TAMBAHAN: Listener untuk tombol Selfie
        binding.btnScanSelfie.setOnClickListener {
            isScanningKtp = false
            checkCameraPermissionAndOpen()
        }

        binding.btnSimpan.setOnClickListener {
            submitDriverData()
        }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Izin Diperlukan")
                .setMessage("Aplikasi membutuhkan akses Kamera untuk mengambil foto Driver.")
                .setPositiveButton("Lanjut") { _, _ ->
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun openCamera() {
        tempImageUri = getImageUri(requireContext())
        tempImageUri?.let { uri -> takePictureLauncher.launch(uri) }
    }

    private fun processKtpImage(uri: Uri) {
        Glide.with(this).load(uri).into(binding.ivKtpPreview)
        binding.flScanGuide.visibility = View.GONE

        val myFile = uriToFile(uri, requireContext())
        ktpFile = myFile.reduceFileImage()

        val apiService = ApiConfig.getApiService(requireContext())
        ktpFile?.let { file ->
            viewModel.scanKtp(apiService, file)
        }
    }

    // ✅ TAMBAHAN: Proses render dan kompresi Selfie
    private fun processSelfieImage(uri: Uri) {
        Glide.with(this).load(uri).into(binding.ivSelfiePreview)
        binding.layoutSelfiePlaceholder.visibility = View.GONE
        binding.btnScanSelfie.text = "UBAH FOTO SELFIE"

        val myFile = uriToFile(uri, requireContext())
        selfieFile = myFile.reduceFileImage()

        checkFormValid() // Perbarui status tombol simpan
    }

    // --- PERBAIKAN: Mapping pengiriman data yang akurat ---
    private fun submitDriverData() {
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val nik = binding.etNik.text.toString().trim()
        val nama = binding.etNama.text.toString().trim()

        if (ktpFile == null || selfieFile == null) {
            Toast.makeText(requireContext(), "Foto KTP dan Selfie wajib ada!", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = ApiConfig.getApiService(requireContext())

        // ⚠️ PENTING: Anda harus memperbarui parameter addDriver di DriverViewModel.kt Anda
        // agar menerima `selfieFile`, `gender`, `bloodType` dan menghapus `password`.
        viewModel.addDriver(
            apiService = apiService,
            email = email,
            phone = phone,
            nik = nik,
            fullName = nama,
            birthPlaceDate = binding.etTtl.text.toString().trim(),
            gender = binding.etGender.text.toString().trim(),
            bloodType = binding.etGolDarah.text.toString().trim(),
            addressKtp = binding.etAlamat.text.toString().trim(),
            rtRw = binding.etRtRw.text.toString().trim(),
            village = binding.etKelurahan.text.toString().trim(),
            subdistrict = binding.etKecamatan.text.toString().trim(),
            city = binding.etKota.text.toString().trim(),
            province = binding.etProvinsi.text.toString().trim(),
            religion = binding.etAgama.text.toString().trim(),
            maritalStatus = binding.etStatus.text.toString().trim(),
            occupation = binding.etPekerjaan.text.toString().trim(),
            nationality = binding.etKewarganegaraan.text.toString().trim(),
            validUntil = binding.etBerlaku.text.toString().trim(),
            ktpFile = ktpFile!!,
            selfieFile = selfieFile!!
        )
    }

    private fun setupObservers() {
        viewModel.isLoadingKtp.observe(viewLifecycleOwner) { isLoading ->
            binding.flKtpLoadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnScanKtp.isEnabled = !isLoading
            checkFormValid()
        }

        viewModel.isSaving.observe(viewLifecycleOwner) { isSaving ->
            binding.flGlobalLoading.visibility = if (isSaving) View.VISIBLE else View.GONE
            checkFormValid()
        }

        viewModel.ktpData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                fillForm(data)
            } else {
                binding.ivKtpPreview.setImageDrawable(null)
                binding.flScanGuide.visibility = View.VISIBLE
                ktpFile = null
                clearForm()
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                // ✅ PERBAIKAN: Hanya pindah halaman jika kata kuncinya "didaftarkan" atau "ditambahkan"
                // Jangan gunakan kata "berhasil" karena bertabrakan dengan KTP
                if (msg.contains("didaftarkan", ignoreCase = true) || msg.contains("ditambahkan", ignoreCase = true)) {
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun fillForm(data: DataIdentity) {
        binding.apply {
            etNik.setText(data.nik ?: "")
            etNama.setText(data.fullName ?: "")
            etTtl.setText(data.birthPlaceDate ?: "")
            etGender.setText(data.gender ?: "")             // ✅ BARU
            etGolDarah.setText(data.bloodType ?: "")        // ✅ BARU
            etAgama.setText(data.religion ?: "")
            etStatus.setText(data.maritalStatus ?: "")
            etAlamat.setText(data.addressKtp ?: "")
            etRtRw.setText(data.rtRw ?: "")
            etKelurahan.setText(data.village ?: "")
            etKecamatan.setText(data.subdistrict ?: "")
            etKota.setText(data.city ?: "")
            etProvinsi.setText(data.province ?: "")
            etPekerjaan.setText(data.occupation ?: "")
            etKewarganegaraan.setText(data.nationality ?: "")
            etBerlaku.setText(data.validUntil ?: "")
        }
        checkFormValid()
    }

    private fun setupFormValidation() {
        binding.etNik.doOnTextChanged { _, _, _, _ -> checkFormValid() }
        binding.etNama.doOnTextChanged { _, _, _, _ -> checkFormValid() }
        binding.etEmail.doOnTextChanged { _, _, _, _ -> checkFormValid() }
        binding.etPhone.doOnTextChanged { _, _, _, _ -> checkFormValid() }
        // ❌ Validasi etPassword dihapus
    }

    private fun checkFormValid() {
        val isNikValid = binding.etNik.text.toString().trim().isNotEmpty()
        val isNamaValid = binding.etNama.text.toString().trim().isNotEmpty()
        val isEmailValid = binding.etEmail.text.toString().trim().isNotEmpty()
        val isPhoneValid = binding.etPhone.text.toString().trim().isNotEmpty()

        val isFotoAda = ktpFile != null && selfieFile != null // ✅ Wajib ada 2 foto

        val isNotLoadingKtp = viewModel.isLoadingKtp.value != true
        val isNotSaving = viewModel.isSaving.value != true

        binding.btnSimpan.isEnabled = isNikValid && isNamaValid && isEmailValid &&
                isPhoneValid && isFotoAda && isNotLoadingKtp && isNotSaving
    }

    private fun clearForm() {
        binding.apply {
            etNik.text = null; etNama.text = null; etTtl.text = null; etAgama.text = null
            etStatus.text = null; etAlamat.text = null; etRtRw.text = null; etKelurahan.text = null
            etKecamatan.text = null; etKota.text = null; etProvinsi.text = null; etPekerjaan.text = null
            etKewarganegaraan.text = null; etBerlaku.text = null
            etGender.text = null; etGolDarah.text = null
        }
        checkFormValid()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}