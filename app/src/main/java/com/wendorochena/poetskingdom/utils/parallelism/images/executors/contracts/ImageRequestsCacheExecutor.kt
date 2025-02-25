package com.wendorochena.poetskingdom.utils.parallelism.images.executors.contracts

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher

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
     * The applications context that will denote a folder within the application in
     * order to cache images
     */
    val context : Context

    /**
     * Read files from the applications context and insert them into Coils in memory Cache
     */
    suspend fun execute()
}