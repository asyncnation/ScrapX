package com.asyncnation.scrapx.UI

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.asyncnation.scrapx.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityHomeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnAddItem.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
        }

        binding.btnPurchase.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    PurchaseActivity::class.java
                )
            )
        }

        binding.btnPreviousPurchases.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    PreviousPurchasesActivity::class.java
                )
            )
        }

        binding.btnDataBackup.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    DataBackupActivity::class.java
                )
            )
        }
    }
}