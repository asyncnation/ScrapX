package com.asyncnation.scrapx.UI

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.asyncnation.scrapx.room.AppDatabase

open class BaseActivity : AppCompatActivity() {
    lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = AppDatabase.getDatabase(this)
    }

}