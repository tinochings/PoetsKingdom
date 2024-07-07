package com.wendorochena.poetskingdom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wendorochena.poetskingdom.screens.MyPoemsApp
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import com.wendorochena.poetskingdom.viewModels.MyPoemsViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPoemsCompose : ComponentActivity() {
    private var permissionsResultLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsResultLauncher = permissionsActivityResult()
        val myPoemsViewModel = MyPoemsViewModel()
        myPoemsViewModel.permissionsResultLauncher.observe(this) {
            permissionsResultLauncher?.launch(it)
        }
        myPoemsViewModel.shareIntent.observe(this) {
            startActivity(Intent.createChooser(it, null))
        }
        val sharedPreferences =
            applicationContext.getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(getString(R.string.glide_cache_clear), false)) {
            sharedPreferences.edit().putBoolean(getString(R.string.glide_cache_clear), false).apply()
            val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            }
            lifecycleScope.launch(Dispatchers.IO + exceptionHandler) {
                Glide.get(this@MyPoemsCompose).clearDiskCache()
            }
        }

        setContent {
            PoetsKingdomTheme {
                    MyPoemsApp(myPoemsViewModel)
                }
            }
    }
    private fun permissionsActivityResult(): ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted)
                Toast.makeText(this, R.string.register_launcher_success, Toast.LENGTH_LONG).show()
            else
                Toast.makeText(this, R.string.register_launcher_fail, Toast.LENGTH_LONG).show()

        }
}