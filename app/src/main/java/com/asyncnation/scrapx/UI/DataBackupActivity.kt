package com.asyncnation.scrapx.UI

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asyncnation.scrapx.BackupManager
import com.asyncnation.scrapx.databinding.ActivityDataBackupBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataBackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataBackupBinding

    companion object {

        private const val REQUEST_EXPORT = 1001
        private const val REQUEST_IMPORT = 1002
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        binding =
            ActivityDataBackupBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        binding.btnExport.setOnClickListener {
            exportBackup()
        }

        binding.btnImport.setOnClickListener {
            importBackup()
        }
    }

    // =========================================================
    // EXPORT
    // =========================================================

    private fun exportBackup() {

        val date =
            SimpleDateFormat(
                "yyyy-MM-dd_HH-mm-ss",
                Locale.getDefault()
            ).format(
                Date()
            )

        val fileName =
            "ScrapX_Backup_$date.json"

        val intent =
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type = "application/json"

                putExtra(
                    Intent.EXTRA_TITLE,
                    fileName
                )
            }

        startActivityForResult(
            intent,
            REQUEST_EXPORT
        )
    }

    // =========================================================
    // IMPORT
    // =========================================================

    private fun importBackup() {

        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type = "application/json"

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

        startActivityForResult(
            intent,
            REQUEST_IMPORT
        )
    }

    // =========================================================
    // FILE PICKER RESULT
    // =========================================================

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            resultCode != Activity.RESULT_OK ||
            data?.data == null
        ) {
            return
        }

        val uri =
            data.data ?: return

        when (requestCode) {

            REQUEST_EXPORT -> {

                performExport(uri)
            }

            REQUEST_IMPORT -> {

                performImport(uri)
            }
        }
    }

    // =========================================================
    // ACTUAL EXPORT
    // =========================================================

    private fun performExport(
        uri: android.net.Uri
    ) {

        lifecycleScope.launch {

            setButtonsEnabled(false)

            val result =
                BackupManager.exportDatabase(
                    this@DataBackupActivity,
                    uri
                )

            setButtonsEnabled(true)

            if (result.isSuccess) {

                Toast.makeText(
                    this@DataBackupActivity,
                    "Backup exported successfully",
                    Toast.LENGTH_LONG
                ).show()

            } else {

                Toast.makeText(
                    this@DataBackupActivity,
                    "Backup export failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // =========================================================
    // ACTUAL IMPORT
    // =========================================================

    private fun performImport(
        uri: android.net.Uri
    ) {

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Restore Backup?")
            .setMessage(
                "This will replace all existing ScrapX data with the selected backup.\n\n" +
                        "This action cannot be undone."
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Restore"
            ) { _, _ ->

                restoreBackup(uri)
            }
            .show()
    }

    private fun restoreBackup(
        uri: android.net.Uri
    ) {

        lifecycleScope.launch {

            setButtonsEnabled(false)

            val result =
                BackupManager.importDatabase(
                    this@DataBackupActivity,
                    uri
                )

            setButtonsEnabled(true)

            if (result.isSuccess) {

                Toast.makeText(
                    this@DataBackupActivity,
                    "Backup restored successfully",
                    Toast.LENGTH_LONG
                ).show()

            } else {

                Toast.makeText(
                    this@DataBackupActivity,
                    "Restore failed: ${
                        result.exceptionOrNull()?.message
                    }",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // =========================================================
    // BUTTON STATE
    // =========================================================

    private fun setButtonsEnabled(
        enabled: Boolean
    ) {

        binding.btnExport.isEnabled =
            enabled

        binding.btnImport.isEnabled =
            enabled
    }
}