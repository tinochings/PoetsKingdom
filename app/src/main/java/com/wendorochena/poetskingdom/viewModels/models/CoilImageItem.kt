package com.wendorochena.poetskingdom.viewModels.models

import coil3.request.ImageRequest

data class CoilImageItem(val key : String, val imageState : Pair<ImageRequest, Boolean>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoilImageItem

        if (key != other.key) return false
        if (imageState != other.imageState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + imageState.hashCode()
        return result
    }
}
