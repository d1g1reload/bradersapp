package com.id.titikjemputmitra.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.id.titikjemputmitra.data.response.BankListResponse
import com.id.titikjemputmitra.data.response.DataBank
import com.id.titikjemputmitra.data.response.GeneralResponse
import com.id.titikjemputmitra.data.retrofit.ApiService
import com.id.titikjemputmitra.utils.Event
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _logoutStatus = MutableLiveData<Event<Boolean>>()
    val logoutStatus: LiveData<Event<Boolean>> = _logoutStatus

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    private val _addBankResult = MutableLiveData<Event<GeneralResponse>>()
    val addBankResult: LiveData<Event<GeneralResponse>> = _addBankResult

    private val _listBank = MutableLiveData<List<DataBank>>()
    val listBank: LiveData<List<DataBank>> = _listBank

    private val _deleteBankResult = MutableLiveData<Event<GeneralResponse>>()
    val deleteBankResult: LiveData<Event<GeneralResponse>> = _deleteBankResult

    fun getListBank(apiService: ApiService) {
        _isLoading.value = true

        apiService.getListBank().enqueue(object : Callback<BankListResponse> {
            override fun onResponse(call: Call<BankListResponse>, response: Response<BankListResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _listBank.value = response.body()?.data ?: emptyList()
                } else {
                    _listBank.value = emptyList()
                }
            }

            override fun onFailure(call: Call<BankListResponse>, t: Throwable) {
                _isLoading.value = false
                _listBank.value = emptyList()
                _message.value = Event("Koneksi bermasalah saat memuat daftar rekening.")
            }
        })
    }

    fun deleteBank(apiService: ApiService, accountId: String) {
        _isLoading.value = true

        apiService.deleteBank(accountId).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _deleteBankResult.value = Event(response.body()!!)
                } else {
                    val errorMsg = parseErrorBody(response.errorBody()?.string(), "Gagal menghapus rekening")
                    _message.value = Event(errorMsg)
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi bermasalah saat menghapus rekening.")
            }
        })
    }

    fun addBankAccount(apiService: ApiService, bankName: String, accountNumber: String, accountName: String, isPrimary: Int) {
        _isLoading.value = true

        apiService.addBankAccount(bankName, accountNumber, accountName, isPrimary).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _addBankResult.value = Event(response.body()!!)
                } else {
                    val errorMsg = parseErrorBody(response.errorBody()?.string(), "Gagal menambahkan rekening")
                    _message.value = Event(errorMsg)
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi bermasalah: ${t.message}")
            }
        })
    }

    fun logout(apiService: ApiService) {
        _isLoading.value = true

        apiService.logoutUser().enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isLoading.value = false
                // Kita tetap menganggap logout berhasil secara lokal meskipun API gagal (best practice)
                _logoutStatus.value = Event(true)
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                // Tetap pindah ke login jika koneksi gagal agar user tidak terjebak
                _logoutStatus.value = Event(true)
                _message.value = Event("Logout API gagal, sesi lokal dibersihkan.")
            }
        })
    }

    // HELPER: Untuk membaca pesan error dari JSON Backend CI3
    private fun parseErrorBody(errorString: String?, fallback: String): String {
        return try {
            if (!errorString.isNullOrEmpty()) {
                val jsonObject = JSONObject(errorString)
                jsonObject.getString("message")
            } else fallback
        } catch (e: Exception) {
            fallback
        }
    }
}