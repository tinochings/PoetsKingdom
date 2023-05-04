package com.wendorochena.poetskingdom.screens

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
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
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme


@Composable
fun HomePageScreenApp(modifier: Modifier = Modifier, onImagesClick : () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        QuadrantRowOne()
        QuadrantRowTwo(onImagesClick)
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun QuadrantRowTwo(onImagesClick : () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .clickable { onImagesClick.invoke() }, contentAlignment = Alignment.Center
            ) {

                Column {
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
                            .fillMaxWidth()
                            .padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp), contentAlignment = Alignment.Center
            ) {

                Column {
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
                        style = MaterialTheme.typography.h1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun QuadrantRowOne(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp), contentAlignment = Alignment.BottomCenter
            ) {
                Column {
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
                            .fillMaxWidth()
                            .padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp), contentAlignment = Alignment.BottomCenter
            ) {

                Column {
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
                            .fillMaxWidth()
                            .padding(10.dp),
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
    PoetsKingdomTheme {
        HomePageScreenApp(onImagesClick = {})
    }
}