package com.wendorochena.poetskingdom.viewModels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.viewModelScope
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.ui.theme.MadzinzaGreen
import com.wendorochena.poetskingdom.utils.TypefaceHelper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class HeadingSelection {
    OUTLINE, BACKGROUND, TEXT
}

class PoemThemeViewModel : ViewModel() {

    private val _poemThemeState = MutableStateFlow(PoemTheme(BackgroundType.DEFAULT))
    val uiState: StateFlow<PoemTheme> = _poemThemeState.asStateFlow()
    var headingSelection by mutableStateOf(HeadingSelection.OUTLINE)
        private set
    var backgroundType by mutableStateOf(BackgroundType.DEFAULT)
        private set
    var backgroundImageChosen: String? by mutableStateOf(null)
    var backgroundColorChosen: String? = null
    var backgroundColorChosenAsInt: Int? by mutableStateOf(null)
    var outlineType: OutlineTypes? by mutableStateOf(null)
    var outlineColor: Int by mutableIntStateOf(MadzinzaGreen.toArgb())
    var fontColor: Int by mutableIntStateOf(Color.Black.toArgb())
    var textFontFamily: FontFamily by mutableStateOf(FontFamily(android.graphics.Typeface.DEFAULT))
    var textFontFamilyString : String by mutableStateOf("default")
    var fontSize: Float by mutableFloatStateOf(14f)
        private set
    var textAlignment: TextAlignment by mutableStateOf(TextAlignment.LEFT)
    var shouldDisplayDialog by mutableStateOf(false)
        private set
    var poemThemeResult by mutableIntStateOf(-2)
    private set
    var poemTitle =  ""
    var isBold by mutableStateOf(false)
    var isItalic by mutableStateOf(false)
    var isEditTheme = false
    var savedAlbumName : String? = null
    /**
     * Changes Background Type
     */
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
     * Updates Color Background
     */
    fun updateBackground(
        backgroundType: BackgroundType,
        backgroundColor: String,
        backgroundColorAsInt: Int
    ) {
        uiState.value.backgroundType = backgroundType
        changeBackgroundType(backgroundType)
        backgroundColorChosen = backgroundColor
        backgroundColorChosenAsInt = backgroundColorAsInt
        uiState.value.backgroundColor = backgroundColor
        uiState.value.backgroundColorAsInt = backgroundColorAsInt
    }

    /**
     * Parses outline from the saved string
     */
    private fun parseOutlineType(outline: String): OutlineTypes {
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
    fun shapeFromOutline(): Shape {
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
     * Updates an OUTLINE_WITH_COLOR background
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
     * Updates an OUTLINE_WITH_IMAGE background
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
     * Updates an OUTLINE background
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
     * Updates an IMAGE BACKGROUND
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

    /**
     * Returns an arraylist containing headings not selected
     */
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

    fun setFontFamily(fontFamily: FontFamily, fontFamilyString: String) {
        this.textFontFamily = fontFamily
        textFontFamilyString = fontFamilyString
        uiState.value.textFontFamily = fontFamilyString
    }

    fun setTextColor(color: Int, hexCode : String) {
        this.fontColor = color
        uiState.value.textColorAsInt = color
        uiState.value.textColor = hexCode
    }

    fun setTextAlign(textAlignToAdd: TextAlignment) {
        textAlignment = textAlignToAdd
        uiState.value.textAlignment = textAlignToAdd
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

    /**
     * Returns the TextAlign value of the current text alignment of the poem
     */
    fun texAlignmentToTextAlign(): TextAlign {
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
    fun boxAlignment(): Alignment {
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

    fun setDisplayDialog(boolean: Boolean) {
        shouldDisplayDialog = boolean
    }

    /**
     * Initialises viewModels state by copying the value of the loaded poem
     */
     fun initialisePoemTheme(poemThemeXmlParser: PoemThemeXmlParser) {
        uiState.value.poemTitle = poemThemeXmlParser.getPoemTheme().poemTitle
        poemTitle =  uiState.value.poemTitle
        uiState.value.backgroundType = poemThemeXmlParser.getPoemTheme().backgroundType
        backgroundType =  uiState.value.backgroundType
        uiState.value.textFontFamily = poemThemeXmlParser.getPoemTheme().textFontFamily
        setFontFamily(TypefaceHelper.getTypeFace(uiState.value.textFontFamily), uiState.value.textFontFamily)
        uiState.value.textAlignment = poemThemeXmlParser.getPoemTheme().textAlignment
        setTextAlign(uiState.value.textAlignment)
        uiState.value.outline = poemThemeXmlParser.getPoemTheme().outline
        outlineType = parseOutlineType(uiState.value.outline)
        uiState.value.outlineColor = poemThemeXmlParser.getPoemTheme().outlineColor
        outlineColor = uiState.value.outlineColor
        uiState.value.textColorAsInt = poemThemeXmlParser.getPoemTheme().textColorAsInt
        fontColor = uiState.value.textColorAsInt
        uiState.value.textColor = poemThemeXmlParser.getPoemTheme().textColor
        uiState.value.textSize = poemThemeXmlParser.getPoemTheme().textSize
        fontSize = uiState.value.textSize.toFloat()
        uiState.value.backgroundColorAsInt = poemThemeXmlParser.getPoemTheme().backgroundColorAsInt
        backgroundColorChosenAsInt = uiState.value.backgroundColorAsInt
        uiState.value.backgroundColor = poemThemeXmlParser.getPoemTheme().backgroundColor
        backgroundColorChosen = uiState.value.backgroundColor
        uiState.value.imagePath = poemThemeXmlParser.getPoemTheme().imagePath
        backgroundImageChosen = uiState.value.imagePath
        uiState.value.bold = poemThemeXmlParser.getPoemTheme().bold
        isBold = uiState.value.bold
        uiState.value.italic = poemThemeXmlParser.getPoemTheme().italic
        isItalic = uiState.value.italic
    }
    fun resetResultToDefault() {
        poemThemeResult = -2
    }

    /**
     * Saves a poem theme as a file. poemThemeResult is assigned the deferred result to trigger a
     * recomposition which will allow the composable state to respond to a failed file write or a
     * successful one
     *
     * @param poemName the name of the poem
     * @param context the application context
     * @param isEditTheme true if the CreatePoemActivity called PoemThemeActivity
     *
     */
    fun savePoemTheme(poemName : String, context : Context, isEditTheme : Boolean)  {
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            //add a better way to manage failure
//            println("Error saving file")
        }
        uiState.value.poemTitle = poemName
         poemTitle = poemName
        viewModelScope.launch(Dispatchers.Main + exceptionHandler) {
            val poemTheme = _poemThemeState.value
            val poemThemeXmlParser =
                PoemThemeXmlParser(poemTheme, context = context)
            poemThemeXmlParser.setIsEditTheme(isEditTheme)
            val savePoemResult =  async {
                poemThemeXmlParser.savePoemThemeToLocalFile(
                    backgroundImageChosen,
                    backgroundColorChosen,
                    null
                )
            }
            poemThemeResult = savePoemResult.await()
        }
    }

    fun determineFirstUse(context: Context, key : String) : Boolean{
        val sharedPreferences =
            context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean(key, false)) {
            sharedPreferences.edit().putBoolean(key, true).apply()
            return true
        }
        return false
    }

    /**
     * Bold-ens or italicises text
     */
    fun boldenOrItaliciseText(boldOrItalic : String) {
        if (boldOrItalic == "bold") {
            isBold = !isBold
            uiState.value.bold = isBold
        }
        else {
            isItalic = !isItalic
            uiState.value.italic = isItalic
        }
    }

    companion object {
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
    }
}