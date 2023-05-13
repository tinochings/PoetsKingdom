package com.wendorochena.poetskingdom.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.ui.theme.DefaultColor
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import com.wendorochena.poetskingdom.utils.TextMarginUtil
import com.wendorochena.poetskingdom.utils.TypefaceHelper
import com.wendorochena.poetskingdom.viewModels.CreatePoemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePoemApp(createPoemViewModel: CreatePoemViewModel) {
    Scaffold(topBar = { HomeScreenAppBar(displaySearch = false, onSearchClick = {}) }) {
        it
        if (createPoemViewModel.isDimmerDisplayed) {
            DimmerProgress()
        } else {
            Column(
                modifier = Modifier
                    .background(androidx.compose.material.MaterialTheme.colors.background)
                    .fillMaxSize()
                    .padding(top = dimensionResource(id = R.dimen.margin_top_action_bar_size))
            ) {
                PortraitPoemView(
                    modifier = Modifier
                        .fillMaxHeight(0.9f)
                        .fillMaxWidth(), createPoemViewModel
                )
            }
        }
    }
}

@Composable
fun DimmerProgress() {
    Column(
        modifier = Modifier
            .background(Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(80.dp)
                        .height(80.dp)
                        .zIndex(5f), color = DefaultColor, strokeWidth = 10.dp
                )
            }
        }
    }
}

@OptIn(
    ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun PortraitPoemView(modifier: Modifier, createPoemViewModel: CreatePoemViewModel) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (createPoemViewModel.poemTheme.backgroundType) {
                BackgroundType.DEFAULT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                    }
                }

                BackgroundType.COLOR -> {
                    val backgroundColor: Int = createPoemViewModel.poemTheme.backgroundColorAsInt
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color(backgroundColor))
                    ) {
                    }
                }

                BackgroundType.IMAGE -> {
                    GlideImage(
                        model = createPoemViewModel.poemTheme.imagePath,
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                BackgroundType.OUTLINE -> {
                    DisplayOutline(
                        PaddingValues(),
                        createPoemViewModel.parseOutlineType(createPoemViewModel.poemTheme.outline),
                        createPoemViewModel.poemTheme.outlineColor,
                        createPoemViewModel.poemTheme.backgroundColorAsInt
                    ) {}

                }

                BackgroundType.OUTLINE_WITH_COLOR -> {
                    DisplayOutline(
                        PaddingValues(),
                        createPoemViewModel.parseOutlineType(createPoemViewModel.poemTheme.outline),
                        createPoemViewModel.poemTheme.outlineColor,
                        createPoemViewModel.poemTheme.backgroundColorAsInt
                    ) {}
                }

                BackgroundType.OUTLINE_WITH_IMAGE -> {
                    OutlineAndImage(
                        paddingValues = PaddingValues(8.dp),
                        shape = createPoemViewModel.shapeFromOutline(),
                        createPoemViewModel.poemTheme.imagePath,
                        createPoemViewModel.poemTheme.outlineColor
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .combinedClickable(
                        enabled = true,
                        onClick = {},
                    )
            ) {
                var textToDisplay by remember {
                    mutableStateOf(
                        createPoemViewModel.getPage(
                            createPoemViewModel.currentPageNumber
                        )!!
                    )
                }
                if (createPoemViewModel.poemTheme.backgroundType.name.contains("OUTLINE")) {
                    val textMarginUtil = TextMarginUtil()
                    textMarginUtil.determineTextMargins(
                        createPoemViewModel.poemTheme.outline,
                        LocalContext.current.resources,
                        LocalContext.current.resources.getDimensionPixelSize(R.dimen.strokeSize)
                    )
//                    val density = LocalContext.current.resources.displayMetrics.densityDpi
//                    val marginStart = (textMarginUtil.marginLeft / (density / 160f)).roundToInt()
//                    val marginEnd = (textMarginUtil.marginRight / (density / 160f)).roundToInt()
//                    val marginTop = (textMarginUtil.marginTop / (density / 160f)).roundToInt()
//                    val marginBottom = (textMarginUtil.marginBottom / (density / 160f)).roundToInt()
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TextField(
                            value = textToDisplay,
                            placeholder = { Text(text = stringResource(id = R.string.create_poem_text_view_hint))},
                            onValueChange = { textToDisplay = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(createPoemViewModel.shapeFromOutline()),
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = Color(
                                    createPoemViewModel.poemTheme.textColorAsInt
                                ), containerColor = Color.Transparent
                            ),
                            textStyle = TextStyle(
                                fontSize = createPoemViewModel.poemTheme.textSize.sp,
                                fontFamily = TypefaceHelper.getTypeFace(createPoemViewModel.poemTheme.textFontFamily),
                                textAlign = createPoemViewModel.texAlignmentToTextAlign()
                            )
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        TextField(
                            value = textToDisplay,
                            placeholder = { Text(text = stringResource(id = R.string.create_poem_text_view_hint))},
                            onValueChange = { textToDisplay = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = Color(
                                    createPoemViewModel.poemTheme.textColorAsInt
                                ), containerColor = Color.Transparent,
                                
                            ),
                            textStyle = TextStyle(
                                fontSize = createPoemViewModel.poemTheme.textSize.sp,
                                fontFamily = TypefaceHelper.getTypeFace(createPoemViewModel.poemTheme.textFontFamily),
                                textAlign = createPoemViewModel.texAlignmentToTextAlign()
                            )
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CreatePoemAppReview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material.MaterialTheme.colors.background
    ) {
        PoetsKingdomTheme {
            CreatePoemApp(viewModel())
        }
    }
}
