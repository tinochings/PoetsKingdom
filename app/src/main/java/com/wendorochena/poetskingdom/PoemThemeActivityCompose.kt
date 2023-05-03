package com.wendorochena.poetskingdom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.screens.PoemThemeViewModel
import com.wendorochena.poetskingdom.screens.ThemePoemApp
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PoemThemeActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val poemThemeViewModel : PoemThemeViewModel by viewModels()
        val intentExtras = intent.extras
        if (intentExtras?.getString("poemThemeName") != null) {
            poemThemeViewModel.isEditTheme = true
            val poemName = intentExtras.getString("poemThemeName")
            val poemThemeXmlParser = PoemThemeXmlParser(
                PoemTheme(BackgroundType.DEFAULT, applicationContext),
                applicationContext
            )
            val handler = CoroutineExceptionHandler { _, exception ->
                exception.printStackTrace()
                val builder = MaterialAlertDialogBuilder(this@PoemThemeActivityCompose)
                builder.setTitle(R.string.failed_poem_load_title)
                    .setMessage(R.string.failed_poem_load_message)
                    .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
            }

            lifecycleScope.launch(Dispatchers.Main + handler) {
                if (poemThemeXmlParser.parseTheme(poemName) == 0) {
                    poemThemeViewModel.initialisePoemTheme(poemThemeXmlParser)
                } else {
                    val builder = MaterialAlertDialogBuilder(this@PoemThemeActivityCompose)
                    builder.setTitle(R.string.failed_poem_load_title)
                        .setMessage(R.string.failed_poem_load_message)
                        .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }.show()
                }
            }
        }
        setContent {
            PoetsKingdomTheme {
                ThemePoemApp(poemThemeViewModel = poemThemeViewModel)
            }
        }
    }
}