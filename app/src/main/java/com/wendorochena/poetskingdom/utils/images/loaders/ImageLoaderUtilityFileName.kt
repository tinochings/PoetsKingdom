package com.wendorochena.poetskingdom.utils.images.loaders

import android.content.Context
import android.graphics.Bitmap
import coil3.BitmapImage
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.utils.files.images.ImageSortType
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.images.loaders.ImageLoaderUtility.Companion.buildImageRequest
import com.wendorochena.poetskingdom.utils.images.loaders.contracts.ImageLoaderResourceManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Image loader utility instance that uses the file name without extension as a cache key
 */
class ImageLoaderUtilityFileName(imageFolderType: ImageFolderType) : ImageLoaderResourceManager(imageFolderType) {

    override suspend fun loadAllImages(
        context: Context,
        ioDispatcher: CoroutineDispatcher
    ): List<ImageRequest> {
        val imageRequests = ArrayList<ImageRequest>()
        return withContext(ioDispatcher) {
            val imagesDirectoryFolder = retrieveCacheDirectoryFiles(context, imageFolderSortType = ImageSortType.DESCENDING)
            for ((index,file) in imagesDirectoryFolder.withIndex()){
                val cacheKey = file.name
                val cachedValue = context.imageLoader.memoryCache?.get(MemoryCache.Key(cacheKey))
                if (cachedValue != null){
                    val cachedImage = cachedValue.image as BitmapImage
                    val imageBitmap = cachedImage.bitmap
                    val byteStream = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
                    val byteArray = byteStream.toByteArray()

                    val imageRequest = buildImageRequest(context = context, data = byteArray, key = cacheKey, ioDispatcher = ioDispatcher)
                    imageRequests.add(imageRequest)
                } else {
                    imageRequests.add(buildImageRequestFromDisk(context = context, fileName = cacheKey, filePath = file.absolutePath))
                }
            }
            return@withContext imageRequests
        }
    }

    private fun buildImageRequestFromDisk(context: Context, fileName : String, filePath : String) : ImageRequest{
        return buildImageRequest(context = context, data = filePath, key = fileName)
    }

    /**
     * Finds the full cache file path to delete given the cache key
     * @return the absolute path of the cached item else an empty string
     */
    fun resolveDiskNameFromCacheKey(context: Context, cacheKey : String) : String{
        return cacheFolderFromImageType(context)?.let {
            return File(it, cacheKey).absolutePath
        } ?: ""

    }
}