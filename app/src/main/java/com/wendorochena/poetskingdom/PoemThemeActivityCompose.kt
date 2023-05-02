package com.wendorochena.poetskingdom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.wendorochena.poetskingdom.screens.ThemePoemApp
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme

class PoemThemeActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PoetsKingdomTheme {
                ThemePoemApp()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PoetsKingdomTheme {
        ThemePoemApp()
    }
}