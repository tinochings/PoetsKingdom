package com.wendorochena.poetskingdom

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.ui.theme.LightBlack
import com.wendorochena.poetskingdom.ui.theme.MadzinzaGreen
import com.wendorochena.poetskingdom.ui.theme.OffWhite
import com.wendorochena.poetskingdom.utils.TextMarginUtil
import com.wendorochena.poetskingdom.viewModels.PoemThemeViewModel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * These test cases just assert that the view model works as it should when given
 * specific instructions. Kindly navigate to the regression tests to see me try
 * to break functionality
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PoemThemeViewModelTest {

    private var poemThemeViewModel = PoemThemeViewModel()
    private val outlineColor = MadzinzaGreen.toArgb()
    private val dummyImagePath = "/usr/local/bin/terminate"
    private val dummyColor = "#FFFFFF"
    private val backgroundColor = OffWhite.toArgb()

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
    @Before
    fun setup(){
        val savedFileTheme = File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/themes/POEM.xml")
        if (savedFileTheme.exists())
            savedFileTheme.delete()
    }
    @Test
    fun testSetOutline() = runTest {
        poemThemeViewModel = PoemThemeViewModel(StandardTestDispatcher(testScheduler))
        poemThemeViewModel.updateBackground(
            BackgroundType.OUTLINE,
            outlineColor,
            OutlineTypes.ROUNDED_RECTANGLE
        )

        assert(poemThemeViewModel.modelState.value.poemThemeState.outline == OutlineTypes.ROUNDED_RECTANGLE.name)
        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.OUTLINE)
        assert(poemThemeViewModel.modelState.value.outlineColor == outlineColor)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outlineColor == outlineColor)
    }

    @Test
    fun testSetOutlineWithColor() = runTest {
        poemThemeViewModel = PoemThemeViewModel(StandardTestDispatcher(testScheduler))
        val blackBackgroundColor = LightBlack.toArgb()
        poemThemeViewModel.updateBackground(
            BackgroundType.OUTLINE_WITH_COLOR,
            outlineColor = outlineColor,
            outline = OutlineTypes.TEARDROP.name,
            "#454545",
            blackBackgroundColor
        )

        assert(poemThemeViewModel.modelState.value.poemThemeState.outline == OutlineTypes.TEARDROP.name)
        assert(poemThemeViewModel.modelState.value.outlineType == OutlineTypes.TEARDROP)
        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.OUTLINE_WITH_COLOR)
        assert(poemThemeViewModel.modelState.value.outlineColor == outlineColor)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outlineColor == outlineColor)

        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == blackBackgroundColor)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == blackBackgroundColor)

        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == "#454545")
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == "#454545")
    }

    @Test
    fun testSetBackgroundImage() = runTest {
        poemThemeViewModel = PoemThemeViewModel(StandardTestDispatcher(testScheduler))
        poemThemeViewModel.updateBackground(BackgroundType.IMAGE, dummyImagePath)

        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.IMAGE)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == BackgroundType.IMAGE)
        assert(poemThemeViewModel.modelState.value.backgroundImageChosen == dummyImagePath)
        assert(poemThemeViewModel.modelState.value.poemThemeState.imagePath == dummyImagePath)
    }

    @Test
    fun testSetOutlineWithImage() = runTest {
        poemThemeViewModel = PoemThemeViewModel(StandardTestDispatcher(testScheduler))
        poemThemeViewModel.updateBackground(BackgroundType.OUTLINE_WITH_IMAGE, outlineColor,
            OutlineTypes.ROUNDED_RECTANGLE.name, dummyImagePath)

        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.OUTLINE_WITH_IMAGE)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == BackgroundType.OUTLINE_WITH_IMAGE)

        assert(poemThemeViewModel.modelState.value.backgroundImageChosen == dummyImagePath)
        assert(poemThemeViewModel.modelState.value.poemThemeState.imagePath == dummyImagePath)

        assert(poemThemeViewModel.modelState.value.outlineColor == outlineColor)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outlineColor == outlineColor)
    }

    @Test
    fun testSetBackgroundColor() = runTest {
        poemThemeViewModel = PoemThemeViewModel(StandardTestDispatcher(testScheduler))
        poemThemeViewModel.updateBackground(
            BackgroundType.COLOR,
            backgroundColor = dummyColor,
            backgroundColorAsInt = backgroundColor
        )

        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.COLOR)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == dummyColor)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == dummyColor)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == backgroundColor)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == backgroundColor)
    }

    @Test
    fun testChangeBackgroundType() = runTest {
        poemThemeViewModel = PoemThemeViewModel(StandardTestDispatcher(testScheduler))
        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.DEFAULT)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == BackgroundType.DEFAULT)

        poemThemeViewModel.updateBackground(BackgroundType.COLOR, dummyColor, backgroundColor)

        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.COLOR)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == BackgroundType.COLOR)

        poemThemeViewModel.changeBackgroundType(BackgroundType.IMAGE)

        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == null)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == null)
        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.IMAGE)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == BackgroundType.IMAGE)
    }

    /**
     * Tests that the Poem Theme parser parses the poem theme correctly. Furthermore, it verifies
     * the correctness of the current data held in the view model to assert that it is correct
     */
    @Test
    fun testInitialPoemTheme(): Unit = runTest {
        poemThemeViewModel = PoemThemeViewModel(StandardTestDispatcher(testScheduler))
        val poemTheme = PoemTheme(BackgroundType.DEFAULT, mockContext)
        val result =
            PoemThemeXmlParser(poemTheme, mockContext, StandardTestDispatcher(testScheduler))

        assert(result.parseTheme("Outline_With_Color") == 0)

        poemThemeViewModel.initialisePoemTheme(result)
        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.OUTLINE_WITH_COLOR)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == BackgroundType.OUTLINE_WITH_COLOR)

        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == "#ff59ef")
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == "#ff59ef")

        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == -42513)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == -42513)

        assert(poemThemeViewModel.modelState.value.outlineColor == -7821273)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outlineColor == -7821273)

        assert(poemThemeViewModel.modelState.value.poemThemeState.imagePath == "")
        assert(poemThemeViewModel.modelState.value.backgroundImageChosen == "")

        assert(poemThemeViewModel.modelState.value.poemThemeState.outline == OutlineTypes.ROUNDED_RECTANGLE.toString())
        assert(poemThemeViewModel.modelState.value.outlineType == OutlineTypes.ROUNDED_RECTANGLE)

        assert(poemThemeViewModel.modelState.value.poemThemeState.textSize == 14)
        assert(poemThemeViewModel.modelState.value.fontSize == 14f)

        assert(poemThemeViewModel.modelState.value.poemThemeState.textColor == "#000000")

        assert(poemThemeViewModel.modelState.value.poemThemeState.textColorAsInt == -16777216)
        assert(poemThemeViewModel.modelState.value.fontColor == -16777216)

        assert(poemThemeViewModel.modelState.value.poemThemeState.textAlignment == TextAlignment.CENTRE_VERTICAL)
        assert(poemThemeViewModel.modelState.value.textAlignment == TextAlignment.CENTRE_VERTICAL)

        assert(poemThemeViewModel.modelState.value.poemThemeState.textFontFamily == "Default")
        assert(poemThemeViewModel.modelState.value.textFontFamilyString == "Default")
    }

    @Test
    fun testOutlineWithImageBackgroundInitialisePoem(): Unit = runTest {
        poemThemeViewModel = PoemThemeViewModel(StandardTestDispatcher(testScheduler))
        val poemTheme = PoemTheme(BackgroundType.DEFAULT, mockContext)
        val result =
            PoemThemeXmlParser(poemTheme, mockContext, StandardTestDispatcher(testScheduler))

        assert(result.parseTheme("Outline_With_Image") == 0)

        poemThemeViewModel.initialisePoemTheme(result)
        assert(poemThemeViewModel.modelState.value.backgroundType == BackgroundType.OUTLINE_WITH_IMAGE)
        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundType == BackgroundType.OUTLINE_WITH_IMAGE)

        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColor == "#FFFFFF")
        assert(poemThemeViewModel.modelState.value.backgroundColorChosen == "#FFFFFF")

        assert(poemThemeViewModel.modelState.value.poemThemeState.backgroundColorAsInt == -1)
        assert(poemThemeViewModel.modelState.value.backgroundColorChosenAsInt == -1)

        assert(poemThemeViewModel.modelState.value.outlineColor == -7821273)
        assert(poemThemeViewModel.modelState.value.poemThemeState.outlineColor == -7821273)

        assert(poemThemeViewModel.modelState.value.poemThemeState.imagePath == "app/src/test/java/com/wendorochena/poetskingdom/MockFiles/test_images/createpoem.jpg")
        assert(poemThemeViewModel.modelState.value.backgroundImageChosen == "app/src/test/java/com/wendorochena/poetskingdom/MockFiles/test_images/createpoem.jpg")

        assert(poemThemeViewModel.modelState.value.poemThemeState.outline == OutlineTypes.ROUNDED_RECTANGLE.toString())
        assert(poemThemeViewModel.modelState.value.outlineType == OutlineTypes.ROUNDED_RECTANGLE)

        assert(poemThemeViewModel.modelState.value.poemThemeState.textSize == 14)
        assert(poemThemeViewModel.modelState.value.fontSize == 14f)

        assert(poemThemeViewModel.modelState.value.poemThemeState.textColor == "#fdfcff")

        assert(poemThemeViewModel.modelState.value.poemThemeState.textColorAsInt == -131841)
        assert(poemThemeViewModel.modelState.value.fontColor == -131841)

        assert(poemThemeViewModel.modelState.value.textAlignment == TextAlignment.LEFT)
        assert(poemThemeViewModel.modelState.value.poemThemeState.textAlignment == TextAlignment.LEFT)

        assert(poemThemeViewModel.modelState.value.poemThemeState.textFontFamily == "Default")
        assert(poemThemeViewModel.modelState.value.textFontFamilyString == "Default")
    }

    @Test
    fun boldenText() = runTest {
        poemThemeViewModel = PoemThemeViewModel(StandardTestDispatcher(testScheduler))

        poemThemeViewModel.boldenOrItaliciseText("bold")

        assert(poemThemeViewModel.modelState.value.isBold)
        assert(poemThemeViewModel.modelState.value.poemThemeState.bold)

        assert(!poemThemeViewModel.modelState.value.poemThemeState.italic)
        assert(!poemThemeViewModel.modelState.value.isItalic)
    }

    @Test
    fun testItaliciseText() = runTest {
        poemThemeViewModel = PoemThemeViewModel(StandardTestDispatcher(testScheduler))

        poemThemeViewModel.boldenOrItaliciseText("italic")
        assert(!poemThemeViewModel.modelState.value.isBold)
        assert(!poemThemeViewModel.modelState.value.poemThemeState.bold)

        assert(poemThemeViewModel.modelState.value.poemThemeState.italic)
        assert(poemThemeViewModel.modelState.value.isItalic)
    }



    @Test
    fun testSavePoemTheme() = runTest {
        val mainDispatcher = StandardTestDispatcher(testScheduler)
        val poemName = "POEM"

        poemThemeViewModel = PoemThemeViewModel(mainDispatcher = mainDispatcher, ioDispatcher = mainDispatcher)

        var counter = 0
        val blackBackgroundColor = LightBlack.toArgb()
        poemThemeViewModel.updateBackground(BackgroundType.IMAGE, imagePath = dummyImagePath)
        poemThemeViewModel.setTextAlign(TextAlignment.CENTRE_VERTICAL)
        poemThemeViewModel.setTextColor(blackBackgroundColor, "#454545")
        poemThemeViewModel.setTextSize(25f)
        poemThemeViewModel.setTextMarginUtility(TextMarginUtil(5,5,5,5))
        poemThemeViewModel.savePoemTheme(poemName, mockContext, false, { a, b -> counter++ })

        testScheduler.advanceUntilIdle()
        //asserts start activity was invoked
        assert(counter == 1)
        val savedFileTheme = File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/themes/$poemName.xml")
        assert(savedFileTheme.exists())
        savedFileTheme.delete()
    }

}