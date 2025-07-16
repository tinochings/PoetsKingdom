package com.wendorochena.poetskingdom.utils.parallelism.images.executors

import android.content.Context
import coil3.imageLoader
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.utils.generators.ImageCacheKeyGen
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.parallelism.images.executors.contracts.ImageRequestsCacheExecutor
import com.wendorochena.poetskingdom.utils.parallelism.images.task.ImageCachingTask
import com.wendorochena.poetskingdom.utils.parallelism.images.task.TaskSupervisor
import com.wendorochena.poetskingdom.utils.parallelism.images.tasksImplementation.DivideAndConquerTaskImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.File

class ImagesCacheExecutor(
    override val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    override val context: Context,
    override val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    override val taskSupervisor: TaskSupervisor
) : ImageRequestsCacheExecutor {
    private val imageThumbnailsCacheName = "compressed_image_thumbnails"
    private val myPoemsThumbnailsCacheName = "compressed_my_image_thumbnails"

    /**
     * Sort the files in the directory {@code imagesFolder} in ascending order and then divide work
     * into two ImageCachingTask<ImageRequest> tasks. Each task uses a pre-determined range of
     * integer keys provided by by the parameter keyRange
     *
     * @param keyRange the start index and end index of the keys to be used
     * @param imagesSize the number of images in the images folder
     * @param imagesFolder directory containing image files
     * @param cacheFolderName name of the cache to store images in
     * @return an array of 2 tasks
     */
    private fun divideWork(
        keyRange: Pair<Int, Int>,
        imagesFiles: Array<File>,
        cacheFolderName: String,
        imageFolderType: ImageFolderType
    ): Array<ImageCachingTask<ImageRequest>> {
        val imagesSize = imagesFiles.size
        val midPoint = (keyRange.first + keyRange.second) / 2
        val firstRange: Pair<Int, Int> = Pair(keyRange.first, midPoint)
        val secondRange: Pair<Int, Int> = Pair(midPoint + 1, (keyRange.second))
        val imageCacheKeyGen1 = ImageCacheKeyGen(firstRange, imageFolderType = imageFolderType)
        val imageCacheKeyGen2 = ImageCacheKeyGen(secondRange, imageFolderType = imageFolderType)
        val firstTaskRange = IntRange(0, (imagesSize / 2) - 1)
        val secondTaskRange = IntRange((imagesSize / 2), imagesSize - 1)
        val sortedList = imagesFiles.sortedBy { it.lastModified() }

        val firstTask = DivideAndConquerTaskImpl(
            dataToCache = sortedList.slice(firstTaskRange),
            context = context,
            imageCacheKeyGen = imageCacheKeyGen1,
            ioDispatcher = ioDispatcher,
            imageThumbnailsCacheName = cacheFolderName,
            taskSupervisor = taskSupervisor
        )
        val secondTask = DivideAndConquerTaskImpl(
            dataToCache = sortedList.slice(secondTaskRange),
            context = context,
            imageCacheKeyGen = imageCacheKeyGen2,
            ioDispatcher = ioDispatcher,
            imageThumbnailsCacheName = cacheFolderName,
            taskSupervisor = taskSupervisor
        )
        return arrayOf(firstTask, secondTask)
    }


    /**
     *
     */
    override suspend fun execute() {
        val imagesFolder = context.getDir(
            context.getString(R.string.my_images_folder_name),
            Context.MODE_PRIVATE
        )
        val myPoemsThumbnailsFolder = context.getDir(
            context.getString(R.string.thumbnails_folder_name),
            Context.MODE_PRIVATE
        )
        val imagesSize = imagesFolder?.listFiles()?.size
        val myPoemsSize = myPoemsThumbnailsFolder?.listFiles()?.size

        // pre-allocate keys before threading
        if (imagesSize != null && imagesSize > 0) {
            val keyRange = ImageCacheKeyGen(imageFolderType = ImageFolderType.IMAGES).generateKeys(context, imagesSize)
            if (keyRange != null) {
                executeInParallel(
                    keyRange = keyRange,
                    imagesFiles = imagesFolder.listFiles()!!,
                    cacheFolderName = imageThumbnailsCacheName,
                    imageFolderType = ImageFolderType.IMAGES
                )
            }
        }
        if (myPoemsSize != null && myPoemsSize > 0) {
            val keyRange = ImageCacheKeyGen(imageFolderType = ImageFolderType.POEM_THUMBNAILS).generateKeys(context, myPoemsSize)
            if (keyRange != null) {
                executeInParallel(
                    keyRange = keyRange,
                    imagesFiles = myPoemsThumbnailsFolder.listFiles()!!,
                    cacheFolderName = myPoemsThumbnailsCacheName,
                    imageFolderType = ImageFolderType.POEM_THUMBNAILS
                )
            }
        }
    }

    /**
     *
     */
    override suspend fun executePreloadedFiles(filesToCache : Array<File>, imageFolderType: ImageFolderType): List<ImageRequest> {
        val keyRange = ImageCacheKeyGen(imageFolderType = imageFolderType).generateKeys(context, filesToCache.size)
        val minimisedImageRequest = ArrayList<ImageRequest>()
        if (keyRange != null) {
            executeInParallel(
                keyRange = keyRange,
                imagesFiles = filesToCache,
                cacheFolderName = imageThumbnailsCacheName,
                tasks = minimisedImageRequest,
                imageFolderType = imageFolderType
            )
            return minimisedImageRequest
        }
        return ArrayList()
    }
    private suspend fun executeInParallel(
        keyRange: Pair<Int, Int>,
        imagesFiles: Array<File>,
        cacheFolderName: String,
        tasks : ArrayList<ImageRequest>? = null,
        imageFolderType: ImageFolderType
    ) {
        val dividedWork =
            divideWork(
                keyRange = keyRange,
                imagesFiles = imagesFiles,
                cacheFolderName = cacheFolderName,
                imageFolderType = imageFolderType
            )
        coroutineScope {
            val resultsList: ArrayList<Deferred<List<ImageRequest>?>> = ArrayList()
            dividedWork.forEach {
                resultsList.add(async(defaultDispatcher) {
                    it.execute()
                })
            }

            resultsList.forEach {
               val res = it.await()
                res?.forEach { s ->
                    context.imageLoader.enqueue(s)
                }
                if (res != null && tasks != null) {
                    tasks.addAll(res.toTypedArray())
                }
            }
        }
    }
}