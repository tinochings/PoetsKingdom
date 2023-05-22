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
                    "poems",
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
        assert(currTheme.outline == "")
        assert(currTheme.imagePath == "app/src/test/java/com/wendorochena/poetskingdom/MockFiles/test_images/mypoems.jpg")
        assert(currTheme.textSize == 14)
        assert(currTheme.textColor == "#fdfcff")
        assert(currTheme.textColorAsInt == -131841)
        assert(currTheme.textAlignment == TextAlignment.LEFT)
        assert(currTheme.textFontFamily == "Default")
    }

    @Test
    fun testDefaultBackground() : Unit = runTest {

        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Default") == 0)
        val currTheme = result.getPoemTheme()

        assert(currTheme.backgroundType == BackgroundType.DEFAULT)
        assert(currTheme.backgroundColor == "#FFFFFF")
        assert(currTheme.backgroundColorAsInt == -1)
        assert(currTheme.imagePath == "")
        assert(currTheme.outline == "")
        assert(currTheme.textSize == 14)
        assert(currTheme.textColor== "#000000")
        assert(currTheme.textColorAsInt == -16777216)
        assert(currTheme.textAlignment == TextAlignment.LEFT)
        assert(currTheme.textFontFamily== "Default")
    }

    @Test
    fun testColorBackground () : Unit = runTest{
        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Color") == 0)
        val currTheme = result.getPoemTheme()

        assert(currTheme.backgroundType == BackgroundType.COLOR)
        assert(currTheme.backgroundColor == "#ff59ef")
        assert(currTheme.backgroundColorAsInt == -42513)
        assert(currTheme.imagePath == "")
        assert(currTheme.outline == "")
        assert(currTheme.textSize == 14)
        assert(currTheme.textColor == "#000000")
        assert(currTheme.textColorAsInt == -16777216)
        assert(currTheme.textAlignment == TextAlignment.CENTRE_VERTICAL)
        assert(currTheme.textFontFamily == "Default")
    }

    @Test
    fun testOutlineBackground () : Unit = runTest{
        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Outline") == 0)
        val currTheme = result.getPoemTheme()

        assert(currTheme.backgroundType == BackgroundType.OUTLINE)
        assert(currTheme.backgroundColor == "#FFFFFFFF")
        assert(currTheme.backgroundColorAsInt == -1)
        assert(currTheme.imagePath == "")
        assert(currTheme.outline == OutlineTypes.ROUNDED_RECTANGLE.toString())
        assert(currTheme.textSize == 14)
        assert(currTheme.textColor == "#000000")
        assert(currTheme.textColorAsInt == -16777216)
        assert(currTheme.textAlignment == TextAlignment.LEFT)
        assert(currTheme.textFontFamily == "Default")
    }

    @Test
    fun testOutlineWithColorBackground () : Unit = runTest{
        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Outline_With_Color") == 0)
        val currTheme = result.getPoemTheme()

        assert(currTheme.backgroundType == BackgroundType.OUTLINE_WITH_COLOR)
        assert(currTheme.backgroundColor == "#ff59ef")
        assert(currTheme.backgroundColorAsInt == -42513)
        assert(currTheme.outlineColor == -7821273)
        assert(currTheme.imagePath == "")
        assert(currTheme.outline == OutlineTypes.ROUNDED_RECTANGLE.toString())
        assert(currTheme.textSize == 14)
        assert(currTheme.textColor == "#000000")
        assert(currTheme.textColorAsInt == -16777216)
        assert(currTheme.textAlignment == TextAlignment.CENTRE_VERTICAL)
        assert(currTheme.textFontFamily == "Default")
    }

    @Test
    fun testOutlineWithImageBackground () : Unit = runTest{
        val result = PoemThemeXmlParser(poemTheme,mockContext)

        assert(result.parseTheme("Outline_With_Image") == 0)
        val currTheme = result.getPoemTheme()

        assert(currTheme.backgroundType == BackgroundType.OUTLINE_WITH_IMAGE)
        assert(currTheme.backgroundColor == "#FFFFFFFF")
        assert(currTheme.backgroundColorAsInt == -1)
        assert(currTheme.outlineColor == -7821273)
        assert(currTheme.imagePath == "app/src/test/java/com/wendorochena/poetskingdom/MockFiles/test_images/createpoem.jpg")
        assert(currTheme.outline == OutlineTypes.ROUNDED_RECTANGLE.toString())
        assert(currTheme.textSize == 14)
        assert(currTheme.textColor == "#fdfcff")
        assert(currTheme.textColorAsInt == -131841)
        assert(currTheme.textAlignment == TextAlignment.LEFT)
        assert(currTheme.textFontFamily == "Default")
    }


}