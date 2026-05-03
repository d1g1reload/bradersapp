package com.id.titikjemputmitra.ui.inbox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.id.titikjemputmitra.data.response.ChatListResponse
import com.id.titikjemputmitra.data.response.DataItemChat
import com.id.titikjemputmitra.data.retrofit.ApiService
import com.id.titikjemputmitra.utils.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InboxViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _chatList = MutableLiveData<List<DataItemChat>>()
    val chatList: LiveData<List<DataItemChat>> = _chatList

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

    fun fetchChatList(apiService: ApiService) {
        _isLoading.value = true

        apiService.getChatList().enqueue(object : Callback<ChatListResponse> {
            override fun onResponse(
                call: Call<ChatListResponse>,
                response: Response<ChatListResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status) {
                        // Jika ada data, masukkan ke LiveData. Jika null, kirim list kosong.
                        _chatList.value = body.data ?: emptyList()
                    } else {
                        _toastMessage.value = Event(body?.message ?: "Gagal memuat daftar obrolan.")
                    }
                } else {
                    _toastMessage.value = Event("Gagal menghubungi server. Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ChatListResponse>, t: Throwable) {
                _isLoading.value = false
                _toastMessage.value = Event("Koneksi jaringan bermasalah: ${t.message}")
            }
        })
    }
}