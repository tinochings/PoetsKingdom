package com.wendorochena.poetskingdom.utils.images.loaders

import android.content.Context
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.exceptions.images.ImageFileNotFoundException
import com.wendorochena.poetskingdom.utils.generators.ImageCacheKeyGen
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.images.loaders.contracts.ImageLoaderResourceManager
import com.wendorochena.poetskingdom.utils.images.models.ImageItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Loads an image using a LONG ImageCacheKeyGenerator
 */
class ImageLoaderUtility(val ioDispatcher: CoroutineDispatcher,
                         override val imageFolderType: ImageFolderType
) : ImageLoaderResourceManager {

    private val imageCacheKeyGen : ImageCacheKeyGen = ImageCacheKeyGen()

    /**
     * Clear memory and disk caches
     * @param context the application context
     */
    override fun invalidateCache(context: Context) {
        context.imageLoader.diskCache?.clear()
        context.imageLoader.memoryCache?.clear()
        imageCacheKeyGen.invalidateCache(context)
    }

    override fun removeFromCache(cacheKey: String, context: Context) {
        context.imageLoader.memoryCache?.remove(MemoryCache.Key(key = cacheKey))
        context.imageLoader.diskCache?.remove(cacheKey)
    }

    override suspend fun loadAllImages(
        context: Context,
        ioDispatcher: CoroutineDispatcher,
        imagesDirectory : File
    ) : List<ImageRequest>? {

        return withContext(ioDispatcher) {
            try {
                return@withContext imagesDirectory.listFiles()?.sortedByDescending { it.lastModified() }
                    ?.mapNotNull { file: File? ->
                        if (file != null)
                            return@mapNotNull ImageItem(imageCacheKeyGen.generateKey(context), file)
                        else
                            null
                    }?.map { imageItem: ImageItem ->
                        val imageRequest: ImageRequest =
                            ImageRequest.Builder(context).data(imageItem.imageFile.absolutePath)
                                .coroutineContext(ioDispatcher)
                                .fetcherCoroutineContext(ioDispatcher)
                                .decoderCoroutineContext(ioDispatcher)
                                .memoryCacheKey(imageItem.id.toString())
                                .diskCacheKey(imageItem.id.toString())
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED).build()
                        return@map imageRequest
                    }
            } catch (_: SecurityException) {

            } catch (_: ImageFileNotFoundException) {

            }
            return@withContext null
        }
    }
}