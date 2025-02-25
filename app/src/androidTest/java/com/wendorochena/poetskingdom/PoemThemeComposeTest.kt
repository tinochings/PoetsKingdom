package com.wendorochena.poetskingdom

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasInsertTextAtCursorAction
import androidx.compose.ui.test.hasNoClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.screens.ThemePoemApp
import com.wendorochena.poetskingdom.ui.theme.MadzinzaGreen
import com.wendorochena.poetskingdom.ui.theme.OffWhite
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import com.wendorochena.poetskingdom.utils.TypefaceHelper
import com.wendorochena.poetskingdom.viewModels.PoemThemeViewModel
import com.wendorochena.poetskingdom.viewModels.services.PoemThemeViewModelService
import org.junit.Rule
import org.junit.Test

class PoemThemeComposeTest {

    private val poemThemeService = PoemThemeViewModelService()

    @get:Rule
    val composeTestRule = createComposeRule()
    private val textLayoutListTag = "TextLayoutList"
    private val outlineLayoutListTag = "OutlineLayoutList"
    private val offWhiteColor = OffWhite

    @Test
    fun testFirstUse() {
        val poemThemeViewModel = PoemThemeViewModel()
        var context: Context? = null
        composeTestRule.setContent {
            context = LocalContext.current
            ensureOnFirstUse(context!!)
            PoetsKingdomTheme {
                ThemePoemApp(poemThemeViewModel = poemThemeViewModel)
            }
        }

        composeTestRule.onNodeWithText(context!!.getString(R.string.guide_outline)).assertExists()
        composeTestRule.onNodeWithText(context!!.getString(R.string.builder_understood))
            .performClick()
        composeTestRule.onNodeWithText("Background").performClick()

        composeTestRule.onNodeWithText(context!!.getString(R.string.guide_background))
            .assertExists()
        composeTestRule.onNodeWithText(context!!.getString(R.string.builder_understood))
            .performClick()
        composeTestRule.onNodeWithText("Text").performClick()

        composeTestRule.onNodeWithText(context!!.getString(R.string.guide_text)).assertExists()
        composeTestRule.onNodeWithText(context!!.getString(R.string.builder_understood))
            .performClick()
    }

    @Test
    fun testChangeHeadingSelection() {
        val poemThemeViewModel = PoemThemeViewModel()
        var context: Context? = null

        composeTestRule.setContent {
            context = LocalContext.current
            skipOnFirstUse(LocalContext.current)
            PoetsKingdomTheme {
                ThemePoemApp(poemThemeViewModel = poemThemeViewModel)
            }
        }

        composeTestRule.onNodeWithText("Background").performClick()
        composeTestRule.onNodeWithContentDescription(context!!.getString(R.string.color_palette))
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Text").performClick()
        val allFonts = context!!.resources.getStringArray(R.array.customFontNamesCompose)

        allFonts.forEach {
            composeTestRule.onNodeWithTag(textLayoutListTag, useUnmergedTree = true)
                .performScrollToNode(
                    hasAnyDescendant(hasText(fontToString(it)))
                ).assertExists()
        }
    }

    @Test
    fun testTextHeading() {
        val poemThemeViewModel = PoemThemeViewModel()
        var context: Context? = null
        composeTestRule.setContent {
            context = LocalContext.current
            skipOnFirstUse(LocalContext.current)
            PoetsKingdomTheme {
                ThemePoemApp(poemThemeViewModel = poemThemeViewModel)
            }
        }
        assert(
            poemThemeViewModel.modelState.value.textFontFamilyString.equals(
                "default",
                ignoreCase = true
            )
        )
        composeTestRule.onNodeWithText("Text").performClick()
        composeTestRule.onNodeWithContentDescription(context!!.getString(R.string.center_text_align))
            .performClick()
        assert(poemThemeViewModel.modelState.value.textAlignment == TextAlignment.CENTRE)
        assert(poemThemeViewModel.modelState.value.poemThemeState.textAlignment == TextAlignment.CENTRE)
        getTextItemFromList(textLayoutListTag, "Adinekirnberg").performClick()
        assert(
            poemThemeViewModel.modelState.value.textFontFamilyString == "adinekirnberg"
        )
        assert(poemThemeViewModel.modelState.value.textFontFamily == TypefaceHelper.getTypeFace("adinekirnberg"))

        getTextItemFromList(textLayoutListTag, "Woodland").performClick()
        assert(
            poemThemeViewModel.modelState.value.textFontFamilyString == "woodland"
        )
        assert(poemThemeViewModel.modelState.value.textFontFamily == TypefaceHelper.getTypeFace("woodland"))

        getTextItemFromList(textLayoutListTag, "X Typewriter").performClick()
        assert(
            poemThemeViewModel.modelState.value.textFontFamilyString == "x_typewriter"
        )
        assert(poemThemeViewModel.modelState.value.textFontFamily == TypefaceHelper.getTypeFace("x_typewriter"))

        getTextItemFromList(textLayoutListTag, "Text Color")

    }

    @Test
    fun testPoemThemeDisplay() {
        val poemThemeViewModel = PoemThemeViewModel()
        var context: Context? = null
        composeTestRule.setContent {
            context = LocalContext.current
            skipOnFirstUse(LocalContext.current)
            PoetsKingdomTheme {
                ThemePoemApp(poemThemeViewModel = poemThemeViewModel)
            }
        }
        val previewText = context!!.resources.getString(R.string.preview_text)
        composeTestRule.onNodeWithText(context!!.getString(R.string.outline)).performClick()
        composeTestRule.onNodeWithTag("RoundedRectangleOutline").performScrollTo().performClick()
        assertDisplayType(poemThemeViewModel, BackgroundType.OUTLINE)
        resetDisplay(previewText)
        assertDefaultBackground(poemThemeViewModel)

        composeTestRule.onNodeWithTag("RectangleOutline").performScrollTo().performClick()
        assertDisplayType(poemThemeViewModel, BackgroundType.OUTLINE)
        resetDisplay(previewText)
        assertDefaultBackground(poemThemeViewModel)
        composeTestRule.onNodeWithTag("TeardropOutline").performScrollTo().performClick()
        assertDisplayType(poemThemeViewModel, BackgroundType.OUTLINE)
        resetDisplay(previewText)
        assertDefaultBackground(poemThemeViewModel)
        composeTestRule.onNodeWithTag("RotatedTeardropOutline").performScrollTo().performClick()

        assertDisplayType(poemThemeViewModel, BackgroundType.OUTLINE)
        resetDisplay(previewText)
        assertDefaultBackground(poemThemeViewModel)
        composeTestRule.onNodeWithTag("LemonOutline").performScrollTo().performClick()

        assertDisplayType(poemThemeViewModel, BackgroundType.OUTLINE)
        resetDisplay(previewText)
        assertDefaultBackground(poemThemeViewModel)

        composeTestRule.onNodeWithTag("RotatedLemonOutline").performScrollTo().performClick()
        assertDisplayType(poemThemeViewModel, BackgroundType.OUTLINE)
        resetDisplay(previewText)
        assertDefaultBackground(poemThemeViewModel)

        composeTestRule.onNodeWithText(context!!.getString(R.string.background)).performClick()
        composeTestRule.onNodeWithText(context!!.getString(R.string.colors)).performClick()

        composeTestRule.onNodeWithText(context!!.getString(R.string.color_picker_title))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context!!.getString(R.string.title_change_cancel))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context!!.getString(R.string.confirm)).assertIsDisplayed()
    }

    @Test
    fun testResetPoemThemeDisplay(){
        val poemThemeViewModel = PoemThemeViewModel()
        var context: Context? = null
        composeTestRule.setContent {
            context = LocalContext.current
            skipOnFirstUse(LocalContext.current)
            PoetsKingdomTheme {
                ThemePoemApp(poemThemeViewModel = poemThemeViewModel)
            }
        }
        val previewText = context!!.resources.getString(R.string.preview_text)
        poemThemeViewModel.updateBackground(
            BackgroundType.OUTLINE_WITH_COLOR,
            outlineColor = offWhiteColor.toArgb(),
            outline = OutlineTypes.ROUNDED_RECTANGLE.name,
            backgroundColor = "#88A827",
            backgroundColorAsInt = MadzinzaGreen.toArgb()
        )
        resetDisplay(previewText)
        
        composeTestRule.onNodeWithText(context!!.getString(R.string.remove_background_title)).assertIsDisplayed()
        composeTestRule.onNode(hasText(context!!.getString(R.string.remove_background_color_outline_popup))).assertIsDisplayed().assertExists()
        composeTestRule.onNodeWithText(context!!.getString(R.string.negative_background_color_outline_button)).assertIsDisplayed()
        composeTestRule.onNode(isDialog()).assert(hasAnyDescendant(hasText(context!!.getString(R.string.positive_background_color_outline_button))))
        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText("Outline")).performClick()

        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.COLOR)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == MadzinzaGreen.toArgb())
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == MadzinzaGreen.toArgb())
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == "#88A827")
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == "#88A827")

        poemThemeViewModel.updateBackground(
            BackgroundType.OUTLINE_WITH_COLOR,
            outlineColor = offWhiteColor.toArgb(),
            outline = OutlineTypes.ROUNDED_RECTANGLE.name,
            backgroundColor = "#88A827",
            backgroundColorAsInt = MadzinzaGreen.toArgb()
        )
        resetDisplay(previewText)

        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText("Color")).performClick()
        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.OUTLINE)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == null)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == -1)
        assert(poemThemeViewModel.modelState.value.outlineColor == offWhiteColor.toArgb())
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == null)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == "#FFFFFF")
    }

    @Test
    fun testCreatePoem(){
        val poemThemeViewModel = PoemThemeViewModel()
        var context: Context? = null
        composeTestRule.setContent {
            context = LocalContext.current
            skipOnFirstUse(LocalContext.current)
            PoetsKingdomTheme {
                ThemePoemApp(poemThemeViewModel = poemThemeViewModel)
            }
        }
        composeTestRule.onNodeWithText("Create Poem").performClick()
        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText("Give Your Poem A Title :)")).assertIsDisplayed()
        composeTestRule.onNode(isDialog() and hasAnyDescendant(hasInsertTextAtCursorAction())).assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm").performClick()

        composeTestRule.onNode(hasText(context!!.getString(R.string.retry)) and hasNoClickAction()).assertIsDisplayed()
        composeTestRule.onNodeWithText(context!!.getString(R.string.invalid_input_message)).assertIsDisplayed()
        composeTestRule.onNode(hasText(context!!.getString(R.string.retry)) and hasClickAction()).performClick()

        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText("Give Your Poem A Title :)")).assertIsDisplayed()
        composeTestRule.onNode(isDialog() and hasAnyDescendant(hasInsertTextAtCursorAction())).assertIsDisplayed()

        composeTestRule.onNode(hasInsertTextAtCursorAction()).performTextInput("ABCD")
        composeTestRule.onNodeWithText("ABCD").assertExists()
        composeTestRule.onNodeWithText("Confirm").performClick()
    }

    private fun assertDisplayType(
        poemThemeViewModel: PoemThemeViewModel,
        backgroundType: BackgroundType,
        color: Int = MadzinzaGreen.toArgb(),
        outlineName: String? = null
    ) {
        assert(poemThemeViewModel.modelState.value.backgroundType == backgroundType)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == backgroundType)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outlineColor == color)
        assert(poemThemeViewModel.modelState.value.outlineColor == color)
    }

    private fun assertDefaultBackground(poemThemeViewModel: PoemThemeViewModel) {
        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.DEFAULT)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == BackgroundType.DEFAULT)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == -1)
    }

    private fun resetDisplay(previewText: String) {
        composeTestRule.onNodeWithText(previewText).performTouchInput {
            longClick()
        }
    }

    private fun getTextItemFromList(tag: String, textItem: String): SemanticsNodeInteraction {
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollToNode(
                hasAnyDescendant(hasText(textItem))
            ).assertExists()
        return composeTestRule.onNodeWithText(textItem)
    }


    private fun skipOnFirstUse(context: Context) {
        context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            .edit().putBoolean("outlineFirstUse", true).commit()
        context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            .edit().putBoolean("backgroundFirstUse", true).commit()
        context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            .edit().putBoolean("textFirstUse", true).commit()
        context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            .edit().putBoolean("createPoemFirstUse", true).commit()
    }

    private fun ensureOnFirstUse(context: Context) {
        context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            .edit().putBoolean("outlineFirstUse", false).commit()
        context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            .edit().putBoolean("backgroundFirstUse", false).commit()
        context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            .edit().putBoolean("textFirstUse", false).commit()
    }

    private fun fontToString(text: String): String {
        return poemThemeService.formatFontItem(text)
    }
}