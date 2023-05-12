package com.wendorochena.poetskingdom.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.wendorochena.poetskingdom.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

enum class CurrentSelection {
    IMAGES, POEMS
}

enum class FloatingButtonState {
    ADDIMAGE, DELETEIMAGE
}

class MyImagesViewModel : ViewModel() {

    var currentSelection by mutableStateOf(CurrentSelection.IMAGES)
        private set
    var floatingButtonStateVar by mutableStateOf(FloatingButtonState.ADDIMAGE)
        private set
    var onImageLongPressed by mutableStateOf(false)
        private set
    var savedPoemImages = mutableStateMapOf<File, Boolean>()
        private set
    var imageFiles = mutableStateMapOf<File, Boolean>()
        private set

    fun setSelection(selection: CurrentSelection) {
        currentSelection = selection
    }

    fun setFloatingButtonState(state: FloatingButtonState) {
        floatingButtonStateVar = state
    }

    private fun addAllFiles(
        arrayList: ArrayList<File>?,
        listFiles: Array<File>?,
        isImages: Boolean
    ) {
        if (isImages) {
            for (file in listFiles!!) {
                imageFiles[file] = false
            }
        } else {
            for (file in arrayList!!) {
                savedPoemImages[file] = false
            }
        }
    }

    fun getImageFiles(context: Context): MutableMap<File, Boolean> {
        if (imageFiles.isEmpty()) {
            val imagesFolder = context.getDir(
                context.getString(R.string.my_images_folder_name),
                Context.MODE_PRIVATE
            )

            if (imagesFolder?.listFiles() != null)
                addAllFiles(null, imagesFolder.listFiles()!!, true)
        }
        return imageFiles
    }

    fun setOnLongClick(boolean: Boolean) {
        onImageLongPressed = boolean
    }

    /**
     * Deletes image files
     */
    fun deleteImages() {
        val filesToDelete = imageFiles.filter { it.value }
        for (entry in filesToDelete) {
            if (entry.key.delete())
                imageFiles.remove(entry.key)
            else
                Log.e("Failed to remove file: ", entry.key.name)
        }
        onImageLongPressed = false
        setFloatingButtonState(FloatingButtonState.ADDIMAGE)
    }

    /**
     * Deletes saved poem images
     *
     * @param context application context
     */
    fun deleteSavedPoems(context: Context) {
        val filesToDelete = savedPoemImages.filter { it.value }
        for (entry in filesToDelete) {
            try {
                val file = entry.key
                val splitString = file.name.split(File.separator)
                val poemName = splitString[splitString.size - 1]
                val savedImagesFilePath = context.getDir(
                    context.getString(R.string.saved_images_folder_name),
                    Context.MODE_PRIVATE
                )
                val fullPathToDelete = File(
                    savedImagesFilePath.absolutePath + File.separator + poemName.replace(
                        ".png",
                        ""
                    )
                )
                if (fullPathToDelete.exists())
                    if(fullPathToDelete.deleteRecursively())
                        savedPoemImages.remove(file)
                else {
                    Log.e("Failed to remove file: ", entry.key.name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Failed to remove file: ", entry.key.name)
            }
        }
        onImageLongPressed = false
        setFloatingButtonState(FloatingButtonState.ADDIMAGE)
    }

    /**
     * Gets an arraylist containing thumbnails to display
     */
    fun getThumbnails(context: Context): MutableMap<File, Boolean> {
        val arrayListToRet = ArrayList<File>()

        if (savedPoemImages.isEmpty()) {
            val savedImagesFolder =
                context.getDir(
                    context.getString(R.string.saved_images_folder_name),
                    Context.MODE_PRIVATE
                )
            val thumbnailsFolder =
                context.getDir(
                    context.getString(R.string.thumbnails_folder_name),
                    Context.MODE_PRIVATE
                )
            try {
                val savedImageFiles = savedImagesFolder.listFiles()?.toMutableList()
                if (savedImageFiles != null) {
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
            addAllFiles(arrayListToRet, null, false)
        }
        return savedPoemImages
    }
    /**
     * @param imagePath the path to the image that we want to copy
     *
     * This function copies any selected image or images to our own local folder
     */
    fun copyToLocalFolder(imagePath: String?, context: Context) {
        val localImageFolder = context.getDir(context.getString(R.string.my_images_folder_name), Context.MODE_PRIVATE)
        try {
            var newFileName = ""
            if (imagePath != null) {
                val splitString = imagePath.split("/")
                newFileName = splitString[splitString.size - 1]
            }
            if (newFileName != "") {
                val imageToAdd = File(localImageFolder?.path + File.separator + newFileName)

                if (!imageToAdd.exists()) {
                    if (imageToAdd.createNewFile()) {
                        val imageFile = imagePath?.let { File(it) }
                        val inputStream = FileInputStream(imagePath)
                        val outputStream = FileOutputStream(imageToAdd)

                        if (imageFile != null) {
                            outputStream.channel.transferFrom(
                                inputStream.channel,
                                0,
                                imageFile.totalSpace
                            )
                        }
                        outputStream.close()
                        inputStream.close()
                        imageFiles[imageToAdd] = false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}