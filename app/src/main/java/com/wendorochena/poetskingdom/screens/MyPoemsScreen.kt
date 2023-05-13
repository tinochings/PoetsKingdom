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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme
import com.wendorochena.poetskingdom.viewModels.MyPoemsViewModel
import kotlinx.coroutines.Dispatchers
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPoemsApp(myPoemsViewModel: MyPoemsViewModel) {
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
    Scaffold(
        bottomBar = { if (myPoemsViewModel.onImageLongPressed) BottomBar(myPoemsViewModel) },
        topBar = { HomeScreenAppBar(displaySearch = true, onSearchClick = onSearchClick) }) {
        if (myPoemsViewModel.onImageLongPressed) {
            BackHandler(true) {
                myPoemsViewModel.setOnLongClick(false)
            }
        }
        if (myPoemsViewModel.searchButtonClicked) {
            BackHandler(true) {
                if (myPoemsViewModel.hitsFound) {
                    myPoemsViewModel.clearSearchOptions()
                    myPoemsViewModel.searchButtonClicked = true
                } else
                    myPoemsViewModel.clearSearchOptions()
            }
        }
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
            if (isFirstUse) {
                FirstUseDialog(
                    heading = R.string.my_poems_text,
                    guideText = R.string.guide_my_poems
                )
                isFirstUse = false
            }
        }
    }
}

@Composable
fun NoResultsDialog(myPoemsViewModel: MyPoemsViewModel) {
    var shouldDismiss by remember { mutableStateOf(false) }
    if (myPoemsViewModel.displayNoResultsFound) {
        Dialog(
            onDismissRequest = { shouldDismiss = true; myPoemsViewModel.displayNoResultsFound = false;},
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
                            .height(200.dp),
                        onThumbnailClicked,
                        {},
                        myPoemsViewModel.onImageLongPressed
                    )
                    if (subStringLocations.size > 0 && poemBackgroundTypeArrayList.size > 0)
                        SearchResultText(
                            myPoemsViewModel,
                            myPoemsViewModel.poemBackgroundTypeArrayList[it],
                            myPoemsViewModel.substringLocations[it]
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

@OptIn(ExperimentalMaterial3Api::class)
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
                colors = CheckboxDefaults.colors(checkedColor = DefaultColor),
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
                colors = CheckboxDefaults.colors(checkedColor = DefaultColor),
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
                colors = CheckboxDefaults.colors(checkedColor = DefaultColor),
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
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.White,
            containerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
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
            LocalContext.current.startActivity(newActivityIntent)
        } else {
            myPoemsViewModel.savedPoems[it] = !myPoemsViewModel.savedPoems[it]!!
        }
    }
    val onLongClick: (File) -> Unit = {
        myPoemsViewModel.savedPoems[it] = true
        myPoemsViewModel.setOnLongClick(true)
    }
    val imageFiles = myPoemsViewModel.getThumbnails(LocalContext.current.applicationContext)
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
                        .height(150.dp),
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