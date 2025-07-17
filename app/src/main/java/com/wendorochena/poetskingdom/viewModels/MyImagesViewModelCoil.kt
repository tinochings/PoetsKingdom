package com.wendorochena.poetskingdom.viewModels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.ImageViewer
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.screens.reusables.loaders.ImagesNotificationModel
import com.wendorochena.poetskingdom.utils.UriUtils
import com.wendorochena.poetskingdom.utils.files.images.ImageSortType
import com.wendorochena.poetskingdom.utils.files.images.ImagesFolderOperations
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.images.loaders.ImageLoaderUtilityFileName
import com.wendorochena.poetskingdom.utils.parallelism.images.executors.ImagesCacheExecutorFileName
import com.wendorochena.poetskingdom.utils.parallelism.images.task.TaskSupervisorModel
import com.wendorochena.poetskingdom.utils.parallelism.images.tasksImplementation.TaskSupervisorImpl
import com.wendorochena.poetskingdom.viewModels.models.CoilImageItem
import com.wendorochena.poetskingdom.viewModels.models.MyImagesScreenModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.TreeSet
import java.util.concurrent.Executors
import kotlin.math.round

class MyImagesViewModelCoil(
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    // keeps track of selected indexes
    private var selectedImages: TreeSet<Int> = sortedSetOf()

    private val _model = MutableStateFlow(MyImagesScreenModel())
    val modelState = _model.asStateFlow()
    private val _imagesNotificationModel = MutableStateFlow(ImagesNotificationModel())
    val imagesNotificationModelState = _imagesNotificationModel.asStateFlow()

    private lateinit var myPoemsViewModel: MyPoemsViewModel


    /**
     * Resets notification to the default state
     */
    fun clearNotificationState() {
        updateImagesNotificationModelState {
            it.copy(
                notificationHeader = "",
                progress = mutableFloatStateOf(0f),
                totalImages = 0,
                totalImagesProcessed = mutableIntStateOf(0),
                shouldDisplayNotification = mutableStateOf(false),
                percentage = mutableIntStateOf(0)
            )
        }
    }

    /**
     * Determines whether to invoke notification display
     * @param boolean true to display the notification else false
     */
    fun setShouldDisplayNotification(boolean: Boolean) {
        updateImagesNotificationModelState {
            it.copy(
                shouldDisplayNotification = mutableStateOf(
                    boolean
                )
            )
        }
    }

    /**
     * Retrieves the poem thumbnails file path located in the main poem thumbnail folder
     * @param fileNameWithExtension the name of the file to retrieve the file path
     * @param context the applications context
     */
    fun retrieveThumbnailFilePath(fileNameWithExtension: String, context: Context): File {
        val thumbnailsFolder =
            ImagesFolderOperations(ImageSortType.NONE).retrievePoemThumbnailImagesFolder(context)
        val currentIndexFileName = fileNameWithExtension.split(".")[0]
        return File(thumbnailsFolder, currentIndexFileName.plus(".png"))
    }

    /**
     * Starts the ImageViewer Activity if no images have been long pressed. If there is an image
     * that has been long pressed, this function determines if [index] should be added or removed
     * from the list of images currently selected
     * @param index the index of the image clicked
     * @param context composable context
     */
    fun onImageItemClick(index: Int, context: Context) {
        if (!modelState.value.onImageLongPressed) {
            val imageIntent =
                Intent(context, ImageViewer::class.java)
            when (modelState.value.currentSelection) {
                CurrentSelection.IMAGES -> {
                    imageIntent.putExtra("imageLoadType", "image")
                    imageIntent.putExtra(
                        "imagePath",
                        fullResolutionImageFilePath(context, index)
                    )
                }

                CurrentSelection.POEMS -> {
                    val poemName =
                        modelState.value.poemThumbnails[index].imageState.first.memoryCacheKey!!.split(
                            "."
                        )[0].replace(
                            '_',
                            ' '
                        )
                    imageIntent.putExtra("imageLoadType", "poem saved image")
                    imageIntent.putExtra("poemName", poemName)
                }
            }
            context.startActivity(imageIntent)
        } else {
            //remove index if it is already in selected images
            var shouldAddOrRemoveSelectedItem = true

            when (modelState.value.currentSelection) {
                CurrentSelection.IMAGES -> {
                    val valueToUpdate = !modelState.value.imageThumbnails[index].imageState.second
                    shouldAddOrRemoveSelectedItem = valueToUpdate
                    modelState.value.imageThumbnails[index] =
                        modelState.value.imageThumbnails[index].copy(
                            imageState = modelState.value.imageThumbnails[index].imageState.copy(
                                second = valueToUpdate
                            )
                        )
                }

                CurrentSelection.POEMS -> {
                    val valueToUpdate = !modelState.value.poemThumbnails[index].imageState.second
                    shouldAddOrRemoveSelectedItem = valueToUpdate
                    modelState.value.poemThumbnails[index] =
                        modelState.value.poemThumbnails[index].copy(
                            imageState = modelState.value.poemThumbnails[index].imageState.copy(
                                second = valueToUpdate
                            )
                        )
                }
            }
            if (shouldAddOrRemoveSelectedItem)
                selectedImages.add(index)
            else
                selectedImages.remove(index)
        }
    }

    /**
     * Initiates the long click state if it is not already active
     * @param index the index of the item long pressed
     */
    fun onImageLongClick(index: Int) {
        if (!modelState.value.onImageLongPressed) {
            selectedImages.add(index)
            when (modelState.value.currentSelection) {
                CurrentSelection.IMAGES -> {
                    modelState.value.imageThumbnails[index] =
                        modelState.value.imageThumbnails[index].copy(
                            imageState = modelState.value.imageThumbnails[index].imageState.copy(
                                second = true
                            )
                        )
                }

                CurrentSelection.POEMS -> {
                    modelState.value.poemThumbnails[index] =
                        modelState.value.poemThumbnails[index].copy(
                            imageState = modelState.value.poemThumbnails[index].imageState.copy(
                                second = true
                            )
                        )
                }
            }

            setOnLongClick(true)
            setFloatingButtonState(FloatingButtonState.DELETEIMAGE)
        }
    }

    /**
     * @param state the state to set the floating button
     */
    fun setFloatingButtonState(state: FloatingButtonState) {
        updateModelState { it.copy(floatingButtonStateVar = state) }
    }

    /**
     * Adds all files and sets the long press boolean value to false
     * @param listFiles an array containing files in a map
     * @param isImages true when the current selection is images
     */
    private fun addAllFiles(
        listFiles: List<ImageRequest>,
        isImages: Boolean,
    ) {
        val objectToUpdate = if (isImages)
            modelState.value.imageThumbnails
        else
            modelState.value.poemThumbnails

        for (imageRequest in listFiles) {
            val cacheKey = imageRequest.memoryCacheKey!!
            val imageItem = CoilImageItem(key = cacheKey, Pair(imageRequest, false))
            objectToUpdate.add(imageItem)
        }
    }

    /**
     * Gets all image files to display
     * @param context The context of application or activity
     * @return A state list of CoilImageItems
     */
    fun getImageFilesAsImageRequest(context: Context): List<CoilImageItem> {

        if (modelState.value.imageThumbnails.isEmpty()) {
            val imageLoaderUtility = ImageLoaderUtilityFileName(ImageFolderType.IMAGES)

            viewModelScope.launch(mainDispatcher) {
                val imageRequests = imageLoaderUtility.loadAllImages(
                    context,
                    ioDispatcher,
                )
                addAllFiles(imageRequests, true)
            }
        }
        return modelState.value.imageThumbnails
    }

    /**
     * Sets on long click value to [boolean]
     */
    fun setOnLongClick(boolean: Boolean) {
        updateModelState { it.copy(onImageLongPressed = boolean) }
    }


    /**
     * Deletes image files from the cache folder and the main folder
     *
     */
    private suspend fun deleteImages(context: Context) {
        withContext(ioDispatcher) {
            try {
                var currentIndex = selectedImages.size
                val imagesFolderOperations = ImagesFolderOperations(ImageSortType.NONE)
                for ((index, indexToDelete) in selectedImages.descendingIterator().withIndex()) {
                    val cacheKey =
                        modelState.value.imageThumbnails[indexToDelete].imageState.first.memoryCacheKey
                            ?: ""
                    val imageLoaderUtility =
                        ImageLoaderUtilityFileName(imageFolderType = ImageFolderType.IMAGES)
                    val cacheFilePath =
                        imageLoaderUtility.resolveDiskNameFromCacheKey(context, cacheKey)
                    if (cacheFilePath.isNotEmpty()) {
                        imageLoaderUtility.removeFromCache(
                            cacheKey,
                            context = context,
                            cacheFilePath = cacheFilePath
                        )

                        val fileName = File(cacheFilePath).nameWithoutExtension
                        val fileToDelete = imagesFolderOperations.findImageFilePathFromFileName(
                            context = context,
                            fileNameWithoutExtension = fileName
                        )
                        fileToDelete?.delete()
                        currentIndex--
                        val updatedImagesProcessed = index + 1
                        val updatedProgress =
                            updatedImagesProcessed.toFloat() / selectedImages.size.toFloat()
                        val percentage =
                            round(updatedImagesProcessed.toFloat() / selectedImages.size * 100).toInt()
                        updateImagesNotificationModelState {
                            it.copy(
                                totalImagesProcessed = mutableIntStateOf(
                                    index + 1
                                ),
                                progress = mutableFloatStateOf(updatedProgress),
                                percentage = mutableIntStateOf(percentage)
                            )
                        }
                    }
                }
                updateModelState { it.copy(onImageLongPressed = false) }
                setFloatingButtonState(FloatingButtonState.ADDIMAGE)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Deletes all selected images
     * @param dismissNotificationAlert action to perform when deletion is done
     */
    fun deleteSelectedImages(context: Context, dismissNotificationAlert: () -> Unit) {
        updateImagesNotificationModelState {
            it.copy(
                notificationHeader = "Deleting images from Poets Kingdom... ",
                totalImages = selectedImages.size
            )
        }
        viewModelScope.launch(mainDispatcher) {
            deleteImages(context)
            selectedImages.descendingIterator().forEach {
                modelState.value.imageThumbnails.removeAt(it)
            }
            selectedImages.clear()
            dismissNotificationAlert.invoke()
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
    private suspend fun deleteSavedPoemThumbnails(context: Context) {
        withContext(ioDispatcher) {
            val savedPoemsPath = context.getDir(
                context.getString(R.string.poems_folder_name),
                Context.MODE_PRIVATE
            )
            for (indexToDelete in selectedImages.descendingIterator()) {
                try {
                    val cacheKey =
                        modelState.value.poemThumbnails[indexToDelete].imageState.first.memoryCacheKey
                            ?: ""

                    val poemName = cacheKey.split(".")[0]
                    val savedImagesFilePath = context.getDir(
                        context.getString(R.string.saved_images_folder_name),
                        Context.MODE_PRIVATE
                    )
                    val fullPathToDelete = File(
                        savedImagesFilePath.absolutePath + File.separator + poemName
                    )
                    val hasSavedPoem = hasPoemPath(savedPoemsPath, poemName.plus(".xml"))
                    if (fullPathToDelete.exists()) {
                        if (fullPathToDelete.deleteRecursively()) {
                            val imageLoaderUtility =
                                ImageLoaderUtilityFileName(ImageFolderType.POEM_THUMBNAILS)
                            val cacheFilePath =
                                imageLoaderUtility.resolveDiskNameFromCacheKey(context, cacheKey)
                            imageLoaderUtility.removeFromCache(
                                cacheKey,
                                context,
                                cacheFilePath
                            )
                            if (!hasSavedPoem) {
                                val thumbnailsFolder =
                                    ImagesFolderOperations(ImageSortType.NONE).retrievePoemThumbnailImagesFolder(
                                        context
                                    )
                                File(thumbnailsFolder, poemName.plus(".png")).delete()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Initiates the deletion of saved poems
     */
    fun deleteSavedPoems(context: Context) {
        viewModelScope.launch(mainDispatcher) {
            deleteSavedPoemThumbnails(context)
            selectedImages.descendingIterator().forEach {
                modelState.value.poemThumbnails.removeAt(it)
            }
            selectedImages.clear()
            updateModelState { it.copy(onImageLongPressed = false) }
            setFloatingButtonState(FloatingButtonState.ADDIMAGE)
        }
    }

    /**
     * Gets an arraylist containing thumbnail images to display
     * @param context context of the activity
     */
    fun getThumbnails(context: Context): List<CoilImageItem> {

        if (modelState.value.poemThumbnails.isEmpty()) {
            val savedImagesFolder =
                context.getDir(
                    context.getString(R.string.saved_images_folder_name),
                    Context.MODE_PRIVATE
                )

            viewModelScope.launch(mainDispatcher) {
                val imageLoaderUtility =
                    ImageLoaderUtilityFileName(ImageFolderType.POEM_THUMBNAILS)
                val thumbnailImages = imageLoaderUtility.loadAllImages(
                    context = context,
                    ioDispatcher = ioDispatcher
                )

                val allSavedImages = savedImagesFolder.listFiles()?.map { it.nameWithoutExtension }
                val filterImages = async(ioDispatcher) {
                    thumbnailImages.filter {
                        return@filter allSavedImages
                            ?.contains(it.memoryCacheKey!!.split(".")[0]) ?: false
                    }
                }


                val filteredImages = filterImages.await()
                addAllFiles(filteredImages, isImages = false)
            }
        }

        return modelState.value.poemThumbnails
    }

    /**
     * Transfers files to the local folder in a non blocking manner
     */
    private suspend fun nonBlockingTransfer(uriList: List<Uri>, context: Context): ArrayList<File> {
        return withContext(ioDispatcher) {
            val imagesToMinify = ArrayList<File>()
            for ((index, uri) in uriList.withIndex()) {
                val uriUtils =
                    UriUtils(context, uri = uri)
                val copiedFile = copyToLocalFolder(
                    uriUtils.getRealPathFromURI(),
                    context = context
                )
                copiedFile?.let { imagesToMinify.add(it) }
                val updatedImagesProcessed = index + 1
                val updatedProgress = updatedImagesProcessed.toFloat() / uriList.size.toFloat()
                val percentage =
                    round(updatedImagesProcessed.toFloat() / uriList.size * 100).toInt()
                updateImagesNotificationModelState {
                    it.copy(
                        totalImagesProcessed = mutableIntStateOf(
                            index + 1
                        ),
                        progress = mutableFloatStateOf(updatedProgress),
                        percentage = mutableIntStateOf(percentage)
                    )
                }
            }
            return@withContext imagesToMinify
        }
    }

    /**
     * Initiates the transfer of images and invokes [dismissNotificationAlert] when the transfer
     * is finished
     */
    fun transferImagesToLocalFolder(
        uriList: List<Uri>,
        context: Context,
        dismissNotificationAlert: () -> Unit
    ) {
        updateImagesNotificationModelState {
            it.copy(
                notificationHeader = "Copying new images to Poets Kingdom... ",
                totalImages = uriList.size
            )
        }
        viewModelScope.launch(mainDispatcher) {
            if (uriList.isNotEmpty()) {
                val imagesToMinify = nonBlockingTransfer(uriList, context)
                updateImagesNotificationModelState {
                    it.copy(
                        notificationHeader = "Creating image thumbnails.. ",
                        percentage = mutableIntStateOf(0),
                        progress = mutableFloatStateOf(0f),
                        totalImagesProcessed = mutableIntStateOf(0),
                        totalImages = uriList.size
                    )
                }
                val customDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
                val taskSupervisorModel = TaskSupervisorModel(totalImages = imagesToMinify.size)
                val taskSupervisor = TaskSupervisorImpl(
                    taskSupervisorModel = taskSupervisorModel,
                    coroutineScope = viewModelScope,
                    dispatcher = customDispatcher
                )
                val imageCacheExecutor =
                    ImagesCacheExecutorFileName(context = context, taskSupervisor = taskSupervisor, ioDispatcher = ioDispatcher)

                taskSupervisor.observeTaskSupervisorModel{ valueCollected ->
                    updateImagesNotificationModelState {
                        it.copy(
                            progress = mutableFloatStateOf(
                                valueCollected.progress
                            ),
                            totalImagesProcessed = mutableIntStateOf(valueCollected.currentImagesProcessed),
                            percentage = mutableIntStateOf(valueCollected.percentage)
                        )
                    }
                }

                val cachedImages = imageCacheExecutor.executePreloadedFiles(
                    filesToCache = imagesToMinify.toTypedArray(),
                    imageFolderType = ImageFolderType.IMAGES
                ).map {
                    val cacheKey = it.memoryCacheKey!!
                    return@map CoilImageItem(key = cacheKey, imageState = Pair(it, false))
                }

                modelState.value.imageThumbnails.addAll(0, cachedImages)
                dismissNotificationAlert.invoke()
            }
        }
    }


    /**
     * @param imagePath the path to the image that we want to copy
     * @param context Context
     *
     * This function copies any selected image or images to our own local folder
     */
    private fun copyToLocalFolder(imagePath: String?, context: Context): File? {
        val localImageFolder =
            ImagesFolderOperations(ImageSortType.NONE).retrieveImagesFolder(context)

        try {
            var newFileName = ""
            if (imagePath != null) {
                val splitString = imagePath.split("/")
                newFileName = splitString[splitString.size - 1]
            }
            if (newFileName != "") {
                val imageToAdd = File(localImageFolder.path + File.separator + newFileName)

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
                        return imageToAdd
                    }
                }
            }
        } catch (e: IOException) {
            return null
        }
        return null
    }

    /**
     * Sets the on click value of each item to false if it was selected
     */
    fun resetSelectedImages() {
        val objectToUpdate = if (modelState.value.currentSelection == CurrentSelection.IMAGES)
            modelState.value.imageThumbnails
        else
            modelState.value.poemThumbnails

        for (index in selectedImages) {
            val coilImageItem = objectToUpdate[index]
            objectToUpdate[index] =
                coilImageItem.copy(imageState = coilImageItem.imageState.copy(second = false))
        }
        selectedImages.clear()

    }

    /**
     * Initiates a share intent
     */
    fun initiateShareIntent(context: Context) {
        if (!this::myPoemsViewModel.isInitialized)
            myPoemsViewModel = MyPoemsViewModel()

        if (selectedImages.size > 1)
            Toast.makeText(context, R.string.share_as_image_toast, Toast.LENGTH_LONG)
                .show()
        val fileToShare = File(
            ImagesFolderOperations(ImageSortType.NONE).retrievePoemThumbnailImagesFolder(context),
            modelState.value.poemThumbnails[selectedImages.elementAt(0)].key
        )
        val imageUris =
            myPoemsViewModel.shareIntent(context, fileToShare)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
            type = "image/*"
        }
        updateModelState { it.copy(onImageLongPressed = false) }
        setFloatingButtonState(FloatingButtonState.ADDIMAGE)
        resetSelectedImages()
        ContextCompat.startActivity(context, shareIntent, null)
    }

    /**
     * Determines if it is the users first time accessing the ImageView activity
     */
    fun determineFirstUse(context: Context, key: String): Boolean {
        val sharedPreferences =
            context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean(key, false)) {
            sharedPreferences.edit { putBoolean(key, true) }
            return true
        }
        return false
    }

    /**
     * Finds the full local image folder file path
     */
    private fun fullResolutionImageFilePath(context: Context, index: Int): String {
        val resolvedFileName =
            ImagesFolderOperations(ImageSortType.NONE).findImageFilePathFromFileName(
                context,
                modelState.value.imageThumbnails[index].key.split(".")[0]
            )
        return resolvedFileName?.absolutePath ?: ""
    }

    fun updateCurrentSelection(currentSelection: CurrentSelection) {
        if (modelState.value.currentSelection != currentSelection)
            updateModelState { it.copy(currentSelection = currentSelection) }
    }

    /**
     * Updates the models mutable state flow
     * @param function the function containing parameters to update
     */
    private fun updateModelState(function: (MyImagesScreenModel) -> MyImagesScreenModel) {
        viewModelScope.launch(mainDispatcher) {
            _model.update(function)
        }
    }

    /**
     * Updates the images notification state
     * @param function the function containing parameters to update
     */
    private fun updateImagesNotificationModelState(function: (ImagesNotificationModel) -> ImagesNotificationModel) {
        viewModelScope.launch(mainDispatcher) {
            _imagesNotificationModel.update(function)
        }
    }
}