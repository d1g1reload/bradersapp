package com.id.titikjemputmitra.ui.inbox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.adapter.InboxAdapter
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentInboxBinding
import com.id.titikjemputmitra.utils.MyFirebase

class InboxFragment : Fragment() {
    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InboxViewModel by viewModels()
    private lateinit var inboxAdapter: InboxAdapter

    // Receiver untuk auto-refresh saat notifikasi chat masuk
    private val inboxUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            fetchData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        fetchData()
        // Daftarkan receiver untuk menangkap sinyal dari MyFirebase service
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(inboxUpdateReceiver, IntentFilter(MyFirebase.ACTION_NEW_CHAT_MESSAGE))
    }

    override fun onPause() {
        super.onPause()
        // Unregister agar tidak memory leak
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(inboxUpdateReceiver)
    }

    private fun setupRecyclerView() {
        inboxAdapter = InboxAdapter { chatItem ->
            val bundle = Bundle().apply {
                putString("EXTRA_ROOM_ID", chatItem.roomId)
                putString("EXTRA_CHAT_TITLE", chatItem.chatTitle)
                putString("EXTRA_CUSTOMER_ID", chatItem.counterpartId)
                putString("EXTRA_TRX_CODE", null) // Bisa diisi jika chat dari detail order
            }
            findNavController().navigate(
                R.id.action_navigation_inbox_to_navigation_chat_room,
                bundle
            )
        }

        binding.rvInbox.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = inboxAdapter
        }
    }

    private fun fetchData() {
        val apiService = ApiConfig.getApiService(requireContext())
        viewModel.fetchChatList(apiService)
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchData()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!binding.swipeRefresh.isRefreshing) {
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
            if (!isLoading) binding.swipeRefresh.isRefreshing = false
        }

        viewModel.chatList.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.rvInbox.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvInbox.visibility = View.VISIBLE
                inboxAdapter.submitList(list)
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}