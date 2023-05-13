package com.wendorochena.poetskingdom.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.poemdata.TextAlignment

/**
 * Currently this is a work in progress. Saving an Image is tightly ingrained into the XML implementation
 * As such critical caution is needed migrating CreatePoem. This ViewModel will initially serve as
 * a read only mode when read only poems are implemented
 */
class CreatePoemViewModel : ViewModel() {

    var poemTheme: PoemTheme = PoemTheme(BackgroundType.DEFAULT)
    var pages = 1
    var currentPageNumber by mutableStateOf(1)
    var hasFileBeenEdited = true
    var isDimmerDisplayed by mutableStateOf(false)
    private val pageNumberAndText: HashMap<Int, String> = HashMap()

    init {
        pageNumberAndText[1] = ""
    }

    fun getPage(pageNumber: Int): String? {
        if (pageNumber in 1..pages)
            return pageNumberAndText[pageNumber]
        return null
    }

    /**
     * Create  memory for current poem theme container
     * @param poemThemeXmlParser The poem theme parser with users saved theme preference
     */
    fun initialisePoemTheme(poemThemeXmlParser: PoemThemeXmlParser) {
        poemTheme.poemTitle = poemThemeXmlParser.getPoemTheme().poemTitle
        poemTheme.backgroundType = poemThemeXmlParser.getPoemTheme().backgroundType
        poemTheme.textFontFamily = poemThemeXmlParser.getPoemTheme().textFontFamily
        poemTheme.textAlignment = poemThemeXmlParser.getPoemTheme().textAlignment
        poemTheme.outline = poemThemeXmlParser.getPoemTheme().outline
        poemTheme.outlineColor = poemThemeXmlParser.getPoemTheme().outlineColor
        poemTheme.textColorAsInt = poemThemeXmlParser.getPoemTheme().textColorAsInt
        poemTheme.textColor = poemThemeXmlParser.getPoemTheme().textColor
        poemTheme.textSize = poemThemeXmlParser.getPoemTheme().textSize
        poemTheme.backgroundColorAsInt = poemThemeXmlParser.getPoemTheme().backgroundColorAsInt
        poemTheme.backgroundColor = poemThemeXmlParser.getPoemTheme().backgroundColor
        poemTheme.imagePath = poemThemeXmlParser.getPoemTheme().imagePath
    }

    /**
     * Returns a shape from the current selected outline
     */
    fun shapeFromOutline(): Shape {
        when (parseOutlineType(poemTheme.outline)) {
            OutlineTypes.RECTANGLE -> {
                return RectangleShape
            }

            OutlineTypes.LEMON -> {
                return com.wendorochena.poetskingdom.ui.theme.LemonOutline
            }

            OutlineTypes.ROTATED_TEARDROP -> {
                return com.wendorochena.poetskingdom.ui.theme.RotatedTeardropOutline
            }

            OutlineTypes.TEARDROP -> {
                return com.wendorochena.poetskingdom.ui.theme.TeardropOutline
            }

            OutlineTypes.ROUNDED_RECTANGLE -> {
                return com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline
            }

        }
    }

    fun texAlignmentToTextAlign(): TextAlign {
        return when (poemTheme.textAlignment) {
            TextAlignment.LEFT, TextAlignment.CENTRE_VERTICAL_LEFT -> {
                TextAlign.Start
            }

            TextAlignment.CENTRE, TextAlignment.CENTRE_VERTICAL -> {
                TextAlign.Center
            }

            TextAlignment.CENTRE_VERTICAL_RIGHT, TextAlignment.RIGHT -> {
                TextAlign.End
            }
        }
    }

    /**
     * Parses outline from the saved string
     */
    fun parseOutlineType(outline: String): OutlineTypes {
        when (outline) {
            OutlineTypes.ROUNDED_RECTANGLE.toString() -> {
                return OutlineTypes.ROUNDED_RECTANGLE
            }

            OutlineTypes.TEARDROP.toString() -> {
                return OutlineTypes.TEARDROP
            }

            OutlineTypes.ROTATED_TEARDROP.toString() -> {
                return OutlineTypes.ROTATED_TEARDROP
            }

            OutlineTypes.RECTANGLE.toString() -> {
                return OutlineTypes.RECTANGLE
            }

            OutlineTypes.LEMON.toString() -> {
                return OutlineTypes.LEMON
            }
        }
        return OutlineTypes.RECTANGLE
    }

    /**
     * Loads the corresponding frame layout views for each stanza
     * @param stanzas The arrayList containing the each stanzas saved text
     */
    fun loadSavedPoem(stanzas: ArrayList<String>) {
        pageNumberAndText[1] = stanzas[0]
        var stanzaCounter = 1

        while (stanzaCounter < stanzas.size) {
            pages++
            pageNumberAndText[pages] = stanzas[stanzaCounter]
            stanzaCounter++
        }
    }

    companion object {
        /**
         * Parses outline from the saved string
         */
        fun parseOutlineType(outline: String): OutlineTypes {
            when (outline) {
                OutlineTypes.ROUNDED_RECTANGLE.toString() -> {
                    return OutlineTypes.ROUNDED_RECTANGLE
                }

                OutlineTypes.TEARDROP.toString() -> {
                    return OutlineTypes.TEARDROP
                }

                OutlineTypes.ROTATED_TEARDROP.toString() -> {
                    return OutlineTypes.ROTATED_TEARDROP
                }

                OutlineTypes.RECTANGLE.toString() -> {
                    return OutlineTypes.RECTANGLE
                }

                OutlineTypes.LEMON.toString() -> {
                    return OutlineTypes.LEMON
                }
            }
            return OutlineTypes.RECTANGLE
        }

        /**
         * Returns a shape from the current selected outline
         */
        fun shapeFromOutline(outline: String): Shape {
            when (parseOutlineType(outline)) {
                OutlineTypes.RECTANGLE -> {
                    return RectangleShape
                }

                OutlineTypes.LEMON -> {
                    return com.wendorochena.poetskingdom.ui.theme.LemonOutline
                }

                OutlineTypes.ROTATED_TEARDROP -> {
                    return com.wendorochena.poetskingdom.ui.theme.RotatedTeardropOutline
                }

                OutlineTypes.TEARDROP -> {
                    return com.wendorochena.poetskingdom.ui.theme.TeardropOutline
                }

                OutlineTypes.ROUNDED_RECTANGLE -> {
                    return com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline
                }

            }
        }
    }
}