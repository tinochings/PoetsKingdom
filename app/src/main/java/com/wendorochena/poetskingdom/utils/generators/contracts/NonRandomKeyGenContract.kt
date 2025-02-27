package com.wendorochena.poetskingdom.utils.generators.contracts

import android.content.Context

/**
 * A deterministic key generator that keeps track of generated keys
 */
interface NonRandomKeyGenContract<T> {

    val imageFolderType : ImageFolderType
    /**
     * Generates a key of type <T>. Retrieves the last cached key if the this method is called
     * with the default values
     * @param context applications context
     * @return value of type T
     */
    fun generateKey(context: Context) : T

    /**
     * Reserves a certain range of values as keys
     *
     * @return a pair with the first value as the beginning key and the second value as the last key
     * null is returned when a range could not be created
     */
    fun generateKeys(context: Context, numOfKeys : Int) : Pair<T, T>?

    /**
     * Caches the last known key
     * @param context applications context
     */
    fun cacheLastKnownKey(context: Context)

    /**
     * Clears cache
     * @param context applications context
     */
    fun invalidateCache(context: Context)

    /**
     * @param context the applications context
     * @return last known cached key
     */
    fun retrieveLastKnownCachedKey(context: Context) : T
}

enum class ImageFolderType {
    IMAGES, POEM_THUMBNAILS
}