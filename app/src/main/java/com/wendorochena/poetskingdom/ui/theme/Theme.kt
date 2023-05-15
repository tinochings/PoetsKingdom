/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wendorochena.poetskingdom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

//todo
private val DarkColorPalette = darkColors(
    background = DarkDefaultBackgroundColor,
    surface = DarkDefaultBackgroundColor,
    primary = DarkTextColor,
    secondary = DarkDefaultStatusBarColor,
    secondaryVariant = OffWhite
)
private val LightColorPalette = lightColors(
    background = DefaultBackgroundColor,
    primary = TextColor,
    surface = DefaultBackgroundColor,
    secondary = DefaultStatusBarColor,
    secondaryVariant = LightBlack
)

@Composable
fun PoetsKingdomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    MaterialTheme(
        colors = colors,
        typography = TypographyPK,
        content = content
    )
}
