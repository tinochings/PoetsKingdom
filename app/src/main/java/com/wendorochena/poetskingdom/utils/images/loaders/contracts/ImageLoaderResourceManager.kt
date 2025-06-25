package com.wendorochena.poetskingdom.utils.images.loaders.contracts

import android.content.Context
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.utils.files.cache.FileCacheUtil
import com.wendorochena.poetskingdom.utils.files.cache.FileCacheUtility
import com.wendorochena.poetskingdom.utils.files.images.ImageSortType
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.generators.contracts.NonRandomKeyGenContract
import com.wendorochena.poetskingdom.utils.parallelism.images.executors.contracts.ImageRequestsCacheExecutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File

/**
 * Responsible for managing the loading and invalidating of cache resources
 */
abstract class ImageLoaderResourceManager(val imageFolderType: ImageFolderType) {
    protected lateinit var sortedCacheFiles: List<File>

    /**
     * Clear memory and disk caches
     * @param context the application context
     */
    fun invalidateCache(context: Context, cacheKeyGen: NonRandomKeyGenContract<Any>) {
        context.imageLoader.diskCache?.clear()
        context.imageLoader.memoryCache?.clear()
        cacheKeyGen.invalidateCache(context)
        val fileCacheUtility: FileCacheUtil = FileCacheUtility()
        when (imageFolderType) {
            ImageFolderType.IMAGES -> {
                fileCacheUtility.invalidateCacheFolder(
                    context = context,
                    cacheName = imageThumbnailsCacheName
                )
            }

            ImageFolderType.POEM_THUMBNAILS -> {
                fileCacheUtility.invalidateCacheFolder(
                    context = context,
                    cacheName = myPoemsThumbnailsCacheName
                )
            }
        }
    }

    /**
     * Removes a specific cached item from both memory and disk cache. Normally this is invoked when
     * an image is deleted
     */
    fun removeFromCache(cacheKey: String, context: Context, cacheFilePath: String) {
        context.imageLoader.memoryCache?.remove(MemoryCache.Key(key = cacheKey))
        context.imageLoader.diskCache?.remove(cacheKey)
        val fileCacheUtility: FileCacheUtil = FileCacheUtility()
        when (imageFolderType) {
            ImageFolderType.IMAGES -> {
                fileCacheUtility.deleteEntriesFromCacheFolder(
                    context = context,
                    cacheName = imageThumbnailsCacheName,
                    filesToDelete = arrayOf(cacheFilePath)
                )
            }

            ImageFolderType.POEM_THUMBNAILS -> {
                fileCacheUtility.deleteEntriesFromCacheFolder(
                    context = context,
                    cacheName = myPoemsThumbnailsCacheName,
                    filesToDelete = arrayOf(cacheFilePath)
                )
            }
        }
    }

    protected fun cacheFolderFromImageType(context: Context) : File? {
        return when (imageFolderType) {
            ImageFolderType.IMAGES -> {
                FileCacheUtility().retrieveCacheDirectory(context, imageThumbnailsCacheName)
            }

            ImageFolderType.POEM_THUMBNAILS -> {
                FileCacheUtility().retrieveCacheDirectory(context, myPoemsThumbnailsCacheName)
            }
        }
    }
    /**
     * Insert image into cache
     * @param context application context
     * @param imageRequest request to cache
     * @param imageRequestsCacheExecutor the cache executor to load images into cache
     */
    suspend fun insertIntoCache(
        context: Context,
        filesToCache: Array<File>,
        imageRequestsCacheExecutor: ImageRequestsCacheExecutor
    ): List<ImageRequest> {
        return imageRequestsCacheExecutor.executePreloadedFiles(
            filesToCache = filesToCache,
            imageFolderType = imageFolderType
        )
    }

    private fun sortedCacheDirectory(imageFolderSortType: ImageSortType, folder : File?) : List<File>{
        return when(imageFolderSortType){
            ImageSortType.DESCENDING -> {
                folder?.listFiles()?.sortedByDescending { it.lastModified() } ?: ArrayList()
            }
            ImageSortType.ASCENDING -> {
                folder?.listFiles()?.sortedBy { it.lastModified() } ?: ArrayList()
            }
            ImageSortType.NONE -> {
                folder?.listFiles()?.toList() ?: ArrayList()
            }
        }
    }

    protected fun retrieveCacheDirectoryFiles(context: Context, imageFolderSortType: ImageSortType = ImageSortType.ASCENDING): List<File> {
        if (!::sortedCacheFiles.isInitialized) {
            sortedCacheFiles = when (imageFolderType) {
                ImageFolderType.IMAGES -> sortedCacheDirectory(folder = FileCacheUtility().retrieveCacheDirectory(
                    context = context,
                    imageThumbnailsCacheName
                ), imageFolderSortType = imageFolderSortType)

                ImageFolderType.POEM_THUMBNAILS -> sortedCacheDirectory(imageFolderSortType = imageFolderSortType, folder = FileCacheUtility().retrieveCacheDirectory(
                    context = context,
                    myPoemsThumbnailsCacheName
                ))
            }
        }

        return sortedCacheFiles
    }

    /**
     * Reads all files from cache and transform them to a Coil ImageRequest. In the event
     * of a cache miss then images are loaded from disk and made cacheable
     * @param context application context
     * @param ioDispatcher the dispatcher to run the operations on. The default is the IO dispatcher
     * @param imagesDirectoryFolderSize the directory containing all images sorted in ascending order
     *
     * @return a list of image requests else null if there are none
     */
    abstract suspend fun loadAllImages(
        context: Context,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): List<ImageRequest>

    companion object {
        private val imageThumbnailsCacheName: String = "compressed_image_thumbnails"
        private val myPoemsThumbnailsCacheName = "compressed_my_image_thumbnails"

        fun getImageThumbnailsCacheName() : String {
            return imageThumbnailsCacheName
        }
        fun  getMyPoemsThumbnailsCacheName() : String {
            return myPoemsThumbnailsCacheName
        }

        fun getCacheNameFromImageFolderType(imageFolderType: ImageFolderType) : String{
           return when(imageFolderType) {
                ImageFolderType.IMAGES -> return imageThumbnailsCacheName
                ImageFolderType.POEM_THUMBNAILS -> myPoemsThumbnailsCacheName
            }
        }
    }
}