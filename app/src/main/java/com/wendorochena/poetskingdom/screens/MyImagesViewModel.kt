package com.wendorochena.poetskingdom.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.wendorochena.poetskingdom.R
import java.io.File

enum class CurrentSelection {
    IMAGES, POEMS
}

class MyImagesViewModel : ViewModel() {

    var currentSelection by mutableStateOf(CurrentSelection.IMAGES)
        private set
//    var poem

    fun setSelection(selection: CurrentSelection) {
        currentSelection = selection
    }

    /**
     * Gets an arraylist containing thumbnails to display
     */
    fun getThumbnails(context: Context): ArrayList<File> {
        val arrayListToRet = ArrayList<File>()
        val savedImagesFolder =
            context.getDir(
                context.getString(R.string.saved_images_folder_name),
                Context.MODE_PRIVATE
            )
        val thumbnailsFolder =
            context.getDir(context.getString(R.string.thumbnails_folder_name), Context.MODE_PRIVATE)
        try {
            val savedImageFiles = savedImagesFolder.listFiles()?.toMutableList()
            if (savedImageFiles != null) {
                savedImageFiles.sortByDescending { it.lastModified() }
                for (file in savedImageFiles) {
                    val thumbnailFile = File(
                        thumbnailsFolder?.absolutePath + File.separator + file.name + ".png"
                    )
                    if (thumbnailFile.exists()) {
                        arrayListToRet.add(File(thumbnailFile.absolutePath))
                    } else {
                        Log.e("No Such Thumbnail", file.name)
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return arrayListToRet
    }
}