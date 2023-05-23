package com.wendorochena.poetskingdom

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.ui.theme.MadzinzaGreen
import com.wendorochena.poetskingdom.ui.theme.OffWhite
import com.wendorochena.poetskingdom.viewModels.PoemThemeViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class PoemThemeViewModelTest {
    private val poemThemeViewModel = PoemThemeViewModel()
    private val outlineColor = MadzinzaGreen.toArgb()
    private val dummyImagePath = "/usr/local/bin/terminate"
    private val dummyColor = "#FFFFFF"
    private val backgroundColor = OffWhite.toArgb()
    @Mock
    val mockContext : Context = mock {
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

    @Test
    fun testSetOutline() {
        val uiState = poemThemeViewModel.uiState.value

        poemThemeViewModel.updateBackground(BackgroundType.OUTLINE,outlineColor, OutlineTypes.ROUNDED_RECTANGLE)

        assert(uiState.outline == OutlineTypes.ROUNDED_RECTANGLE.name)
        assert(uiState.backgroundType == BackgroundType.OUTLINE)
        assert(uiState.outlineColor == this.outlineColor)
    }

    @Test
    fun testSetBackgroundImage() {
        val uiState = poemThemeViewModel.uiState.value

        poemThemeViewModel.updateBackground(BackgroundType.IMAGE,dummyImagePath)
        assert(uiState.backgroundType == BackgroundType.IMAGE)
        assert(uiState.imagePath == dummyImagePath)
    }

    @Test
    fun testSetBackgroundColor() {
        val uiState = poemThemeViewModel.uiState.value
        poemThemeViewModel.updateBackground(BackgroundType.COLOR, backgroundColor = dummyColor, backgroundColorAsInt = backgroundColor)

        assert(uiState.backgroundType == BackgroundType.COLOR)
        assert(uiState.backgroundColor == dummyColor)
        assert(uiState.backgroundColorAsInt == backgroundColor)
    }

    @Test
    fun testChangeBackgroundType() {
        val uiState = poemThemeViewModel.uiState.value
        poemThemeViewModel.changeBackgroundType(BackgroundType.COLOR)
        assert(uiState.backgroundType == BackgroundType.COLOR)
        poemThemeViewModel.changeBackgroundType(BackgroundType.IMAGE)
        assert(uiState.backgroundType == BackgroundType.IMAGE)
    }

    @Test
    fun testInitialPoemTheme() : Unit = runTest {
        val uiState = poemThemeViewModel.uiState.value
        val poemTheme = PoemTheme(BackgroundType.DEFAULT, mockContext)
        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Outline_With_Color") == 0)

        poemThemeViewModel.initialisePoemTheme(result)
        assert(uiState.backgroundType == BackgroundType.OUTLINE_WITH_COLOR)
        assert(uiState.backgroundColor == "#ff59ef")
        assert(uiState.backgroundColorAsInt == -42513)
        assert(uiState.outlineColor == -7821273)
        assert(uiState.imagePath == "")
        assert(uiState.outline == OutlineTypes.ROUNDED_RECTANGLE.toString())
        assert(uiState.textSize == 14)
        assert(uiState.textColor == "#000000")
        assert(uiState.textColorAsInt == -16777216)
        assert(uiState.textAlignment == TextAlignment.CENTRE_VERTICAL)
        assert(uiState.textFontFamily == "Default")
    }

    @Test
    fun testOutlineWithImageBackgroundInitialisePoem () : Unit = runTest{
        val uiState = poemThemeViewModel.uiState.value
        val poemTheme = PoemTheme(BackgroundType.DEFAULT, mockContext)
        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Outline_With_Image") == 0)

        poemThemeViewModel.initialisePoemTheme(result)
        assert(uiState.backgroundType == BackgroundType.OUTLINE_WITH_IMAGE)
        assert(uiState.backgroundColor == "#FFFFFFFF")
        assert(uiState.backgroundColorAsInt == -1)
        assert(uiState.outlineColor == -7821273)
        assert(uiState.imagePath == "app/src/test/java/com/wendorochena/poetskingdom/MockFiles/test_images/createpoem.jpg")
        assert(uiState.outline == OutlineTypes.ROUNDED_RECTANGLE.toString())
        assert(uiState.textSize == 14)
        assert(uiState.textColor == "#fdfcff")
        assert(uiState.textColorAsInt == -131841)
        assert(uiState.textAlignment == TextAlignment.LEFT)
        assert(uiState.textFontFamily == "Default")
    }
}