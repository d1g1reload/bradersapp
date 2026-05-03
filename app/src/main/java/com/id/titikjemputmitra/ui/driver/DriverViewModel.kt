package com.id.titikjemputmitra.ui.driver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.id.titikjemputmitra.data.response.DataIdentity
import com.id.titikjemputmitra.data.response.DataItemDriver
import com.id.titikjemputmitra.data.response.DriverResponse
import com.id.titikjemputmitra.data.response.GeneralResponse
import com.id.titikjemputmitra.data.retrofit.ApiService
import com.id.titikjemputmitra.utils.Event
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class DriverViewModel : ViewModel() {

    private val _isLoadingKtp = MutableLiveData<Boolean>()
    val isLoadingKtp: LiveData<Boolean> = _isLoadingKtp

    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    // Loading untuk list driver
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    private val _ktpData = MutableLiveData<DataIdentity?>()
    val ktpData: LiveData<DataIdentity?> = _ktpData

    // Data List Driver
    private val _listDriver = MutableLiveData<List<DataItemDriver>>()
    val listDriver: LiveData<List<DataItemDriver>> = _listDriver

    /**
     * TAHAP 1: Kirim Foto KTP ke AI untuk di-ekstrak teksnya
     */
    fun scanKtp(apiService: ApiService, ktpFile: File) {
        _isLoadingKtp.value = true

        val requestImageFile = ktpFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData(
            "ktp_image",
            ktpFile.name,
            requestImageFile
        )

        viewModelScope.launch {
            try {
                val response = apiService.scanKtp(multipartBody)
                _isLoadingKtp.value = false

                // CEK: Status harus true, data tidak boleh null, dan NIK harus terbaca (tidak kosong)
                if (response.status == true && response.data != null && !response.data.nik.isNullOrEmpty()) {
                    _ktpData.value = response.data
                    _message.value = Event("KTP berhasil diekstrak.")
                } else {
                    // Jika data null atau NIK gagal terbaca, anggap gagal
                    _ktpData.value = null
                    _message.value = Event("AI gagal membaca KTP dengan jelas. Pastikan foto tidak silau dan ulangi kembali.")
                }
            } catch (e: Exception) {
                _isLoadingKtp.value = false
                _ktpData.value = null
                _message.value = Event("Gagal menghubungkan ke AI. Periksa koneksi internet Anda.")
            }
        }
    }

    /**
     * TAHAP 2: Simpan seluruh data Driver ke Database
     */
    fun addDriver(
        apiService: ApiService,
        email: String,
        phone: String,
        nik: String,
        fullName: String,
        birthPlaceDate: String,
        gender: String,
        bloodType: String, // ✅ DATA BARU
        addressKtp: String,
        rtRw: String,
        village: String,
        subdistrict: String,
        city: String,
        province: String,
        religion: String,
        maritalStatus: String,
        occupation: String,
        nationality: String,
        validUntil: String,
        ktpFile: File,
        selfieFile: File // ✅ DATA BARU
    ) {
        _isSaving.value = true
        val textType = "text/plain".toMediaTypeOrNull()

        // Siapkan KTP Part
        val requestKtpFile = ktpFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val ktpPart = MultipartBody.Part.createFormData("ktp_photo", ktpFile.name, requestKtpFile)

        // Siapkan Selfie Part
        val requestSelfieFile = selfieFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val selfiePart = MultipartBody.Part.createFormData("selfie_photo", selfieFile.name, requestSelfieFile)

        apiService.addDriver(
            email.toRequestBody(textType),
            phone.toRequestBody(textType),
            nik.toRequestBody(textType),
            fullName.toRequestBody(textType),
            birthPlaceDate.toRequestBody(textType),
            gender.toRequestBody(textType),
            bloodType.toRequestBody(textType), // ✅ MAPPING DATA BARU
            addressKtp.toRequestBody(textType),
            rtRw.toRequestBody(textType),
            village.toRequestBody(textType),
            subdistrict.toRequestBody(textType),
            city.toRequestBody(textType),
            province.toRequestBody(textType),
            religion.toRequestBody(textType),
            maritalStatus.toRequestBody(textType),
            occupation.toRequestBody(textType),
            nationality.toRequestBody(textType),
            validUntil.toRequestBody(textType),
            ktpPart,
            selfiePart
        ).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isSaving.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _message.value = Event(response.body()?.message ?: "Driver berhasil ditambahkan.")
                } else {
                    _message.value = Event(response.body()?.message ?: "Gagal mendaftarkan driver.")
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isSaving.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }

    /**
     * TAHAP 3: Ambil List Driver dari Server
     */
    fun getDrivers(apiService: ApiService) {
        _isLoading.value = true
        apiService.getDriverList().enqueue(object : Callback<DriverResponse> {
            override fun onResponse(call: Call<DriverResponse>, response: Response<DriverResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _listDriver.value = response.body()?.data ?: emptyList()
                } else {
                    _listDriver.value = emptyList()
                    _message.value = Event(response.body()?.message ?: "Gagal memuat daftar driver.")
                }
            }

            override fun onFailure(call: Call<DriverResponse>, t: Throwable) {
                _isLoading.value = false
                _listDriver.value = emptyList()
                _message.value = Event("Terjadi kesalahan jaringan.")
            }
        })
    }

    fun deleteDriver(apiService: ApiService, userId: String) {
        _isLoading.value = true
        apiService.deleteDriver(userId).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _message.value = Event(response.body()?.message ?: "Driver dihapus.")
                    // Refresh list setelah hapus
                    getDrivers(apiService)
                } else {
                    _message.value = Event(response.body()?.message ?: "Gagal menghapus.")
                }
            }
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi gagal.")
            }
        })
    }
}