package com.wendorochena.poetskingdom.utils.generators

import android.content.Context
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.utils.generators.contracts.NonRandomKeyGenContract

class ImageCacheKeyGen : NonRandomKeyGenContract<Long> {
    private var currentGeneratedKey : Long = -1L
    private val sharedPreferencesKey = "long_key"

    override fun generateKey(context: Context): Long {
        if (currentGeneratedKey == -1L)
            retrieveLastKnownKey(context)

        return currentGeneratedKey++
    }

    override fun cacheLastKnownKey(context: Context) {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.image_key_gen_cache_name),
            Context.MODE_PRIVATE
        )
        sharedPreferences.edit().putLong(sharedPreferencesKey, currentGeneratedKey).apply()
    }

    /**
     * Clears cache
     */
    override fun invalidateCache(context: Context) {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.image_key_gen_cache_name),
            Context.MODE_PRIVATE
        )
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Retrieves the last remembered key
     */
    private fun retrieveLastKnownKey(context: Context) {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.image_key_gen_cache_name),
            Context.MODE_PRIVATE
        )
        currentGeneratedKey = sharedPreferences.getLong(sharedPreferencesKey, 0)
    }


}