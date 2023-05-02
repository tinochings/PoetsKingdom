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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(0.dp)
)
val RoundedRectangleOutline = RoundedCornerShape(15.dp)
val RectangleOutline = RectangleShape
val TeardropOutline = RoundedCornerShape(
    topStart = 55.dp,
    topEnd = 0.dp,
    bottomStart = 55.dp,
    bottomEnd = 55.dp
)
val RotatedTeardropOutline = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 35.dp,
    bottomStart = 35.dp,
    bottomEnd = 35.dp
)

val LemonOutline  = RoundedCornerShape(
    topStart = 90.dp,
    topEnd = 5.dp,
    bottomStart = 5.dp,
    bottomEnd = 90.dp
)

val RotatedLemonOutline  = RoundedCornerShape(
    topStart = 5.dp,
    topEnd = 90.dp,
    bottomStart = 90.dp,
    bottomEnd = 5.dp
)