package com.id.titikjemputmitra.ui.inbox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.id.titikjemputmitra.data.response.ChatInitResponse
import com.id.titikjemputmitra.data.response.ChatSendResponse
import com.id.titikjemputmitra.data.response.GeneralResponse
import com.id.titikjemputmitra.data.response.MessageItem
import com.id.titikjemputmitra.data.retrofit.ApiService
import com.id.titikjemputmitra.utils.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatRoomViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Kita gunakan MutableList agar bisa menambahkan pesan baru secara instan (real-time)
    private val _messages = MutableLiveData<MutableList<MessageItem>>()
    val messages: LiveData<MutableList<MessageItem>> = _messages

    private val _roomId = MutableLiveData<String>()
    val roomId: LiveData<String> = _roomId

    private val _sendSuccess = MutableLiveData<Event<Boolean>>()
    val sendSuccess: LiveData<Event<Boolean>> = _sendSuccess

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage



    private val _isPartnerTyping = MutableLiveData<Boolean>()
    val isPartnerTyping: LiveData<Boolean> = _isPartnerTyping

    fun setPartnerTyping(typing: Boolean) {
        _isPartnerTyping.postValue(typing)
    }

    fun sendTypingStatus(apiService: ApiService, roomId: String, isTyping: Boolean) {
        // Kita jalankan secara diam-diam tanpa loading
        apiService.sendTypingStatus(roomId, if (isTyping) "1" else "0").enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {}
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {}
        })
    }

    /**
     * Memuat riwayat chat pertama kali (History)
     */
    fun initChat(apiService: ApiService, mitraId: String?, trxCode: String?, roomIdInput: String?) {
        _isLoading.value = true
        apiService.initChat(mitraId, trxCode, roomIdInput).enqueue(object : Callback<ChatInitResponse> {
            override fun onResponse(call: Call<ChatInitResponse>, response: Response<ChatInitResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status) {
                        _roomId.value = body.data?.roomId ?: ""
                        // Konversi List ke MutableList agar bisa di-manipulasi nanti
                        _messages.value = body.data?.messages?.toMutableList() ?: mutableListOf()
                    } else {
                        _toastMessage.value = Event(body?.message ?: "Gagal memuat percakapan.")
                    }
                } else {
                    _toastMessage.value = Event("Error Server: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ChatInitResponse>, t: Throwable) {
                _isLoading.value = false
                _toastMessage.value = Event("Koneksi bermasalah: ${t.message}")
            }
        })
    }

    /**
     * Mengirim pesan ke server
     */
    fun sendMessage(apiService: ApiService, roomId: String, message: String, trxCode: String?) {
        // Saat kirim pesan, kita tidak perlu loading screen (agar UX mulus)
        apiService.sendChatMessage(roomId, message, trxCode).enqueue(object : Callback<ChatSendResponse> {
            override fun onResponse(call: Call<ChatSendResponse>, response: Response<ChatSendResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status) {
                        // Jika berhasil kirim, tambahkan pesan ke dalam list secara lokal
                        body.data?.let { addNewMessage(it) }
                        _sendSuccess.value = Event(true)
                    }
                } else {
                    _toastMessage.value = Event("Gagal mengirim pesan.")
                }
            }

            override fun onFailure(call: Call<ChatSendResponse>, t: Throwable) {
                _toastMessage.value = Event("Gagal terhubung: ${t.message}")
            }
        })
    }

    /**
     * Fungsi helper untuk menambahkan pesan tunggal ke dalam RecyclerView.
     * Digunakan saat: 1. Kita sukses kirim pesan, 2. Pusher menerima pesan dari lawan bicara.
     */
    fun addNewMessage(message: MessageItem) {
        val currentList = _messages.value ?: mutableListOf()
        // Cek dulu apakah message_id sudah ada (untuk mencegah duplikasi jika Pusher & API kirim data sama)
        val isDuplicate = currentList.any { it.messageId == message.messageId && it.messageId != null }

        if (!isDuplicate) {
            currentList.add(message)
            _messages.value = currentList // Trigger observer di Fragment
        }
    }
}