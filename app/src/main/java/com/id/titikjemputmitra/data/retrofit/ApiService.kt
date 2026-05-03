package com.id.titikjemputmitra.data.retrofit

import com.id.titikjemputmitra.data.response.AgreementResponse
import com.id.titikjemputmitra.data.response.AiDescriptionResponse
import com.id.titikjemputmitra.data.response.BankListResponse
import com.id.titikjemputmitra.data.response.BankResponse
import com.id.titikjemputmitra.data.response.BrandResponse
import com.id.titikjemputmitra.data.response.ChatInitResponse
import com.id.titikjemputmitra.data.response.ChatListResponse
import com.id.titikjemputmitra.data.response.ChatSendResponse
import com.id.titikjemputmitra.data.response.DriverResponse
import com.id.titikjemputmitra.data.response.GeneralResponse
import com.id.titikjemputmitra.data.response.IdentityResponse
import com.id.titikjemputmitra.data.response.LoginResponse
import com.id.titikjemputmitra.data.response.ModelResponse
import com.id.titikjemputmitra.data.response.OrderDetailResponse
import com.id.titikjemputmitra.data.response.OrderHistoryResponse
import com.id.titikjemputmitra.data.response.OtpResponse
import com.id.titikjemputmitra.data.response.ProductResponse
import com.id.titikjemputmitra.data.response.RegisterResponse
import com.id.titikjemputmitra.data.response.UnreadChatResponse
import com.id.titikjemputmitra.data.response.WalletHistoryResponse
import com.id.titikjemputmitra.data.response.WalletResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("user/register")
    fun userRegister(
        @Field("role_id") roleId: String,
        @Field("fullname") fullname: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("phone") phone: String,
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("user/verifyOtp")
    fun userOtpVerify(
        @Field("email") email: String,
        @Field("otp_code") otpCode: String,
    ): Call<OtpResponse>

    @FormUrlEncoded
    @POST("user/resendOtp")
    fun userOtpResend(
        @Field("email") email: String,
    ): Call<RegisterResponse>


    @GET("wallet/history")
    fun getWalletHistory(): Call<WalletHistoryResponse>

    @GET("wallet/bank")
    fun getPrimaryBank(): Call<BankResponse>

    @FormUrlEncoded
    @POST("user/setup/pin")
    fun setupPin(
        @Field("pin") pin: String,
        @Field("pin_confirmation") pinConfirmation: String
    ): Call<GeneralResponse>

    @FormUrlEncoded
    @POST("wallet/add_bank_account")
    fun addBankAccount(
        @Field("bank_name") bankName: String,
        @Field("account_number") accountNumber: String,
        @Field("account_name") accountName: String,
        @Field("is_primary") isPrimary: Int
    ): Call<GeneralResponse>

    @GET("wallet/list_bank")
    fun getListBank(): Call<BankListResponse>

    @FormUrlEncoded
    @POST("wallet/delete_bank")
    fun deleteBank(
        @Field("account_id") accountId: String
    ): Call<GeneralResponse>

    @FormUrlEncoded
    @POST("wallet/withdraw")
    fun requestWithdraw(
        @Field("amount") amount: String,
        @Field("pin") pin: String,
        @Field("account_id") accountId: String
    ): Call<GeneralResponse>

    // 1. Endpoint Publik (Tanpa Token)
    @FormUrlEncoded
    @POST("user/login")
    fun userLogin(
        @Field("identity") email: String,
        @Field("password") password: String,
        @Field("role") role: Int,
    ): Call<LoginResponse>

    @GET("wallet/info")
    fun wallet(): Call<WalletResponse>


    // Tambahkan di dalam ApiService.kt
    @Multipart
    @POST("user/driver/add") // Sesuaikan URL dengan route di CI3 Anda
    fun addDriver(
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("full_name") fullName: RequestBody,
        @Part("birth_place_date") birthPlaceDate: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("blood_type") bloodType: RequestBody?,
        @Part("address_ktp") addressKtp: RequestBody?,
        @Part("rt_rw") rtRw: RequestBody?,
        @Part("village") village: RequestBody?,
        @Part("subdistrict") subdistrict: RequestBody?,
        @Part("city") city: RequestBody?,
        @Part("province") province: RequestBody?,
        @Part("religion") religion: RequestBody?,
        @Part("marital_status") maritalStatus: RequestBody?,
        @Part("occupation") occupation: RequestBody?,
        @Part("nationality") nationality: RequestBody?,
        @Part("valid_until") validUntil: RequestBody?,
        @Part ktp_photo: MultipartBody.Part,
        @Part selfie_photo: MultipartBody.Part
    ): Call<GeneralResponse>

    @GET("user/driver")
    fun getDriverList(
        @Query("search") search: String? = null
    ): Call<DriverResponse>

    @FormUrlEncoded
    @POST("user/driver/delete")
    fun deleteDriver(
        @Field("user_id") userId: String
    ): Call<GeneralResponse>

    @Multipart
    @POST("user/detect/ktp")
    suspend fun scanKtp(
        @Part ktp_image: MultipartBody.Part
    ): IdentityResponse

    @Multipart
    @POST("user/verify/selfie") // Sesuaikan path-nya
    suspend fun verifySelfie(
        @Part selfie_photo: MultipartBody.Part
    ): GeneralResponse

    @Multipart
    @POST("user/verify/submit") // Sesuaikan jika endpoint di CI Anda berbeda
    suspend fun submitKyc(
        @Part("nik") nik: RequestBody,
        @Part("full_name") fullName: RequestBody,
        @Part("birth_place_date") birthPlaceDate: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("blood_type") bloodType: RequestBody,
        @Part("religion") religion: RequestBody,
        @Part("marital_status") maritalStatus: RequestBody,
        @Part("address_ktp") addressKtp: RequestBody,
        @Part("rt_rw") rtRw: RequestBody,
        @Part("village") village: RequestBody,
        @Part("subdistrict") subdistrict: RequestBody,
        @Part("city") city: RequestBody,
        @Part("province") province: RequestBody,
        @Part("occupation") occupation: RequestBody,
        @Part("nationality") nationality: RequestBody,
        @Part("valid_until") validUntil: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part ktpPhoto: MultipartBody.Part,
        @Part selfiePhoto: MultipartBody.Part
    ): GeneralResponse

    @GET("product/list")
    fun getProductList(): Call<ProductResponse>

    @GET("product/brands")
    fun getBrands(): Call<BrandResponse>

    // Ambil daftar model berdasarkan brand_id yang dipilih
    @GET("product/models")
    fun getModelsByBrand(
        @Query("brand_id") brandId: Int
    ): Call<ModelResponse>

    /**
     * Tambah Produk Baru dengan Multiple Images (Multipart)
     */
    @Multipart
    @POST("product/create")
    fun createProduct(
        @Part("model_id") modelId: RequestBody,
        @Part("price_daily") priceDaily: RequestBody, // Menggunakan penamaan baru
        @Part("price_monthly") priceMonthly: RequestBody?, // Bisa null (opsional)
        @Part("price_yearly") priceYearly: RequestBody?,   // Bisa null (opsional)
        @Part("license_plate") plate: RequestBody,
        @Part("year") year: RequestBody,
        @Part("transmission") transmission: RequestBody,
        @Part("fuel_type") fuelType: RequestBody, // Tambahan
        @Part("seat_count") seatCount: RequestBody, // Tambahan
        @Part("is_driver_included") isDriverIncluded: RequestBody, // Tambahan
        @Part("is_negotiable") isNegotiable: RequestBody, // Tambahan
        @Part("description") description: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Call<GeneralResponse>


    @FormUrlEncoded
    @POST("product/delete")
    fun deleteProduct(
        @Field("product_id") productId: String
    ): Call<GeneralResponse>

    @Multipart
    @POST("product/update")
    fun updateProduct(
        @Part("product_id") productId: RequestBody, // WAJIB untuk Edit
        @Part("model_id") modelId: RequestBody,
        @Part("price_daily") priceDaily: RequestBody,
        @Part("price_monthly") priceMonthly: RequestBody?,
        @Part("price_yearly") priceYearly: RequestBody?,
        @Part("license_plate") plate: RequestBody,
        @Part("year") year: RequestBody,
        @Part("transmission") transmission: RequestBody,
        @Part("fuel_type") fuelType: RequestBody,
        @Part("seat_count") seatCount: RequestBody,
        @Part("is_driver_included") isDriverIncluded: RequestBody,
        @Part("is_negotiable") isNegotiable: RequestBody,
        @Part("description") description: RequestBody,
        @Part images: List<MultipartBody.Part> // Kosongkan list ini jika tidak ada foto baru
    ): Call<GeneralResponse>

    @FormUrlEncoded
    @POST("product/generateDesc")
    fun generateAiDescription(
        @Field("brand") brand: String,
        @Field("model") model: String,
        @Field("year") year: String,
        @Field("transmission") transmission: String
    ): Call<AiDescriptionResponse>


    @GET("chat/unread")
    fun getUnreadChatCount(): Call<UnreadChatResponse>

    @GET("rental/history")
    fun getRecentOrders(
        @Query("tab") tab: String = "active"
    ): Call<OrderHistoryResponse>

    @GET("rental/detail/{transaction_code}")
    fun getOrderDetail(
        @Path("transaction_code") transactionCode: String
    ): Call<OrderDetailResponse>

    @GET("user/get_agreement")
    fun getAgreement(): Call<AgreementResponse>

    @FormUrlEncoded
    @POST("user/approve_agreement")
    fun approveAgreement(
        @Field("agreement_id") agreementId: String
    ): Call<GeneralResponse>

    @FormUrlEncoded
    @POST("rental/update/status")
    fun updateRentalStatus(
        @Field("transaction_code") transactionCode: String,
        @Field("new_status") newStatus: String,
        @Field("cancel_reason") cancelReason: String? = null
    ): Call<GeneralResponse>

    @FormUrlEncoded
    @POST("user/fcm")
    fun updateFcmToken(
        @Field("fcm_token") fcmToken: String
    ): Call<GeneralResponse>

    @POST("user/logout")
    fun logoutUser(): Call<GeneralResponse>

    @GET("chat/list")
    fun getChatList(): Call<ChatListResponse>

    // Untuk memuat riwayat chat di awal
    @GET("chat/init")
    fun initChat(
        @Query("mitra_id") mitraId: String?,
        @Query("transaction_code") transactionCode: String?,
        @Query("room_id") roomId: String?
    ): Call<ChatInitResponse>

    // Untuk mengirim pesan baru
    @FormUrlEncoded
    @POST("chat/send")
    fun sendChatMessage(
        @Field("room_id") roomId: String,
        @Field("message") message: String,
        @Field("transaction_code") transactionCode: String?,
        @Field("product_name") productName: String? = null,
        @Field("product_image") productImage: String? = null
    ): Call<ChatSendResponse>

    @FormUrlEncoded
    @POST("chat/typing")
    fun sendTypingStatus(
        @Field("room_id") roomId: String,
        @Field("is_typing") isTyping: String
    ): Call<GeneralResponse>
}