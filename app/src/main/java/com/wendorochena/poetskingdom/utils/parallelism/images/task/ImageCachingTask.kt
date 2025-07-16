package com.wendorochena.poetskingdom.utils.parallelism.images.task

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File

/**
 * Denotes a task that caches images and returns a cache representation value of type T.
 */
interface ImageCachingTask<T> {

    val dataToCache : List<File>

    /**
     * The applications context
     */
    val context : Context

    /**
     * Files that can be minified
     */
    val exifSupportedFiles: Array<String>
        get() = arrayOf(".jpg", ".bmp", ".gif", ".png", ".heic", ".heif", ".avif", ".webp")

    /**
     * Name of the cache to insert minified thumbnails into
     */
    val imageThumbnailsCacheName: String

    /**
     * Dispatcher used for ImageLoaders caching context
     */
    val ioDispatcher : CoroutineDispatcher
    val taskSupervisor : TaskSupervisor

    /**
     * Iterate over every file in the dataToCache variable and cache the subsequent image file
     * by creating a minimised bitmap version in the JPEG format.
     */
    fun execute() : List<T>?
}