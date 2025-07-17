package com.wendorochena.poetskingdom.viewModels.services

import android.content.Context
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.viewModels.HeadingSelection
import com.wendorochena.poetskingdom.viewModels.models.PoemThemeViewModelModel

class PoemThemeViewModelService {

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
    fun shapeFromOutline(outlineType: OutlineTypes?): Shape {
        when (outlineType) {
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

            null -> {
                return RectangleShape
            }
        }
    }

    /**
     * Returns an arraylist containing headings not selected
     */
    fun unselectedHeadings(headingSelection: HeadingSelection): ArrayList<HeadingSelection> {
        return when (headingSelection) {
            HeadingSelection.OUTLINE -> {
                arrayListOf(HeadingSelection.BACKGROUND, HeadingSelection.TEXT)
            }

            HeadingSelection.TEXT -> {
                arrayListOf(HeadingSelection.OUTLINE, HeadingSelection.BACKGROUND)
            }

            HeadingSelection.BACKGROUND -> {
                arrayListOf(HeadingSelection.OUTLINE, HeadingSelection.TEXT)
            }
        }
    }

    fun changePreviewBackground(
        backgroundType: BackgroundType,
        restoreDefault: () -> Unit
    ): Pair<String, String> {
        return when (backgroundType) {
            BackgroundType.OUTLINE_WITH_COLOR -> {
                Pair("Outline", "Color")
            }

            BackgroundType.OUTLINE_WITH_IMAGE -> {
                Pair("Outline", "Image")
            }

            BackgroundType.DEFAULT -> {
                Pair("", "")
            }

            else -> {
                restoreDefault.invoke()
                Pair("", "")
            }
        }
    }

    /**
     * Returns the TextAlign value of the current text alignment of the poem
     */
    fun texAlignmentToTextAlign(textAlignment: TextAlignment): TextAlign {
        return when (textAlignment) {
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
     *
     */
    fun boxAlignment(textAlignment: TextAlignment): Alignment {
        return when (textAlignment) {
            TextAlignment.LEFT, TextAlignment.CENTRE_VERTICAL_LEFT -> {
                Alignment.TopStart
            }

            TextAlignment.CENTRE, TextAlignment.CENTRE_VERTICAL -> {
                Alignment.Center
            }

            TextAlignment.CENTRE_VERTICAL_RIGHT, TextAlignment.RIGHT -> {
                Alignment.TopEnd
            }
        }
    }

    /**
     * Simple algorithm that checks whether the string typed by a user is safe
     */
    fun isValidatedInput(toValidate: String): Boolean {
        if (toValidate.isEmpty())
            return false
        for (char in toValidate) {
            if (char == '_')
                continue
            if (!char.isLetterOrDigit() || char.isWhitespace()) {
                return false
            }
        }
        return true
    }

    /**
     * Determines where the
     */
    fun determineFirstUse(context: Context, key: String): Boolean {
        val sharedPreferences =
            context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean(key, false)) {
            sharedPreferences.edit().putBoolean(key, true).apply()
            return true
        }
        return false
    }

    /**
     * Formats a font name. The beginning letter and every subsequent letter after a space character are capitalised. The underscore character is removed and replaced with an empty space
     */
    fun formatFontItem(fontItem: String): String {
        val typefaceNameArr = fontItem.split('_')
        var fontText = if (typefaceNameArr.size > 1)
            ""
        else
            fontItem[0].uppercase() + fontItem.substring(1, fontItem.length)
        if (fontText == "") {
            for ((index, word) in typefaceNameArr.withIndex()) {
                val wordToAdd = word[0].uppercase() + word.substring(1, word.length)
                fontText += if (index < (typefaceNameArr.size - 1)) {
                    "$wordToAdd "
                } else {
                    wordToAdd
                }
            }
        }
        return fontText
    }

    fun onColorChange(
        headingSelection: HeadingSelection,
        modelState: PoemThemeViewModelModel,
        onUpdateBackgroundColorAndOutline: (backgroundType: BackgroundType, outlineColor: Int, outline: String, backgroundColor: String, backgroundColorAsInt: Int) -> Unit,
        onUpdateBackgroundColor: (backgroundType: BackgroundType, backgroundColor: String, backgroundColorAsInt: Int) -> Unit,
        onSetTextColor: (Int, String) -> Unit,
        onOutlineColorUpdate: (Int) -> Unit,
        colorHexString: String, colorInt: Int
    ) {
        when (headingSelection) {
            HeadingSelection.BACKGROUND -> {
                if (modelState.backgroundType.name.contains("OUTLINE")
                ) {
                    onUpdateBackgroundColorAndOutline.invoke(
                        BackgroundType.OUTLINE_WITH_COLOR,
                        modelState.outlineColor,
                        modelState.outlineType?.name!!,
                        colorHexString,
                        colorInt
                    )
                } else {
                    onUpdateBackgroundColor.invoke(
                        BackgroundType.COLOR,
                        colorHexString,
                        colorInt
                    )
                }
            }

            HeadingSelection.TEXT -> {
                onSetTextColor.invoke(colorInt, colorHexString)
            }

            HeadingSelection.OUTLINE -> {
                onOutlineColorUpdate.invoke(colorInt)
            }
        }
    }

    /**
     * Determines the initial color for each heading selection
     * @param headingSelection the current heading
     * @param modelState the current state of the UI
     *
     * @return the current color for the selected heading as an integer
     */
    fun determineInitialColorToUse(
        headingSelection: HeadingSelection,
        modelState: PoemThemeViewModelModel
    ): Color {

        return when (headingSelection) {
            HeadingSelection.BACKGROUND -> {
                if (modelState.backgroundColorChosenAsInt != null)
                    Color(modelState.backgroundColorChosenAsInt)
                else
                    Color.White
            }

            HeadingSelection.TEXT -> {
                Color(modelState.fontColor)
            }

            HeadingSelection.OUTLINE -> {
                Color(modelState.outlineColor)
            }
        }
    }
}