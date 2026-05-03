package com.id.titikjemputmitra.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.id.titikjemputmitra.data.response.RegisterResponse
import com.id.titikjemputmitra.data.retrofit.ApiService
import com.id.titikjemputmitra.utils.Event
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registerResult = MutableLiveData<Event<RegisterResponse>>()
    val registerResult: LiveData<Event<RegisterResponse>> = _registerResult

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    private val _otpVerifyResult = MutableLiveData<Event<com.id.titikjemputmitra.data.response.OtpResponse>>()
    val otpVerifyResult: LiveData<Event<com.id.titikjemputmitra.data.response.OtpResponse>> = _otpVerifyResult

    private val _resendResult = MutableLiveData<Event<RegisterResponse>>()
    val resendResult: LiveData<Event<RegisterResponse>> = _resendResult

    fun verifyOtp(apiService: ApiService, email: String, otpCode: String) {
        _isLoading.value = true

        apiService.userOtpVerify(email, otpCode).enqueue(object : Callback<com.id.titikjemputmitra.data.response.OtpResponse> {
            override fun onResponse(call: Call<com.id.titikjemputmitra.data.response.OtpResponse>, response: Response<com.id.titikjemputmitra.data.response.OtpResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _otpVerifyResult.value = Event(response.body()!!)
                } else {
                    val errorMsg = parseErrorBody(response.errorBody()?.string(), "Verifikasi gagal, periksa kembali kode OTP Anda.")
                    _message.value = Event(errorMsg)
                }
            }

            override fun onFailure(call: Call<com.id.titikjemputmitra.data.response.OtpResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }

    fun resendOtp(apiService: ApiService, email: String) {
        _isLoading.value = true

        apiService.userOtpResend(email).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _resendResult.value = Event(response.body()!!)
                } else {
                    val errorMsg = parseErrorBody(response.errorBody()?.string(), "Gagal mengirim ulang OTP.")
                    _message.value = Event(errorMsg)
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }

    fun registerUser(apiService: ApiService, role: String, fullname: String, email: String, pass: String, phone: String) {
        _isLoading.value = true

        apiService.userRegister(role, fullname, email, pass, phone).enqueue(object :
            Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                _isLoading.value = false

                if (response.isSuccessful && response.body() != null) {
                    _registerResult.value = Event(response.body()!!)
                } else {
                    val errorMsg = parseErrorBody(response.errorBody()?.string(), "Registrasi gagal, silakan coba lagi.")
                    _message.value = Event(errorMsg)
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }

    // HELPER: Standar Titik Jemput untuk membedah JSON Error dari CI3
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