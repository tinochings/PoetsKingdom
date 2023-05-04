package com.wendorochena.poetskingdom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.ui.theme.DefaultBackgroundColor
import com.wendorochena.poetskingdom.ui.theme.DefaultStatusBarColor
import com.wendorochena.poetskingdom.ui.theme.PoetsKingdomTheme

enum class HomeScreen {
    HOME, MYIMAGES
}

@Composable
fun HomeScreenAppBar(modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.action_bar_size))
            .background(DefaultStatusBarColor)
    ) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            HomeScreenAppBar()
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = HomeScreen.HOME.name,
            modifier = Modifier
                .padding(it)
                .background(DefaultBackgroundColor)
        ){
            composable(route = HomeScreen.HOME.name) {
                val onMyImagesClicked : () -> Unit = {
                    navController.navigate(HomeScreen.MYIMAGES.name)
                }
                HomePageScreenApp(onImagesClick = onMyImagesClicked)
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