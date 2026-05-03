package com.id.titikjemputmitra.ui.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.id.titikjemputmitra.data.response.AiDescriptionResponse
import com.id.titikjemputmitra.data.response.BrandResponse
import com.id.titikjemputmitra.data.response.DataItemBrand
import com.id.titikjemputmitra.data.response.DataItemModel
import com.id.titikjemputmitra.data.response.GeneralResponse
import com.id.titikjemputmitra.data.response.ModelResponse
import com.id.titikjemputmitra.data.retrofit.ApiService
import com.id.titikjemputmitra.utils.Event
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProductViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    private val _brands = MutableLiveData<List<DataItemBrand>>()
    val brands: LiveData<List<DataItemBrand>> = _brands

    private val _models = MutableLiveData<List<DataItemModel>>()
    val models: LiveData<List<DataItemModel>> = _models

    private val _isAiLoading = MutableLiveData<Boolean>()
    val isAiLoading: LiveData<Boolean> = _isAiLoading

    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    private val _aiDescription = MutableLiveData<Event<String>>()
    val aiDescription: LiveData<Event<String>> = _aiDescription

    // PERBAIKAN: Penambahan parameter sesuai skema database baru
    fun uploadProduct(
        apiService: ApiService,
        modelId: Int,
        priceDaily: String,
        priceMonthly: String,
        priceYearly: String,
        plate: String,
        year: String,
        transmission: String,
        fuelType: String,
        seatCount: String,
        isDriverIncluded: String,
        isNegotiable: String,
        description: String,
        imageFiles: List<File>
    ) {
        // 1. Nyalakan overlay loading layar penuh
        _isSaving.value = true

        val mediaType = "text/plain".toMediaTypeOrNull()

        // 2. Konversi Teks/Angka menjadi RequestBody
        val rbModelId = modelId.toString().toRequestBody(mediaType)
        val rbPriceDaily = priceDaily.toRequestBody(mediaType)
        val rbPlate = plate.toRequestBody(mediaType)
        val rbYear = year.toRequestBody(mediaType)
        val rbTrans = transmission.toRequestBody(mediaType)
        val rbFuelType = fuelType.toRequestBody(mediaType)
        val rbSeatCount = seatCount.toRequestBody(mediaType)
        val rbIsDriverIncluded = isDriverIncluded.toRequestBody(mediaType)
        val rbIsNegotiable = isNegotiable.toRequestBody(mediaType)
        val rbDesc = description.toRequestBody(mediaType)

        // Penanganan khusus untuk Harga Bulanan & Tahunan (Opsional/Bisa Null)
        val rbPriceMonthly = if (priceMonthly.isNotEmpty()) priceMonthly.toRequestBody(mediaType) else null
        val rbPriceYearly = if (priceYearly.isNotEmpty()) priceYearly.toRequestBody(mediaType) else null

        // 3. Konversi List<File> menjadi List<MultipartBody.Part>
        val parts = imageFiles.map { file ->
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images[]", file.name, requestFile)
        }

        // 4. Eksekusi API
        apiService.createProduct(
            modelId = rbModelId,
            priceDaily = rbPriceDaily,
            priceMonthly = rbPriceMonthly,
            priceYearly = rbPriceYearly,
            plate = rbPlate,
            year = rbYear,
            transmission = rbTrans,
            fuelType = rbFuelType,
            seatCount = rbSeatCount,
            isDriverIncluded = rbIsDriverIncluded,
            isNegotiable = rbIsNegotiable,
            description = rbDesc,
            images = parts
        ).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isSaving.value = false

                if (response.isSuccessful && response.body()?.status == true) {
                    // Gunakan pesan asli dari API ("Armada berhasil ditambahkan.") agar cocok dengan pengecekan di Fragment
                    val successMsg = response.body()?.message ?: "Armada berhasil ditambahkan."
                    _message.value = Event(successMsg)
                } else {
                    _message.value = Event(response.body()?.message ?: "Gagal menyimpan data")
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isSaving.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }

    // --- (Fungsi generateAiDescription, fetchBrands, dan fetchModelsByBrand tidak ada perubahan) ---
    fun generateAiDescription(
        apiService: ApiService,
        brand: String,
        model: String,
        year: String,
        transmission: String
    ) {
        _isAiLoading.value = true

        apiService.generateAiDescription(brand, model, year, transmission).enqueue(object : Callback<AiDescriptionResponse> {
            override fun onResponse(
                call: Call<AiDescriptionResponse>,
                response: Response<AiDescriptionResponse>
            ) {
                _isAiLoading.value = false

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status) {
                        val generatedText = body.data.description
                        _aiDescription.value = Event(generatedText)
                    } else {
                        _message.value = Event(body?.message ?: "Gagal membuat deskripsi")
                    }
                } else {
                    _message.value = Event("Error server: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AiDescriptionResponse>, t: Throwable) {
                _isAiLoading.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }

    fun fetchBrands(apiService: ApiService) {
        _isLoading.value = true
        apiService.getBrands().enqueue(object : Callback<BrandResponse> {
            override fun onResponse(call: Call<BrandResponse>, response: Response<BrandResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _brands.value = response.body()?.data ?: emptyList()
                } else {
                    _message.value = Event(response.body()?.message ?: "Gagal memuat merk")
                }
            }

            override fun onFailure(call: Call<BrandResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }

    fun fetchModelsByBrand(apiService: ApiService, brandId: Int) {
        _isLoading.value = true
        apiService.getModelsByBrand(brandId).enqueue(object : Callback<ModelResponse> {
            override fun onResponse(call: Call<ModelResponse>, response: Response<ModelResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _models.value = response.body()?.data ?: emptyList()
                } else {
                    _message.value = Event(response.body()?.message ?: "Gagal memuat tipe mobil")
                }
            }

            override fun onFailure(call: Call<ModelResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }

    // Tambahkan di dalam class ProductViewModel
    fun deleteProduct(apiService: ApiService, productId: String) {
        _isSaving.value = true // Gunakan overlay loading yang sama dengan saat simpan

        apiService.deleteProduct(productId).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isSaving.value = false
                if (response.isSuccessful && response.body()?.status == true) {
                    _message.value = Event("Produk berhasil dihapus")
                } else {
                    _message.value = Event(response.body()?.message ?: "Gagal menghapus produk")
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isSaving.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }


    fun updateProduct(
        apiService: ApiService,
        productId: String,
        modelId: Int,
        priceDaily: String,
        priceMonthly: String,
        priceYearly: String,
        plate: String,
        year: String,
        transmission: String,
        fuelType: String,
        seatCount: String,
        isDriverIncluded: String,
        isNegotiable: String,
        description: String,
        imageFiles: List<File>
    ) {
        // 1. Nyalakan overlay loading layar penuh
        _isSaving.value = true

        val mediaType = "text/plain".toMediaTypeOrNull()

        // 2. Konversi Teks/Angka menjadi RequestBody
        val rbProductId = productId.toRequestBody(mediaType)
        val rbModelId = modelId.toString().toRequestBody(mediaType)
        val rbPriceDaily = priceDaily.toRequestBody(mediaType)
        val rbPlate = plate.toRequestBody(mediaType)
        val rbYear = year.toRequestBody(mediaType)
        val rbTrans = transmission.toRequestBody(mediaType)
        val rbFuelType = fuelType.toRequestBody(mediaType)
        val rbSeatCount = seatCount.toRequestBody(mediaType)
        val rbIsDriverIncluded = isDriverIncluded.toRequestBody(mediaType)
        val rbIsNegotiable = isNegotiable.toRequestBody(mediaType)
        val rbDesc = description.toRequestBody(mediaType)

        // Penanganan khusus untuk Harga Bulanan & Tahunan (Opsional/Bisa Null)
        val rbPriceMonthly = if (priceMonthly.isNotEmpty()) priceMonthly.toRequestBody(mediaType) else null
        val rbPriceYearly = if (priceYearly.isNotEmpty()) priceYearly.toRequestBody(mediaType) else null

        // 3. Konversi List<File> menjadi List<MultipartBody.Part> (JIKA ADA FOTO BARU)
        val parts = if (imageFiles.isNotEmpty()) {
            imageFiles.map { file ->
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images[]", file.name, requestFile)
            }
        } else {
            emptyList() // Jika kosong, Retrofit tidak akan mengirim form-data "images[]"
        }

        // 4. Eksekusi API Update
        apiService.updateProduct(
            productId = rbProductId,
            modelId = rbModelId,
            priceDaily = rbPriceDaily,
            priceMonthly = rbPriceMonthly,
            priceYearly = rbPriceYearly,
            plate = rbPlate,
            year = rbYear,
            transmission = rbTrans,
            fuelType = rbFuelType,
            seatCount = rbSeatCount,
            isDriverIncluded = rbIsDriverIncluded,
            isNegotiable = rbIsNegotiable,
            description = rbDesc,
            images = parts
        ).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                _isSaving.value = false

                if (response.isSuccessful && response.body()?.status == true) {
                    val successMsg = response.body()?.message ?: "Armada berhasil diperbarui."
                    _message.value = Event(successMsg)
                } else {
                    _message.value = Event(response.body()?.message ?: "Gagal memperbarui data")
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isSaving.value = false
                _message.value = Event("Koneksi gagal: ${t.message}")
            }
        })
    }
}