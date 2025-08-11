package com.wendorochena.poetskingdom.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.screens.reusables.loaders.ImagesLoading
import com.wendorochena.poetskingdom.screens.reusables.loaders.ImagesNotification
import com.wendorochena.poetskingdom.screens.reusables.loaders.ImagesNotificationModel
import com.wendorochena.poetskingdom.ui.theme.DefaultColor
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import com.wendorochena.poetskingdom.viewModels.CurrentSelection
import com.wendorochena.poetskingdom.viewModels.FloatingButtonState
import com.wendorochena.poetskingdom.viewModels.MyImagesViewModelCoil
import com.wendorochena.poetskingdom.viewModels.models.CoilImageItem
import kotlinx.coroutines.flow.StateFlow
import java.io.File

@Composable
fun MyImagesScreenAppCoil(
    myImagesViewModel: MyImagesViewModelCoil
) {
    val viewModelState by myImagesViewModel.modelState.collectAsStateWithLifecycle()
    var isFirstUse = myImagesViewModel.determineFirstUse(LocalContext.current, "myImagesFirstUse")
    val topLevelContext = LocalContext.current
    myImagesViewModel.performImagePreChecks(topLevelContext)
    val onImageItemClick: (Int) -> Unit = {
        myImagesViewModel.onImageItemClick(index = it, context = topLevelContext)
    }
    val onImageLongClick: (Int) -> Unit = {
        myImagesViewModel.onImageLongClick(it)
    }
    val onCurrentHeadingClick: (CurrentSelection) -> Unit =
        { myImagesViewModel.updateCurrentSelection(it) }
    val transferImagesToLocalFolder: (List<Uri>, Context) -> Unit = { uriList, context ->
        myImagesViewModel.setShouldDisplayNotification(true)
        myImagesViewModel.transferImagesToLocalFolder(uriList, context) {
            myImagesViewModel.clearNotificationState()
        }
    }
    val initiateShareIntent: (Context) -> Unit = {
        myImagesViewModel.initiateShareIntent(it)
    }
    val deleteSelectedImages: (Context) -> Unit = {
        myImagesViewModel.setShouldDisplayNotification(true)
        myImagesViewModel.deleteSelectedImages(it) {
            myImagesViewModel.clearNotificationState()
        }
    }
    val deleteSavedPoems: (Context) -> Unit = {
        myImagesViewModel.deleteSavedPoems(it)
    }

    val retrieveThumbnailFilePath: (String) -> File = {
        myImagesViewModel.retrieveThumbnailFilePath(it, topLevelContext)
    }
    val retrieveImageFiles: () -> Unit = {
        myImagesViewModel.getImageFilesAsImageRequest(topLevelContext)
    }
    val retrieveImagePoemThumbnailsFiles: () -> Unit = {
        myImagesViewModel.getThumbnails(topLevelContext)
    }

    Scaffold(floatingActionButton = {
        FloatingActionButtonCoil(
            viewModelState.floatingButtonStateVar,
            viewModelState.currentSelection,
            transferImagesToLocalFolder,
            initiateShareIntent,
            deleteSelectedImages,
            deleteSavedPoems,
            { myImagesViewModel.clearNotificationState() }
        )
    }, floatingActionButtonPosition = FabPosition.End) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(MaterialTheme.colors.background)
        ) {

            if (viewModelState.onImageLongPressed) {
                BackHandler(true) {
                    myImagesViewModel.setOnLongClick(false)
                    myImagesViewModel.setFloatingButtonState(FloatingButtonState.ADDIMAGE)
                    myImagesViewModel.resetSelectedImages()
                }
            }

            TopRowCoil(
                currentSelection = viewModelState.currentSelection,
                onCurrentHeadingClick = onCurrentHeadingClick,
                onImageLongPressed = viewModelState.onImageLongPressed
            )
            when (viewModelState.currentSelection) {
                CurrentSelection.IMAGES -> {
                    ImagesViewCoil(
                        onImageItemClick = onImageItemClick,
                        onLongClick = onImageLongClick,
                        coilImageItems = viewModelState.imageThumbnails,
                        onImageLongPressed = viewModelState.onImageLongPressed,
                        retrieveImageFiles = retrieveImageFiles,
                        isPerformingPreChecks = viewModelState.isPerformingPreChecks
                    )
                }

                CurrentSelection.POEMS -> {
                    PoemImagesViewCoil(
                        viewModelState.onImageLongPressed,
                        onImageItemClick,
                        onImageLongClick,
                        viewModelState.poemThumbnails,
                        retrieveThumbnailFilePath,
                        retrieveImagePoemThumbnailsFiles = retrieveImagePoemThumbnailsFiles
                    )
                }
            }

            if (isFirstUse) {
                FirstUseDialog(R.string.my_images, R.string.guide_my_images, false)
                isFirstUse = false
            }
        }
    }

    ImagesNotificationCont(myImagesViewModel.imagesNotificationModelState)
}

@Composable
fun ImagesNotificationCont(imagesNotificationModelStateFlow: StateFlow<ImagesNotificationModel>) {
    val imagesNotificationModel = imagesNotificationModelStateFlow.collectAsStateWithLifecycle()

    if (imagesNotificationModel.value.shouldDisplayNotification.value)
        ImagesNotification(imagesNotificationModel = imagesNotificationModel.value)
}

@Composable
fun FloatingActionButtonCoil(
    floatingButtonStateVar: FloatingButtonState,
    currentSelection: CurrentSelection,
    transferImagesToLocalFolder: (List<Uri>, Context) -> Unit,
    initiateShareIntent: (Context) -> Unit,
    deleteSelectedImages: (Context) -> Unit,
    deleteSavedPoems: (Context) -> Unit,
    dismissNotificationAlert: () -> Unit
) {
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uriList ->
            if (uriList.isEmpty())
                dismissNotificationAlert.invoke()
            else
                transferImagesToLocalFolder(uriList, context)
        }
    )
    val permissionsResultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it)
            imagePicker.launch("image/*")
    }

    if (floatingButtonStateVar == FloatingButtonState.ADDIMAGE) {
        androidx.compose.material3.FloatingActionButton(
            onClick = {
                if (Build.VERSION.SDK_INT < 33) {
                    if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        imagePicker.launch("image/*")
                    } else {
                        permissionsResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(
                            Manifest.permission.READ_MEDIA_IMAGES
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        imagePicker.launch("image/*")
                    } else {
                        permissionsResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            },
            containerColor = DefaultColor,
            shape = CircleShape
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_baseline_add_a_photo_24),
                contentDescription = "",
                colorFilter = ColorFilter.tint(color = Color.White)
            )
        }
    } else {
        Column {
            if (currentSelection == CurrentSelection.POEMS) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = { initiateShareIntent(context) },
                    containerColor = DefaultColor,
                    shape = CircleShape
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_baseline_share_24),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(color = Color.White),
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
            }
            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    if (currentSelection == CurrentSelection.IMAGES) deleteSelectedImages(context)
                    else deleteSavedPoems(context)
                },
                containerColor = DefaultColor,
                shape = CircleShape
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(color = Color.White),
                )
            }
        }
    }

}

@Composable
fun PoemImagesViewCoil(
    onImageLongPressed: Boolean,
    onImageItemClick: (Int) -> Unit,
    onImageLongClick: (Int) -> Unit,
    imageFiles: List<CoilImageItem>,
    retrieveThumbnailFilePath: (String) -> File,
    modifier: Modifier = Modifier,
    retrieveImagePoemThumbnailsFiles: () -> Unit
) {
    var initialLoading by remember { mutableStateOf(true) }

    if (initialLoading) {
        retrieveImagePoemThumbnailsFiles.invoke()
        initialLoading = false
    }

    LazyVerticalGrid(
        modifier = modifier
            .padding(top = 5.dp)
            .fillMaxWidth(), columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items(count = imageFiles.count()) {
            Column {
                val imageRequestEntry = imageFiles.elementAt(it)
                ImagesItemCoil(
                    imageRequestEntry.imageState,
                    modifier = Modifier
                        .padding(3.dp)
                        .aspectRatio(1f),
                    onImageItemClick,
                    onImageLongClick,
                    onImageLongPressed,
                    it
                )
                TitleItem(retrieveThumbnailFilePath(imageRequestEntry.key))
            }
        }
    }
}

@Composable
fun ImagesItemCoil(
    imageRequestPair: Pair<ImageRequest, Boolean>,
    modifier: Modifier,
    onImageItemClick: (Int) -> Unit,
    onLongClick: (Int) -> Unit,
    isLongClicked: Boolean,
    index: Int
) {
    var imageClicked by remember { mutableStateOf(false) }
    Box(modifier = Modifier) {
        if (isLongClicked) {
            if (imageRequestPair.second) {
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
        AsyncImage(
            model = imageRequestPair.first,
            contentDescription = "",
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    enabled = true,
                    onClick = { imageClicked = true },
                    onLongClick = { onLongClick.invoke(index) }),
            contentScale = ContentScale.Crop
        )
        if (imageClicked) {
            onImageItemClick.invoke(index)
            imageClicked = false
        }
    }
}

@Composable
fun ImagesViewCoil(
    modifier: Modifier = Modifier,
    onLongClick: (Int) -> Unit,
    onImageItemClick: (Int) -> Unit,
    coilImageItems: List<CoilImageItem>,
    onImageLongPressed: Boolean,
    retrieveImageFiles: () -> Unit,
    isPerformingPreChecks : Boolean,
) {
    var performInitialLoad by remember { mutableStateOf(true) }
    if (performInitialLoad && !isPerformingPreChecks) {
        retrieveImageFiles.invoke()
        performInitialLoad = false
    }

    if (coilImageItems.isEmpty() && !isPerformingPreChecks)
        ImagesLoading()

    LazyVerticalGrid(
        modifier = modifier.padding(top = 5.dp), columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(count = coilImageItems.size, key = { return@items coilImageItems[it].key }) {
            val imageRequestEntry = coilImageItems.elementAt(it)
            ImagesItemCoil(
                imageRequestEntry.imageState,
                modifier = Modifier
                    .padding(3.dp)
                    .aspectRatio(1f),
                onImageItemClick,
                onLongClick,
                onImageLongPressed,
                it
            )
        }
    }
}

@Composable
fun TopRowCoil(
    modifier: Modifier = Modifier,
    currentSelection: CurrentSelection,
    onCurrentHeadingClick: (CurrentSelection) -> Unit,
    onImageLongPressed: Boolean
) {

    Row(
        modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable {
                    if (!onImageLongPressed)
                        onCurrentHeadingClick.invoke(
                            CurrentSelection.IMAGES
                        )
                }) {
            if (currentSelection == CurrentSelection.IMAGES) {
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
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable {
                    if (!onImageLongPressed)
                        onCurrentHeadingClick.invoke(
                            CurrentSelection.POEMS
                        )
                }) {
            if (currentSelection == CurrentSelection.POEMS) {
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
fun MyImagesScreenPreviewCoil() {
    PoetsKingdomTheme {
        MyImagesScreenAppCoil(myImagesViewModel = MyImagesViewModelCoil())
    }
}