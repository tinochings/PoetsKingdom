package com.wendorochena.poetskingdom.regression

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.ui.theme.MadzinzaGreen
import com.wendorochena.poetskingdom.ui.theme.OffWhite
import com.wendorochena.poetskingdom.utils.TextMarginUtil
import com.wendorochena.poetskingdom.viewModels.HeadingSelection
import com.wendorochena.poetskingdom.viewModels.PoemThemeViewModel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PoemThemeViewModelRegressionTest {

    private var poemThemeViewModel = PoemThemeViewModel()
    private val offWhiteString = "FAF9F6"
    private val offWhiteColor = OffWhite.toArgb()
    private val dummyImagePath = "/usr/local/bin/terminate"

    @Mock
    val mockContext: Context = mock {
        on {
            this.getDir(
                this.getString(R.string.poems_folder_name),
                Context.MODE_PRIVATE
            )
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/poems")

        on {
            this.getDir(this.getString(R.string.poem_themes_folder_name), Context.MODE_PRIVATE)
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/themes")
    }

    /**
     * Constantly change poem theme type. The change of poem theme type requires a breaking change
     * and should keep state consistent. Changing from COLOR -> IMAGE should make sure that
     * backgroundColor is reset to default. Changing from Image -> Color should make sure that the
     * backgroundImagePath is reset to default. Changing from OUTLINE_WITH_COLOR -> IMAGE should
     * make sure that outline color, background color, and background color as in should all be
     * set to the default value
     */
    @Test
    fun  testTryBreakUiAndPoemThemeState() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val fontSize = 25f
        poemThemeViewModel = PoemThemeViewModel(ioDispatcher = StandardTestDispatcher(testScheduler), mainDispatcher = dispatcher)

        //default to color
        poemThemeViewModel.updateBackground(BackgroundType.COLOR, offWhiteString, offWhiteColor)
        poemThemeViewModel.setTextSize(fontSize)
        poemThemeViewModel.setTextColor(offWhiteColor, offWhiteString)

        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == offWhiteString)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == offWhiteColor)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == offWhiteString)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == offWhiteColor)

        //update to Outline_With_Image
        poemThemeViewModel.updateBackground(
            BackgroundType.OUTLINE_WITH_IMAGE,
            offWhiteColor,
            OutlineTypes.LEMON.name,
            dummyImagePath
        )

        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == null)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == null)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == "#FFFFFF")
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == -1)
        assert(poemThemeViewModel.modelState.value.outlineType == OutlineTypes.LEMON)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outline == OutlineTypes.LEMON.name)

        //update to Image
        poemThemeViewModel.updateBackground(BackgroundType.IMAGE, dummyImagePath)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == null)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == null)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == "#FFFFFF")
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == -1)
        assert(poemThemeViewModel.modelState.value.outlineType == null)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outline == "")

        //update to Outline_With_Color
        poemThemeViewModel.updateBackground(
            BackgroundType.OUTLINE_WITH_COLOR,
            offWhiteColor,
            OutlineTypes.ROUNDED_RECTANGLE.name,
            offWhiteString,
            offWhiteColor
        )
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == offWhiteString)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == offWhiteColor)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == offWhiteString)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == offWhiteColor)
        assert(poemThemeViewModel.modelState.value.outlineType == OutlineTypes.ROUNDED_RECTANGLE)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outline == OutlineTypes.ROUNDED_RECTANGLE.name)
        assert(poemThemeViewModel.modelState.value.backgroundImageChosen == null)
        assert(poemThemeViewModel.modelState.value.poemThemeState.imagePath.isEmpty())

        //change to COLOR
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen != null)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt != null)
        poemThemeViewModel.updateBackground(BackgroundType.COLOR, poemThemeViewModel.modelState.value.backgroundColorChosen!!, poemThemeViewModel.modelState.value.backgroundColorChosenAsInt!!)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == offWhiteString)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == offWhiteColor)

        //change to outline
        poemThemeViewModel.updateBackground(BackgroundType.OUTLINE, offWhiteColor, OutlineTypes.ROUNDED_RECTANGLE)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == null)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == null)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == "#FFFFFF")
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == -1)

        assert(poemThemeViewModel.modelState.value.fontSize == fontSize)
        assert(poemThemeViewModel.modelState.value.poemThemeState.textSize == fontSize.toInt())
        assert(poemThemeViewModel.modelState.value.fontColor == offWhiteColor)
        assert(poemThemeViewModel.modelState.value.poemThemeState.textColorAsInt == offWhiteColor)
    }

    @Test
    fun testSetters() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        poemThemeViewModel = PoemThemeViewModel(ioDispatcher = StandardTestDispatcher(testScheduler), mainDispatcher = dispatcher)
        poemThemeViewModel.setTextMarginUtility(TextMarginUtil(5,5,5,5))
        assert(poemThemeViewModel.modelState.value.textMarginUtil == TextMarginUtil(5,5,5,5))
        poemThemeViewModel.setEditTheme(true)
        assert(poemThemeViewModel.modelState.value.isEditTheme)
        poemThemeViewModel.setEditTheme(false)
        assert(!poemThemeViewModel.modelState.value.isEditTheme)

        poemThemeViewModel.changeSelection(HeadingSelection.TEXT)
        assert(poemThemeViewModel.modelState.value.headingSelection == HeadingSelection.TEXT)
        poemThemeViewModel.setTextAlign(TextAlignment.LEFT)
        assert(poemThemeViewModel.modelState.value.textAlignment == TextAlignment.LEFT)

        poemThemeViewModel.setDisplayDialog(true)
        assert(poemThemeViewModel.modelState.value.shouldDisplayDialog)

        poemThemeViewModel.setAlbumName("NOOOO")
        assert(poemThemeViewModel.modelState.value.savedAlbumName == "NOOOO")
        val defaultFontFamily = FontFamily(Typeface.DEFAULT)
        poemThemeViewModel.setFontFamily(defaultFontFamily, "default")
        assert(poemThemeViewModel.modelState.value.textFontFamily == defaultFontFamily)
        assert(poemThemeViewModel.modelState.value.poemThemeState.textFontFamily == "default")
        assert(poemThemeViewModel.modelState.value.textFontFamilyString == "default")

        poemThemeViewModel.updateOutlineColor(offWhiteColor)
        assert(poemThemeViewModel.modelState.value.outlineColor == offWhiteColor)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outlineColor == offWhiteColor)

        poemThemeViewModel.resetResultToDefault()
        assert(poemThemeViewModel.modelState.value.poemThemeResult == -2)
        val poemThemeXmlParser = PoemThemeXmlParser(
            PoemTheme(BackgroundType.DEFAULT, mockContext),
            mockContext,
            dispatcher
        )
        poemThemeXmlParser.parseTheme("Outline_With_Color")
        testScheduler.advanceUntilIdle()

        poemThemeViewModel.initialisePoemTheme(poemThemeXmlParser)

        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.OUTLINE_WITH_COLOR)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == BackgroundType.OUTLINE_WITH_COLOR)
        assert(poemThemeViewModel.modelState.value.outlineType == OutlineTypes.ROUNDED_RECTANGLE)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outline == OutlineTypes.ROUNDED_RECTANGLE.name)
        assert(poemThemeViewModel.modelState.value.outlineColor == -7821273)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outlineColor == -7821273)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == "#ff59ef")
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == "#ff59ef")
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == -42513)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == -42513)
        assert(poemThemeViewModel.modelState.value.fontSize == 14f)
        assert(poemThemeViewModel.modelState.value.poemThemeState.textSize == 14)
        assert(poemThemeViewModel.modelState.value.fontColor == -16777216)
        assert(poemThemeViewModel.modelState.value.poemThemeState.textColorAsInt == -16777216)
        assert(poemThemeViewModel.modelState.value.poemThemeState.textColor == "#000000")
        assert(poemThemeViewModel.modelState.value.textAlignment == TextAlignment.CENTRE_VERTICAL)
        assert(poemThemeViewModel.modelState.value.poemThemeState.textAlignment == TextAlignment.CENTRE_VERTICAL)

        assert(poemThemeViewModel.modelState.value.textFontFamilyString== "Default")
        assert(poemThemeViewModel.modelState.value.poemThemeState.textFontFamily == "Default")
    }

    @Test
    fun testSecureDefaults(){
        poemThemeViewModel = PoemThemeViewModel()
        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.DEFAULT)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == BackgroundType.DEFAULT)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == null)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == "#FFFFFF")
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == null)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == -1)
        assert(poemThemeViewModel.modelState.value.backgroundImageChosen == null)
        assert(poemThemeViewModel.modelState.value.poemThemeState.imagePath == "")
        assert(poemThemeViewModel.modelState.value.poemThemeResult == -2)
        assert(!poemThemeViewModel.modelState.value.isBold)
        assert(!poemThemeViewModel.modelState.value.poemThemeState.bold)
        assert(!poemThemeViewModel.modelState.value.poemThemeState.italic)
        assert(!poemThemeViewModel.modelState.value.shouldDisplayDialog)
        assert(poemThemeViewModel.modelState.value.textFontFamilyString == "default")
        assert(poemThemeViewModel.modelState.value.poemThemeState.textFontFamily == "Default")
        assert(poemThemeViewModel.modelState.value.textAlignment == TextAlignment.LEFT)
        assert(poemThemeViewModel.modelState.value.fontColor == Color.Black.toArgb())
        assert(poemThemeViewModel.modelState.value.poemThemeState.textColorAsInt == Color.Black.toArgb())
        assert(poemThemeViewModel.modelState.value.poemThemeState.textColor == "#000000")
        assert(poemThemeViewModel.modelState.value.fontSize == 14f)
        assert(poemThemeViewModel.modelState.value.poemThemeState.textSize == 14)
        assert(poemThemeViewModel.modelState.value.textMarginUtil == TextMarginUtil())
        assert(poemThemeViewModel.modelState.value.poemThemeState.textMarginUtil == TextMarginUtil())
        assert(poemThemeViewModel.modelState.value.savedAlbumName == null)
        assert(!poemThemeViewModel.modelState.value.isEditTheme)
        assert(poemThemeViewModel.modelState.value.poemTitle == "")
        assert(poemThemeViewModel.modelState.value.headingSelection == HeadingSelection.OUTLINE)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outline == "")
        assert(poemThemeViewModel.modelState.value.outlineType == null)
        assert(poemThemeViewModel.modelState.value.outlineColor == MadzinzaGreen.toArgb())
        assert(poemThemeViewModel.modelState.value.poemThemeState.outlineColor == MadzinzaGreen.toArgb())
        assert(poemThemeViewModel.modelState.value.poemThemeState.poemTitle == "")
    }
}