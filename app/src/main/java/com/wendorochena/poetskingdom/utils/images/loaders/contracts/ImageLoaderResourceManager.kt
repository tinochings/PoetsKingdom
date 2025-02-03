package com.wendorochena.poetskingdom.utils.images.loaders.contracts

import android.content.Context
import coil3.request.ImageRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File

interface ImageLoaderResourceManager {

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
     * Reads all files from the images directory and transform them to a cacheable Coil ImageRequest
     * @param context application context
     * @param ioDispatcher the dispatcher to run the operations on. The default is the IO dispatcher
     * @param imagesDirectory the directory containing all images
     *
     * @return a list of image requests else null if there are none
     */
    suspend fun loadAllImages(
        context: Context,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        imagesDirectory: File
    ): List<ImageRequest>?
}