package com.wendorochena.poetskingdom

import android.content.Context
import com.wendorochena.poetskingdom.poemdata.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class PoemThemeParserTest {

    @Mock
    private lateinit var mockContext: Context
    private lateinit var poemTheme : PoemTheme

    @Before
    fun setUp() {
        mockContext = mock {
            on {
                this.getDir(
                    getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )
            } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/poems")

            on {
                this.getDir("poemThemes", Context.MODE_PRIVATE)
            } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/themes")

        }

        poemTheme = PoemTheme(BackgroundType.DEFAULT, mockContext)
    }

    @Test
    fun testImageBackground() : Unit = runTest {

        val result = PoemThemeXmlParser(poemTheme,mockContext)
        assert(result.parseTheme("Image") == 0)
        val currTheme = result.getPoemTheme()
        println(currTheme)
        assert(currTheme.backgroundType == BackgroundType.IMAGE)
        assert(currTheme.getOutline() == "")
        assert(currTheme.getImagePath() == "app/src/test/java/com/wendorochena/poetskingdom/MockFiles/test_images/mypoems.jpg")
        assert(currTheme.getTextSize() == 14)
        assert(currTheme.getTextColor() == "#fdfcff")
        assert(currTheme.getTextColorAsInt() == -131841)
        assert(currTheme.getTextAlignment() == TextAlignment.LEFT)
        assert(currTheme.getTextFont() == "Default")
    }

    @Test
    fun testDefaultBackground() : Unit = runTest {

        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Default") == 0)
        val currTheme = result.getPoemTheme()

        assert(currTheme.backgroundType == BackgroundType.DEFAULT)
        assert(currTheme.getBackgroundColor() == "#FFFFFF")
        assert(currTheme.getBackgroundColorAsInt() == -1)
        assert(currTheme.getImagePath() == "")
        assert(currTheme.getOutline() == "")
        assert(currTheme.getTextSize() == 14)
        assert(currTheme.getTextColor() == "#000000")
        assert(currTheme.getTextColorAsInt() == -16777216)
        assert(currTheme.getTextAlignment() == TextAlignment.LEFT)
        assert(currTheme.getTextFont() == "Default")
    }

    @Test
    fun testColorBackground () : Unit = runTest{
        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Color") == 0)
        val currTheme = result.getPoemTheme()

        assert(currTheme.backgroundType == BackgroundType.COLOR)
        assert(currTheme.getBackgroundColor() == "#ff59ef")
        assert(currTheme.getBackgroundColorAsInt() == -42513)
        assert(currTheme.getImagePath() == "")
        assert(currTheme.getOutline() == "")
        assert(currTheme.getTextSize() == 14)
        assert(currTheme.getTextColor() == "#000000")
        assert(currTheme.getTextColorAsInt() == -16777216)
        assert(currTheme.getTextAlignment() == TextAlignment.CENTRE_VERTICAL)
        assert(currTheme.getTextFont() == "Default")
    }

    @Test
    fun testOutlineBackground () : Unit = runTest{
        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Outline") == 0)
        val currTheme = result.getPoemTheme()

        assert(currTheme.backgroundType == BackgroundType.OUTLINE)
        assert(currTheme.getBackgroundColor() == "#FFFFFFFF")
        assert(currTheme.getBackgroundColorAsInt() == -1)
        assert(currTheme.getImagePath() == "")
        assert(currTheme.getOutline() == OutlineTypes.ROUNDED_RECTANGLE.toString())
        assert(currTheme.getTextSize() == 14)
        assert(currTheme.getTextColor() == "#000000")
        assert(currTheme.getTextColorAsInt() == -16777216)
        assert(currTheme.getTextAlignment() == TextAlignment.LEFT)
        assert(currTheme.getTextFont() == "Default")
    }

    @Test
    fun testOutlineWithColorBackground () : Unit = runTest{
        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Outline_With_Color") == 0)
        val currTheme = result.getPoemTheme()

        assert(currTheme.backgroundType == BackgroundType.OUTLINE_WITH_COLOR)
        assert(currTheme.getBackgroundColor() == "#ff59ef")
        assert(currTheme.getBackgroundColorAsInt() == -42513)
        assert(currTheme.getOutlineColor() == -7821273)
        assert(currTheme.getImagePath() == "")
        assert(currTheme.getOutline() == OutlineTypes.ROUNDED_RECTANGLE.toString())
        assert(currTheme.getTextSize() == 14)
        assert(currTheme.getTextColor() == "#000000")
        assert(currTheme.getTextColorAsInt() == -16777216)
        assert(currTheme.getTextAlignment() == TextAlignment.CENTRE_VERTICAL)
        assert(currTheme.getTextFont() == "Default")
    }

    @Test
    fun testOutlineWithImageBackground () : Unit = runTest{
        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Outline_With_Image") == 0)
        val currTheme = result.getPoemTheme()

        assert(currTheme.backgroundType == BackgroundType.OUTLINE_WITH_IMAGE)
        assert(currTheme.getBackgroundColor() == "#FFFFFFFF")
        assert(currTheme.getBackgroundColorAsInt() == -1)
        assert(currTheme.getOutlineColor() == -7821273)
        assert(currTheme.getImagePath() == "app/src/test/java/com/wendorochena/poetskingdom/MockFiles/test_images/createpoem.jpg")
        assert(currTheme.getOutline() == OutlineTypes.ROUNDED_RECTANGLE.toString())
        assert(currTheme.getTextSize() == 14)
        assert(currTheme.getTextColor() == "#fdfcff")
        assert(currTheme.getTextColorAsInt() == -131841)
        assert(currTheme.getTextAlignment() == TextAlignment.LEFT)
        assert(currTheme.getTextFont() == "Default")
    }


}