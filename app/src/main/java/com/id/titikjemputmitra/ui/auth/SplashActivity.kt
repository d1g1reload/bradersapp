package com.id.titikjemputmitra.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.id.titikjemputmitra.data.UserPreference
import com.id.titikjemputmitra.databinding.ActivitySplashBinding
import com.id.titikjemputmitra.ui.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var userPref: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        userPref = UserPreference(this)

        setAppVersion()

        lifecycleScope.launch {
            delay(2000L)

            if (userPref.isLoggedIn()) {
                // Jika sudah punya API Key (Sudah Login), langsung ke MainActivity
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
            } else {
                // Jika belum Login, arahkan ke LoginActivity
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
            }
            finish()
        }
    }

    private fun setAppVersion() {
        try {
            val pInfo = packageManager.getPackageInfo(packageName,0)
            val version = pInfo.versionName
            binding.tvAppVersion.text = "Versi Aplikasi $version"

        }catch (e: PackageManager.NameNotFoundException){
            e.printStackTrace()
            binding.tvAppVersion.text = "Versi Aplikasi -"
        }
    }
}