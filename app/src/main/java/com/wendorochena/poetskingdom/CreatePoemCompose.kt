package com.wendorochena.poetskingdom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.poemdata.PoemXMLParser
import com.wendorochena.poetskingdom.screens.CreatePoemApp
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import com.wendorochena.poetskingdom.viewModels.CreatePoemViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatePoemCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val createPoemViewModel = CreatePoemViewModel()
        val intentExtras = intent.extras
        val loadPoemArg = getString(R.string.load_poem_argument_name)
        val poemTitleArg = getString(R.string.poem_title_argument_name)

        // parse users theme. If we cant parse it terminate the activity
        if (intentExtras?.getString(poemTitleArg) != null) {
            val poemParser =
                PoemThemeXmlParser(
                    PoemTheme(BackgroundType.DEFAULT, applicationContext),
                    applicationContext
                )

            val exceptionHandler = CoroutineExceptionHandler { _, exception ->
                exception.printStackTrace()
                val builder = MaterialAlertDialogBuilder(this@CreatePoemCompose)
                builder.setTitle(R.string.failed_poem_load_title)
                    .setMessage(R.string.failed_poem_load_message)
                    .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
            }

            // load file on background thread and then populate UI
            lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                val isLoadPoem = intentExtras.getBoolean(loadPoemArg, false)
                if (isLoadPoem)
                    createPoemViewModel.isDimmerDisplayed = true

                val poemThemeResult = poemParser.parseTheme(intentExtras.getString(poemTitleArg))
                val poemLoadResult =
                    if (isLoadPoem) PoemXMLParser.parseSavedPoem(
                        poemParser.getPoemTheme().poemTitle,
                        applicationContext,
                        Dispatchers.IO,
                        null
                    )
                    else
                        null

                if (poemThemeResult == 0) {
                    createPoemViewModel.initialisePoemTheme(poemParser)

                    if (isLoadPoem) {
                        if (poemLoadResult!!.size > 0)
                            createPoemViewModel.loadSavedPoem(poemLoadResult)
                        else {
                            createPoemViewModel.hasFileBeenEdited = true
                        }
                    } else {
                        createPoemViewModel.hasFileBeenEdited = true
                    }
                    createPoemViewModel.isDimmerDisplayed = false
                } else {
                    val builder = MaterialAlertDialogBuilder(this@CreatePoemCompose)
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
                   CreatePoemApp(createPoemViewModel)
            }
        }
    }

}