package com.wendorochena.poetskingdom.utils.parallelism.images.executors.contracts

import android.content.Context
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File

/**
 * The main purpose of this interface is to multi-thread the generation of minified thumbnails to
 * enhance coil image processing.
 */
interface ImageRequestsCacheExecutor {
    /**
     * Dispatcher to run the executor on
     */
    val defaultDispatcher : CoroutineDispatcher

    /**
     * IO dispatcher to execute subsequent IO tasks on
     */
    val ioDispatcher : CoroutineDispatcher

    /**
     * The applications context that will denote a folder within the application in
     * order to cache images
     */
    val context : Context

    /**
     * Read files from the applications context and insert them into Coils in memory Cache
     */
    suspend fun execute()

    /**
     * Reads an array of files loaded in memory and inserts them into Coils in memory cache
     * @param filesToCache files to insert into cache
     * @param imageFolderType the type of image folder
     */
    suspend fun executePreloadedFiles(filesToCache : Array<File>, imageFolderType: ImageFolderType): List<ImageRequest>
}