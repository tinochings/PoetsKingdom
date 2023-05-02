package com.wendorochena.poetskingdom.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.ui.theme.MadzinzaGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class HeadingSelection {
    OUTLINE, BACKGROUND, TEXT
}

class PoemThemeViewModel : ViewModel() {

    private val _poemThemeState = MutableStateFlow(PoemTheme(BackgroundType.DEFAULT))
    private val uiState: StateFlow<PoemTheme> = _poemThemeState.asStateFlow()
    var headingSelection by mutableStateOf(HeadingSelection.OUTLINE)
        private set
    var backgroundType by mutableStateOf(BackgroundType.DEFAULT)
        private set
    var backgroundImageChosen: String? by mutableStateOf(null)
    var backgroundColorChosen: String? = null
    var backgroundColorChosenAsInt: Int? by mutableStateOf(null)
    var outlineType: OutlineTypes? by mutableStateOf(null)
    var outlineColor: Int by mutableStateOf(MadzinzaGreen.toArgb())
    var fontColor: Int by mutableStateOf(Color.Black.toArgb())
    var textFontFamily : FontFamily by mutableStateOf(FontFamily(android.graphics.Typeface.DEFAULT))
    var fontSize : Float by mutableStateOf(14f)
        private set
    var textAlignment : TextAlignment by mutableStateOf(TextAlignment.LEFT)

    fun changeBackgroundType(backgroundType: BackgroundType) {
        if (this.backgroundType.toString().lowercase()
                .contains("color") && !backgroundType.toString().lowercase().contains("color")
        ) {
            backgroundColorChosen = null
            backgroundColorChosenAsInt = null
        } else if (this.backgroundType.toString().lowercase()
                .contains("image") && !backgroundType.toString().lowercase().contains("image")
        ) {
            backgroundImageChosen = null
        }
        this.backgroundType = backgroundType
        _poemThemeState.value.backgroundType = backgroundType
    }

    //color background
    /**
     * Color Background
     */
    fun updateBackground(
        backgroundType: BackgroundType,
        backgroundColor: String,
        backgroundColorAsInt: Int
    ) {
        uiState.value.backgroundType = backgroundType
        changeBackgroundType(backgroundType)
        backgroundColorChosen = backgroundColor
        backgroundColorChosenAsInt  = backgroundColorAsInt
        uiState.value.backgroundColor = backgroundColor
        uiState.value.backgroundColorAsInt = backgroundColorAsInt
    }
    private fun parseOutlineType(outline: String) : OutlineTypes {
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

    fun shapeFromOutline() : Shape {
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
    //outline and color background
    /**
     * OUTLINE_WITH_COLOR
     */
    fun updateBackground(
        backgroundType: BackgroundType,
        outlineColor: Int,
        outline: String,
        backgroundColor: String,
        backgroundColorAsInt: Int
    ) {
        uiState.value.backgroundType = backgroundType
        uiState.value.outline = outline
        uiState.value.outlineColor = outlineColor
        this.outlineColor = outlineColor
        outlineType = parseOutlineType(outline)
        changeBackgroundType(backgroundType)
        uiState.value.backgroundColor = backgroundColor
        uiState.value.backgroundColorAsInt = backgroundColorAsInt
    }
    //outline and image background
    /**
     * OUTLINE_WITH_IMAGE
     */
    fun updateBackground(
        backgroundType: BackgroundType,
        outlineColor: Int,
        outline: String,
        imagePath: String
    ) {
        changeBackgroundType(backgroundType)
        uiState.value.backgroundType = backgroundType
        uiState.value.outlineColor = outlineColor
        this.outlineColor = outlineColor
        backgroundImageChosen = imagePath
        outlineType = parseOutlineType(outline)
        uiState.value.outline = outline
        uiState.value.imagePath = imagePath
    }
    //outline background
    /**
     * OUTLINE
     */
    fun updateBackground(backgroundType: BackgroundType, outlineColor: Int, outline: OutlineTypes) {
        uiState.value.backgroundType = backgroundType
        uiState.value.outline = outline.name
        outlineType = outline
        this.outlineColor = outlineColor
        changeBackgroundType(backgroundType)
        uiState.value.outlineColor = outlineColor
    }
    //image background
    /**
     * IMAGE BACKGROUND
     */
    fun updateBackground(backgroundType: BackgroundType, imagePath: String) {
        uiState.value.backgroundType = backgroundType
        changeBackgroundType(backgroundType)
        backgroundImageChosen = imagePath
        uiState.value.imagePath = imagePath
    }

    fun changeSelection(headingSelection: HeadingSelection) {
        this.headingSelection = headingSelection
    }

    fun unselectedHeadings(): ArrayList<HeadingSelection> {
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

    fun setTextSize(textSize: Float) {
        uiState.value.textSize = textSize.toInt()
        this.fontSize = textSize
    }

    fun setFontFamily(fontFamily: FontFamily) {
        this.textFontFamily = fontFamily
    }
    fun setTextColor(color : Int) {
        this.fontColor = color
    }
    fun setTextAlign(textAlignToAdd: TextAlignment) {
        textAlignment = textAlignToAdd
    }
    /**
     * @return a Pair where the first and second values are the options to select in the removal dialog
     */
    fun changePreviewBackground(): Pair<String, String> {
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
                changeBackgroundType(BackgroundType.DEFAULT)
                Pair("", "")
            }
        }
    }

    fun texAlignmentToTextAlign() : TextAlign {
        return when (textAlignment) {
            TextAlignment.LEFT, TextAlignment.CENTRE_VERTICAL_LEFT -> {
                TextAlign.Start
            }
            TextAlignment.CENTRE,TextAlignment.CENTRE_VERTICAL  -> {
                TextAlign.Center
            }
             TextAlignment.CENTRE_VERTICAL_RIGHT, TextAlignment.RIGHT-> {
                TextAlign.End
            }
        }
    }
    fun boxAlignment() : Alignment {
        return when (textAlignment) {
            TextAlignment.LEFT, TextAlignment.CENTRE_VERTICAL_LEFT -> {
                Alignment.TopStart
            }
            TextAlignment.CENTRE,TextAlignment.CENTRE_VERTICAL  -> {
                Alignment.Center
            }
            TextAlignment.CENTRE_VERTICAL_RIGHT, TextAlignment.RIGHT-> {
                Alignment.TopEnd
            }
        }
    }
//    fun savePoemTheme() {
//        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
//            exception.printStackTrace()
//            //add a better wayy to manage failure
//            println("Error saving file")
//        }
//
//        viewModelScope.launch(Dispatchers.Main + exceptionHandler) {
//            val poemTheme = _poemThemeState.value
//            val poemThemeXmlParser =
//                PoemThemeXmlParser(poemTheme)
//            poemThemeXmlParser.setIsEditTheme(true)
//
//            poemThemeSaveValue = poemThemeXmlParser.savePoemThemeToLocalFile(
//                    poemTheme.imagePath,
//                    poemTheme.backgroundColor,
//                    poemTheme.outline
//                )
//        }
//    }
}