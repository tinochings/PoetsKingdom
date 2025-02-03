package com.wendorochena.poetskingdom.utils.generators.contracts

import android.content.Context

interface NonRandomKeyGenContract<T> {

    /**
     * Generates a key of type <T>. Retrieves the last cached key if the this method is called
     * with the default values
     */
    fun generateKey(context: Context) : T

    /**
     * Caches the last known key
     */
    fun cacheLastKnownKey(context: Context)

    /**
     * Clears cache
     */
    fun invalidateCache(context: Context)
}