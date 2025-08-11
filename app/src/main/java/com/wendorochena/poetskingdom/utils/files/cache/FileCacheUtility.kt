package com.wendorochena.poetskingdom.utils.files.cache

import android.content.Context
import java.io.File
import java.io.IOException

class FileCacheUtility : FileCacheUtil {

    /**
     * Creates a folder in the cache directory and returns the file if it was successfully created
     * @param context the applications context
     * @param cacheName the name of the cache folder to create
     * @return returns the newly created folder or null if the the folder could not be created
     */
    override fun retrieveCacheDirectory(context: Context, cacheName: String): File? {
        val cacheFolder = context.cacheDir.resolve(cacheName)

        try {
            if (!cacheFolder.exists())
                cacheFolder.mkdir()
            return cacheFolder
        } catch (_: IOException) {

        } catch (_: SecurityException) {

        }

        return null
    }

    /**
     * Clears entire cache directory and children
     * @param context applications context
     * @param cacheName name of cache to invalidate
     */
    override fun invalidateCacheFolder(context: Context, cacheName: String) {
        context.cacheDir.resolve(cacheName).deleteRecursively()
    }

    /**
     * Deletes a child file element from a cache directory
     * @param context applications context
     * @param cacheName name of cache to delete children from
     * @param filesToDelete names of children to delete from @cacheName
     */
    override fun deleteEntriesFromCacheFolder(
        context: Context,
        cacheName: String,
        filesToDelete: Array<String>
    ) {
        filesToDelete.forEach {
            File(it).delete()
        }
    }
}