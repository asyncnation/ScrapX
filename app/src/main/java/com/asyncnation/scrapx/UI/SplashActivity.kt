package com.asyncnation.scrapx.UI

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.asyncnation.scrapx.R
import com.asyncnation.scrapx.databinding.ActivitySplashBinding
import android.os.Looper

class SplashActivity : BaseActivity () {

    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

            finish()

        }, 2000)
    }
}