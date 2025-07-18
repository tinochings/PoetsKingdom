package com.wendorochena.poetskingdom.viewModels

import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.utils.TextMarginUtil
import com.wendorochena.poetskingdom.utils.TypefaceHelper
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.images.loaders.ImageLoaderUtilityFileName
import com.wendorochena.poetskingdom.viewModels.models.PoemThemeViewModelModel
import com.wendorochena.poetskingdom.viewModels.services.PoemThemeViewModelService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HeadingSelection {
    OUTLINE, BACKGROUND, TEXT
}

class PoemThemeViewModel(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main) :
    ViewModel() {

    private val _model = MutableStateFlow(PoemThemeViewModelModel())
    val modelState: StateFlow<PoemThemeViewModelModel> = _model.asStateFlow()
    val viewModelService = PoemThemeViewModelService()

    /**
     * Clears background state if the previous state had a color background or an image background.
     * Finally it updates the UI background type and the poem theme background type
     * @param backgroundType the background type to set
     */
    fun changeBackgroundType(backgroundType: BackgroundType) {
        //clear state if need be
        if (modelState.value.backgroundType.toString().lowercase()
                .contains("color") && !backgroundType.toString().lowercase().contains("color")
        ) {
            updateModelState { state ->
                state.copy(
                    backgroundColorChosen = null, backgroundColorChosenAsInt = null,
                    poemThemeState = state.poemThemeState.copy(
                        backgroundColor = "#FFFFFF",
                        backgroundColorAsInt = -1
                    )
                )
            }
        }
        if (modelState.value.backgroundType.toString().lowercase()
                .contains("image") && !backgroundType.toString().lowercase().contains("image")
        ) {
            updateModelState { state ->
                state.copy(
                    backgroundImageChosen = null,
                    poemThemeState = state.poemThemeState.copy(imagePath = "")
                )
            }
        }
        if (modelState.value.backgroundType.toString().lowercase()
                .contains("outline") && !backgroundType.toString().lowercase().contains("outline")
        ) {
            updateModelState { state ->
                state.copy(
                    outlineType = null,
                    outlineColor = -7821273,
                    poemThemeState = state.poemThemeState.copy(outline = "", outlineColor = -7821273)
                )
            }
        }
        updateModelState { state ->
            state.copy(
                backgroundType = backgroundType,
                poemThemeState = state.poemThemeState.copy(backgroundType = backgroundType)
            )
        }
    }


    /**
     * Updates the current background to a Color Background
     * @param backgroundType the background type to set
     * @param backgroundColor the background color represented as a string
     * @param backgroundColorAsInt the background color represented as an int
     */
    fun updateBackground(
        backgroundType: BackgroundType,
        backgroundColor: String,
        backgroundColorAsInt: Int
    ) {
        //positioning of this call more often than not is negligible due to the state of the model
        //always being the same within the scope of this function. However it doesnt hurt just to put
        //it before the rest of the function
        changeBackgroundType(backgroundType)

        updateModelState { state ->
            state.copy(
                backgroundColorChosen = backgroundColor,
                backgroundColorChosenAsInt = backgroundColorAsInt,
                poemThemeState = state.poemThemeState.copy(
                    backgroundColor = backgroundColor, backgroundColorAsInt = backgroundColorAsInt
                )
            )
        }
    }

    /**
     * Parses outline from the saved string
     * @param outline string value representing an outline
     */
    private fun parseOutlineType(outline: String): OutlineTypes {
        return viewModelService.parseOutlineType(outline)
    }


    /**
     * Updates the background to an OUTLINE_WITH_COLOR
     * @param backgroundType the background type to set
     * @param outlineColor the color of the outline as an int
     * @param outline OutlineType.name value of the outline to use
     * @param backgroundColorAsInt the background color represented as an int
     */
    fun updateBackground(
        backgroundType: BackgroundType,
        outlineColor: Int,
        outline: String,
        backgroundColor: String,
        backgroundColorAsInt: Int
    ) {
        changeBackgroundType(backgroundType)
        updateModelState { state ->
            state.copy(
                outlineType = parseOutlineType(outline),
                outlineColor = outlineColor,
                backgroundColorChosen = backgroundColor,
                backgroundColorChosenAsInt = backgroundColorAsInt,
                poemThemeState = state.poemThemeState.copy(
                    outline = outline, outlineColor = outlineColor,
                    backgroundColor = backgroundColor, backgroundColorAsInt = backgroundColorAsInt
                )
            )
        }
    }

    /**
     * Updates the background to an OUTLINE_WITH_IMAGE
     * @param backgroundType the background type to set
     * @param outlineColor the color of the outline as an int
     * @param outline OutlineType.name value of the outline to use
     * @param imagePath the path to the image for the background
     */
    fun updateBackground(
        backgroundType: BackgroundType,
        outlineColor: Int,
        outline: String,
        imagePath: String
    ) {
        changeBackgroundType(backgroundType)
        updateModelState { state ->
            state.copy(
                outlineColor = outlineColor, outlineType = parseOutlineType(outline),
                backgroundImageChosen = imagePath,
                poemThemeState = state.poemThemeState.copy(
                    outlineColor = outlineColor, outline = outline, imagePath = imagePath
                )
            )
        }
    }

    /**
     * Updates the background to an OUTLINE
     * @param backgroundType the background type to set
     * @param outlineColor the color of the outline as an int
     * @param outline OutlineType.name value of the outline to use
     */
    fun updateBackground(backgroundType: BackgroundType, outlineColor: Int, outline: OutlineTypes) {
        changeBackgroundType(backgroundType)
        updateModelState { state ->
            state.copy(
                outlineType = outline, outlineColor = outlineColor,
                poemThemeState = state.poemThemeState.copy(
                    backgroundType = backgroundType,
                    outline = outline.name, outlineColor = outlineColor
                )
            )
        }
    }

    /**
     * Updates the background to an IMAGE
     * @param backgroundType the background type to set
     *  @param imagePath the path to the image for the background
     */
    fun updateBackground(backgroundType: BackgroundType, imagePath: String) {
        changeBackgroundType(backgroundType)

        updateModelState { state ->
            state.copy(
                backgroundImageChosen = imagePath, poemThemeState =
                state.poemThemeState.copy(imagePath = imagePath)
            )
        }
    }


    /**
     * Sets the text margin utility
     * @param textMarginUtility the text margin utility to set
     */
    fun setTextMarginUtility(textMarginUtility: TextMarginUtil) {
        updateModelState { state ->
            state.copy(poemThemeState = state.poemThemeState.copy(textMarginUtil = textMarginUtility),
                textMarginUtil = textMarginUtility)
        }
    }

    /**
     * Changes heading selection
     * @param headingSelection the heading selection to set
     */
    fun changeSelection(headingSelection: HeadingSelection) {
        updateModelState { state ->
            state.copy(headingSelection = headingSelection)
        }
    }

    /**
     * Sets text size for poem theme
     * @param textSize the text size to set
     */
    fun setTextSize(textSize: Float) {
        updateModelState { state ->
            state.copy(
                poemThemeState = state.poemThemeState.copy(textSize = textSize.toInt()),
                fontSize = textSize
            )
        }
    }

    /**
     * Sets font family for poem theme
     * @param fontFamily the font family to set
     * @param fontFamilyString the font family to set as a string
     */
    fun setFontFamily(fontFamily: FontFamily, fontFamilyString: String) {
        updateModelState { state ->
            state.copy(
                textFontFamily = fontFamily, textFontFamilyString = fontFamilyString,
                poemThemeState = state.poemThemeState.copy(textFontFamily = fontFamilyString)
            )
        }
    }

    /**
     * Updates the colour of a selected outline
     * @param color the colour selected as an int
     */
    fun updateOutlineColor(color: Int) {
        updateModelState { state ->
            state.copy(
                outlineColor = color,
                poemThemeState = state.poemThemeState.copy(outlineColor = color)
            )
        }
    }

    /**
     * Updates the text color for UI and poem theme state
     * @param color the color represented as an int
     * @param hexCode the color represented as a hexadecimal string
     */
    fun setTextColor(color: Int, hexCode: String) {
        updateModelState { state ->
            state.copy(
                fontColor = color,
                poemThemeState = state.poemThemeState.copy(
                    textColorAsInt = color,
                    textColor = hexCode
                )
            )
        }
    }

    /**
     * Sets text alignment of the UI and poem theme
     * @param textAlignToAdd the text alignment to set
     */
    fun setTextAlign(textAlignToAdd: TextAlignment) {
        updateModelState { state ->
            state.copy(
                textAlignment = textAlignToAdd,
                poemThemeState = state.poemThemeState.copy(textAlignment = textAlignToAdd)
            )
        }
    }

    /**
     * Sets the album name if any
     * @param albumName a name of an album or string
     */
    fun setAlbumName(albumName: String?) {
        updateModelState { state ->
            state.copy(savedAlbumName = albumName)
        }
    }

    /**
     * @param editTheme true or false
     */
    fun setEditTheme(editTheme: Boolean) {
        updateModelState { state ->
            state.copy(isEditTheme = editTheme)
        }
    }

    /**
     * Sets the display dialog view display
     * @param boolean true if the dialog should be open otherwise false
     */
    fun setDisplayDialog(boolean: Boolean) {
        updateModelState { state ->
            state.copy(shouldDisplayDialog = boolean)
        }
    }

    /**
     * Initialises viewModels state by copying the value of the loaded poem
     * @param poemThemeXmlParser the object containing the parsed poem theme
     */
    fun initialisePoemTheme(poemThemeXmlParser: PoemThemeXmlParser) {
        val parsedPoemTheme = poemThemeXmlParser.getPoemTheme()
        updateModelState { state ->
            state.copy(
                poemTitle = parsedPoemTheme.poemTitle,
                backgroundType = parsedPoemTheme.backgroundType,
                outlineType = parseOutlineType(parsedPoemTheme.outline),
                outlineColor = parsedPoemTheme.outlineColor,
                fontColor = parsedPoemTheme.textColorAsInt,
                fontSize = parsedPoemTheme.textSize.toFloat(),
                backgroundColorChosenAsInt = parsedPoemTheme.backgroundColorAsInt,
                backgroundColorChosen = parsedPoemTheme.backgroundColor,
                backgroundImageChosen = parsedPoemTheme.imagePath,
                isBold = parsedPoemTheme.bold,
                isItalic = parsedPoemTheme.italic,
                textMarginUtil = parsedPoemTheme.textMarginUtil,
                textFontFamily = TypefaceHelper.getTypeFace(parsedPoemTheme.textFontFamily),
                textFontFamilyString = parsedPoemTheme.textFontFamily,
                textAlignment = parsedPoemTheme.textAlignment,
                poemThemeState = state.poemThemeState.copy(
                    poemTitle = parsedPoemTheme.poemTitle,
                    backgroundType = parsedPoemTheme.backgroundType,
                    textFontFamily = parsedPoemTheme.textFontFamily,
                    textAlignment = parsedPoemTheme.textAlignment,
                    outline = parsedPoemTheme.outline,
                    outlineColor = parsedPoemTheme.outlineColor,
                    textColorAsInt = parsedPoemTheme.textColorAsInt,
                    textColor = parsedPoemTheme.textColor,
                    textSize = parsedPoemTheme.textSize,
                    backgroundColorAsInt = parsedPoemTheme.backgroundColorAsInt,
                    backgroundColor = parsedPoemTheme.backgroundColor,
                    imagePath = parsedPoemTheme.imagePath,
                    bold = parsedPoemTheme.bold,
                    italic = parsedPoemTheme.italic,
                    textMarginUtil = parsedPoemTheme.textMarginUtil
                )
            )
        }
    }

    /**
     * Resets the poem theme result state to default value
     */
    fun resetResultToDefault() {
        updateModelState { state ->
            state.copy(poemThemeResult = -2)
        }
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
    fun savePoemTheme(poemName: String, context: Context, isEditTheme: Boolean, onStartActivity : (String, String?) -> Unit) {
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        }
        viewModelScope.launch(mainDispatcher + exceptionHandler) {
            //ensure synchronous execution
                _model.update { state ->
                    state.copy(
                        poemTitle = poemName,
                        poemThemeState = state.poemThemeState.copy(poemTitle = poemName)
                    )
                }

            val poemTheme: PoemTheme = modelState.value.poemThemeState
            val poemThemeXmlParser =
                PoemThemeXmlParser(poemTheme, context = context, dispatcher = ioDispatcher)
            poemThemeXmlParser.setIsEditTheme(isEditTheme)

            val savePoemResult = async {
                poemThemeXmlParser.savePoemThemeToLocalFile(
                    modelState.value.backgroundImageChosen,
                    modelState.value.backgroundColorChosen,
                    null
                )
            }
            val poemThemeResult = savePoemResult.await()
            if (poemThemeResult == 0){
                resetResultToDefault()
                onStartActivity(modelState.value.poemTitle, modelState.value.savedAlbumName)
            }
            else {
                _model.update { state ->
                    state.copy(poemThemeResult = poemThemeResult)
                }
            }
        }
    }

    /**
     * Bold-ens or italicises text
     * @param boldOrItalic can only be 'bold' or 'italic'
     */
    fun boldenOrItaliciseText(boldOrItalic: String) {
        if (boldOrItalic == "bold") {
            val isBold = !modelState.value.isBold
            updateModelState { state ->
                state.copy(
                    isBold = isBold,
                    poemThemeState = state.poemThemeState.copy(bold = isBold)
                )
            }

        } else {
            val isItalic = !modelState.value.isItalic
            updateModelState { state ->
                state.copy(
                    isItalic = isItalic,
                    poemThemeState = state.poemThemeState.copy(italic = isItalic)
                )
            }
        }
    }

    /**
     * @param context
     */
    fun loadAllPoemThemes(context: Context){
                val imagesFolder = context.getDir(
            context.getString(R.string.my_images_folder_name),
            Context.MODE_PRIVATE
        )
        viewModelScope.launch (mainDispatcher){
            val allImages = ImageLoaderUtilityFileName(imageFolderType = ImageFolderType.IMAGES).loadAllImages(context, ioDispatcher = ioDispatcher)

                updateModelState { state ->
                    state.copy(imageRequests = allImages)
                }
        }
    }
    /**
     * Updates the models mutable state flow
     * @param function the function containing parameters to update
     */
    private fun updateModelState(function: (PoemThemeViewModelModel) -> PoemThemeViewModelModel) {
        viewModelScope.launch {
            _model.update(function)
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