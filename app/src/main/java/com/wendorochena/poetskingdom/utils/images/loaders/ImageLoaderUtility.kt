package com.wendorochena.poetskingdom.utils.images.loaders

import android.content.Context
import android.util.Log
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.utils.files.cache.FileCacheUtil
import com.wendorochena.poetskingdom.utils.files.cache.FileCacheUtility
import com.wendorochena.poetskingdom.utils.generators.ImageCacheKeyGen
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.images.loaders.contracts.ImageLoaderResourceManager
import com.wendorochena.poetskingdom.utils.parallelism.images.executors.contracts.ImageRequestsCacheExecutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Loads an image using a LONG ImageCacheKeyGenerator
 */
class ImageLoaderUtility(val ioDispatcher: CoroutineDispatcher,
                         override val imageFolderType: ImageFolderType
) : ImageLoaderResourceManager {

    private val imageCacheKeyGen: ImageCacheKeyGen = ImageCacheKeyGen(imageFolderType = imageFolderType)
    private val cacheMissTag = "IMAGE_CACHE_MISS"
    private val missingIndexTag = "IMAGE_INDEX_INVALID"
    private val imageThumbnailsCacheName = "compressed_image_thumbnails"
    private val myPoemsThumbnailsCacheName = "compressed_my_image_thumbnails"
    private lateinit var sortedCacheFiles : List<File>

    /**
     * Clear memory and disk caches
     * @param context the application context
     */
    override fun invalidateCache(context: Context) {
        context.imageLoader.diskCache?.clear()
        context.imageLoader.memoryCache?.clear()
        imageCacheKeyGen.invalidateCache(context)
        val fileCacheUtility : FileCacheUtil = FileCacheUtility()
        when(imageFolderType) {
            ImageFolderType.IMAGES -> {
                fileCacheUtility.invalidateCacheFolder(context = context, cacheName = imageThumbnailsCacheName)
            }
            ImageFolderType.POEM_THUMBNAILS -> {
                fileCacheUtility.invalidateCacheFolder(context = context, cacheName =  myPoemsThumbnailsCacheName)
            }
        }
    }

    override fun removeFromCache(cacheKey: String, context: Context) {
        context.imageLoader.memoryCache?.remove(MemoryCache.Key(key = cacheKey))
        context.imageLoader.diskCache?.remove(cacheKey)
        val fileCacheUtility : FileCacheUtil = FileCacheUtility()
        when(imageFolderType) {
            ImageFolderType.IMAGES -> {
                fileCacheUtility.deleteEntriesFromCacheFolder(context = context, cacheName = imageThumbnailsCacheName, filesToDelete = arrayOf(cacheKey))
            }
            ImageFolderType.POEM_THUMBNAILS -> {
                fileCacheUtility.deleteEntriesFromCacheFolder(context = context, cacheName = myPoemsThumbnailsCacheName, filesToDelete = arrayOf(cacheKey))
            }
        }
    }

    /**
     * Insert image into cache
     */
    override suspend fun insertIntoCache(
        context: Context,
        filesToCache: Array<File>,
        imageRequestsCacheExecutor: ImageRequestsCacheExecutor
    ): List<ImageRequest> {
        return imageRequestsCacheExecutor.executePreloadedFiles(filesToCache = filesToCache, imageFolderType = imageFolderType)
    }

    override suspend fun loadAllImages(
        context: Context,
        ioDispatcher: CoroutineDispatcher,
        imagesDirectoryFolderSize: Int,
    ): List<ImageRequest> {
        val retrievedCachedImages = ArrayList<ImageRequest>()
        return withContext(ioDispatcher) {
            try {
                val lastKnownKey =  imageCacheKeyGen.retrieveLastKnownCachedKey(context)
                val endIndex = lastKnownKey - (imagesDirectoryFolderSize - 1)
                if (endIndex >= 0 && imagesDirectoryFolderSize > 0) {
                    for (i in lastKnownKey  downTo endIndex) {
                        val cachedValue =
                            context.imageLoader.memoryCache?.get(MemoryCache.Key("$i"))

                        if (cachedValue != null) {
                            //cache hit
                            val imageRequest = buildImageRequest(context = context, data = cachedValue.image, key = "$i", ioDispatcher = ioDispatcher)
                            retrievedCachedImages.add(imageRequest)
                        } else {
                            //cache miss
                            val ithIndex = i - lastKnownKey + imagesDirectoryFolderSize - 1
                            Log.i(
                                cacheMissTag,
                                "Failed to retrieve index $ithIndex for folder: $imageFolderType, retrieving from disk"
                            )
                            buildImageRequestFromDisk(
                                context = context,
                                imageFileNumber = ithIndex,
                                key = "$i",
                            )?.let {
                                retrievedCachedImages.add(
                                    it
                                )
                            }
                        }
                    }
                }
            } catch (_: SecurityException) {
            }
            return@withContext retrievedCachedImages
        }
    }

    /**
     * Lazily retrieves the correct cache directory files and sorts them in ascending order of last modified.
     *
     */
    private fun retrieveCacheDirectoryFiles(context: Context) : List<File>{
        if (!::sortedCacheFiles.isInitialized){
            sortedCacheFiles = when(imageFolderType) {
                ImageFolderType.IMAGES -> FileCacheUtility().retrieveCacheDirectory(context = context, imageThumbnailsCacheName)!!.listFiles()!!.sortedBy { it.lastModified() }
                ImageFolderType.POEM_THUMBNAILS -> FileCacheUtility().retrieveCacheDirectory(context = context, myPoemsThumbnailsCacheName)!!.listFiles()!!.sortedBy { it.lastModified() }
            }
        }

        return sortedCacheFiles
    }
    /**
     *
     * @param context
     * @param imageFileNumber
     * @param key
     */
    private fun buildImageRequestFromDisk(
        context: Context,
        imageFileNumber: Int,
        key: String,
    ): ImageRequest? {
        val cacheDirectoryFiles = retrieveCacheDirectoryFiles(context)

        if (cacheDirectoryFiles.size > imageFileNumber) {
            return buildImageRequest(context = context, data = cacheDirectoryFiles[imageFileNumber].absolutePath, key = key, ioDispatcher = ioDispatcher)
        }
        Log.w(
            missingIndexTag, "$imageFileNumber index exceeds the maximum amount of " +
                    "cached images for folder: $imageFolderType. There is a total of ${cacheDirectoryFiles.size} files"
        )
        return null
    }


    companion object  {

        fun buildImageRequest(
            context: Context,
            data: Any,
            key: String,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO
        ): ImageRequest {
            return ImageRequest.Builder(context).data(data)
                .coroutineContext(ioDispatcher)
                .fetcherCoroutineContext(ioDispatcher)
                .decoderCoroutineContext(ioDispatcher)
                .memoryCacheKey(key)
                .diskCacheKey(key)
                .diskCachePolicy(CachePolicy.ENABLED)
                .error(R.drawable.baseline_broken_image_24)
                .placeholder(R.drawable.baseline_placeholder)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
        }
    }
}