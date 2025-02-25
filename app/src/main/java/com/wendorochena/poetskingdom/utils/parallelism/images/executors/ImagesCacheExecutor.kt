package com.wendorochena.poetskingdom.utils.parallelism.images.executors

import android.content.Context
import coil3.imageLoader
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.utils.generators.ImageCacheKeyGen
import com.wendorochena.poetskingdom.utils.parallelism.images.executors.contracts.ImageRequestsCacheExecutor
import com.wendorochena.poetskingdom.utils.parallelism.images.task.ImageCachingTask
import com.wendorochena.poetskingdom.utils.parallelism.images.tasksImplementation.DivideAndConquerTaskImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.File

class ImagesCacheExecutor(
    override val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    override val context: Context
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
        imagesFolder: File,
        cacheFolderName: String
    ): Array<ImageCachingTask<ImageRequest>> {
        val imagesSize = imagesFolder.listFiles()!!.size
        val midPoint = (keyRange.first + keyRange.second) / 2
        val firstRange: Pair<Int, Int> = Pair(keyRange.first, midPoint)
        val secondRange: Pair<Int, Int> = Pair(midPoint + 1, (keyRange.second))
        val imageCacheKeyGen1 = ImageCacheKeyGen(firstRange)
        val imageCacheKeyGen2 = ImageCacheKeyGen(secondRange)
        val firstTaskRange = IntRange(0, (imagesSize / 2) - 1)
        val secondTaskRange = IntRange((imagesSize / 2), imagesSize - 1)
        val sortedList = imagesFolder.listFiles()!!.sortedBy { it.lastModified() }

        val firstTask = DivideAndConquerTaskImpl(
            dataToCache = sortedList.slice(firstTaskRange),
            context = context,
            imageCacheKeyGen = imageCacheKeyGen1,
            ioDispatcher = defaultDispatcher,
            imageThumbnailsCacheName = cacheFolderName
        )
        val secondTask = DivideAndConquerTaskImpl(
            dataToCache = sortedList.slice(secondTaskRange),
            context = context,
            imageCacheKeyGen = imageCacheKeyGen2,
            ioDispatcher = defaultDispatcher,
            imageThumbnailsCacheName = cacheFolderName
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
            val keyRange = ImageCacheKeyGen().generateKeys(context, imagesSize)
            if (keyRange != null) {
                executeInParallel(
                    keyRange = keyRange,
                    imagesFolder = imagesFolder,
                    cacheFolderName = imageThumbnailsCacheName
                )
            }
        }
        if (myPoemsSize != null && myPoemsSize > 0) {
            val keyRange = ImageCacheKeyGen().generateKeys(context, myPoemsSize)
            if (keyRange != null) {
                executeInParallel(
                    keyRange = keyRange,
                    imagesFolder = myPoemsThumbnailsFolder,
                    cacheFolderName = myPoemsThumbnailsCacheName
                )
            }
        }
    }

    private suspend fun executeInParallel(
        keyRange: Pair<Int, Int>,
        imagesFolder: File,
        cacheFolderName: String
    ) {
        val dividedWork =
            divideWork(
                keyRange = keyRange,
                imagesFolder = imagesFolder,
                cacheFolderName = cacheFolderName
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
            }
        }
    }
}