package com.wendorochena.poetskingdom.utils.images.loaders.contracts

import android.content.Context
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.parallelism.images.executors.contracts.ImageRequestsCacheExecutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File

interface ImageLoaderResourceManager {

    val imageFolderType : ImageFolderType
    /**
     * Clear memory and disk caches
     * @param context the application context
     */
    fun invalidateCache(context: Context)

    /**
     * Removes a specific cached item from both memory and disk cache. Normally this is invoked when
     * an image is deleted
     */
   fun removeFromCache(cacheKey : String, context: Context)

    /**
     * Insert image into cache
     * @param context application context
     * @param imageRequest request to cache
     * @param imageRequestsCacheExecutor the cache executor to load images into cache
     */
   suspend fun insertIntoCache(context: Context, filesToCache: Array<File>, imageRequestsCacheExecutor: ImageRequestsCacheExecutor) : List<ImageRequest>

    /**
     * Reads all files from cache and transform them to a Coil ImageRequest. In the event
     * of a cache miss then images are loaded from disk and made cacheable
     * @param context application context
     * @param ioDispatcher the dispatcher to run the operations on. The default is the IO dispatcher
     * @param imagesDirectoryFolderSize the directory containing all images sorted in ascending order
     *
     * @return a list of image requests else null if there are none
     */
    suspend fun loadAllImages(
        context: Context,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        imagesDirectoryFolderSize: Int
    ): List<ImageRequest>?
}