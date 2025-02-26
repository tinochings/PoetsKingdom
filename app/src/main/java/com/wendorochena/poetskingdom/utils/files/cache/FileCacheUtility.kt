package com.wendorochena.poetskingdom.utils.files.cache

import android.content.Context
import java.io.File
import java.io.IOException

class FileCacheUtility {

    /**
     * Creates a folder in the cache directory and returns the file if it was successfully created
     * @param context the applications context
     * @param cacheName the name of the cache folder to create
     * @return returns the newly created folder or null if the the folder could not be created
     */
    fun retrieveCacheDirectory(context: Context, cacheName : String) : File? {
        val cacheFolder = context.cacheDir.resolve(cacheName)

        try {
            if (!cacheFolder.exists())
                cacheFolder.mkdir()
            return cacheFolder
        } catch (_ : IOException){

        } catch (_ : SecurityException){

        }

        return null
    }

    /**
     * Clears entire cache directory and children
     * @param context applications context
     * @param cacheName name of cache to invalidate
     */
    fun invalidateCacheFolder(context: Context, cacheName: String) {
        context.cacheDir.resolve(cacheName).deleteRecursively()
    }
}