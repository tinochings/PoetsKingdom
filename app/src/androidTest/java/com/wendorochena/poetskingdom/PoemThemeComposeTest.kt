package com.wendorochena.poetskingdom

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wendorochena.poetskingdom.screens.ThemePoemApp
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import org.junit.Rule
import org.junit.Test

class PoemThemeComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDefaultStartUp(){
        composeTestRule.setContent { 
            PoetsKingdomTheme {
                ThemePoemApp(poemThemeViewModel = viewModel())
            }
        }

        composeTestRule.onNodeWithText("Outline").assertIsDisplayed()
    }
}