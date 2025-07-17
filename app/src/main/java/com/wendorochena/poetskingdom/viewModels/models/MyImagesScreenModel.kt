package com.wendorochena.poetskingdom.viewModels.models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.wendorochena.poetskingdom.viewModels.CurrentSelection
import com.wendorochena.poetskingdom.viewModels.FloatingButtonState

data class MyImagesScreenModel(
    val currentSelection: CurrentSelection = CurrentSelection.IMAGES,
    val floatingButtonStateVar: FloatingButtonState = FloatingButtonState.ADDIMAGE,
    val onImageLongPressed: Boolean = false,
    val poemThumbnails : SnapshotStateList<CoilImageItem> = mutableStateListOf(),
    val imageThumbnails : SnapshotStateList<CoilImageItem> = mutableStateListOf()
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyImagesScreenModel

        if (currentSelection != other.currentSelection) return false
        if (floatingButtonStateVar != other.floatingButtonStateVar) return false
        if (onImageLongPressed != other.onImageLongPressed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentSelection.hashCode()
        result = 31 * result + floatingButtonStateVar.hashCode()
        result = 31 * result + onImageLongPressed.hashCode()
        return result
    }
}
