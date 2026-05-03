package com.id.titikjemputmitra.ui.product

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.adapter.PhotoPreviewAdapter
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentFormAddBinding
import com.id.titikjemputmitra.utils.getImageUri
import com.id.titikjemputmitra.utils.reduceFileImage
import com.id.titikjemputmitra.utils.uriToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FormAddFragment : Fragment() {
    private var _binding: FragmentFormAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()
    private val args: FormAddFragmentArgs by navArgs()

    private var selectedBrandId: Int? = null
    private var selectedModelId: Int? = null

    private var isEditMode = false
    private var editProductId: String = ""

    private var currentImageUri: Uri? = null
    private val imageFiles = mutableListOf<File>()
    private lateinit var photoAdapter: PhotoPreviewAdapter
    private val MAX_PHOTOS = 5

    private val launcherPermissionCamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) openCamera()
        else Toast.makeText(requireContext(), "Izin kamera diperlukan.", Toast.LENGTH_SHORT).show()
    }

    private val launcherIntentCameraCustom = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentImageUri?.let { uri -> processImage(uri) }
        } else {
            Toast.makeText(requireContext(), "Batal mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPhotoRecyclerView()
        setupStaticDropdowns()
        setupPricingToggles()
        setupActionListeners()
        setupObservers()
        setupFormValidation()

        checkAndPrefillData()

        val apiService = ApiConfig.getApiService(requireContext())
        viewModel.fetchBrands(apiService)
    }

    private fun checkAndPrefillData() {
        val product = args.product
        if (product != null) {
            isEditMode = true
            editProductId = product.productId ?: ""
            selectedModelId = product.modelId?.toIntOrNull()

            binding.tvTitle.text = "Edit Armada"
            binding.btnSave.text = "Simpan Perubahan"
            binding.tvLabelPhoto.text = "Foto Kendaraan (Klik silang untuk menghapus foto lama)"

            // 1. Tampilkan Teks
            binding.apply {
                etPlate.setText(product.licensePlate)
                etYear.setText(product.year)
                etSeat.setText(product.seatCount)
                etPriceDaily.setText(product.priceDaily)
                etDescription.setText(product.description)

                acBrand.setText(product.brandName, false)
                acModel.setText(product.modelName, false)

                val trans = product.transmission?.replaceFirstChar { it.uppercase() }
                acTransmission.setText(trans, false)
                acFuel.setText(product.fuelType, false)

                swMonthly.isChecked = !product.priceMonthly.isNullOrEmpty() && product.priceMonthly != "0"
                if (swMonthly.isChecked) {
                    tilPriceMonthly.visibility = View.VISIBLE
                    etPriceMonthly.setText(product.priceMonthly)
                }

                swYearly.isChecked = !product.priceYearly.isNullOrEmpty() && product.priceYearly != "0"
                if (swYearly.isChecked) {
                    tilPriceYearly.visibility = View.VISIBLE
                    etPriceYearly.setText(product.priceYearly)
                }

                swDriver.isChecked = product.isDriverIncluded == "1"
                swNegotiable.isChecked = product.isNegotiable == "1"
            }

            // 2. Download Foto Lama ke UI
            val oldImages = product.images ?: emptyList()
            if (oldImages.isNotEmpty()) {
                downloadOldImages(oldImages)
            }
        }
    }

    // FUNGSI BARU: Mengubah URL lama menjadi File agar masuk ke RecyclerView
    private fun downloadOldImages(images: List<com.id.titikjemputmitra.data.response.ImagesItem?>) {
        val ctx = context ?: return
        binding.loadingOverlay.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            images.forEach { item ->
                item?.imageUrl?.let { url ->
                    try {
                        val file = Glide.with(ctx)
                            .asFile()
                            .load(url)
                            .submit()
                            .get()

                        val localFile = File(ctx.cacheDir, "old_${System.currentTimeMillis()}_${item.imageId}.jpg")
                        file.copyTo(localFile, true)

                        withContext(Dispatchers.Main) {
                            if (imageFiles.size < MAX_PHOTOS) {
                                imageFiles.add(localFile)
                                photoAdapter.notifyItemInserted(imageFiles.size - 1)
                                updateCameraState()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                binding.loadingOverlay.visibility = View.GONE
                checkFormValid() // Refresh validasi form setelah gambar termuat
            }
        }
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoPreviewAdapter(imageFiles) { file, position ->
            imageFiles.remove(file)
            photoAdapter.notifyItemRemoved(position)
            photoAdapter.notifyItemRangeChanged(position, imageFiles.size)
            updateCameraState()
            checkFormValid()
        }
        binding.rvPhotoPreview.adapter = photoAdapter
    }

    private fun setupPricingToggles() {
        binding.swMonthly.setOnCheckedChangeListener { _, isChecked ->
            binding.tilPriceMonthly.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) binding.etPriceMonthly.text?.clear()
            checkFormValid()
        }

        binding.swYearly.setOnCheckedChangeListener { _, isChecked ->
            binding.tilPriceYearly.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) binding.etPriceYearly.text?.clear()
            checkFormValid()
        }
    }

    private fun setupActionListeners() {
        binding.btnGenerateAI.setOnClickListener {
            val brandName = binding.acBrand.text.toString()
            val modelName = binding.acModel.text.toString()

            if (brandName.isEmpty() || modelName.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih Merk dan Tipe terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val year = binding.etYear.text.toString()
            val transmission = binding.acTransmission.text.toString()

            val apiService = ApiConfig.getApiService(requireContext())
            viewModel.generateAiDescription(apiService, brandName, modelName, year, transmission)
        }

        binding.btnCaptureCamera.setOnClickListener {
            if (imageFiles.size >= MAX_PHOTOS) {
                Toast.makeText(requireContext(), "Maksimal $MAX_PHOTOS foto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                launcherPermissionCamera.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnSave.setOnClickListener {
            val modelId = selectedModelId ?: return@setOnClickListener
            val plate = binding.etPlate.text.toString().trim()
            val year = binding.etYear.text.toString().trim()
            val trans = binding.acTransmission.text.toString().trim()
            val fuel = binding.acFuel.text.toString().trim()
            val seat = binding.etSeat.text.toString().trim()

            val priceDaily = binding.etPriceDaily.text.toString().trim()
            val priceMonthly = if (binding.swMonthly.isChecked) binding.etPriceMonthly.text.toString().trim() else ""
            val priceYearly = if (binding.swYearly.isChecked) binding.etPriceYearly.text.toString().trim() else ""

            val isDriver = if (binding.swDriver.isChecked) "1" else "0"
            val isNego = if (binding.swNegotiable.isChecked) "1" else "0"
            val desc = binding.etDescription.text.toString().trim()

            val apiService = ApiConfig.getApiService(requireContext())

            if (isEditMode) {
                viewModel.updateProduct(
                    apiService = apiService,
                    productId = editProductId,
                    modelId = modelId,
                    priceDaily = priceDaily,
                    priceMonthly = priceMonthly,
                    priceYearly = priceYearly,
                    plate = plate,
                    year = year,
                    transmission = trans,
                    fuelType = fuel,
                    seatCount = seat,
                    isDriverIncluded = isDriver,
                    isNegotiable = isNego,
                    description = desc,
                    imageFiles = imageFiles
                )
            } else {
                viewModel.uploadProduct(
                    apiService = apiService,
                    modelId = modelId,
                    priceDaily = priceDaily,
                    priceMonthly = priceMonthly,
                    priceYearly = priceYearly,
                    plate = plate,
                    year = year,
                    transmission = trans,
                    fuelType = fuel,
                    seatCount = seat,
                    isDriverIncluded = isDriver,
                    isNegotiable = isNego,
                    description = desc,
                    imageFiles = imageFiles
                )
            }
        }
    }

    private fun openCamera() {
        currentImageUri = getImageUri(requireContext())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri)
            putExtra("android.intent.extras.CAMERA_FACING", 0)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", false)
            putExtra("android.intent.extra.USE_BACK_CAMERA", true)
        }
        launcherIntentCameraCustom.launch(intent)
    }

    private fun processImage(uri: Uri) {
        val myFile = uriToFile(uri, requireContext())
        val reducedFile = myFile.reduceFileImage()

        imageFiles.add(reducedFile)
        photoAdapter.notifyItemInserted(imageFiles.size - 1)

        updateCameraState()
        checkFormValid()
    }

    private fun updateCameraState() {
        if (imageFiles.size >= MAX_PHOTOS) {
            binding.btnCaptureCamera.isEnabled = false
            binding.btnCaptureCamera.alpha = 0.5f
        } else {
            binding.btnCaptureCamera.isEnabled = true
            binding.btnCaptureCamera.alpha = 1.0f
        }
    }

    private fun setupStaticDropdowns() {
        val transmissions = listOf("Manual", "Automatic")
        val transAdapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, transmissions)
        binding.acTransmission.setAdapter(transAdapter)

        val fuels = listOf("Bensin", "Diesel", "Listrik/EV", "Hybrid")
        val fuelAdapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, fuels)
        binding.acFuel.setAdapter(fuelAdapter)
    }

    private fun setupFormValidation() {
        binding.etPlate.doOnTextChanged { _, _, _, _ -> checkFormValid() }
        binding.etYear.doOnTextChanged { _, _, _, _ -> checkFormValid() }
        binding.etPriceDaily.doOnTextChanged { _, _, _, _ -> checkFormValid() }
        binding.etSeat.doOnTextChanged { _, _, _, _ -> checkFormValid() }
        binding.acTransmission.setOnItemClickListener { _, _, _, _ -> checkFormValid() }
        binding.acFuel.setOnItemClickListener { _, _, _, _ -> checkFormValid() }
        binding.etPriceMonthly.doOnTextChanged { _, _, _, _ -> checkFormValid() }
        binding.etPriceYearly.doOnTextChanged { _, _, _, _ -> checkFormValid() }
    }

    private fun checkFormValid() {
        val isBrandValid = binding.acBrand.text.toString().isNotEmpty()
        val isModelValid = selectedModelId != null
        val isPlateValid = binding.etPlate.text.toString().trim().isNotEmpty()
        val isYearValid = binding.etYear.text.toString().trim().isNotEmpty()
        val isTransValid = binding.acTransmission.text.toString().trim().isNotEmpty()
        val isFuelValid = binding.acFuel.text.toString().trim().isNotEmpty()
        val isSeatValid = binding.etSeat.text.toString().trim().isNotEmpty()
        val isPriceDailyValid = binding.etPriceDaily.text.toString().trim().isNotEmpty()

        // PERBAIKAN: Foto selalu wajib minimal 1 (karena foto lama sudah di-download ke array)
        val isPhotoValid = imageFiles.isNotEmpty()

        var isMonthlyValid = true
        if (binding.swMonthly.isChecked && binding.etPriceMonthly.text.toString().trim().isEmpty()) {
            isMonthlyValid = false
        }
        var isYearlyValid = true
        if (binding.swYearly.isChecked && binding.etPriceYearly.text.toString().trim().isEmpty()) {
            isYearlyValid = false
        }

        val isAllFilled = isBrandValid && isModelValid && isPlateValid && isYearValid &&
                isTransValid && isFuelValid && isSeatValid && isPriceDailyValid &&
                isPhotoValid && isMonthlyValid && isYearlyValid

        val isAiNotLoading = viewModel.isAiLoading.value != true
        val isDataNotLoading = viewModel.isLoading.value != true
        val isNotSaving = viewModel.isSaving.value != true

        binding.btnSave.isEnabled = isAllFilled && isAiNotLoading && isDataNotLoading && isNotSaving
    }

    private fun setupObservers() {
        viewModel.isSaving.observe(viewLifecycleOwner) { isSaving ->
            binding.loadingOverlay.visibility = if (isSaving) View.VISIBLE else View.GONE
            checkFormValid()
        }

        viewModel.isAiLoading.observe(viewLifecycleOwner) { isAiLoading ->
            binding.btnGenerateAI.isEnabled = !isAiLoading
            binding.btnGenerateAI.text = if (isAiLoading) "Membuat..." else "Buat dengan AI"
            binding.tilDescription.isEnabled = !isAiLoading
            checkFormValid()
        }

        viewModel.aiDescription.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { generatedText ->
                binding.etDescription.setText(generatedText)
                binding.etDescription.setSelection(generatedText.length)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbLoadingModel.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.tvLoadingText.visibility = if (isLoading) View.VISIBLE else View.GONE
            checkFormValid()
        }

        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                if (msg == "Armada berhasil ditambahkan." || msg == "Armada berhasil diperbarui.") {
                    findNavController().popBackStack(R.id.navigation_dashboard, false)
                }
            }
        }

        viewModel.brands.observe(viewLifecycleOwner) { brands ->
            val brandNames = brands.map { it.brandName }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, brandNames)
            binding.acBrand.setAdapter(adapter)

            binding.acBrand.setOnItemClickListener { _, _, position, _ ->
                val selectedBrand = brands[position]
                selectedBrandId = selectedBrand.brandId?.toInt()

                binding.acModel.text = null
                selectedModelId = null
                checkFormValid()

                val apiService = ApiConfig.getApiService(requireContext())
                viewModel.fetchModelsByBrand(apiService, selectedBrand.brandId.toInt())
            }
        }

        viewModel.models.observe(viewLifecycleOwner) { models ->
            val modelNames = models.map { it.modelName }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, modelNames)
            binding.acModel.setAdapter(adapter)

            binding.acModel.isEnabled = true
            binding.tilModel.helperText = null

            binding.acModel.setOnItemClickListener { _, _, position, _ ->
                selectedModelId = models[position].modelId?.toInt()
                checkFormValid()
            }
            checkFormValid()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}