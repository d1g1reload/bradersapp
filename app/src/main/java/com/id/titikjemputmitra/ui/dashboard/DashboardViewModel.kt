package com.id.titikjemputmitra.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.id.titikjemputmitra.data.response.DataItemProduct
import com.id.titikjemputmitra.data.response.ProductResponse
import com.id.titikjemputmitra.data.retrofit.ApiService
import com.id.titikjemputmitra.utils.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    private val _products = MutableLiveData<List<DataItemProduct>>()
    val products: LiveData<List<DataItemProduct>> = _products

    fun fetchProducts(apiService: ApiService) {
        _isLoading.value = true
        apiService.getProductList().enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == true) {
                        _products.value = body.data ?: emptyList()
                    } else {
                        _message.value = Event(body?.message ?: "Data tidak ditemukan")
                    }
                } else {
                    _message.value = Event("Gagal mengambil data dari server")
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }
}