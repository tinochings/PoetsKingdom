package com.wendorochena.poetskingdom.utils.files.cache

import android.content.Context
import java.io.File

/**
 * Defines the operations a FileCacheUtility can operate
 */
interface FileCacheUtil {
    /**
     * Creates a folder in the cache directory and returns the file if it was successfully created
     * @param context the applications context
     * @param cacheName the name of the cache folder to create
     * @return returns the newly created folder or null if the the folder could not be created
     */
    fun retrieveCacheDirectory(context: Context, cacheName : String) : File?

    /**
     * Clears entire cache directory and children
     * @param context applications context
     * @param cacheName name of cache to invalidate
     */
    fun invalidateCacheFolder(context: Context, cacheName: String)

    /**
     * Deletes a child file element from a cache directory
     * @param context applications context
     * @param cacheName name of cache to delete children from
     * @param filesToDelete names of children to delete from @cacheName
     */
    fun deleteEntriesFromCacheFolder(context: Context, cacheName: String, filesToDelete : Array<String>)
}