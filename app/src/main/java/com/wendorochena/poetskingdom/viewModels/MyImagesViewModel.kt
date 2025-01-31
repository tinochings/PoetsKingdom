package com.wendorochena.poetskingdom.viewModels

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.wendorochena.poetskingdom.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

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
    private lateinit var myPoemsViewModel: MyPoemsViewModel

    /**
     * @param selection the selection to set the current value to
     */
    fun setSelection(selection: CurrentSelection) {
        currentSelection = selection
    }

    /**
     * @param state the state to set the floating button
     */
    fun setFloatingButtonState(state: FloatingButtonState) {
        floatingButtonStateVar = state
    }

    /**
     * Adds all files and sets the long press boolean value to false
     * @param arrayList an arraylist containing files in a map
     * @param listFiles an array containing files in a map
     * @param isImages true when the current selection is images
     */
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

    /**
     * Gets all image files to display
     * @param context The context of application or activity
     * @return A map with the file image as key and the long press boolean as the value
     */
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
        try {
            for (entry in filesToDelete) {
                if (entry.key.delete())
                    imageFiles.remove(entry.key)
            }
            onImageLongPressed = false
            setFloatingButtonState(FloatingButtonState.ADDIMAGE)
        } catch (e: IOException) {
        }
    }

    /**
     * Checks to see if there is a saved poem with the corresponding poem name
     *
     * @param savedPoemsPath the file with the path to consider
     * @param poemName the name of the poem
     */
    private fun hasPoemPath(savedPoemsPath: File, poemName: String): Boolean {
        if (File(savedPoemsPath.absolutePath + File.separator + poemName).exists())
            return true
        val albums = savedPoemsPath.listFiles()

        if (albums != null) {
            for (album in albums) {
                if (File(album.absolutePath + File.separator + poemName).exists())
                    return true
            }
        }
        return false
    }

    /**
     * Deletes saved poem images
     *
     * @param context activity context
     */
    fun deleteSavedPoems(context: Context) {
        val filesToDelete = savedPoemImages.filter { it.value }
        val savedPoemsPath = context.getDir(
            context.getString(R.string.poems_folder_name),
            Context.MODE_PRIVATE
        )
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
                val hasSavedPoem = hasPoemPath(savedPoemsPath, file.name.replace(".png", ".xml"))
                if (fullPathToDelete.exists())
                    if (fullPathToDelete.deleteRecursively()) {
                        savedPoemImages.remove(file)
                        if (!hasSavedPoem)
                            file.delete()
                    }
            } catch (e: Exception) {
            }
        }
        onImageLongPressed = false
        setFloatingButtonState(FloatingButtonState.ADDIMAGE)
    }

    /**
     * Gets an arraylist containing thumbnail images to display
     * @param context context of the activity
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
                            thumbnailsFolder.absolutePath + File.separator + file.name + ".png"
                        )
                        if (thumbnailFile.exists()) {
                            arrayListToRet.add(File(thumbnailFile.absolutePath))
                        }
                    }
                }
            } catch (exception: IOException) {
            }
            addAllFiles(arrayListToRet, null, false)
        }
        return savedPoemImages
    }

    /**
     * @param imagePath the path to the image that we want to copy
     * @param context Context
     *
     * This function copies any selected image or images to our own local folder
     */
    fun copyToLocalFolder(imagePath: String?, context: Context) {
        val localImageFolder =
            context.getDir(context.getString(R.string.my_images_folder_name), Context.MODE_PRIVATE)
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
        } catch (e: IOException) {
        }
    }

    /**
     * Sets the on click value of each file to false if it was selected
     */
    fun resetSelectedImages() {
        if (currentSelection == CurrentSelection.IMAGES) {
            val keys = imageFiles.filter { it.value }
            for (selectedKeys in keys) {
                imageFiles[selectedKeys.key] = false
            }
        } else {
            val keys = savedPoemImages.filter { it.value }
            for (selectedKeys in keys) {
                savedPoemImages[selectedKeys.key] = false
            }
        }
    }

    /**
     * Initiates a share intent
     */
    fun initiateShareIntent(context: Context) {
        if (!this::myPoemsViewModel.isInitialized)
            myPoemsViewModel = MyPoemsViewModel()

        val fileToShare = savedPoemImages.filter { it.value }

        if (fileToShare.size > 1)
            Toast.makeText(context, R.string.share_as_image_toast, Toast.LENGTH_LONG)
                .show()

        val imageUris = myPoemsViewModel.shareIntent(context, fileToShare.keys.first())

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
            type = "image/*"
        }
        onImageLongPressed = false
        setFloatingButtonState(FloatingButtonState.ADDIMAGE)
        resetSelectedImages()
        ContextCompat.startActivity(context,shareIntent,null)
    }
}