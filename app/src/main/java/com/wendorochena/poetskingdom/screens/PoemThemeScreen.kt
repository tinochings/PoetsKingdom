package com.wendorochena.poetskingdom.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.screens.reusables.CircleColorPicker
import com.wendorochena.poetskingdom.screens.reusables.layouts.DialogLayout
import com.wendorochena.poetskingdom.screens.reusables.layouts.SimpleTextDialogBody
import com.wendorochena.poetskingdom.ui.theme.DefaultColor
import com.wendorochena.poetskingdom.ui.theme.DefaultStatusBarColor
import com.wendorochena.poetskingdom.ui.theme.MadzinzaGreen
import com.wendorochena.poetskingdom.ui.theme.OffWhite
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import com.wendorochena.poetskingdom.utils.TextMarginUtil
import com.wendorochena.poetskingdom.utils.TypefaceHelper
import com.wendorochena.poetskingdom.viewModels.HeadingSelection
import com.wendorochena.poetskingdom.viewModels.PoemThemeViewModel
import com.wendorochena.poetskingdom.viewModels.models.PoemThemeViewModelModel
import java.io.File
import kotlin.math.roundToInt

@Composable
fun ThemePoemApp(
    poemThemeViewModel: PoemThemeViewModel,
    onStartActivity : (String, String?) -> Unit
) {
    val isFirstUse = poemThemeViewModel.viewModelService.determineFirstUse(
        LocalContext.current.applicationContext,
        "outlineFirstUse"
    )
    val composableContext = LocalContext.current
    val modelState by poemThemeViewModel.modelState.collectAsStateWithLifecycle()
    val onSetTextMarginUtil: (TextMarginUtil) -> Unit = { textMarginUtil ->
        poemThemeViewModel.setTextMarginUtility(textMarginUtil)
    }
    val onUpdateBackgroundColor: (backgroundType: BackgroundType, backgroundColor: String, backgroundColorAsInt: Int) -> Unit =
        { backgroundType, backgroundColor, backgroundColorAsInt ->
            poemThemeViewModel.updateBackground(
                backgroundType,
                backgroundColor = backgroundColor,
                backgroundColorAsInt
            )
        }
    val onUpdateBackgroundColorAndOutline: (
        backgroundType: BackgroundType,
        outlineColor: Int,
        outline: String,
        backgroundColor: String,
        backgroundColorAsInt: Int
    ) -> Unit =
        { backgroundType, outlineColor, outline, backgroundColor, backgroundColorAsInt ->
            poemThemeViewModel.updateBackground(
                backgroundType, outlineColor, outline, backgroundColor, backgroundColorAsInt
            )
        }
    val onUpdateBackgroundImage: (backgroundType: BackgroundType, imagePath: String) -> Unit =
        { backgroundType, imagePath ->
            poemThemeViewModel.updateBackground(backgroundType, imagePath)
        }
    val onUpdateBackgroundImageAndOutline: (backgroundType: BackgroundType, outlineColor: Int, outline: String, imagePath: String) -> Unit =
        { backgroundType, outlineColor, outline, imagePath ->
            poemThemeViewModel.updateBackground(backgroundType, outlineColor, outline, imagePath)
        }
    val onUpdateBackgroundOutline: (backgroundType: BackgroundType, outlineColor: Int, outline: OutlineTypes) -> Unit =
        { backgroundType, outlineColor, outline ->
            poemThemeViewModel.updateBackground(
                backgroundType,
                outlineColor = outlineColor,
                outline
            )
        }
    val changePreviewBackground: () -> Pair<String, String> = {
        poemThemeViewModel.viewModelService.changePreviewBackground(backgroundType = modelState.backgroundType) {
            poemThemeViewModel.changeBackgroundType(BackgroundType.DEFAULT)
        }
    }
    val shapeFromOutline: () -> Shape = {
        poemThemeViewModel.viewModelService.shapeFromOutline(modelState.outlineType)
    }
    val boxAlignment: () -> Alignment = {
        poemThemeViewModel.viewModelService.boxAlignment(modelState.textAlignment)
    }
    val textAlignmentToTextAlign: () -> TextAlign = {
        poemThemeViewModel.viewModelService.texAlignmentToTextAlign(modelState.textAlignment)
    }
    val onHeadingClicked: (HeadingSelection) -> Unit = {
        poemThemeViewModel.changeSelection(it)
    }
    val onOutlineColorUpdate: (Int) -> Unit = {
        poemThemeViewModel.updateOutlineColor(it)
    }
    val onTextSizeChange: (Float) -> Unit = {
        poemThemeViewModel.setTextSize(it)
    }
    val onSetTextColor: (Int, String) -> Unit = { color, colorString ->
        poemThemeViewModel.setTextColor(color, colorString)
    }
    val onFontItemClicked: (FontFamily, String) -> Unit = { fontFamily, fontFamilyString ->
        poemThemeViewModel.setFontFamily(fontFamily, fontFamilyString)
    }
    val onTextAlignClicked: (TextAlignment) -> Unit = {
        poemThemeViewModel.setTextAlign(it)
    }
    val onBoldOrItalicClicked: (String) -> Unit = {
        poemThemeViewModel.boldenOrItaliciseText(it)
    }
    val onDetermineFirstUse: (Context, String) -> Boolean = { context, key ->
        poemThemeViewModel.viewModelService.determineFirstUse(context, key)
    }
    val onSavePoemTheme: (poemName: String, context: Context, isEditTheme: Boolean) -> Unit =
        { poemName, context, isEditTheme ->
            poemThemeViewModel.savePoemTheme(poemName, context, isEditTheme, onStartActivity)
        }
    val onResetResultToDefault: () -> Unit = {
        poemThemeViewModel.resetResultToDefault()
    }
    val onSetDisplayDialog: (Boolean) -> Unit = {
        poemThemeViewModel.setDisplayDialog(it)
    }
    val onIsValidatedInput: (String) -> Boolean = {
        poemThemeViewModel.viewModelService.isValidatedInput(it)
    }
    val onFormatFontItem: (String) -> String = {
        poemThemeViewModel.viewModelService.formatFontItem(it)
    }
    val onOutlineClicked: (OutlineTypes) -> Unit = { outline ->
        val textMarginUtil = TextMarginUtil()
        textMarginUtil.determineTextMargins(
            outline.name,
            composableContext.resources,
            composableContext.resources.getDimensionPixelSize(R.dimen.strokeSize)
        )
        onSetTextMarginUtil.invoke(textMarginUtil)
        when (modelState.backgroundType) {
            BackgroundType.COLOR, BackgroundType.OUTLINE_WITH_COLOR -> {
                onUpdateBackgroundColorAndOutline(
                    BackgroundType.OUTLINE_WITH_COLOR,
                    modelState.outlineColor,
                    outline.name,
                    modelState.backgroundColorChosen!!,
                    modelState.backgroundColorChosenAsInt!!
                )
            }

            BackgroundType.IMAGE, BackgroundType.OUTLINE_WITH_IMAGE -> {
                onUpdateBackgroundImageAndOutline.invoke(
                    BackgroundType.OUTLINE_WITH_IMAGE,
                    modelState.outlineColor,
                    outline.name,
                    modelState.backgroundImageChosen!!
                )
            }

            else -> {
                onUpdateBackgroundOutline(
                    BackgroundType.OUTLINE,
                    modelState.outlineColor,
                    outline
                )
            }
        }
    }

    val onImageItemClick: (File) -> Unit = {
        if (modelState.backgroundType.name.contains("OUTLINE")) {
            onUpdateBackgroundImageAndOutline(
                BackgroundType.OUTLINE_WITH_IMAGE,
                modelState.outlineColor,
                modelState.outlineType?.name!!,
                it.absolutePath
            )
        } else {
            onUpdateBackgroundImage(BackgroundType.IMAGE, it.absolutePath)
        }
    }

    val onColorPickerInvoked: @Composable (HeadingSelection, () -> Unit) -> Unit =
        { headingSelected, onDismiss ->
            val controller = rememberColorPickerController()
            DialogLayout(
                dialogBody = {
                    val initialColor =
                        poemThemeViewModel.viewModelService.determineInitialColorToUse(
                            headingSelected,
                            modelState
                        )

                    CircleColorPicker(
                        modifier = Modifier
                            .heightIn(max = 350.dp)
                            .padding(10.dp),
                        controller = controller,
                        initialColor = initialColor
                    ) { colorSelected ->
                        poemThemeViewModel.viewModelService.onColorChange(
                            headingSelection = headingSelected,
                            modelState = modelState,
                            onUpdateBackgroundColorAndOutline = onUpdateBackgroundColorAndOutline,
                            onUpdateBackgroundColor = onUpdateBackgroundColor,
                            onSetTextColor = onSetTextColor,
                            onOutlineColorUpdate = onOutlineColorUpdate,
                            colorInt = colorSelected.color.toArgb(),
                            colorHexString = colorSelected.hexCode
                        )
                    }
                },
                positiveButtonText = stringResource(id = R.string.confirm),
                negativeButtonText = stringResource(id = R.string.title_change_cancel),
                onNegativeAction = {
                    onDismiss.invoke()
                    true
                }, onPositiveAction = {
                    onDismiss.invoke()
                    true
                }, onDismiss = {
                    onDismiss.invoke()
                })
        }

    val onPreviewLongClicked: @Composable (() -> Unit) -> Unit = { onDismiss ->
        val returnedPair = changePreviewBackground()
        if (returnedPair.first.isNotEmpty()) {
            val positiveButton = R.string.positive_background_color_outline_button
            val negativeButton = if (returnedPair.second == "Image")
                R.string.negative_background_image_outline_button
            else
                R.string.negative_background_color_outline_button
            val bodyTextId = if (returnedPair.second == "Image")
                R.string.remove_background_image_outline_popup
            else
                R.string.remove_background_color_outline_popup
            val onNegativeButtonClicked = {
                onUpdateBackgroundOutline.invoke(
                    BackgroundType.OUTLINE,
                    modelState.outlineColor,
                    modelState.outlineType!!
                )
                onDismiss.invoke()
                true
            }
            val onPositiveButtonClicked = {
                onSetTextMarginUtil.invoke(TextMarginUtil())
                if (returnedPair.second == "Image")
                    onUpdateBackgroundImage.invoke(
                        BackgroundType.IMAGE,
                        modelState.backgroundImageChosen!!
                    )
                else
                    onUpdateBackgroundColor.invoke(
                        BackgroundType.COLOR,
                        modelState.backgroundColorChosen!!,
                        modelState.backgroundColorChosenAsInt!!
                    )
                onDismiss.invoke()
                true
            }
            DialogLayout(
                dialogBody = {
                    SimpleTextDialogBody(
                        title = stringResource(id = R.string.remove_background_title),
                        textBody = stringResource(
                            id = bodyTextId
                        )
                    )
                },
                positiveButtonText = stringResource(id = positiveButton),
                negativeButtonText = stringResource(id = negativeButton),
                onNegativeAction = onNegativeButtonClicked,
                onPositiveAction = onPositiveButtonClicked,
                onDismiss = onDismiss
            )
        } else {
            onDismiss.invoke()
        }
    }
    val onLoadAllImages : (Context) -> Unit = { context->
        poemThemeViewModel.loadAllPoemThemes(context)
    }

    Scaffold(topBar = {
        val setDisplayDialog: @Composable (Boolean) -> Unit = {
            if (modelState.isEditTheme) {
                poemThemeViewModel.savePoemTheme(
                    modelState.poemTitle,
                    LocalContext.current.applicationContext,
                    modelState.isEditTheme,
                    onStartActivity
                )
            } else {
                poemThemeViewModel.setDisplayDialog(it)
            }
        }
        AppBar(setDisplayDialog = setDisplayDialog, isEditTheme = modelState.isEditTheme)
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            ThemePreview(
                it,
                shapeFromOutline,
                boxAlignment,
                textAlignmentToTextAlign,
                modelState,
                onPreviewLongClicked
            )
            ThemeOptions(
                modelState,
                onHeadingClicked,
                onTextSizeChange,
                onBoldOrItalicClicked,
                onDetermineFirstUse,
                poemThemeViewModel.viewModelService.unselectedHeadings(modelState.headingSelection),
                onFontItemClicked,
                onTextAlignClicked,
                onFormatFontItem,
                onColorPickerInvoked,
                modelState.imageRequests,
                onLoadAllImages,
                onImageItemClick,
                onOutlineClicked
            )

            if (modelState.shouldDisplayDialog)
                SavePoemThemeDialog(
                    modelState,
                    onSavePoemTheme,
                    onResetResultToDefault,
                    onSetDisplayDialog,
                    onIsValidatedInput
                )

            if (isFirstUse) {
                FirstUseDialog(
                    heading = R.string.outline,
                    guideText = R.string.guide_outline,
                    false
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemePreview(
    paddingValues: PaddingValues,
    shapeFromOutline: () -> Shape,
    boxAlignment: () -> Alignment,
    textAlignmentToTextAlign: () -> TextAlign,
    modelState: PoemThemeViewModelModel,
    onPreviewLongClicked: @Composable (() -> Unit) -> Unit
) {
    var shouldChangeBackground by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxHeight(0.44f)
    ) {
        when (modelState.backgroundType) {
            BackgroundType.DEFAULT -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                }
            }

            BackgroundType.COLOR -> {
                val backgroundColor: Int =
                    modelState.backgroundColorChosenAsInt
                        ?: android.graphics.Color.parseColor("#FFFFFFFF")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color(backgroundColor))
                ) {
                }
            }

            BackgroundType.IMAGE -> {
                AsyncImage(
                    model = modelState.backgroundImageChosen,
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            BackgroundType.OUTLINE -> {
                if (modelState.outlineType != null) {
                    DisplayOutline(
                        paddingValues,
                        modelState.outlineType,
                        modelState.outlineColor,
                        modelState.backgroundColorChosenAsInt,
                        onOutlineLongClicked = { _, _ -> })
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                    }
                }
            }

            BackgroundType.OUTLINE_WITH_COLOR -> {
                if (modelState.outlineType != null) {
                    DisplayOutline(
                        paddingValues,
                        modelState.outlineType,
                        modelState.outlineColor,
                        modelState.backgroundColorChosenAsInt
                    ) { _, _ -> }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                    }
                }
            }

            BackgroundType.OUTLINE_WITH_IMAGE -> {
                if (modelState.outlineType != null) {
                    OutlineAndImage(
                        paddingValues = paddingValues,
                        shape = shapeFromOutline(),
                        modelState.backgroundImageChosen!!,
                        modelState.outlineColor
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(paddingValues)
                .combinedClickable(
                    enabled = true,
                    onClick = {},
                    onLongClick = { shouldChangeBackground = true })
        ) {
            if (modelState.backgroundType.name.contains("OUTLINE")) {
                val textMarginUtil = TextMarginUtil()
                textMarginUtil.determineTextMargins(
                    modelState.outlineType!!.toString(),
                    LocalContext.current.resources,
                    LocalContext.current.resources.getDimensionPixelSize(R.dimen.strokeSize)
                )
                val density = LocalContext.current.resources.displayMetrics.densityDpi
                val marginStart = (textMarginUtil.marginLeft / (density / 160f)).roundToInt()
                val marginEnd = (textMarginUtil.marginRight / (density / 160f)).roundToInt()
                val marginTop = (textMarginUtil.marginTop / (density / 160f)).roundToInt()
                val marginBottom = (textMarginUtil.marginBottom / (density / 160f)).roundToInt()
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = boxAlignment()
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = marginStart.dp,
                                end = marginEnd.dp,
                                top = marginTop.dp,
                                bottom = marginBottom.dp
                            ),
                        text = stringResource(id = R.string.preview_text),
                        color = Color(modelState.fontColor),
                        fontFamily = modelState.textFontFamily,
                        textAlign = textAlignmentToTextAlign(),
                        fontSize = modelState.fontSize.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = boxAlignment()
                ) {
                    Text(
                        text = stringResource(id = R.string.preview_text),
                        fontSize = modelState.fontSize.sp,
                        color = Color(modelState.fontColor),
                        fontFamily = modelState.textFontFamily,
                        fontWeight = if (modelState.isBold) FontWeight.Bold else null,
                        fontStyle = if (modelState.isItalic) FontStyle.Italic else null,
                        textAlign = textAlignmentToTextAlign()
                    )
                }
            }
        }
    }
    if (shouldChangeBackground) {
        onPreviewLongClicked { shouldChangeBackground = false }
    }
}

@Composable
fun OutlineAndImage(
    paddingValues: PaddingValues,
    shape: Shape,
    imagePath: String,
    outlineColor: Int
) {
    AsyncImage(
        model = imagePath,
        contentDescription = "",
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .clip(shape)
            .border(
                dimensionResource(id = R.dimen.strokeSize),
                shape = shape,
                color = Color(outlineColor)
            ),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun DisplayOutline(
    paddingValues: PaddingValues,
    outlineType: OutlineTypes,
    outlineColor: Int,
    outlineBackgroundColor: Int?,
    onOutlineLongClicked: @Composable (HeadingSelection, () -> Unit) -> Unit
) {
    when (outlineType) {
        OutlineTypes.LEMON -> {
            if (outlineBackgroundColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .border(
                            BorderStroke(
                                dimensionResource(id = R.dimen.strokeSize),
                                Color(outlineColor)
                            ),
                            shape = com.wendorochena.poetskingdom.ui.theme.LemonOutline
                        )
                        .background(
                            color = Color(outlineBackgroundColor),
                            shape = com.wendorochena.poetskingdom.ui.theme.LemonOutline
                        )
                ) {
                }
            } else {
                LemonOutline(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    outlineColor,
                    onOutlineClicked = { },
                    onOutlineLongClicked = onOutlineLongClicked
                )
            }
        }

        OutlineTypes.RECTANGLE -> {
            if (outlineBackgroundColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .border(
                            BorderStroke(
                                dimensionResource(id = R.dimen.strokeSize),
                                Color(outlineColor)
                            ),
                            shape = RectangleShape
                        )
                        .background(
                            color = Color(outlineBackgroundColor),
                            shape = com.wendorochena.poetskingdom.ui.theme.RectangleOutline
                        )
                ) {
                }
            } else {
                RectangleOutline(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    outlineColor,
                    onOutlineClicked = { },
                    onOutlineLongClicked = onOutlineLongClicked
                )
            }

        }

        OutlineTypes.ROTATED_TEARDROP -> {
            if (outlineBackgroundColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .border(
                            BorderStroke(
                                dimensionResource(id = R.dimen.strokeSize),
                                Color(outlineColor)
                            ),
                            shape = com.wendorochena.poetskingdom.ui.theme.RotatedTeardropOutline
                        )
                        .background(
                            color = Color(outlineBackgroundColor),
                            shape = com.wendorochena.poetskingdom.ui.theme.RotatedTeardropOutline
                        )
                ) {
                }
            } else {
                RotatedTeardropOutline(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    outlineColor,
                    onOutlineClicked = { },
                    onOutlineLongClicked = onOutlineLongClicked
                )
            }

        }

        OutlineTypes.ROUNDED_RECTANGLE -> {
            if (outlineBackgroundColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .border(
                            BorderStroke(
                                dimensionResource(id = R.dimen.strokeSize),
                                Color(outlineColor)
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            color = Color(outlineBackgroundColor),
                            shape = com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline
                        )
                ) {
                }
            } else {
                RoundedRectangleOutline(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    outlineColor,
                    onOutlineClicked = { },
                    onOutlineLongClicked = onOutlineLongClicked
                )
            }
        }

        OutlineTypes.TEARDROP -> {
            if (outlineBackgroundColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .border(
                            BorderStroke(
                                dimensionResource(id = R.dimen.strokeSize),
                                Color(outlineColor)
                            ),
                            shape = com.wendorochena.poetskingdom.ui.theme.TeardropOutline
                        )
                        .background(
                            color = Color(outlineBackgroundColor),
                            shape = com.wendorochena.poetskingdom.ui.theme.TeardropOutline
                        )
                ) {
                }
            } else {
                TeardropOutline(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    outlineColor,
                    onOutlineClicked = { },
                    onOutlineLongClicked = onOutlineLongClicked
                )
            }
        }
    }
}

@Composable
fun ThemeOptions(
    modelState: PoemThemeViewModelModel,
    onHeadingClicked: (HeadingSelection) -> Unit,
    onTextSizeChange: (Float) -> Unit,
    onBoldOrItalicClicked: (String) -> Unit,
    onDetermineFirstUse: (Context, String) -> Boolean,
    unselectedHeadings: ArrayList<HeadingSelection>,
    onFontItemClicked: (FontFamily, String) -> Unit,
    onTextAlignClicked: (TextAlignment) -> Unit,
    onFormatFontItem: (String) -> String,
    onColorPickerInvoked: @Composable (HeadingSelection, () -> Unit) -> Unit,
    imageRequests: List<ImageRequest>, onLoadAllImages : (Context) -> Unit,
    onImageItemClick: (File) -> Unit,
    onOutlineClicked: (OutlineTypes) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            when (modelState.headingSelection) {
                HeadingSelection.TEXT -> {
                    UnselectedHeadingBox(
                        headingName = unselectedHeadings[0],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                    UnselectedHeadingBox(
                        headingName = unselectedHeadings[1],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                    SelectedHeadingBox(
                        headingName = modelState.headingSelection.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .border(
                                BorderStroke(5.dp, MaterialTheme.colors.primaryVariant),
                                shape = RoundedCornerShape(15.dp)
                            )
                    )

                    if (onDetermineFirstUse.invoke(
                            LocalContext.current.applicationContext,
                            "textFirstUse"
                        )
                    ) {
                        FirstUseDialog(
                            heading = R.string.text,
                            guideText = R.string.guide_text,
                            false
                        )
                    }
                }

                HeadingSelection.BACKGROUND -> {
                    UnselectedHeadingBox(
                        headingName = unselectedHeadings[0],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                    SelectedHeadingBox(
                        headingName = modelState.headingSelection.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .border(
                                BorderStroke(5.dp, MaterialTheme.colors.primaryVariant),
                                shape = RoundedCornerShape(15.dp)
                            ),
                    )
                    UnselectedHeadingBox(
                        headingName = unselectedHeadings[1],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                    if (onDetermineFirstUse.invoke(
                            LocalContext.current.applicationContext,
                            "backgroundFirstUse"
                        )
                    ) {
                        FirstUseDialog(
                            heading = R.string.background,
                            guideText = R.string.guide_background,
                            false
                        )
                    }
                }

                HeadingSelection.OUTLINE -> {
                    SelectedHeadingBox(
                        headingName = modelState.headingSelection.name, modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .border(
                                BorderStroke(5.dp, MaterialTheme.colors.primaryVariant),
                                shape = RoundedCornerShape(15.dp)
                            )
                    )
                    UnselectedHeadingBox(
                        headingName = unselectedHeadings[0],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                    UnselectedHeadingBox(
                        headingName = unselectedHeadings[1],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                }
            }
        }
        when (modelState.headingSelection) {
            HeadingSelection.OUTLINE -> {
                OutlineLayout(
                    onOutlineClicked = onOutlineClicked,
                    onOutlineLongClicked = onColorPickerInvoked,
                    outlineColor = modelState.outlineColor
                )
            }

            HeadingSelection.BACKGROUND -> {
                BackgroundLayout(
                    colorPickerDialog = onColorPickerInvoked,
                    onImageItemClick = onImageItemClick,
                    imageRequests = imageRequests,
                    onLoadAllImages = onLoadAllImages
                )
            }

            HeadingSelection.TEXT -> {
                TextLayout(
                    textSizeChange = onTextSizeChange,
                    colorPickerDialog = onColorPickerInvoked,
                    textColor = modelState.fontColor,
                    onFontItemClicked = onFontItemClicked,
                    onTextAlignClicked = onTextAlignClicked,
                    onBoldOrItalicClicked = onBoldOrItalicClicked,
                    defaultTextValue = modelState.fontSize,
                    selectedFont = modelState.textFontFamilyString,
                    isBold = modelState.isBold,
                    isItalic = modelState.isItalic,
                    onFormatFontItem = onFormatFontItem,
                )
            }
        }

    }
}

@Composable
fun BackgroundLayout(
    colorPickerDialog: @Composable (HeadingSelection, () -> Unit) -> Unit,
    onImageItemClick: (File) -> Unit,
    imageRequests: List<ImageRequest>,
    onLoadAllImages : (Context) -> Unit
) {
    var shouldDisplayColorDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(
            space = 5.dp,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        Box(
            modifier = Modifier
                .background(color = DefaultColor, RoundedCornerShape(15.dp))
                .size(80.dp)
                .clickable(enabled = true, onClick = { shouldDisplayColorDialog = true })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.colors),
                    style = MaterialTheme.typography.h1,
                    textAlign = TextAlign.Center,
                    color = OffWhite
                )
                Image(
                    modifier = Modifier
                        .width(80.dp)
                        .height(48.dp),
                    painter = painterResource(id = R.drawable.color_pallete_icon),
                    contentDescription = stringResource(
                        id = R.string.color_palette
                    )
                )
            }
        }
        Box(
            modifier = Modifier
                .background(color = DefaultColor, RoundedCornerShape(15.dp))
                .size(80.dp)
                .border(width = 3.dp, color = DefaultStatusBarColor, RoundedCornerShape(15.dp))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.image),
                    style = MaterialTheme.typography.h1,
                    color = OffWhite,
                    textAlign = TextAlign.Center
                )
                Image(
                    modifier = Modifier
                        .width(80.dp)
                        .height(48.dp),
                    painter = painterResource(id = R.drawable.gallery_icon),
                    contentDescription = stringResource(
                        id = R.string.image
                    )
                )
            }
        }
    }

    if (shouldDisplayColorDialog) {
        colorPickerDialog.invoke(HeadingSelection.BACKGROUND) { shouldDisplayColorDialog = false }
    }
    ImagesGrid(onImageItemClick = onImageItemClick, onLoadAllImages = onLoadAllImages, imageRequests = imageRequests)
}

@Composable
fun SliderLayout(textSizeChange: (Float) -> Unit, defaultTextValue: Float) {
    var sliderPosition by remember { mutableFloatStateOf(defaultTextValue) }
    val thumbColor = if (isSystemInDarkTheme())
        OffWhite
    else
        DefaultColor
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                textAlign = TextAlign.Center,
                text = "Text Size: " + sliderPosition.roundToInt().toString() + "sp",
                style = MaterialTheme.typography.h1,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.fillMaxWidth()
            )
            Slider(
                colors = androidx.compose.material.SliderDefaults.colors(
                    thumbColor = thumbColor, inactiveTrackColor = colorResource(
                        id = R.color.seek_bar_background
                    ), activeTrackColor = colorResource(id = R.color.icon_default_color)
                ),
                enabled = true,
                modifier = Modifier.fillMaxWidth(),
                value = sliderPosition,
                valueRange = 0f..76f,
                onValueChange = { textSizeChange.invoke(it); sliderPosition = it })
        }
    }
}

@Composable
fun TextColorAndAlignment(
    colorPickerDialog: @Composable (HeadingSelection, () -> Unit) -> Unit, textColor: Int,
    onTextAlignClicked: (TextAlignment) -> Unit,
    onBoldOrItalicClicked: (String) -> Unit,
    isBold: Boolean,
    isItalic: Boolean,
) {
    var shouldDisplayColorDialog by remember { mutableStateOf(false) }
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f, true)
                    .height(80.dp)
                    .clickable { shouldDisplayColorDialog = true }
            ) {
                Text(
                    text = stringResource(id = R.string.text_color),
                    style = MaterialTheme.typography.h1,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color(textColor)
                )
                Image(
                    painter = painterResource(id = R.drawable.text_color),
                    contentDescription = "Text Color Image",
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    colorFilter = ColorFilter.tint(color = Color(textColor))
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f, true)
                    .height(80.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_format_align_left_24),
                    contentDescription = stringResource(
                        id = R.string.left_text_align
                    ),
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { onTextAlignClicked.invoke(TextAlignment.LEFT) }
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_format_align_center_24),
                    contentDescription = stringResource(
                        id = R.string.center_text_align
                    ),
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { onTextAlignClicked.invoke(TextAlignment.CENTRE) }
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_align_horizontal_center_24),
                    contentDescription = stringResource(
                        id = R.string.center_horizontal_text
                    ),
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { onTextAlignClicked.invoke(TextAlignment.CENTRE_VERTICAL) }
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_format_align_right_24),
                    contentDescription = stringResource(
                        id = R.string.right_align_text
                    ),
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { onTextAlignClicked.invoke(TextAlignment.RIGHT) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .weight(1f, true)
                    .height(80.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_format_bold_24),
                    contentDescription = stringResource(
                        id = R.string.bold_format_text
                    ),
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .weight(1f)
                        .background(color = if (isBold) MaterialTheme.colors.secondary else MaterialTheme.colors.background)
                        .clickable { onBoldOrItalicClicked.invoke("bold") }
                )
                Image(
                    painter = painterResource(id = R.drawable.baseline_format_italic_24),
                    contentDescription = stringResource(
                        id = R.string.italicise_text
                    ),
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .weight(1f)
                        .background(color = if (isItalic) MaterialTheme.colors.secondary else MaterialTheme.colors.background)
                        .clickable { onBoldOrItalicClicked.invoke("italic") }
                )
            }
        }
    }
    if (shouldDisplayColorDialog) {
        colorPickerDialog.invoke(HeadingSelection.TEXT) { shouldDisplayColorDialog = false }
    }
}

@Composable
fun TextLayout(
    textSizeChange: (Float) -> Unit,
    colorPickerDialog: @Composable (HeadingSelection, () -> Unit) -> Unit,
    textColor: Int,
    onFontItemClicked: (FontFamily, String) -> Unit,
    onTextAlignClicked: (TextAlignment) -> Unit,
    onBoldOrItalicClicked: (String) -> Unit,
    onFormatFontItem: (String) -> String,
    defaultTextValue: Float,
    selectedFont: String,
    isBold: Boolean,
    isItalic: Boolean,
) {
    val allFonts = stringArrayResource(id = R.array.customFontNamesCompose)
    val numOfFonts = allFonts.size
    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.testTag("TextLayoutList")) {
        item(span = { GridItemSpan(2) }) {
            SliderLayout(
                textSizeChange = textSizeChange,
                defaultTextValue = defaultTextValue
            )
        }
        item(span = { GridItemSpan(2) }) {
            TextColorAndAlignment(
                colorPickerDialog,
                textColor = textColor,
                onTextAlignClicked = onTextAlignClicked,
                onBoldOrItalicClicked = onBoldOrItalicClicked,
                isBold = isBold,
                isItalic = isItalic,
            )
        }
        items(numOfFonts) {
            val fontItem = allFonts[it]
            val fontFamily = TypefaceHelper.getTypeFace(fontItem)
            val modifier = if (selectedFont == fontItem)
                Modifier
                    .height(80.dp)
                    .clickable { onFontItemClicked.invoke(fontFamily, fontItem) }
                    .background(color = MaterialTheme.colors.secondary)
            else
                Modifier
                    .height(80.dp)
                    .clickable { onFontItemClicked.invoke(fontFamily, fontItem) }

            FontFaceItem(modifier, fontItem, fontFamily, onFormatFontItem)
        }
    }
}


@Composable
fun FontFaceItem(
    modifier: Modifier,
    fontItem: String,
    fontFamily: FontFamily,
    onFormatFontItem: (String) -> String,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = onFormatFontItem.invoke(fontItem),
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            fontFamily = fontFamily
        )
    }
}

@Composable
fun ImageItem(imageRequest: ImageRequest, modifier: Modifier, onImageItemClick: (File) -> Unit) {
    AsyncImage(
        model = imageRequest, contentDescription = "",
        modifier = modifier
            .width(80.dp)
            .height(80.dp)
            .shadow(elevation = 5.dp)
            .clickable { onImageItemClick.invoke(File(imageRequest.data as String)) },
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ImagesGrid(onImageItemClick: (File) -> Unit, imageRequests: List<ImageRequest>, onLoadAllImages : (Context) -> Unit) {
    var hasLoadedAllImages by remember {
        mutableStateOf(false)
    }
    if (!hasLoadedAllImages){
        onLoadAllImages.invoke(LocalContext.current.applicationContext)
        hasLoadedAllImages = true
    }


    LazyVerticalGrid(
        modifier = Modifier.padding(top = 5.dp), columns = GridCells.Fixed(4),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
            items(count = imageRequests.size, key = {it}) {
                ImageItem(imageRequests[it], modifier = Modifier.padding(3.dp), onImageItemClick)
            }
    }
}

@Composable
fun OutlineLayout(
    onOutlineClicked: (OutlineTypes) -> Unit,
    onOutlineLongClicked: @Composable (HeadingSelection, () -> Unit) -> Unit,
    outlineColor: Int
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.testTag("OutlineLayoutList")
    ) {
        items(count = 1) {
            Column {
                Row(modifier = Modifier.fillMaxSize()) {
                    RoundedRectangleOutline(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 5.dp, end = 5.dp)
                            .height(150.dp)
                            .weight(1f)
                            .testTag("RoundedRectangleOutline"),
                        outlineColor,
                        onOutlineClicked,
                        onOutlineLongClicked
                    )
                    RectangleOutline(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 5.dp, end = 5.dp)
                            .padding(top = 5.dp)
                            .height(150.dp)
                            .weight(1f)
                            .testTag("RectangleOutline"),
                        outlineColor,
                        onOutlineClicked,
                        onOutlineLongClicked
                    )
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    TeardropOutline(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 5.dp, end = 5.dp)
                            .height(150.dp)
                            .weight(1f)
                            .testTag("TeardropOutline"),
                        outlineColor,
                        onOutlineClicked,
                        onOutlineLongClicked
                    )
                    RotatedTeardropOutline(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 5.dp, end = 5.dp)
                            .height(150.dp)
                            .weight(1f)
                            .testTag("RotatedTeardropOutline"),
                        outlineColor,
                        onOutlineClicked,
                        onOutlineLongClicked
                    )
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    LemonOutline(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 5.dp, end = 5.dp)
                            .height(150.dp)
                            .weight(1f)
                            .testTag("LemonOutline"),
                        outlineColor,
                        onOutlineClicked,
                        onOutlineLongClicked
                    )
                    RotatedLemonOutline(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 5.dp, end = 5.dp)
                            .height(150.dp)
                            .weight(1f)
                            .testTag("RotatedLemonOutline"),
                        outlineColor,
                        onOutlineClicked,
                        onOutlineLongClicked
                    )
                }
            }
        }
    }

}

@Composable
fun UnselectedHeadingBox(
    modifier: Modifier, headingName: HeadingSelection,
    onItemClick: (HeadingSelection) -> Unit
) {
    val headingToUse = headingName.name[0] + headingName.name.removeRange(0, 1).lowercase()
    Box(
        modifier.clickable(enabled = true) { onItemClick.invoke(headingName) },
    ) {
        Text(
            text = headingToUse,
            style = MaterialTheme.typography.h1,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SelectedHeadingBox(modifier: Modifier, headingName: String) {
    val headingToUse = headingName[0] + headingName.removeRange(0, 1).lowercase()
    Box(modifier)
    {
        Text(
            text = headingToUse,
            style = MaterialTheme.typography.h1,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Dialog display and state holder for saving a poem theme
 * @param modelState
 * @param onSavePoemTheme
 * @param onResetResultToDefault
 * @param onSetDisplayDialog
 * @param onIsValidatedInput
 */
@Composable
fun SavePoemThemeDialog(
    modelState: PoemThemeViewModelModel,
    onSavePoemTheme: (poemName: String, context: Context, isEditTheme: Boolean) -> Unit,
    onResetResultToDefault: () -> Unit,
    onSetDisplayDialog: (Boolean) -> Unit,
    onIsValidatedInput: (String) -> Boolean,
) {
    var poemName by remember { mutableStateOf("") }
    var dialogTitle by remember { mutableIntStateOf(R.string.create_poem_title) }
    var buttonText by remember { mutableIntStateOf(R.string.confirm) }
    var inputMessage by remember { mutableIntStateOf(R.string.valid_input_message) }
    var shouldChangeText by remember { mutableStateOf(false) }
    val validateInput: @Composable (String) -> Boolean = {
        if (onIsValidatedInput.invoke(it.replace(' ', '_'))) {
            //save poem theme and start new activity
            onSavePoemTheme.invoke(
                it,
                LocalContext.current.applicationContext,
                modelState.isEditTheme
            )
            true
        } else {
            false
        }
    }
//    if (modelState.poemThemeResult == 0) {
//        onResetResultToDefault.invoke()
////        onStartActivity(modelState.poemTitle, null)
//    } else
        if (modelState.poemThemeResult == -1) {
        dialogTitle = R.string.retry
        buttonText = R.string.retry
        inputMessage = R.string.file_already_exists
        onResetResultToDefault.invoke()
    }
    val onChangePoemName : (String) -> Unit = {
        poemName = it
    }
    val onShouldChangeText : (Boolean) -> Unit = {
        shouldChangeText = it
    }
    DialogLayout(
        dialogBody = {
            SavePoemTheDialogBody(
                dialogTitle = dialogTitle,
                inputMessage = inputMessage,
                poemName = poemName,
                onChangePoemName = onChangePoemName
            )
        },
        positiveButtonText = stringResource(id = buttonText),
        negativeButtonText = "",
        onNegativeAction = { false },
        onPositiveAction = {
            onShouldChangeText.invoke(true)
            false
        }, onDismiss = { onSetDisplayDialog.invoke(false)})

    if (shouldChangeText) {
        if (dialogTitle == R.string.retry) {
            dialogTitle = R.string.create_poem_title
            buttonText = R.string.confirm
            inputMessage = R.string.valid_input_message
        } else if (!validateInput.invoke(poemName.trim())) {
            dialogTitle = R.string.retry
            buttonText = R.string.retry
            inputMessage = R.string.invalid_input_message
        }
        shouldChangeText = false
    }
}

@Composable
fun AppBar(
    setDisplayDialog: @Composable (Boolean) -> Unit,
    isEditTheme: Boolean
) {
    var shouldDisplayDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.secondary)
    ) {
        Image(
            painter = painterResource(id = R.drawable.appbartitle),
            contentDescription = stringResource(
                id = R.string.the_poets_kingdom_image
            ),
            modifier = Modifier
                .width(150.dp)
                .padding(top = 10.dp),
            alignment = Alignment.Center,
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { shouldDisplayDialog = true },
            shape = RoundedCornerShape(15.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                MadzinzaGreen
            )
        ) {
            if (isEditTheme) {
                Text(text = stringResource(id = R.string.edit_button_theme))
            } else {
                Text(text = stringResource(id = R.string.create_poem))
            }
        }
    }
    if (shouldDisplayDialog) {
        setDisplayDialog.invoke(true)
        shouldDisplayDialog = false
    }
}

/**
 * Dialog body for saving a poem theme
 * @param dialogTitle the resource identifier for the dialog title
 * @param inputMessage the message to display above the text field
 * @param poemName entered poem name text
 * @param onChangePoemName method to invoke when the poem name changes
 */
@Composable
fun SavePoemTheDialogBody(dialogTitle : Int, inputMessage : Int, poemName: String,
                          onChangePoemName : (String) -> Unit){
    val maxChars = 60
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
            .background(DefaultColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(id = dialogTitle),
            style = MaterialTheme.typography.h1,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = stringResource(id = inputMessage),
            style = MaterialTheme.typography.body1,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(15.dp))

        TextField(
            colors = TextFieldDefaults.textFieldColors(textColor = Color.White),
            singleLine = true,
            value = poemName,
            onValueChange = { if (it.length <= maxChars) onChangePoemName(it) },
            label = {
                Text(
                    stringResource(id = R.string.create_poem_edit_text_hint),
                    color = Color.White
                )
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

    }
}
@Preview(showBackground = true)
@Composable
fun PoemThemeScreenPreview() {
    PoetsKingdomTheme {
        ThemePoemApp(poemThemeViewModel = viewModel(), {x, s ->})
    }
}