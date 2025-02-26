package com.wendorochena.poetskingdom.utils.parallelism.images.tasksImplementation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ThumbnailUtils
import android.os.Build
import android.util.Size
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.utils.files.cache.FileCacheUtility
import com.wendorochena.poetskingdom.utils.generators.ImageCacheKeyGen
import com.wendorochena.poetskingdom.utils.images.models.ImageItem
import com.wendorochena.poetskingdom.utils.parallelism.images.task.ImageCachingTask
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.IOException

class DivideAndConquerTaskImpl(
    override val dataToCache: List<File>,
    override val context: Context,
    private val imageCacheKeyGen: ImageCacheKeyGen = ImageCacheKeyGen(),
    override val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    override val imageThumbnailsCacheName: String
) : ImageCachingTask<ImageRequest>{

    private lateinit var reusableBitmap : Bitmap
    private val fileCacheUtility = FileCacheUtility()

    override fun execute(): List<ImageRequest>? {
        try {
            val requests = dataToCache.mapNotNull { file: File? ->
                    if (file != null) {
                            if (exifSupportedFiles.contains("." + file.extension)) {
                                val bitmapFile = generateBitmapThumbnail(file, context)
                                if (bitmapFile != null) {
                                    return@mapNotNull ImageItem(
                                        imageCacheKeyGen.generateKey(
                                            context
                                        ), bitmapFile,
                                    )
                                }
                            }
                        return@mapNotNull ImageItem(imageCacheKeyGen.generateKey(context), file)
                    } else
                        null
                }.map { imageItem: ImageItem ->
                    return@map buildImageRequest(
                        context,
                        imageItem.imageFile.absolutePath,
                        imageItem.id.toString()
                    )
                }
            return requests
        } catch (_ : SecurityException){
            return null
        }
    }
    /**
     * Generates a JPEG thumbnail to be stored on disk cache. After the image is generated the JPEG
     * image is written to cache disk. Any subsequent calls to this method after initial creation will
     * return a cached value. If the cached folder could not be created then this method returns null
     * @param file image file to create a bitmap from
     * @param context applications context
     *
     * @return the newly created bitmap or null if creation was not possible
     */
    private fun generateBitmapThumbnail(file: File, context: Context): File? {
        val compressedImageThumbnailFolder =
            fileCacheUtility.retrieveCacheDirectory(context, imageThumbnailsCacheName)
                ?: return null
        val bitmapFileToBeCreated = File(compressedImageThumbnailFolder, "${file.nameWithoutExtension}.jpg")
        return try {
            if (bitmapFileToBeCreated.createNewFile()){
                resetBitmap()
                reusableBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    ThumbnailUtils.createImageThumbnail(file, Size(250, 250), null)
                else
                    ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.absolutePath), 250, 250)

                if (reusableBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bitmapFileToBeCreated.outputStream())) {
                    bitmapFileToBeCreated.setLastModified(file.lastModified())
                    return bitmapFileToBeCreated
                }
            }
            bitmapFileToBeCreated
        } catch (e: IOException) {
            bitmapFileToBeCreated.delete()
            return null
        } catch (e : SecurityException){
            return null
        }
    }


    private fun resetBitmap(){
        if (::reusableBitmap.isInitialized){
            reusableBitmap = reusableBitmap.copy(Bitmap.Config.ARGB_8888, true)
            reusableBitmap.eraseColor(Color.TRANSPARENT)
        }

    }
    /**
     * Builds a Coil Image Request with memory and disk caching mechanisms
     * @param context applications context
     * @param data the data to load
     * @param key image cache key
     */
    private fun buildImageRequest(
        context: Context,
        data: String,
        key: String
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