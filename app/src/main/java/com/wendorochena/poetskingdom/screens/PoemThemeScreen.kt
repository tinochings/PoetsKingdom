package com.wendorochena.poetskingdom.screens

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
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
import androidx.compose.material3.Card
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.wendorochena.poetskingdom.CreatePoem
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.ui.theme.DefaultColor
import com.wendorochena.poetskingdom.ui.theme.DefaultStatusBarColor
import com.wendorochena.poetskingdom.ui.theme.MadzinzaGreen
import com.wendorochena.poetskingdom.ui.theme.OffWhite
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import com.wendorochena.poetskingdom.utils.TextMarginUtil
import com.wendorochena.poetskingdom.utils.TypefaceHelper
import com.wendorochena.poetskingdom.viewModels.HeadingSelection
import com.wendorochena.poetskingdom.viewModels.PoemThemeViewModel
import java.io.File
import kotlin.math.roundToInt

@Composable
fun ThemePoemApp(
    poemThemeViewModel: PoemThemeViewModel
) {
    val isFirstUse = poemThemeViewModel.determineFirstUse(
        LocalContext.current.applicationContext,
        "outlineFirstUse"
    )

    Scaffold(topBar = {
        val setDisplayDialog: @Composable (Boolean) -> Unit = {
            if (poemThemeViewModel.isEditTheme) {
                poemThemeViewModel.savePoemTheme(
                    poemThemeViewModel.poemTitle,
                    LocalContext.current.applicationContext,
                    poemThemeViewModel.isEditTheme
                )
            } else {
                poemThemeViewModel.setDisplayDialog(it)
            }
        }
        if (poemThemeViewModel.poemThemeResult == 0) {
            val activity = LocalContext.current as ComponentActivity
            val newActivityIntent =
                Intent(activity, CreatePoem::class.java)
            newActivityIntent.putExtra("loadPoem", true)
            newActivityIntent.putExtra(
                "poemTitle",
                poemThemeViewModel.poemTitle
            )
            if (poemThemeViewModel.savedAlbumName != null)
                newActivityIntent.putExtra(
                    activity.getString(R.string.album_argument_name),
                    poemThemeViewModel.savedAlbumName
                )
            poemThemeViewModel.resetResultToDefault()
            activity.finish()
            activity.startActivity(newActivityIntent)
        }
        AppBar(setDisplayDialog = setDisplayDialog, isEditTheme = poemThemeViewModel.isEditTheme)
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            ThemePreview(it, poemThemeViewModel = poemThemeViewModel)
            ThemeOptions(poemThemeViewModel = poemThemeViewModel)

            if (poemThemeViewModel.shouldDisplayDialog)
                SavePoemThemeDialog(poemThemeViewModel)

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

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun ThemePreview(
    paddingValues: PaddingValues,
    poemThemeViewModel: PoemThemeViewModel
) {
    var shouldChangeBackground by remember { mutableStateOf(false) }
    val onPreviewLongClicked: @Composable () -> Unit = {
        val returnedPair = poemThemeViewModel.changePreviewBackground()
        if (returnedPair.first.isNotEmpty()) {
            val positiveButton = R.string.positive_background_color_outline_button
            val negativeButton = if (returnedPair.second == "Image")
                R.string.negative_background_image_outline_button
            else
                R.string.negative_background_color_outline_button

            val builder: AlertDialog.Builder = AlertDialog.Builder(LocalContext.current)
            if (returnedPair.second == "Image")
                builder.setMessage(R.string.remove_background_image_outline_popup)
            else
                builder.setMessage(R.string.remove_background_color_outline_popup)
            builder.setTitle(R.string.remove_background_title)
            builder.apply {
                setPositiveButton(
                    positiveButton
                ) { _, _ ->
                    poemThemeViewModel.textMarginUtil = TextMarginUtil()
                    if (returnedPair.second == "Image")
                        poemThemeViewModel.updateBackground(
                            BackgroundType.IMAGE,
                            poemThemeViewModel.backgroundImageChosen!!
                        )
                    else
                        poemThemeViewModel.updateBackground(
                            BackgroundType.COLOR,
                            backgroundColor = poemThemeViewModel.backgroundColorChosen!!,
                            backgroundColorAsInt = poemThemeViewModel.backgroundColorChosenAsInt!!
                        )
                }
                setNegativeButton(
                    negativeButton
                ) { _, _ ->
                    poemThemeViewModel.updateBackground(
                        BackgroundType.OUTLINE,
                        outlineColor = poemThemeViewModel.outlineColor,
                        outline = poemThemeViewModel.outlineType!!
                    )
                }

            }.show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxHeight(0.44f)
    ) {
        when (poemThemeViewModel.backgroundType) {
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
                    if (poemThemeViewModel.backgroundColorChosenAsInt != null)
                        poemThemeViewModel.backgroundColorChosenAsInt!!
                    else
                        android.graphics.Color.parseColor("#FFFFFFFF")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color(backgroundColor))
                ) {
                }
            }

            BackgroundType.IMAGE -> {
                GlideImage(
                    model = poemThemeViewModel.backgroundImageChosen,
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }

            BackgroundType.OUTLINE -> {
                if (poemThemeViewModel.outlineType != null) {
                    DisplayOutline(
                        paddingValues,
                        poemThemeViewModel.outlineType!!,
                        poemThemeViewModel.outlineColor,
                        poemThemeViewModel.backgroundColorChosenAsInt
                    ) {}
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
                if (poemThemeViewModel.outlineType != null) {
                    DisplayOutline(
                        paddingValues,
                        poemThemeViewModel.outlineType!!,
                        poemThemeViewModel.outlineColor,
                        poemThemeViewModel.backgroundColorChosenAsInt
                    ) {}
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
                if (poemThemeViewModel.outlineType != null) {
                    OutlineAndImage(
                        paddingValues = paddingValues,
                        shape = poemThemeViewModel.shapeFromOutline(),
                        poemThemeViewModel.backgroundImageChosen!!,
                        poemThemeViewModel.outlineColor
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
            if (poemThemeViewModel.backgroundType.name.contains("OUTLINE")) {
                val textMarginUtil = TextMarginUtil()
                textMarginUtil.determineTextMargins(
                    poemThemeViewModel.outlineType!!.toString(),
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
                    contentAlignment = poemThemeViewModel.boxAlignment()
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
                        color = Color(poemThemeViewModel.fontColor),
                        fontFamily = poemThemeViewModel.textFontFamily,
                        textAlign = poemThemeViewModel.texAlignmentToTextAlign(),
                        fontSize = poemThemeViewModel.fontSize.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = poemThemeViewModel.boxAlignment()
                ) {
                    Text(
                        text = stringResource(id = R.string.preview_text),
                        fontSize = poemThemeViewModel.fontSize.sp,
                        color = Color(poemThemeViewModel.fontColor),
                        fontFamily = poemThemeViewModel.textFontFamily,
                        fontWeight = if (poemThemeViewModel.isBold) FontWeight.Bold else null,
                        fontStyle = if (poemThemeViewModel.isItalic) FontStyle.Italic else null,
                        textAlign = poemThemeViewModel.texAlignmentToTextAlign()
                    )
                }
            }
        }
    }
    if (shouldChangeBackground) {
        onPreviewLongClicked.invoke()
        shouldChangeBackground = false
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun OutlineAndImage(
    paddingValues: PaddingValues,
    shape: Shape,
    imagePath: String,
    outlineColor: Int
) {
    GlideImage(
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
        contentScale = ContentScale.FillBounds
    )
}

@Composable
fun DisplayOutline(
    paddingValues: PaddingValues,
    outlineType: OutlineTypes,
    outlineColor: Int,
    outlineBackgroundColor: Int?,
    onOutlineLongClicked: @Composable (HeadingSelection) -> Unit
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
    poemThemeViewModel: PoemThemeViewModel
) {
    val context = LocalContext.current
    val onOutlineClicked: (OutlineTypes) -> Unit = { outline ->
        val textMarginUtil = TextMarginUtil()
        textMarginUtil.determineTextMargins(
            outline.name,
            context.resources,
            context.resources.getDimensionPixelSize(R.dimen.strokeSize)
        )
        poemThemeViewModel.setTextMarginUtility(textMarginUtil)
        when (poemThemeViewModel.backgroundType) {
            BackgroundType.COLOR, BackgroundType.OUTLINE_WITH_COLOR -> {
                poemThemeViewModel.updateBackground(
                    backgroundType = BackgroundType.OUTLINE_WITH_COLOR,
                    outline = outline.name,
                    outlineColor = poemThemeViewModel.outlineColor,
                    backgroundColor = poemThemeViewModel.backgroundColorChosen!!,
                    backgroundColorAsInt = poemThemeViewModel.backgroundColorChosenAsInt!!
                )
            }

            BackgroundType.IMAGE, BackgroundType.OUTLINE_WITH_IMAGE -> {
                poemThemeViewModel.updateBackground(
                    backgroundType = BackgroundType.OUTLINE_WITH_IMAGE,
                    outlineColor = poemThemeViewModel.outlineColor,
                    outline = outline.name,
                    imagePath = poemThemeViewModel.backgroundImageChosen!!
                )
            }

            else -> {
                poemThemeViewModel.updateBackground(
                    backgroundType = BackgroundType.OUTLINE,
                    outlineColor = poemThemeViewModel.outlineColor,
                    outline
                )
            }
        }
    }
    val colorPickerDialog: @Composable (HeadingSelection) -> Unit = {
        ColorPickerDialog.Builder(LocalContext.current).setTitle(
            stringResource(R.string.color_picker_title)
        ).setPositiveButton(R.string.confirm, object :
            ColorEnvelopeListener {
            override fun onColorSelected(
                envelope: ColorEnvelope?,
                fromUser: Boolean
            ) {
                if (envelope != null) {
                    when (it) {
                        HeadingSelection.BACKGROUND -> {
                            poemThemeViewModel.backgroundColorChosenAsInt = envelope.color
                            poemThemeViewModel.backgroundColorChosen = envelope.hexCode
                            if (poemThemeViewModel.backgroundType.name.contains("OUTLINE")
                            ) {
                                poemThemeViewModel.updateBackground(
                                    BackgroundType.OUTLINE_WITH_COLOR,
                                    outlineColor = poemThemeViewModel.outlineColor,
                                    outline = poemThemeViewModel.outlineType?.name!!,
                                    backgroundColor = envelope.hexCode,
                                    backgroundColorAsInt = envelope.color
                                )
                            } else {
                                poemThemeViewModel.updateBackground(
                                    BackgroundType.COLOR,
                                    backgroundColor = envelope.hexCode,
                                    backgroundColorAsInt = envelope.color
                                )
                            }
                        }

                        HeadingSelection.TEXT -> {
                            poemThemeViewModel.setTextColor(envelope.color, envelope.hexCode)
                        }

                        HeadingSelection.OUTLINE -> {
                            poemThemeViewModel.outlineColor = envelope.color
                        }
                    }
                } else {
                    //default case
                }
            }

        }).setNegativeButton(
            R.string.title_change_cancel
        ) { dialog, _ ->
            dialog?.dismiss()
        }.show()
    }
    val onHeadingClicked: (HeadingSelection) -> Unit = {
        poemThemeViewModel.changeSelection(it)
    }
    val textSizeChange: (Float) -> Unit = {
        poemThemeViewModel.setTextSize(it)
    }
    val onImageItemClick: (File) -> Unit = {
        if (poemThemeViewModel.backgroundType.name.contains("OUTLINE")) {
            poemThemeViewModel.updateBackground(
                BackgroundType.OUTLINE_WITH_IMAGE,
                outlineColor = poemThemeViewModel.outlineColor,
                outline = poemThemeViewModel.outlineType?.name!!,
                it.absolutePath
            )
        } else {
            poemThemeViewModel.updateBackground(BackgroundType.IMAGE, it.absolutePath)
        }
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
            when (poemThemeViewModel.headingSelection) {
                HeadingSelection.TEXT -> {
                    UnselectedHeadingBox(
                        headingName = poemThemeViewModel.unselectedHeadings()[0],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                    UnselectedHeadingBox(
                        headingName = poemThemeViewModel.unselectedHeadings()[1],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                    SelectedHeadingBox(
                        headingName = poemThemeViewModel.headingSelection.name,
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

                    if (poemThemeViewModel.determineFirstUse(
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
                        headingName = poemThemeViewModel.unselectedHeadings()[0],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                    SelectedHeadingBox(
                        headingName = poemThemeViewModel.headingSelection.name,
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
                        headingName = poemThemeViewModel.unselectedHeadings()[1],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                    if (poemThemeViewModel.determineFirstUse(
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
                        headingName = poemThemeViewModel.headingSelection.name, modifier = Modifier
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
                        headingName = poemThemeViewModel.unselectedHeadings()[0],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        onItemClick = onHeadingClicked
                    )
                    UnselectedHeadingBox(
                        headingName = poemThemeViewModel.unselectedHeadings()[1],
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
        when (poemThemeViewModel.headingSelection) {
            HeadingSelection.OUTLINE -> {
                OutlineLayout(
                    onOutlineClicked = onOutlineClicked,
                    onOutlineLongClicked = colorPickerDialog,
                    outlineColor = poemThemeViewModel.outlineColor
                )
            }

            HeadingSelection.BACKGROUND -> {
                BackgroundLayout(
                    colorPickerDialog = colorPickerDialog,
                    onImageItemClick = onImageItemClick
                )
            }

            HeadingSelection.TEXT -> {
                TextLayout(
                    textSizeChange = textSizeChange,
                    colorPickerDialog = colorPickerDialog,
                    textColor = poemThemeViewModel.fontColor,
                    onFontItemClicked = onFontItemClicked,
                    onTextAlignClicked = onTextAlignClicked,
                    onBoldOrItalicClicked = onBoldOrItalicClicked,
                    defaultTextValue = poemThemeViewModel.fontSize,
                    selectedFont = poemThemeViewModel.textFontFamilyString,
                    isBold = poemThemeViewModel.isBold,
                    isItalic = poemThemeViewModel.isItalic
                )
            }
        }

    }
}

@Composable
fun BackgroundLayout(
    colorPickerDialog: @Composable (HeadingSelection) -> Unit,
    onImageItemClick: (File) -> Unit
) {
    var showColorDialog by remember { mutableStateOf(false) }
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
                .clickable(enabled = true, onClick = { showColorDialog = true })
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
    if (showColorDialog) {
        colorPickerDialog.invoke(HeadingSelection.BACKGROUND)
        showColorDialog = false
    }
    ImagesGrid(onImageItemClick = onImageItemClick)
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
    colorPickerDialog: @Composable (HeadingSelection) -> Unit, textColor: Int,
    onTextAlignClicked: (TextAlignment) -> Unit,
    onBoldOrItalicClicked: (String) -> Unit,
    isBold: Boolean,
    isItalic: Boolean
) {
    var shouldDisplayDialog by remember { mutableStateOf(false) }
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f, true)
                    .height(80.dp)
                    .clickable { shouldDisplayDialog = true }
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
    if (shouldDisplayDialog) {
        colorPickerDialog.invoke(HeadingSelection.TEXT)
        shouldDisplayDialog = false
    }
}

@Composable
fun TextLayout(
    textSizeChange: (Float) -> Unit,
    colorPickerDialog: @Composable (HeadingSelection) -> Unit,
    textColor: Int,
    onFontItemClicked: (FontFamily, String) -> Unit,
    onTextAlignClicked: (TextAlignment) -> Unit,
    onBoldOrItalicClicked: (String) -> Unit,
    defaultTextValue: Float,
    selectedFont: String,
    isBold: Boolean,
    isItalic: Boolean,
) {
    val allFonts = stringArrayResource(id = R.array.customFontNamesCompose)
    val numOfFonts = allFonts.size
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
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
                isItalic = isItalic
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

            FontFaceItem(modifier, fontItem, fontFamily)
        }
    }
}


@Composable
fun FontFaceItem(
    modifier: Modifier,
    fontItem: String,
    fontFamily: FontFamily
) {
    val typefaceNameArr = fontItem.split('_')
    var fontText = if (typefaceNameArr.size > 1)
        ""
    else
        fontItem[0].uppercase() + fontItem.substring(1, fontItem.length)
    if (fontText == "") {
        for ((index, word) in typefaceNameArr.withIndex()) {
            if (index != typefaceNameArr.size) {
                val wordToAdd = word[0].uppercase() + word.substring(1, word.length)
                fontText += "$wordToAdd "
            }
        }
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = fontText,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            fontFamily = fontFamily
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageItem(imageFile: File, modifier: Modifier, onImageItemClick: (File) -> Unit) {
    GlideImage(
        model = imageFile.absolutePath, contentDescription = "",
        modifier = modifier
            .width(80.dp)
            .height(80.dp)
            .shadow(elevation = 5.dp)
            .clickable { onImageItemClick.invoke(imageFile) },
        contentScale = ContentScale.FillBounds
    )
}

@Composable
fun ImagesGrid(onImageItemClick: (File) -> Unit) {
    val imagesFolder = LocalContext.current.applicationContext.getDir(
        stringResource(id = R.string.my_images_folder_name),
        Context.MODE_PRIVATE
    )
    val imageFiles =
        imagesFolder?.listFiles()?.toMutableList()?.sortedByDescending { it.lastModified() }
    LazyVerticalGrid(
        modifier = Modifier.padding(top = 5.dp), columns = GridCells.Fixed(4),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (imageFiles != null) {
            items(count = imageFiles.size) {
                ImageItem(imageFiles[it], modifier = Modifier.padding(3.dp), onImageItemClick)
            }
        }
    }
}

@Composable
fun OutlineLayout(
    onOutlineClicked: (OutlineTypes) -> Unit,
    onOutlineLongClicked: @Composable (HeadingSelection) -> Unit,
    outlineColor: Int
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(count = 1) {
            Column {
                Row(modifier = Modifier.fillMaxSize()) {
                    RoundedRectangleOutline(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 5.dp, end = 5.dp)
                            .height(150.dp)
                            .weight(1f),
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
                            .weight(1f),
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
                            .weight(1f),
                        outlineColor,
                        onOutlineClicked,
                        onOutlineLongClicked
                    )
                    RotatedTeardropOutline(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 5.dp, end = 5.dp)
                            .height(150.dp)
                            .weight(1f),
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
                            .weight(1f),
                        outlineColor,
                        onOutlineClicked,
                        onOutlineLongClicked
                    )
                    RotatedLemonOutline(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 5.dp, end = 5.dp)
                            .height(150.dp)
                            .weight(1f),
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

@Composable
fun SavePoemThemeDialog(poemThemeViewModel: PoemThemeViewModel) {
    var poemName by remember { mutableStateOf("") }
    var dialogTitle by remember { mutableIntStateOf(R.string.create_poem_title) }
    var buttonText by remember { mutableIntStateOf(R.string.confirm) }
    var inputMessage by remember { mutableIntStateOf(R.string.valid_input_message) }
    val maxChars = 60
    var shouldChangeText by remember { mutableStateOf(false) }
    val validateInput: @Composable (String) -> Boolean = {
        if (PoemThemeViewModel.isValidatedInput(it.replace(' ', '_'))) {
            //save poem theme and start new activity
            poemThemeViewModel.savePoemTheme(
                it,
                LocalContext.current.applicationContext,
                poemThemeViewModel.isEditTheme
            )
            true
        } else {
            false
        }
    }
    if (poemThemeViewModel.poemThemeResult == 0) {
        val activity = LocalContext.current as ComponentActivity
        val newActivityIntent =
            Intent(activity, CreatePoem::class.java)
        newActivityIntent.putExtra("loadPoem", true)
        newActivityIntent.putExtra(
            "poemTitle",
            poemThemeViewModel.poemTitle
        )
        poemThemeViewModel.resetResultToDefault()
        activity.finish()
        activity.startActivity(newActivityIntent)
    } else if (poemThemeViewModel.poemThemeResult == -1) {
        dialogTitle = R.string.retry
        buttonText = R.string.retry
        inputMessage = R.string.file_already_exists
        poemThemeViewModel.resetResultToDefault()
    }
    Dialog(
        onDismissRequest = { poemThemeViewModel.setDisplayDialog(false) },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 3.dp,
                    color = DefaultStatusBarColor,
                    com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline
                ),
            shape = com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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

                Spacer(modifier = Modifier.height(30.dp))

                TextField(
                    colors = TextFieldDefaults.textFieldColors(textColor = Color.White),
                    singleLine = true,
                    value = poemName,
                    onValueChange = { if (it.length <= maxChars) poemName = it },
                    label = {
                        Text(
                            stringResource(id = R.string.create_poem_edit_text_hint),
                            color = OffWhite
                        )
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { shouldChangeText = true },
                    shape = RoundedCornerShape(15.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DefaultColor),
                    modifier = Modifier
                        .align(Alignment.End)
                ) {
                    Text(
                        text = stringResource(id = buttonText),
                        color = Color.White,
                        style = MaterialTheme.typography.body1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
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

@Preview(showBackground = true)
@Composable
fun PoemThemeScreenPreview() {
    PoetsKingdomTheme {
        ThemePoemApp(poemThemeViewModel = viewModel())
    }
}