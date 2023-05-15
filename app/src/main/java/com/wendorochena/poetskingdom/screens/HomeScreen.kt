package com.wendorochena.poetskingdom.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wendorochena.poetskingdom.MyPoemsCompose
import com.wendorochena.poetskingdom.PersonalisationActivity
import com.wendorochena.poetskingdom.PoemThemeActivityCompose
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme

enum class HomeScreen {
    HOME, MYIMAGES
}

@Composable
fun HomeScreenAppBar(modifier: Modifier = Modifier, displaySearch : Boolean, onSearchClick : () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.action_bar_size))
            .background(MaterialTheme.colors.secondary)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.appbartitle),
                contentDescription = stringResource(
                    id = R.string.the_poets_kingdom_image
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                alignment = Alignment.Center,
            )
            if (displaySearch) {
                Image(
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                        .clickable { onSearchClick.invoke() }
                        .align(Alignment.CenterEnd),
                    painter = painterResource(id = R.drawable.ic_baseline_search_24),
                    contentDescription = stringResource(id = R.string.search_button_content_description),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenApp() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            HomeScreenAppBar(displaySearch = false, onSearchClick = {})
        },  modifier = Modifier.background(MaterialTheme.colors.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = HomeScreen.HOME.name,
            modifier = Modifier
                .padding(it)
                .background(MaterialTheme.colors.background)
        ) {
            composable(route = HomeScreen.HOME.name) {
                val context = LocalContext.current
                val myPoemActivityIntent =
                    Intent(context, MyPoemsCompose::class.java)
                val personalisationActivityIntent =
                    Intent(context, PersonalisationActivity::class.java)
                val createPoemIntent = Intent(context, PoemThemeActivityCompose::class.java)
                val onMyImagesClicked: () -> Unit = {
                    navController.navigate(HomeScreen.MYIMAGES.name)
                }
                val onMyPoemsClicked: () -> Unit = {
                    context.startActivity(myPoemActivityIntent)
                }
                val onCreatePoemClicked: () -> Unit = {
                    context.startActivity(createPoemIntent)
                }
                val onPersonalisationClicked: () -> Unit = {
                    context.startActivity(personalisationActivityIntent)
                }
                val sharedPreferences = context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
                if (sharedPreferences?.getBoolean("firstUse", false) == false) {
                    sharedPreferences.edit()?.putBoolean("firstUse",true)?.apply()
                    FirstUseDialog(heading = R.string.guide_title, guideText = R.string.guide_first_fragment, true)
                }
                HomePageScreenApp(
                    onImagesClick = onMyImagesClicked,
                    onMyPoemsClick = onMyPoemsClicked,
                    onCreatePoemClick = onCreatePoemClicked,
                    onPersonalisationClick = onPersonalisationClicked
                )
            }
            composable(route = HomeScreen.MYIMAGES.name) {
                MyImagesScreenApp()
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PoetsKingdomTheme {
        HomeScreenApp()
    }
}