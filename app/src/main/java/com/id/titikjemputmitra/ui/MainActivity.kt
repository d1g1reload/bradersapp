package com.id.titikjemputmitra.ui

import android.Manifest
import android.content.BroadcastReceiver // TAMBAHKAN INI
import android.content.Context // TAMBAHKAN INI
import android.content.Intent
import android.content.IntentFilter // TAMBAHKAN INI
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager // TAMBAHKAN INI
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import com.id.titikjemputmitra.R
import com.id.titikjemputmitra.data.UserPreference
import com.id.titikjemputmitra.data.response.GeneralResponse
import com.id.titikjemputmitra.data.response.UnreadChatResponse
import com.id.titikjemputmitra.data.retrofit.ApiConfig
import com.id.titikjemputmitra.databinding.ActivityMainBinding
import com.id.titikjemputmitra.ui.agreement.AgreementActivity
import com.id.titikjemputmitra.ui.auth.IdentityActivity
import com.id.titikjemputmitra.ui.auth.LoginActivity
import com.id.titikjemputmitra.utils.MyFirebase // Pastikan import ini benar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userPref: UserPreference

    // ✅ RECEIVER: Mendengarkan sinyal dari MyFirebase service
    private val chatUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("MainActivity", "Sinyal chat baru diterima. Refresh badge...")
            fetchUnreadChatCount()
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notifikasi aktif.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPref = UserPreference(this)
        val user = userPref.getUser()

        // --- SECURITY GATES ---
        if (user.userId.isNullOrEmpty()) {
            redirectToLogin("Sesi berakhir. Silakan login kembali.")
            return
        }

        // ... (Logika Gate KTP & Agreement tetap sama) ...
        val statusKtp = user.identityStatus.toString()
        if (statusKtp == "0") {
            showVerificationWaitingDialog()
            return
        } else if (statusKtp != "1") {
            redirectToIdentity()
            return
        }

        val isAgreementApproved = user.isAgreementApproved.toString()
        if (isAgreementApproved != "1") {
            redirectToAgreement()
            return
        }

        // RENDER UI
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationPermission()
        setupBottomNavigation()
        getAndSendFCMToken()
    }

    override fun onResume() {
        super.onResume()
        if (userPref.isLoggedIn() && ::binding.isInitialized) {
            fetchUnreadChatCount()
        }

        // ✅ DAFTARKAN RECEIVER
        LocalBroadcastManager.getInstance(this).registerReceiver(
            chatUpdateReceiver,
            IntentFilter(MyFirebase.ACTION_NEW_CHAT_MESSAGE)
        )
    }

    override fun onPause() {
        super.onPause()
        // ✅ LEPAS RECEIVER agar tidak memory leak
        LocalBroadcastManager.getInstance(this).unregisterReceiver(chatUpdateReceiver)
    }

    // --- SETUP NAVIGASI & NOTIFIKASI ---

    private fun setupBottomNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_product_detail,
                R.id.navigation_add_product,
                R.id.navigation_order_detail,
                R.id.navigation_withdraw,
                R.id.navigation_pin,
                R.id.navigation_bank_account,
                R.id.navigation_form_bank,
                R.id.navigation_transaction_history,
                R.id.navigation_chat_room -> {
                    navView.visibility = View.GONE
                }
                else -> {
                    navView.visibility = View.VISIBLE
                }
            }
        }

        navView.setOnItemSelectedListener { item ->
            val navOptions = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.graph.findStartDestination().id, inclusive = false, saveState = true)
                .build()

            when (item.itemId) {
                R.id.navigation_inbox -> { // Sesuaikan ID menu Inbox Anda
                    // 1. Hilangkan badge secara instan untuk UX yang responsif
                    val badge = binding.navView.getBadge(item.itemId)
                    badge?.isVisible = false

                    // 2. Jalankan navigasi
                    navController.navigate(item.itemId, null, navOptions)

                    // 3. (Opsional) Panggil API setelah jeda singkat untuk memastikan sync
                    binding.root.postDelayed({ fetchUnreadChatCount() }, 1000)
                    true
                }
                else -> {
                    navController.navigate(item.itemId, null, navOptions)
                    true
                }
            }
        }
    }

    private fun fetchUnreadChatCount() {
        ApiConfig.getApiService(this).getUnreadChatCount().enqueue(object : Callback<UnreadChatResponse> {
            override fun onResponse(call: Call<UnreadChatResponse>, response: Response<UnreadChatResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    val count = response.body()?.data?.unreadCount ?: 0
                    updateChatBadge(count)
                }
            }
            override fun onFailure(call: Call<UnreadChatResponse>, t: Throwable) {
                Log.e("Chat", "Gagal update badge: ${t.message}")
            }
        })
    }

    private fun updateChatBadge(count: Int) {
        // Gunakan ID yang sesuai dengan menu Bottom Nav Mitra (navigation_notifications)
        val badge = binding.navView.getOrCreateBadge(R.id.navigation_inbox)
        if (count > 0) {
            badge.isVisible = true
            badge.number = count
            badge.backgroundColor = ContextCompat.getColor(this, R.color.bg_red) // Pastikan warna merah ada
        } else {
            badge.isVisible = false
            badge.clearNumber()
        }
    }

    // --- HELPER REDIRECTS ---

    private fun showVerificationWaitingDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Menunggu Verifikasi")
            .setMessage("Data identitas Anda sedang diproses. Mohon tunggu akun disetujui.")
            .setPositiveButton("Kembali ke Login") { _, _ ->
                userPref.clearUser()
                redirectToLogin("")
            }
            .setCancelable(false)
            .show()
    }

    private fun redirectToIdentity() {
        val intent = Intent(this, IdentityActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun redirectToAgreement() {
        val intent = Intent(this, AgreementActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun redirectToLogin(message: String) {
        if (message.isNotEmpty()) Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun getAndSendFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) task.result?.let { sendTokenToServer(it) }
        }
    }

    private fun sendTokenToServer(fcmToken: String) {
        ApiConfig.getApiService(this).updateFcmToken(fcmToken).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                if (response.isSuccessful) Log.d("FCM", "FCM Token tersinkronisasi.")
            }
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {}
        })
    }
}