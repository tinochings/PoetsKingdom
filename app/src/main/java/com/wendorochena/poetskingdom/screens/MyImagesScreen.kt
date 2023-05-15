package com.wendorochena.poetskingdom.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.wendorochena.poetskingdom.ImageViewer
import com.wendorochena.poetskingdom.PersonalisationActivity
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.ui.theme.DefaultColor
import com.wendorochena.poetskingdom.ui.theme.DefaultStatusBarColor
import com.wendorochena.poetskingdom.ui.theme.OffWhite
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import com.wendorochena.poetskingdom.utils.UriUtils
import com.wendorochena.poetskingdom.viewModels.CurrentSelection
import com.wendorochena.poetskingdom.viewModels.FloatingButtonState
import com.wendorochena.poetskingdom.viewModels.MyImagesViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyImagesScreenApp(
    myImagesViewModel: MyImagesViewModel = viewModel(),
) {
    val sharedPreferences =
        LocalContext.current.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
    var isFirstUse = false
    if (sharedPreferences != null) {
        if (!sharedPreferences.getBoolean("myImagesFirstUse", false)) {
            isFirstUse = true
            sharedPreferences.edit().putBoolean("myImagesFirstUse", true).apply()
        }
    }
    Scaffold(floatingActionButton = {
        FloatingActionButton(myImagesViewModel = myImagesViewModel)
    }, floatingActionButtonPosition = FabPosition.End) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(MaterialTheme.colors.background)
        ) {
            if (myImagesViewModel.onImageLongPressed) {
                BackHandler(true) {
                    myImagesViewModel.setOnLongClick(false)
                    myImagesViewModel.setFloatingButtonState(FloatingButtonState.ADDIMAGE)
                }
            }
            TopRow(myImagesViewModel = myImagesViewModel)
            if (myImagesViewModel.currentSelection == CurrentSelection.IMAGES)
                ImagesView(myImagesViewModel = myImagesViewModel)
            else
                PoemImagesView(myImagesViewModel)
            if (isFirstUse) {
                FirstUseDialog(R.string.my_images, R.string.guide_my_images, false)
                isFirstUse = false
            }
        }
    }
}

@Composable
fun FirstUseDialog(
    heading: Int,
    guideText: Int,
    isHomeScreen: Boolean
) {
    val context = LocalContext.current
    var shouldDismiss by remember { mutableStateOf(false) }
    if (!shouldDismiss) {
        Dialog(
            onDismissRequest = { },
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
                shape = com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline,
                colors = CardDefaults.cardColors(containerColor = DefaultColor)
            ) {
                Text(
                    text = stringResource(id = heading),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h1,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = stringResource(id = guideText),
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(15.dp))

                Button(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(5.dp)
                        .border(
                            3.dp,
                            color = OffWhite,
                            com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline
                        ),
                    onClick = {
                        shouldDismiss = true
                        if (isHomeScreen) {
                            val personalisationActivityIntent =
                                Intent(context, PersonalisationActivity::class.java)
                            context.startActivity(personalisationActivityIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DefaultColor)
                ) {
                    Text(text = stringResource(id = R.string.builder_understood))
                }
            }
        }
    }
}

@Composable
fun FloatingActionButton(myImagesViewModel: MyImagesViewModel) {
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uriList ->
            var counter = 0
            while (counter < uriList.size) {
                val uriUtils =
                    UriUtils(context, uri = uriList[counter])
                myImagesViewModel.copyToLocalFolder(
                    uriUtils.getRealPathFromURI(),
                    context = context
                )
                counter++
            }
        }
    )
    val permissionsResultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it)
            imagePicker.launch("image/*")
    }
    androidx.compose.material.FloatingActionButton(
        onClick = { },
        backgroundColor = DefaultColor
    ) {
        var onClick by remember {
            mutableStateOf(false)
        }
        if (myImagesViewModel.floatingButtonStateVar == FloatingButtonState.ADDIMAGE) {
            Image(
                painter = painterResource(id = R.drawable.ic_baseline_add_a_photo_24),
                contentDescription = "",
                colorFilter = ColorFilter.tint(color = Color.White),
                modifier = Modifier.clickable { onClick = true }
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                contentDescription = "",
                colorFilter = ColorFilter.tint(color = Color.White),
                modifier = Modifier.clickable {
                    if (myImagesViewModel.currentSelection == CurrentSelection.IMAGES) myImagesViewModel.deleteImages()
                    else myImagesViewModel.deleteSavedPoems(context = context)
                }
            )
        }
        if (onClick) {
            if (Build.VERSION.SDK_INT < 33 && LocalContext.current.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT >= 33 && LocalContext.current.checkSelfPermission(
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            )
                imagePicker.launch("image/*")
            else {
                if (Build.VERSION.SDK_INT >= 33)
                    permissionsResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                else
                    permissionsResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            onClick = false
        }
    }

}

@Composable
fun PoemImagesView(myImagesViewModel: MyImagesViewModel, modifier: Modifier = Modifier) {
    val onImageItemClick: @Composable (File) -> Unit = {
        if (!myImagesViewModel.onImageLongPressed) {
            val imageIntent =
                Intent(LocalContext.current.applicationContext, ImageViewer::class.java)
            val poemTitleTextView = it.name.split(".png")[0].replace('_', ' ')
            imageIntent.putExtra("imageLoadType", "poem saved image")
            imageIntent.putExtra("poemName", poemTitleTextView)
            LocalContext.current.startActivity(imageIntent)
        } else {
            myImagesViewModel.savedPoemImages[it] = !myImagesViewModel.savedPoemImages[it]!!
        }
    }

    val onLongClick: (File) -> Unit = {
        myImagesViewModel.savedPoemImages[it] = true
        myImagesViewModel.setOnLongClick(true)
        if (myImagesViewModel.floatingButtonStateVar != FloatingButtonState.DELETEIMAGE)
            myImagesViewModel.setFloatingButtonState(FloatingButtonState.DELETEIMAGE)
    }
    val imageFiles = myImagesViewModel.getThumbnails(LocalContext.current.applicationContext)
    val imageFileKeys = imageFiles.keys.asSequence().sortedByDescending { it.lastModified() }
    LazyVerticalGrid(
        modifier = modifier.padding(top = 5.dp).fillMaxWidth(), columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items(count = imageFileKeys.count()) {
            Column {
                val file = imageFileKeys.elementAt(it)
                ImagesItem(
                    Pair(file, imageFiles[file]!!),
                    modifier = Modifier
                        .padding(3.dp)
                        .height(150.dp),
                    onImageItemClick,
                    onLongClick,
                    myImagesViewModel.onImageLongPressed
                )
                TitleItem(file)
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
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = titleName,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth(),
                style = MaterialTheme.typography.h1,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = dateText, maxLines = 1, modifier = Modifier
                .fillMaxWidth(),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.light_black)
        )

    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun ImagesItem(
    imageFilePair: Pair<File, Boolean>,
    modifier: Modifier,
    onImageItemClick: @Composable (File) -> Unit,
    onLongClick: (File) -> Unit,
    isLongClicked: Boolean
) {
    var imageClicked by remember { mutableStateOf(false) }
    Box(modifier = Modifier) {
        if (isLongClicked) {
            if (imageFilePair.second) {
                Image(
                    modifier = Modifier
                        .width(25.dp)
                        .height(25.dp)
                        .zIndex(5f),
                    painter = painterResource(id = R.drawable.check_mark),
                    contentDescription = ""
                )
            } else {
                Image(
                    modifier = Modifier
                        .width(25.dp)
                        .height(25.dp)
                        .zIndex(5f),
                    painter = painterResource(id = R.drawable.circle_svg),
                    contentDescription = ""
                )
            }
        }
        GlideImage(
            model = imageFilePair.first.absolutePath, contentDescription = "",
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    enabled = true,
                    onClick = { imageClicked = true },
                    onLongClick = { onLongClick.invoke(imageFilePair.first) }).clip(
                    RoundedCornerShape(5.dp)
                ),
            contentScale = ContentScale.FillBounds
        ) { requestBuilder ->
            requestBuilder.placeholder(R.drawable.baseline_placeholder)
                .load(imageFilePair.first.absolutePath).error(R.drawable.baseline_broken_image_24)
        }
        if (imageClicked) {
            onImageItemClick.invoke(imageFilePair.first)
            imageClicked = false
        }
    }
}

@Composable
fun ImagesView(
    modifier: Modifier = Modifier, myImagesViewModel: MyImagesViewModel
) {
    val onImageItemClick: @Composable (File) -> Unit = {
        if (!myImagesViewModel.onImageLongPressed) {
            val imageIntent =
                Intent(LocalContext.current.applicationContext, ImageViewer::class.java)
            imageIntent.putExtra("imageLoadType", "image")
            imageIntent.putExtra("imagePath", it.absolutePath)
            LocalContext.current.startActivity(imageIntent)
        } else {
            myImagesViewModel.imageFiles[it] = !myImagesViewModel.imageFiles[it]!!
        }
    }
    val onLongClick: (File) -> Unit = {
        myImagesViewModel.imageFiles[it] = true
        myImagesViewModel.setOnLongClick(true)
        if (myImagesViewModel.floatingButtonStateVar != FloatingButtonState.DELETEIMAGE)
            myImagesViewModel.setFloatingButtonState(FloatingButtonState.DELETEIMAGE)
    }
    val imageFiles = myImagesViewModel.getImageFiles(LocalContext.current.applicationContext)
    val imageFileKeys = imageFiles.keys.asSequence()
    LazyVerticalGrid(
        modifier = modifier.padding(top = 5.dp), columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(count = imageFiles.size) {
            val file = imageFileKeys.elementAt(it)
            ImagesItem(
                Pair(file, imageFiles[file]!!),
                modifier = Modifier
                    .padding(3.dp)
                    .height(100.dp),
                onImageItemClick,
                onLongClick,
                myImagesViewModel.onImageLongPressed
            )
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
            .clickable {
                if (!myImagesViewModel.onImageLongPressed) onClick.invoke(
                    CurrentSelection.IMAGES
                )
            }) {
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
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.h1
                )
            }

        }
        Row(modifier = Modifier
            .weight(1f)
            .clickable {
                if (!myImagesViewModel.onImageLongPressed) onClick.invoke(
                    CurrentSelection.POEMS
                )
            }) {
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
                    color = MaterialTheme.colors.primary,
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