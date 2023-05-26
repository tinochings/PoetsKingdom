package com.wendorochena.poetskingdom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wendorochena.poetskingdom.screens.HomeScreenApp
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme

class MainActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getSharedPreferences(
            getString(R.string.personalisation_sharedpreferences_key),
            MODE_PRIVATE
        ).getString("orientation", null) == null)
            setupDefaultSettings()
        setContent {
                PoetsKingdomTheme {
                    HomeScreenApp()
                }
        }
    }

    /**
     * Sets up default value for personalisation settings just in case some error happens
     * on starting the app on the first time
     */
    private fun setupDefaultSettings() {
        val sharedPrefEditor = getSharedPreferences(
            getString(R.string.personalisation_sharedpreferences_key),
            MODE_PRIVATE
        ).edit()

        sharedPrefEditor.putString(getString(R.string.author_pref),"Not Selected")
        sharedPrefEditor.putString(getString(R.string.orientation_pref),"portrait")
        sharedPrefEditor.putString(getString(R.string.cover_page_pref),"true")
        sharedPrefEditor.putString(getString(R.string.app_nickname_pref),getString(R.string.nickname_placeholder))
        sharedPrefEditor.putString(getString(R.string.resolution_pref),"1080 1080")
        sharedPrefEditor.putString(getString(R.string.signature_pref),"Not Selected")

        sharedPrefEditor.apply()
    }
}