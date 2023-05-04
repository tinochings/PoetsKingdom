package com.wendorochena.poetskingdom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wendorochena.poetskingdom.screens.HomeScreenApp
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme

class MainActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PoetsKingdomTheme {
                HomeScreenApp()
            }
        }
    }
}