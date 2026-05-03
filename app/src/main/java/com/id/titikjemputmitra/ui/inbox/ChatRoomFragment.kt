package com.id.titikjemputmitra.ui.inbox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.id.titikjemputmitra.data.UserPreference
import com.id.titikjemputmitra.data.adapter.ChatRoomAdapter
import com.id.titikjemputmitra.data.response.MessageItem
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.FragmentChatRoomBinding
import com.id.titikjemputmitra.utils.MyFirebase
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

class ChatRoomFragment : Fragment() {

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatRoomViewModel by viewModels()
    private lateinit var adapter: ChatRoomAdapter
    private lateinit var userPref: UserPreference

    private var pusher: Pusher? = null
    private var currentRoomId: String? = null

    private var typingTimer: Timer? = null
    private var isCurrentlyTyping = false

    // Sinyal FCM untuk refresh jika ada pesan masuk saat di dalam room (sebagai fallback Pusher)
    private val chatUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val roomId = intent?.getStringExtra("room_id")
            if (roomId == currentRoomId) {
                // Opsional: Bisa trigger initChat lagi atau biarkan Pusher bekerja
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPref = UserPreference(requireContext())

        // 1. Tangkap Argumen dari Inbox atau Detail Order
        val roomId = arguments?.getString("EXTRA_ROOM_ID")
        val customerId = arguments?.getString("EXTRA_CUSTOMER_ID")
        val chatTitle = arguments?.getString("EXTRA_CHAT_TITLE") ?: "Pelanggan"
        val trxCode = arguments?.getString("EXTRA_TRX_CODE")

        binding.tvChatTitle.text = chatTitle

        setupRecyclerView()
        setupObservers()
        setupListeners(trxCode)
        setupTypingListener()

        // 2. Load History Chat
        val apiService = ApiConfig.getApiService(requireContext())
        viewModel.initChat(apiService, customerId, trxCode, roomId)
    }

    private fun setupRecyclerView() {
        val myUserId = userPref.getUser().userId ?: ""
        adapter = ChatRoomAdapter(myUserId)

        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true // Pesan mulai dari bawah
            }
            adapter = this@ChatRoomFragment.adapter
        }
    }

    private fun setupTypingListener() {
        binding.edtMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Kirim status "mengetik" hanya jika sebelumnya statusnya "diam"
                if (!isCurrentlyTyping) {
                    isCurrentlyTyping = true
                    sendTyping(true)
                }

                // Reset timer setiap kali ada karakter baru
                typingTimer?.cancel()
                typingTimer = Timer()
                typingTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        isCurrentlyTyping = false
                        sendTyping(false)
                    }
                }, 2000)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun sendTyping(isTyping: Boolean) {
        val roomId = currentRoomId ?: viewModel.roomId.value ?: return
        val apiService = ApiConfig.getApiService(requireContext())
        viewModel.sendTypingStatus(apiService, roomId, isTyping)
    }

    private fun setupListeners(trxCode: String?) {

        binding.btnSendMessage.setOnClickListener {
            val message = binding.edtMessage.text.toString().trim()
            val roomId = viewModel.roomId.value

            if (message.isNotEmpty() && roomId != null) {
                val apiService = ApiConfig.getApiService(requireContext())
                viewModel.sendMessage(apiService, roomId, message, trxCode)
                binding.edtMessage.setText("")
            }
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.messages.observe(viewLifecycleOwner) { list ->
            // Update list dan scroll ke paling bawah
            adapter.submitList(list.toList()) {
                binding.rvChat.scrollToPosition(adapter.itemCount - 1)
            }
        }

        viewModel.roomId.observe(viewLifecycleOwner) { roomId ->
            if (!roomId.isNullOrEmpty()) {
                currentRoomId = roomId
                subscribePusher(roomId)
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isPartnerTyping.observe(viewLifecycleOwner) { isTyping ->
            if (isTyping) {
                binding.tvChatStatus.text = "sedang mengetik..."
                binding.tvChatStatus.visibility = View.VISIBLE
            } else {
                binding.tvChatStatus.visibility = View.GONE
            }
        }
    }

    private fun subscribePusher(roomId: String) {
        if (pusher != null) return

        val options = PusherOptions().apply { setCluster("ap1") }
        pusher = Pusher("3a767a1e4355220af822", options)

        pusher?.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.d("PusherChat", "State: ${change.currentState}")
            }
            override fun onError(message: String, code: String?, e: Exception?) {
                Log.e("PusherChat", "Error: $message")
            }
        }, ConnectionState.ALL)

        val channel = pusher?.subscribe(roomId)

        // 1. Event Pesan Baru
        channel?.bind("new-message") { event ->
            try {
                val json = JSONObject(event.data)
                val senderIdFromSocket = json.getString("sender_id")

                if (senderIdFromSocket != userPref.getUser().userId) {
                    val newMessage = MessageItem(
                        messageId = json.optString("message_id"),
                        senderType = json.getString("sender_type"),
                        senderId = senderIdFromSocket,
                        messageText = json.getString("message_text"),
                        createdAt = json.getString("created_at")
                    )

                    activity?.runOnUiThread {
                        viewModel.addNewMessage(newMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e("PusherChat", "JSON Error: ${e.message}")
            }
        }

        // 2. Event Sedang Mengetik (BARU)
        channel?.bind("typing-event") { event ->
            try {
                val json = JSONObject(event.data)
                val senderIdFromSocket = json.getString("sender_id")
                val isTyping = json.optString("is_typing") == "1"

                // Jika yang mengetik adalah lawan bicara, update UI
                if (senderIdFromSocket != userPref.getUser().userId) {
                    activity?.runOnUiThread {
                        viewModel.setPartnerTyping(isTyping)
                    }
                }
            } catch (e: Exception) {
                Log.e("PusherChat", "Typing Event Error: ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(chatUpdateReceiver, IntentFilter(MyFirebase.ACTION_NEW_CHAT_MESSAGE))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(chatUpdateReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val intent = Intent(MyFirebase.ACTION_NEW_CHAT_MESSAGE)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        pusher?.disconnect()
        _binding = null
    }
}