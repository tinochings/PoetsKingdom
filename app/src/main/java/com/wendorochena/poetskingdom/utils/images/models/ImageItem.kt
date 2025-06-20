package com.wendorochena.poetskingdom.utils.images.models

import java.io.File

data class ImageItem<T>(val id : T, val imageFile: File){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageItem<*>

        if (id != other.id) return false
        if (imageFile != other.imageFile) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + imageFile.hashCode()
        return result
    }
}
