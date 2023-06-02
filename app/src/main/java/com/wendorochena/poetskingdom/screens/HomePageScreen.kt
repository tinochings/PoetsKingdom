package com.wendorochena.poetskingdom.screens

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.ui.theme.DefaultStatusBarColor
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme


@Composable
fun HomePageScreenApp(
    modifier: Modifier = Modifier,
    onImagesClick: () -> Unit,
    onMyPoemsClick: () -> Unit,
    onPersonalisationClick: () -> Unit,
    onCreatePoemClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colors.background)
    ) {
        QuadrantRowOne(onMyPoemsClick = onMyPoemsClick, onCreatePoemClick = onCreatePoemClick)
        QuadrantRowTwo(onImagesClick, onPersonalisationClick)
        NicknameContainer()
    }
}

@Composable
fun NicknameContainer(modifier: Modifier = Modifier) {
    val activity = LocalContext.current.applicationContext
    val toDisplay = activity?.getSharedPreferences(
        stringResource(R.string.personalisation_sharedpreferences_key),
        AppCompatActivity.MODE_PRIVATE
    )?.getString("appNickname", null) ?: stringResource(id = R.string.nickname_placeholder)
    Box(modifier = modifier.fillMaxSize()) {
        Text(
            text = toDisplay,
            fontFamily = FontFamily.Cursive,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun QuadrantRowTwo(
    onImagesClick: () -> Unit,
    onPersonalisationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp), contentAlignment = Alignment.Center
            ) {

                Column(
                    modifier = Modifier.clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = rememberRipple(color = DefaultStatusBarColor, bounded = true),
                        onClick = { onImagesClick.invoke() })
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.myimages),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxHeight(0.75f)
                            .fillMaxWidth()
                            .clip(com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline),
                        contentScale = ContentScale.FillBounds
                    )
                    Text(
                        text = stringResource(id = R.string.my_images),
                        style = MaterialTheme.typography.h1,
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = MaterialTheme.colors.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    modifier = Modifier.clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = rememberRipple(color = DefaultStatusBarColor, bounded = true),
                        onClick = { onPersonalisationClick.invoke() })
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.personalisation),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxHeight(0.75f)
                            .fillMaxWidth()
                            .clip(com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline),
                        contentScale = ContentScale.FillBounds
                    )
                    Text(
                        text = stringResource(id = R.string.personalisation),
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.h1,
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun QuadrantRowOne(
    onMyPoemsClick: () -> Unit,
    onCreatePoemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier.clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = rememberRipple(color = DefaultStatusBarColor, bounded = true),
                        onClick = { onCreatePoemClick.invoke() })
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.createpoem),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxHeight(0.75f)
                            .fillMaxWidth()
                            .clip(com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline),
                        contentScale = ContentScale.FillBounds
                    )
                    Text(
                        text = stringResource(id = R.string.create_poem),
                        style = MaterialTheme.typography.h1,
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = MaterialTheme.colors.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.BottomCenter
            ) {

                Column(
                    modifier = Modifier.clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = rememberRipple(color = DefaultStatusBarColor, bounded = true),
                        onClick = { onMyPoemsClick.invoke() })
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mypoems),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxHeight(0.75f)
                            .fillMaxWidth()
                            .clip(com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline),
                        contentScale = ContentScale.FillBounds
                    )
                    Text(
                        text = stringResource(id = R.string.my_poems_text),
                        style = MaterialTheme.typography.h1,
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = MaterialTheme.colors.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePageScreenPreview() {
    PoetsKingdomTheme(darkTheme = true) {
        HomePageScreenApp(
            onImagesClick = {},
            onPersonalisationClick = {},
            onCreatePoemClick = {},
            onMyPoemsClick = {})
    }
}