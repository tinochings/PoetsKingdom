package com.wendorochena.poetskingdom.utils.parallelism.images.executors

import android.content.Context
import coil3.imageLoader
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.utils.files.images.ImageSortType
import com.wendorochena.poetskingdom.utils.files.images.ImagesFolderOperations
import com.wendorochena.poetskingdom.utils.generators.FileNameImageCacheKeyGen
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.images.loaders.contracts.ImageLoaderResourceManager
import com.wendorochena.poetskingdom.utils.parallelism.images.executors.contracts.ImageRequestsCacheExecutor
import com.wendorochena.poetskingdom.utils.parallelism.images.task.ImageCachingTask
import com.wendorochena.poetskingdom.utils.parallelism.images.task.TaskSupervisor
import com.wendorochena.poetskingdom.utils.parallelism.images.tasksImplementation.DivideAndConquerTaskImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

class ImagesCacheExecutorFileName(
    override val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    override val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    override val context: Context,
    override val taskSupervisor: TaskSupervisor
) : ImageRequestsCacheExecutor {

    private fun divideWork(
        imagesFiles: Array<File>,
        cacheFolderName: String,
        imageFolderType: ImageFolderType
    ): Array<ImageCachingTask<ImageRequest>> {
        val imagesSize = imagesFiles.size
        val midPoint = imagesSize / 2
        val firstListToCache = imagesFiles.slice(0..midPoint)
        val secondListToCache = imagesFiles.slice(midPoint + 1 until imagesSize)

        val firstTask = DivideAndConquerTaskImpl(
            dataToCache = firstListToCache,
            context = context,
            imageCacheKeyGen = FileNameImageCacheKeyGen(
                imageFolderType = imageFolderType,
                files = firstListToCache
            ),
            ioDispatcher = ioDispatcher,
            imageThumbnailsCacheName = cacheFolderName,
            taskSupervisor = taskSupervisor
        )
        val secondTask = DivideAndConquerTaskImpl(
            dataToCache = secondListToCache,
            context = context,
            imageCacheKeyGen = FileNameImageCacheKeyGen(
                imageFolderType = imageFolderType,
                files = secondListToCache
            ),
            ioDispatcher = ioDispatcher,
            imageThumbnailsCacheName = cacheFolderName,
            taskSupervisor = taskSupervisor
        )

        return arrayOf(firstTask, secondTask)
    }

    override suspend fun execute() {
        val imagesFolderOperations = ImagesFolderOperations(ImageSortType.NONE)
        val imagesFiles = imagesFolderOperations.retrieveImagesFolderFiles(context)
        val poemThumbnails = imagesFolderOperations.retrievePoemThumbnailFolderFiles(context)

        if (imagesFiles.isNotEmpty()) {
            executeInParallel(
                imagesFiles = imagesFiles,
                cacheFolderName = ImageLoaderResourceManager.getImageThumbnailsCacheName(),
                imageFolderType = ImageFolderType.IMAGES
            )
        }

        if (poemThumbnails.isNotEmpty()){
            executeInParallel(
                imagesFiles = poemThumbnails,
                cacheFolderName = ImageLoaderResourceManager.getMyPoemsThumbnailsCacheName(),
                imageFolderType = ImageFolderType.POEM_THUMBNAILS
            )
        }

    }

    override suspend fun executePreloadedFiles(
        filesToCache: Array<File>,
        imageFolderType: ImageFolderType,
    ): List<ImageRequest> {
        val minimisedImageRequest = ArrayList<ImageRequest>()
        if (filesToCache.isEmpty())
            return minimisedImageRequest

        executeInParallel(
            imagesFiles = filesToCache.toList(),
            cacheFolderName = ImageLoaderResourceManager.getCacheNameFromImageFolderType(
                imageFolderType
            ),
            tasks = minimisedImageRequest,
            imageFolderType = imageFolderType
        )
        return minimisedImageRequest
    }

    private suspend fun executeInParallel(
        imagesFiles: List<File>,
        cacheFolderName: String,
        imageFolderType: ImageFolderType,
        tasks: ArrayList<ImageRequest>? = null,
    ) {
        coroutineScope {
            launch(defaultDispatcher){
                val dividedWork = divideWork(
                    imagesFiles = imagesFiles.toTypedArray(),
                    cacheFolderName = cacheFolderName,
                    imageFolderType = imageFolderType
                )
                val resultsList: ArrayList<Deferred<List<ImageRequest>?>> = ArrayList()
                dividedWork.forEach {
                    resultsList.add(async(defaultDispatcher) {
                        it.execute()
                    })
                }

                val parallelFinishedTasks = resultsList.awaitAll()

                parallelFinishedTasks.forEach {
                    it?.forEach { s ->
                        context.imageLoader.enqueue(s)
                    }
                    if (it != null && tasks != null) {
                        tasks.addAll(it.toTypedArray())
                    }
                }
            }
        }
    }
}