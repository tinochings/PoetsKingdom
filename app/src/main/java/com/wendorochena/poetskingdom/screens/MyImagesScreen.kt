package com.wendorochena.poetskingdom.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.wendorochena.poetskingdom.ImageViewer
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyImagesScreenApp(
    modifier: Modifier = Modifier,
    myImagesViewModel: MyImagesViewModel = viewModel()
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        TopRow(myImagesViewModel = myImagesViewModel)
        if (myImagesViewModel.currentSelection == CurrentSelection.IMAGES)
            ImagesView()
        else
            PoemImagesView(myImagesViewModel)
    }
}

@Composable
fun PoemImagesView(myImagesViewModel: MyImagesViewModel, modifier: Modifier = Modifier) {
    val onImageItemClick: @Composable (File) -> Unit = {
        val imageIntent = Intent(LocalContext.current.applicationContext, ImageViewer::class.java)
        val poemTitleTextView = it.name.split(".png")[0].replace('_', ' ')
        imageIntent.putExtra("imageLoadType", "poem saved image")
        imageIntent.putExtra("poemName", poemTitleTextView)
        LocalContext.current.startActivity(imageIntent)
    }

    val imageFiles = myImagesViewModel.getThumbnails(LocalContext.current.applicationContext)
    LazyVerticalGrid(
        modifier = modifier.padding(top = 5.dp), columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(count = imageFiles.size) {
            Column {
                ImagesItem(imageFiles[it], modifier = Modifier.padding(3.dp), onImageItemClick)
                TitleItem(imageFiles[it])
            }
        }
    }
}

@Composable
fun TitleItem(imageFile: File, modifier: Modifier = Modifier) {
    val locale = Locale("en")
    val simpleDateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", locale)
    val date = Date(imageFile.lastModified())
    val dateText = simpleDateFormat.format(date)
    val titleName =
        imageFile.name[0].uppercase() + imageFile.name.split(".png")[0].replace('_', ' ')
            .removeRange(0, 1)
    Column {
        Box(modifier = modifier.height(50.dp), contentAlignment = Alignment.Center) {
        Text(
            text = titleName,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth(),
            style = MaterialTheme.typography.h1,
            textAlign = TextAlign.Center
        )
    }
        Text(
            text = dateText, maxLines = 1, modifier = Modifier
                .fillMaxWidth()
                .height(25.dp),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.light_black)
        )

    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImagesItem(imageFile: File, modifier: Modifier, onImageItemClick: @Composable (File) -> Unit) {
    var imageClicked by remember {
        mutableStateOf(false)
    }
    GlideImage(
        model = imageFile.absolutePath, contentDescription = "",
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { imageClicked = true },
        contentScale = ContentScale.FillBounds
    )
    if (imageClicked) {
        onImageItemClick.invoke(imageFile)
        imageClicked = false
    }
}

@Composable
fun ImagesView(modifier: Modifier = Modifier) {
    val onImageItemClick: @Composable (File) -> Unit = {
        val imageIntent = Intent(LocalContext.current.applicationContext, ImageViewer::class.java)
        imageIntent.putExtra("imageLoadType", "image")
        imageIntent.putExtra("imagePath", it.absolutePath)
        LocalContext.current.startActivity(imageIntent)
    }
    val imagesFolder = LocalContext.current.applicationContext.getDir(
        stringResource(id = R.string.my_images_folder_name),
        Context.MODE_PRIVATE
    )
    val imageFiles = imagesFolder?.listFiles()
    LazyVerticalGrid(
        modifier = modifier.padding(top = 5.dp), columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (imageFiles != null) {
            items(count = imageFiles.size) {
                ImagesItem(imageFiles[it], modifier = Modifier.padding(3.dp), onImageItemClick)
            }
        }
    }
}

@Composable
fun TopRow(modifier: Modifier = Modifier, myImagesViewModel: MyImagesViewModel) {

    val onClick: (CurrentSelection) -> Unit = {
        if (myImagesViewModel.currentSelection != it) {
            myImagesViewModel.setSelection(it)
        }
    }
    Row(
        modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(modifier = Modifier
            .weight(1f)
            .clickable { onClick.invoke(CurrentSelection.IMAGES) }) {
            if (myImagesViewModel.currentSelection == CurrentSelection.IMAGES) {
                Image(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.2f),
                    painter = painterResource(id = R.drawable.selected),
                    contentDescription = ""
                )
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.my_images_text),
                    style = MaterialTheme.typography.h1
                )
            }

        }
        Row(modifier = Modifier
            .weight(1f)
            .clickable { onClick.invoke(CurrentSelection.POEMS) }) {
            if (myImagesViewModel.currentSelection == CurrentSelection.POEMS) {
                Image(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.2f),
                    painter = painterResource(id = R.drawable.selected),
                    contentDescription = ""
                )
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.my_poems_text),
                    style = MaterialTheme.typography.h1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyImagesScreenPreview() {
    PoetsKingdomTheme {
        MyImagesScreenApp()
    }
}