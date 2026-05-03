package com.id.titikjemputmitra.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.id.titikjemputmitra.data.response.BankListResponse
import com.id.titikjemputmitra.data.response.BankResponse
import com.id.titikjemputmitra.data.response.DataBank
import com.id.titikjemputmitra.data.response.DataItemHistory
import com.id.titikjemputmitra.data.response.DataItemOrder
import com.id.titikjemputmitra.data.response.DataItemWalletHistory
import com.id.titikjemputmitra.data.response.GeneralResponse
import com.id.titikjemputmitra.data.response.OrderDetailResponse
import com.id.titikjemputmitra.data.response.OrderHistoryResponse
import com.id.titikjemputmitra.data.response.WalletHistoryResponse
import com.id.titikjemputmitra.data.response.WalletResponse
import com.id.titikjemputmitra.data.retrofit.ApiService
import com.id.titikjemputmitra.utils.Event
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _listOrder = MutableLiveData<List<DataItemHistory>>()
    val listOrder: LiveData<List<DataItemHistory>> = _listOrder

    private val _orderDetail = MutableLiveData<DataItemOrder?>()
    val orderDetail: LiveData<DataItemOrder?> = _orderDetail

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    private val _updateStatusResult = MutableLiveData<Event<Boolean>>()
    val updateStatusResult: LiveData<Event<Boolean>> = _updateStatusResult

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> = _balance

    // LiveData untuk PIN & Withdraw
    private val _isPinLoading = MutableLiveData<Boolean>()
    val isPinLoading: LiveData<Boolean> = _isPinLoading

    private val _pinResult = MutableLiveData<Event<GeneralResponse>>()
    val pinResult: LiveData<Event<GeneralResponse>> = _pinResult

    private val _primaryBank = MutableLiveData<DataBank?>()
    val primaryBank: LiveData<DataBank?> = _primaryBank

    private val _withdrawResult = MutableLiveData<Event<GeneralResponse>>()
    val withdrawResult: LiveData<Event<GeneralResponse>> = _withdrawResult

    private val _listBank = MutableLiveData<List<DataBank>>()
    val listBank: LiveData<List<DataBank>> = _listBank

    private val _walletHistory = MutableLiveData<List<DataItemWalletHistory>>()
    val walletHistory: LiveData<List<DataItemWalletHistory>> = _walletHistory

    fun fetchWalletHistory(apiService: ApiService) {
        _isLoading.value = true

        apiService.getWalletHistory().enqueue(object : Callback<WalletHistoryResponse> {
            override fun onResponse(call: Call<WalletHistoryResponse>, response: Response<WalletHistoryResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _walletHistory.value = response.body()?.data ?: emptyList()
                } else {
                    _walletHistory.value = emptyList()
                    val errorMsg = response.body()?.message ?: "Gagal memuat histori transaksi"
                    _message.value = Event(errorMsg)
                }
            }

            override fun onFailure(call: Call<WalletHistoryResponse>, t: Throwable) {
                _isLoading.value = false
                _walletHistory.value = emptyList()
                _message.value = Event("Koneksi bermasalah: ${t.message}")
            }
        })
    }

    // Tambahkan fungsi ini
    fun fetchListBank(apiService: ApiService) {
        _isLoading.value = true

        // Pastikan getListBank() sudah ada di ApiService Anda
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

    // ============================================================================
    // FUNGSI DOMPET & PENARIKAN (WITHDRAW)
    // ============================================================================

    fun fetchUserBalance(apiService: ApiService) {
        _isLoading.value = true
        apiService.wallet().enqueue(object : Callback<WalletResponse> {
            override fun onResponse(call: Call<WalletResponse>, response: Response<WalletResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _balance.value = (response.body()?.data?.balance ?: 0L).toString()
                } else {
                    _message.value = Event(response.body()?.message ?: "Gagal mengambil saldo")
                }
            }
            override fun onFailure(call: Call<WalletResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi bermasalah: ${t.message}")
            }
        })
    }

    fun fetchPrimaryBank(apiService: ApiService) {
        _isLoading.value = true
        apiService.getPrimaryBank().enqueue(object : Callback<BankResponse> {
            override fun onResponse(call: Call<BankResponse>, response: Response<BankResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _primaryBank.value = response.body()?.data
                } else {
                    _primaryBank.value = null
                }
            }
            override fun onFailure(call: Call<BankResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi bermasalah saat mengambil data rekening.")
                Log.e(TAG, "onFailure fetchPrimaryBank: ${t.message}")
            }
        })
    }

    fun withdrawBalance(apiService: ApiService, amount: String, pin: String, accountId: String) {
        _isLoading.value = true
        // Kirim amount langsung sebagai String sesuai yang diminta oleh ApiService Anda
        apiService.requestWithdraw(amount, pin, accountId).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isLoading.value = false
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _withdrawResult.value = Event(body)
                } else {
                    val errorMsg = parseErrorBody(response.errorBody()?.string(), "Gagal memproses penarikan")
                    _message.value = Event(errorMsg)
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi bermasalah.")
            }
        })
    }

    fun setupPin(apiService: ApiService, pin: String,pinConfirmation: String) {
        _isPinLoading.value = true
        apiService.setupPin(pin,pinConfirmation).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isPinLoading.value = false
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _pinResult.value = Event(body)
                } else {
                    val errorMsg = parseErrorBody(response.errorBody()?.string(), "Gagal menyimpan PIN")
                    _message.value = Event(errorMsg)
                }
            }
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isPinLoading.value = false
                _message.value = Event("Koneksi internet bermasalah: ${t.message}")
                Log.e(TAG, "onFailure Setup PIN: ${t.message}")
            }
        })
    }

    // ============================================================================
    // FUNGSI MANAJEMEN PESANAN (ORDER)
    // ============================================================================

    fun fetchRecentOrders(apiService: ApiService) {
        _isLoading.value = true
        apiService.getRecentOrders("active").enqueue(object : Callback<OrderHistoryResponse> {
            override fun onResponse(call: Call<OrderHistoryResponse>, response: Response<OrderHistoryResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _listOrder.value = response.body()?.data ?: emptyList()
                } else {
                    _listOrder.value = emptyList()
                }
            }
            override fun onFailure(call: Call<OrderHistoryResponse>, t: Throwable) {
                _isLoading.value = false
                _listOrder.value = emptyList()
                _message.value = Event("Kesalahan jaringan: ${t.message}")
            }
        })
    }

    fun fetchOrderDetail(apiService: ApiService, transactionCode: String) {
        _isLoading.value = true
        apiService.getOrderDetail(transactionCode).enqueue(object : Callback<OrderDetailResponse> {
            override fun onResponse(call: Call<OrderDetailResponse>, response: Response<OrderDetailResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _orderDetail.value = response.body()?.data
                } else {
                    _orderDetail.value = null
                    _message.value = Event(response.body()?.message ?: "Data tidak ditemukan.")
                }
            }
            override fun onFailure(call: Call<OrderDetailResponse>, t: Throwable) {
                _isLoading.value = false
                _orderDetail.value = null
                _message.value = Event("Kesalahan jaringan: ${t.message}")
            }
        })
    }

    fun updateOrderStatus(apiService: ApiService, transactionCode: String, newStatus: String, cancelReason: String? = null) {
        _isLoading.value = true
        apiService.updateRentalStatus(transactionCode, newStatus, cancelReason).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _updateStatusResult.value = Event(true)
                    _message.value = Event(response.body()?.message ?: "Status berhasil diubah.")
                } else {
                    val errorMsg = parseErrorBody(response.errorBody()?.string(), "Gagal mengubah status")
                    _updateStatusResult.value = Event(false)
                    _message.value = Event(errorMsg)
                }
            }
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _updateStatusResult.value = Event(false)
                _message.value = Event("Kesalahan jaringan: ${t.message}")
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