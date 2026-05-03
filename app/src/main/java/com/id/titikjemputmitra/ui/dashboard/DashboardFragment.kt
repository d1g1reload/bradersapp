package com.id.titikjemputmitra.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.id.titikjemputmitra.data.adapter.ProductAdapter
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupActionListeners()
        setupObservers()

        // Pertama kali buka, load data
        loadData()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            findNavController().navigate(DashboardFragmentDirections.actionNavigationDashboardToNavigationDetailProduct(product))
            Toast.makeText(requireContext(), "Klik: ${product.modelName}", Toast.LENGTH_SHORT).show()
        }

        // Atur agar FAB menyembunyi (hide) saat discroll ke bawah, dan muncul saat scroll ke atas
        binding.rvProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && binding.fabAddProduct.isExtended) {
                    binding.fabAddProduct.shrink() // Jadi bulet kecil
                } else if (dy < 0 && !binding.fabAddProduct.isExtended) {
                    binding.fabAddProduct.extend() // Jadi panjang ada tulisannya
                }
            }
        })

        binding.rvProducts.adapter = productAdapter
    }

    private fun setupActionListeners() {
        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(DashboardFragmentDirections.actionNavigationDashboardToNavigationAddProduct(null))
        }

        // Swipe down to refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }

    private fun loadData() {
        val apiService = ApiConfig.getApiService(requireContext())
        viewModel.fetchProducts(apiService)
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Jika bukan dari swipe refresh, tampilkan progress bar tengah
            if (isLoading && !binding.swipeRefresh.isRefreshing) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false // Matikan putaran refresh
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            // Masukkan data ke adapter (DiffUtil otomatis hitung animasi)
            productAdapter.submitList(products)

            // Atur Empty State
            if (products.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvProducts.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvProducts.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}