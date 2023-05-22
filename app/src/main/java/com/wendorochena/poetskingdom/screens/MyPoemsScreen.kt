package com.wendorochena.poetskingdom.screens

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.rememberDrawerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wendorochena.poetskingdom.CreatePoem
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.ui.theme.BottomDrawerColor
import com.wendorochena.poetskingdom.ui.theme.DefaultColor
import com.wendorochena.poetskingdom.ui.theme.DefaultStatusBarColor
import com.wendorochena.poetskingdom.ui.theme.HelveticaFont
import com.wendorochena.poetskingdom.ui.theme.OffWhite
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import com.wendorochena.poetskingdom.viewModels.MyPoemsViewModel
import com.wendorochena.poetskingdom.viewModels.PoemThemeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MyPoemsApp(myPoemsViewModel: MyPoemsViewModel) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val onMenuClicked: () -> Unit = {
        scope.launch {
            drawerState.open()
        }
    }
    val onSearchClick: () -> Unit = {
        if (myPoemsViewModel.searchButtonClicked)
            myPoemsViewModel.clearSearchOptions()
        myPoemsViewModel.searchButtonClicked = true
    }
    val sharedPreferences =
        LocalContext.current.getSharedPreferences(
            LocalContext.current.getString(R.string.shared_pref),
            Context.MODE_PRIVATE
        )
    var isFirstUse = false
    if (!sharedPreferences.getBoolean("myPoemsFirstUse", false)) {
        isFirstUse = true
        sharedPreferences.edit().putBoolean("myPoemsFirstUse", true).apply()
    }
    ModalDrawer(
        drawerState = drawerState,
        drawerShape = RoundedCornerShape(topEnd = 35.dp, bottomEnd = 35.dp),
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = androidx.compose.material.MaterialTheme.colors.background)
            ) {
                DrawerContainer(myPoemsViewModel = myPoemsViewModel, drawerState, scope)
            }
        }) {
        Scaffold(
            bottomBar = { if (myPoemsViewModel.onImageLongPressed) BottomBar(myPoemsViewModel) },
            topBar = {
                HomeScreenAppBar(
                    displaySearch = true,
                    onSearchClick = onSearchClick,
                    onMenuClicked = onMenuClicked
                )
            }) {
            if (myPoemsViewModel.onImageLongPressed) {
                BackHandler(true) {
                    myPoemsViewModel.setOnLongClick(false)
                    myPoemsViewModel.resetSelectedImages()
                    if (myPoemsViewModel.displayAlbumSelector)
                        myPoemsViewModel.displayAlbumSelector = false
                }
            }
            if (myPoemsViewModel.searchButtonClicked) {
                BackHandler(true) {
                    if (myPoemsViewModel.hitsFound) {
                        myPoemsViewModel.clearSearchOptions()
                        myPoemsViewModel.searchButtonClicked = true
                    } else
                        myPoemsViewModel.clearSearchOptions()
                    myPoemsViewModel.saveSearchHistory(context)
                }
            }
            if (myPoemsViewModel.displayAlbumSelector)
                AlbumsSelector(myPoemsViewModel)
            Column(
                modifier = Modifier
                    .background(androidx.compose.material.MaterialTheme.colors.background)
                    .fillMaxSize()
                    .padding(it)
            ) {
                if (!myPoemsViewModel.searchButtonClicked)
                    PoemListView(myPoemsViewModel)
                else {
                    if (myPoemsViewModel.displayNoResultsFound)
                        NoResultsDialog(myPoemsViewModel)
                    else if (myPoemsViewModel.hitsFound)
                        SearchImageList(myPoemsViewModel = myPoemsViewModel)
                    else
                        SearchView(myPoemsViewModel)
                }
                if (myPoemsViewModel.displayAlbumsDialog)
                    AlbumNameDialog(myPoemsViewModel = myPoemsViewModel)

                if (isFirstUse) {
                    FirstUseDialog(
                        heading = R.string.my_poems_text,
                        guideText = R.string.guide_my_poems, false
                    )
                    isFirstUse = false
                }
            }
        }
    }
}

@Composable
fun AlbumsSelector(myPoemsViewModel: MyPoemsViewModel) {
    val context = LocalContext.current
    val onAlbumClick: (String) -> Unit = {
        if (myPoemsViewModel.addPoemToAlbum(context, it)) {
            myPoemsViewModel.displayAlbumSelector = false
            myPoemsViewModel.setOnLongClick(false)
            myPoemsViewModel.resetSelectedImages()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = dimensionResource(id = R.dimen.action_bar_size))
            .background(color = androidx.compose.material.MaterialTheme.colors.background.copy(0.5f))
            .zIndex(2f),
        verticalArrangement = Arrangement.Bottom
    ) {
        val albums = myPoemsViewModel.getAlbums(LocalContext.current)
        LazyColumn(
            modifier = Modifier
                .padding(10.dp)
                .background(
                    color = BottomDrawerColor,
                    shape = RoundedCornerShape(25.dp)
                )
        ) {
            items(albums.size) {
                AlbumItem(albums[it], onAlbumClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumNameDialog(myPoemsViewModel: MyPoemsViewModel) {
    var albumName by remember { mutableStateOf("") }
    var dialogTitle by remember { mutableStateOf(R.string.add_new_album_content_description) }
    var buttonText by remember { mutableStateOf(R.string.confirm) }
    var inputMessage by remember { mutableStateOf(R.string.valid_input_message) }
    var shouldChangeText by remember { mutableStateOf(false) }

    val validateInput: @Composable (String) -> Boolean = {
        if (PoemThemeViewModel.isValidatedInput(it.replace(' ', '_'))) {
            val boolean = myPoemsViewModel.addAlbumName(it, LocalContext.current)
            boolean
        } else {
            false
        }
    }
    if (myPoemsViewModel.albumSaveResult == -1) {
        dialogTitle = R.string.retry
        buttonText = R.string.retry
        inputMessage = R.string.file_already_exists
        myPoemsViewModel.albumSaveResult = -2
    } else if (myPoemsViewModel.albumSaveResult == 0) {
        myPoemsViewModel.displayAlbumsDialog = false
        myPoemsViewModel.albumSaveResult = -2
    }

    if (myPoemsViewModel.displayAlbumsDialog) {
        Dialog(
            onDismissRequest = { myPoemsViewModel.displayAlbumsDialog = false },
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
                        style = androidx.compose.material.MaterialTheme.typography.h1,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = stringResource(id = inputMessage),
                        style = androidx.compose.material.MaterialTheme.typography.body1,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    TextField(
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            disabledTextColor = Color.White
                        ),
                        singleLine = true,
                        value = albumName,
                        onValueChange = { if (it.length <= 60) albumName = it },
                        label = {
                            androidx.compose.material.Text(
                                stringResource(id = R.string.create_album_hint),
                                color = OffWhite
                            )
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = { shouldChangeText = true },
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultColor
                        ),
                        modifier = Modifier
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = buttonText),
                            color = Color.White,
                            style = androidx.compose.material.MaterialTheme.typography.body1
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
        if (shouldChangeText) {
            if (dialogTitle == R.string.retry) {
                dialogTitle = R.string.add_new_album_content_description
                buttonText = R.string.confirm
                inputMessage = R.string.valid_input_message
            } else if (!validateInput.invoke(albumName)) {
                dialogTitle = R.string.retry
                buttonText = R.string.retry
                inputMessage = R.string.invalid_input_message
            }
            shouldChangeText = false
        }
    }
}

@Composable
fun DrawerContainer(
    myPoemsViewModel: MyPoemsViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val onAlbumClick: (String) -> Unit = {
        myPoemsViewModel.setAlbumSelection(it)
        scope.launch { drawerState.close() }
    }
    val albums = myPoemsViewModel.getAlbums(LocalContext.current)
    LazyColumn(
        modifier = Modifier
            .padding(10.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Image(
                    modifier = Modifier
                        .height(48.dp)
                        .width(48.dp)
                        .clickable { myPoemsViewModel.displayAlbumsDialog = true },
                    painter = painterResource(id = R.drawable.baseline_add_circle_outline_24),
                    contentDescription = stringResource(
                        id = R.string.add_new_album_content_description
                    ),
                    colorFilter = ColorFilter.tint(color = androidx.compose.material.MaterialTheme.colors.primaryVariant)
                )
            }
            Text(
                text = stringResource(id = R.string.albums),
                style = androidx.compose.material.MaterialTheme.typography.h1,
                color = androidx.compose.material.MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = myPoemsViewModel.albumNameSelection,
                style = androidx.compose.material.MaterialTheme.typography.caption,
                color = androidx.compose.material.MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        items(albums.size) {
            AlbumItem(albums[it], onAlbumClick)
        }
    }
}

@Composable
fun AlbumItem(albumName: String, onAlbumClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAlbumClick.invoke(albumName) },
        horizontalArrangement = Arrangement.End
    ) {
        Image(
            painter = painterResource(id = R.drawable.folder_svg),
            contentDescription = stringResource(
                id = R.string.album_icon_content_description
            ),
            modifier = Modifier
                .weight(0.2f)
                .fillMaxWidth()
        )

        Text(
            text = albumName, style = androidx.compose.material.MaterialTheme.typography.h1,
            color = androidx.compose.material.MaterialTheme.colors.primary,
            modifier = Modifier.weight(0.8f)
        )
    }
}

@Composable
fun NoResultsDialog(myPoemsViewModel: MyPoemsViewModel) {
    if (myPoemsViewModel.displayNoResultsFound) {
        Dialog(
            onDismissRequest = {
                myPoemsViewModel.displayNoResultsFound = false
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline,
                colors = CardDefaults.cardColors(containerColor = DefaultColor)
            ) {
                Text(
                    text = stringResource(id = R.string.no_results),
                    style = androidx.compose.material.MaterialTheme.typography.h1,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = stringResource(id = R.string.no_results_found_text),
                    style = androidx.compose.material.MaterialTheme.typography.body1,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    modifier = Modifier
                        .align(Alignment.End),
                    onClick = { myPoemsViewModel.displayNoResultsFound = false; },
                    colors = ButtonDefaults.buttonColors(containerColor = DefaultColor)
                ) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            }
        }
    }

}

@Composable
fun SearchImageList(myPoemsViewModel: MyPoemsViewModel) {
    val onThumbnailClicked: @Composable (File) -> Unit = {
        val newActivityIntent =
            Intent(LocalContext.current, CreatePoem::class.java)
        newActivityIntent.putExtra("loadPoem", true)
        newActivityIntent.putExtra(
            "poemTitle",
            it.name.split(".")[0]
        )
        LocalContext.current.startActivity(newActivityIntent)
    }
    val subStringLocations = myPoemsViewModel.substringLocations
    val poemBackgroundTypeArrayList = myPoemsViewModel.poemBackgroundTypeArrayList
    val imageFiles = myPoemsViewModel.searchResultFiles
    val imageFileKeys = imageFiles.sortedByDescending { it.lastModified() }
    LazyVerticalGrid(
        modifier = Modifier.padding(top = 5.dp), columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(count = imageFileKeys.count()) {
            Column {
                val file = imageFileKeys.elementAt(it)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    ImagesItem(
                        Pair(file, false),
                        modifier = Modifier
                            .padding(3.dp)
                            .fillMaxWidth(),
                        onThumbnailClicked,
                        {},
                        myPoemsViewModel.onImageLongPressed
                    )
                    if (subStringLocations.size > 0 && poemBackgroundTypeArrayList.size > 0)
                        SearchResultText(
                            myPoemsViewModel,
                            backgroundPair = poemBackgroundTypeArrayList[it],
                            substringLocations = subStringLocations[it]
                        )
                }
                TitleItem(file)
            }
        }
    }
}

@Composable
fun SearchResultText(
    myPoemsViewModel: MyPoemsViewModel,
    backgroundPair: Pair<BackgroundType, Int>,
    substringLocations: Pair<String, String>
) {
    val textAndStanzas = myPoemsViewModel.highlightedText(substringLocations)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(3.dp),
    ) {
        Text(
            text = textAndStanzas.first, color = Color(backgroundPair.second),
            fontSize = 14.sp, modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        Text(
            text = stringResource(id = R.string.search_stanza_text, textAndStanzas.second),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 150.dp)
                .height(50.dp),
            style = androidx.compose.material.MaterialTheme.typography.h1,
            maxLines = 1,
            textAlign = TextAlign.Center,
            color = androidx.compose.material.MaterialTheme.colors.primary,
        )
    }
}

@Composable
fun SearchView(myPoemsViewModel: MyPoemsViewModel) {
    var check1 by remember { mutableStateOf(true) }
    var check2 by remember { mutableStateOf(false) }
    var check3 by remember { mutableStateOf(false) }

    val checkManager: (String) -> Unit = {
        when (it) {
            "check1" -> {
                check1 = !check1
                if (check1) {
                    check2 = false
                    check3 = false
                }
            }

            "check2" -> {
                check2 = !check2
                if (check2) {
                    check1 = false
                    check3 = false
                }
            }

            "check3" -> {
                check3 = !check3
                if (check3) {
                    check1 = false
                    check2 = false
                }
            }
        }
    }
    var invokeDimmerProgress by remember { mutableStateOf(false) }
    var textSearch by remember { mutableStateOf("") }
    val currentSearchHeading = if (check1)
        stringResource(id = R.string.exact_phrase_search)
    else if (check2)
        stringResource(id = R.string.approximate_phrase_search)
    else if (check3)
        stringResource(id = R.string.contains_phrase_search)
    else
        stringResource(id = R.string.exact_phrase_search)
    val applicationContext = LocalContext.current.applicationContext
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding()
    ) {
        Row(
            modifier = Modifier
                .weight(1f, true)
                .clickable { checkManager.invoke("check1") },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = check1,
                onCheckedChange = { checkManager.invoke("check1") },
                colors = CheckboxDefaults.colors(
                    checkedColor = DefaultColor,
                    uncheckedColor = androidx.compose.material.MaterialTheme.colors.primary
                ),
                modifier = Modifier.weight(0.2f)
            )
            Text(
                text = stringResource(id = R.string.exact_phrase_search),
                fontWeight = FontWeight.Bold,
                fontFamily = HelveticaFont,
                textAlign = TextAlign.Center,
                color = androidx.compose.material.MaterialTheme.colors.primary,
                modifier = Modifier
                    .weight(0.8f, true)
                    .padding(start = 5.dp)
            )
        }
        Row(
            modifier = Modifier
                .weight(1f, true)
                .clickable { checkManager.invoke("check2") },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = check2,
                onCheckedChange = { checkManager.invoke("check2") },
                colors = CheckboxDefaults.colors(
                    checkedColor = DefaultColor,
                    uncheckedColor = androidx.compose.material.MaterialTheme.colors.primary
                ),
                modifier = Modifier.weight(0.2f)
            )
            Text(
                text = stringResource(id = R.string.approximate_phrase_search),
                color = androidx.compose.material.MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                fontFamily = HelveticaFont,
                modifier = Modifier
                    .weight(0.8f, true)
                    .padding(start = 5.dp)
            )
        }
        Row(
            modifier = Modifier
                .weight(1f, true)
                .clickable { checkManager.invoke("check3") },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = check3,
                onCheckedChange = { checkManager.invoke("check3") },
                colors = CheckboxDefaults.colors(
                    checkedColor = DefaultColor,
                    uncheckedColor = androidx.compose.material.MaterialTheme.colors.primary
                ),
                modifier = Modifier.weight(0.2f)
            )
            Text(
                text = stringResource(id = R.string.contains_phrase_search),
                color = androidx.compose.material.MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                fontFamily = HelveticaFont,
                modifier = Modifier
                    .weight(0.8f, true)
                    .padding(start = 5.dp)
            )
        }
    }
    TextField(
        value = textSearch,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            myPoemsViewModel.updateSearchHistory(textSearch)
            myPoemsViewModel.invokeSearch(
                textSearch,
                applicationContext,
                currentSearchHeading,
                Dispatchers.Main,
                Dispatchers.IO
            )
            invokeDimmerProgress = true
        }),
        onValueChange = { textSearch = it },
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_here),
                color = Color.White,
                fontFamily = HelveticaFont
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DefaultColor, shape = RoundedCornerShape(10.dp))
            .border(width = 3.dp, color = DefaultStatusBarColor, RoundedCornerShape(10.dp))
            .onKeyEvent { it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            focusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
    val searchHistory = myPoemsViewModel.getSearchHistory(LocalContext.current)
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        item {
            Divider(color = DefaultStatusBarColor, thickness = 2.dp)
            Text(
                text = stringResource(id = R.string.recent_searches),
                style = androidx.compose.material.MaterialTheme.typography.caption,
                color = androidx.compose.material.MaterialTheme.colors.secondaryVariant
            )
        }
        items(count = searchHistory.size) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = searchHistory[searchHistory.size - 1 - it],
                    style = androidx.compose.material.MaterialTheme.typography.h2,
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxWidth(),
                    maxLines = 1,
                    color = androidx.compose.material.MaterialTheme.colors.primary
                )
                Row(
                    modifier = Modifier
                        .weight(0.2f),
                    horizontalArrangement = Arrangement.End
                ) {
                    Image(
                        modifier = Modifier
                            .widthIn(max = 48.dp)
                            .clickable { myPoemsViewModel.deleteHistoryItem(searchHistory.size - 1 - it) },
                        painter = painterResource(id = R.drawable.cancel_close),
                        contentDescription = stringResource(
                            id = R.string.cancel_button_content_description
                        ),
                        alignment = Alignment.CenterEnd,
                        colorFilter = ColorFilter.tint(color = androidx.compose.material.MaterialTheme.colors.primary)
                    )
                }
            }
        }
        item { Divider(color = DefaultStatusBarColor, thickness = 2.dp) }
    }
    if (invokeDimmerProgress) {
        DimmerProgress()
        invokeDimmerProgress = false
    }
}

@Composable
fun PoemListView(myPoemsViewModel: MyPoemsViewModel) {
    val onThumbnailClicked: @Composable (File) -> Unit = {
        if (!myPoemsViewModel.onImageLongPressed) {
            val newActivityIntent =
                Intent(LocalContext.current, CreatePoem::class.java)
            newActivityIntent.putExtra("loadPoem", true)
            newActivityIntent.putExtra(
                "poemTitle",
                it.name.split(".")[0]
            )
            val albumNameIfAny = myPoemsViewModel.resolveAlbumName(it)
            if (albumNameIfAny != null)
                newActivityIntent.putExtra(
                    LocalContext.current.getString(R.string.album_argument_name),
                    albumNameIfAny.replace(' ', '_')
                )
            LocalContext.current.startActivity(newActivityIntent)
        } else {
            myPoemsViewModel.allSavedPoems[it] = !myPoemsViewModel.allSavedPoems[it]!!
        }
    }
    val onLongClick: (File) -> Unit = {
        if (myPoemsViewModel.albumNameSelection == myPoemsViewModel.allPoemsString)
            myPoemsViewModel.allSavedPoems[it] = true
        else
            myPoemsViewModel.albumSavedPoems[it] = true
        myPoemsViewModel.setOnLongClick(true)
    }
    val imageFiles = myPoemsViewModel.getThumbnails(
        LocalContext.current.applicationContext,
        myPoemsViewModel.albumNameSelection
    )
    val imageFileKeys = imageFiles.keys.asSequence().sortedByDescending { it.lastModified() }
    LazyVerticalGrid(
        modifier = Modifier.padding(top = 5.dp), columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(count = imageFileKeys.count()) {
            Column {
                val file = imageFileKeys.elementAt(it)
                ImagesItem(
                    Pair(file, imageFiles[file]!!),
                    modifier = Modifier
                        .padding(3.dp)
                        .aspectRatio(1f),
                    onThumbnailClicked,
                    onLongClick,
                    myPoemsViewModel.onImageLongPressed
                )
                TitleItem(file)
            }
        }
    }
}

@Composable
fun BottomBar(myPoemsViewModel: MyPoemsViewModel) {
    val context = LocalContext.current as? ComponentActivity
    val applicationContext = LocalContext.current
    val onShareIntent: () -> Unit = {
        if (context != null)
            myPoemsViewModel.shareIntent(context.applicationContext)
    }
    Box(
        modifier = Modifier
            .height(dimensionResource(id = R.dimen.action_bar_size))
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .background(
                shape = RoundedCornerShape(25.dp),
                color = BottomDrawerColor
            )
            .zIndex(2f), contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 5.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    onClick = { onShareIntent.invoke() }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_baseline_share_24),
                        contentDescription = "Share Button"
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 5.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    onClick = { myPoemsViewModel.deleteSavedPoems(context = applicationContext) }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                        contentDescription = "Share Button",
                        contentScale = ContentScale.FillBounds,
                    )

                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 5.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    onClick = {
                        myPoemsViewModel.displayAlbumSelector =
                            !myPoemsViewModel.displayAlbumSelector
                    }) {
                    Image(
                        painter = painterResource(id = R.drawable.add_to_album),
                        contentDescription = "Share Button",
                        contentScale = ContentScale.FillBounds,
                        colorFilter = ColorFilter.tint(color = androidx.compose.material.MaterialTheme.colors.primaryVariant)
                    )

                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyPoemsPreview() {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PoetsKingdomTheme {
            MyPoemsApp(viewModel())
        }
    }

}