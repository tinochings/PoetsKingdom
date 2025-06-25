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
import com.wendorochena.poetskingdom.utils.generators.ImageCacheKeyGen
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.images.loaders.contracts.ImageLoaderResourceManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads an image using a LONG ImageCacheKeyGenerator
 */
class ImageLoaderUtility(val ioDispatcher: CoroutineDispatcher, imageFolderType: ImageFolderType
) : ImageLoaderResourceManager(imageFolderType) {

    private val imageCacheKeyGen: ImageCacheKeyGen = ImageCacheKeyGen(imageFolderType = imageFolderType)
    private val cacheMissTag = "IMAGE_CACHE_MISS"
    private val missingIndexTag = "IMAGE_INDEX_INVALID"

    /**
     * Finds the full cache file path to delete given the current index
     */
    fun resolveDiskNameFromIndex(context: Context, index : Int) : String{
        val cacheDirectoryFiles = retrieveCacheDirectoryFiles(context,)
        val ithIndex = cacheDirectoryFiles.size - 1 - index
        if (cacheDirectoryFiles.size > ithIndex ) {
            return cacheDirectoryFiles[ithIndex].absolutePath
        }

        return ""
    }

    override suspend fun loadAllImages(
        context: Context,
        ioDispatcher: CoroutineDispatcher,
    ): List<ImageRequest> {
        val retrievedCachedImages = ArrayList<ImageRequest>()
        return withContext(ioDispatcher) {
            try {
                val imagesDirectoryFolderSize = retrieveCacheDirectoryFiles(context,).size
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
        val cacheDirectoryFiles = retrieveCacheDirectoryFiles(context,)

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