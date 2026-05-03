package com.id.titikjemputmitra.data

import android.content.Context
import com.id.titikjemputmitra.data.response.DataLogin

class UserPreference(context: Context) {
    companion object {
        private const val PREFS_NAME = "titikjemput_mitra_pref"
        private const val USER_ID = "user_id"
        private const val ROLE_ID = "role_id"
        private const val FULLNAME = "fullname"
        private const val EMAIL = "email"
        private const val PHONE = "phone"
        private const val APIKEY = "apikey"
        private const val CIN = "cin"
        private const val IS_ACTIVE = "is_active"
        private const val IDENTITY_STATUS = "identity_status"
        private const val WALLET_BALANCE = "wallet_balance"
        private const val IS_PIN = "is_pin"
        private const val FULL_ADDRESS = "full_address"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"
        private const val IS_PARENT = "is_parent"

        // TAMBAHAN BARU: Key untuk status persetujuan perjanjian
        private const val IS_AGREEMENT_APPROVED = "is_agreement_approved"
    }

    private val preference = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Menyimpan data lengkap setelah login berhasil
     */
    fun setUser(value: DataLogin) {
        val editor = preference.edit()
        editor.putString(USER_ID, value.userId)
        editor.putString(ROLE_ID, value.roleId)
        editor.putString(FULLNAME, value.fullname)
        editor.putString(EMAIL, value.email)
        editor.putString(PHONE, value.phone)
        editor.putString(APIKEY, value.apikey)
        editor.putString(CIN, value.cin)
        editor.putString(IS_ACTIVE, value.isActive)
        editor.putString(IDENTITY_STATUS, value.identityStatus)
        editor.putString(WALLET_BALANCE, value.walletBalance)
        editor.putString(IS_PIN, value.isPin ?: "0")
        editor.putString(FULL_ADDRESS, value.fullAddress)
        editor.putString(LATITUDE, value.latitude)
        editor.putString(LONGITUDE, value.longitude)
        editor.putString(IS_PARENT, value.isParent)

        // Simpan status agreement dari API Login
        editor.putString(IS_AGREEMENT_APPROVED, value.isAgreementApproved ?: "0")
        editor.apply()
    }

    /**
     * Mengambil data user untuk kebutuhan UI atau Header API
     */
    fun getUser(): DataLogin {
        return DataLogin(
            userId = preference.getString(USER_ID, ""),
            roleId = preference.getString(ROLE_ID, ""),
            fullname = preference.getString(FULLNAME, ""),
            email = preference.getString(EMAIL, ""),
            phone = preference.getString(PHONE, ""),
            apikey = preference.getString(APIKEY, ""),
            cin = preference.getString(CIN, ""),
            isActive = preference.getString(IS_ACTIVE, ""),
            identityStatus = preference.getString(IDENTITY_STATUS, null),
            walletBalance = preference.getString(WALLET_BALANCE, "0"),
            isPin = preference.getString(IS_PIN, "0"),
            fullAddress = preference.getString(FULL_ADDRESS, ""),
            latitude = preference.getString(LATITUDE, ""),
            longitude = preference.getString(LONGITUDE, ""),
            isParent = preference.getString(IS_PARENT, ""),
            isAgreementApproved = preference.getString(IS_AGREEMENT_APPROVED, "0")
        )
    }

    /**
     * Update status PIN secara spesifik (digunakan setelah setup PIN berhasil)
     */
    fun setPinStatus(isPinValue: String) {
        val editor = preference.edit()
        editor.putString(IS_PIN, isPinValue)
        editor.apply()
    }

    /**
     * Update status identitas (setelah user upload KTP)
     */
    fun setIdentityStatus(status: String) {
        val editor = preference.edit()
        editor.putString(IDENTITY_STATUS, status)
        editor.apply()
    }

    /**
     * TAMBAHAN BARU: Update status agreement (setelah user klik Setuju di UI Perjanjian)
     */
    fun setAgreementStatus(status: String) {
        val editor = preference.edit()
        editor.putString(IS_AGREEMENT_APPROVED, status)
        editor.apply()
    }

    /**
     * Menghapus semua data saat Logout
     */
    fun clearUser() {
        val editor = preference.edit()
        editor.clear()
        editor.apply()
    }

    /**
     * Mengecek apakah session masih aktif berdasarkan keberadaan API Key
     */
    fun isLoggedIn(): Boolean {
        val token = preference.getString(APIKEY, "")
        return !token.isNullOrEmpty()
    }
}