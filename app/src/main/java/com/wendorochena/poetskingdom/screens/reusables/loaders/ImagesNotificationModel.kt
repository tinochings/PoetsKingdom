package com.wendorochena.poetskingdom.screens.reusables.loaders

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

data class ImagesNotificationModel(
    val notificationHeader: String = "",
    val progress: MutableState<Float> = mutableFloatStateOf(0f),
    val totalImages: Int = 0,
    val totalImagesProcessed: MutableState<Int> = mutableIntStateOf(1),
    var shouldDisplayNotification : MutableState<Boolean> = mutableStateOf(false),
    val percentage : MutableState<Int> = mutableIntStateOf(0)
)
