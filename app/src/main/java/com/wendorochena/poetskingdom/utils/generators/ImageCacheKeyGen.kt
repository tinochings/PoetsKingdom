package com.wendorochena.poetskingdom.utils.generators

import android.content.Context
import android.content.SharedPreferences
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.utils.generators.contracts.NonRandomKeyGenContract

class ImageCacheKeyGen(private val keyRange : Pair<Int, Int>? = null) : NonRandomKeyGenContract<Int> {
    private var currentGeneratedKey : Int = -1
    private val sharedPreferencesKey = "int_key"

    override fun generateKey(context: Context): Int {
        if (currentGeneratedKey == -1)
            retrieveLastKnownKey(context)

        return currentGeneratedKey++
    }

    override fun generateKeys(context: Context, numOfKeys : Int) : Pair<Int, Int>? {
        retrieveLastKnownKey(context)

        val checkOverflowAddition = currentGeneratedKey + numOfKeys
        if (checkOverflowAddition < Int.MAX_VALUE){
            val pairToReturn = Pair(currentGeneratedKey, checkOverflowAddition)
            currentGeneratedKey = checkOverflowAddition
            cacheLastKnownKey(context)
            return pairToReturn
        }
        return null
    }

    override fun cacheLastKnownKey(context: Context) {
        getSharedPreferences(context).edit().putInt(sharedPreferencesKey, currentGeneratedKey).apply()
    }

    /**
     * Clears cache
     */
    override fun invalidateCache(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }

    /**
     * @param context the applications context
     * @return the list of cache keys of type T
     */
    override fun retrieveLastKnownCachedKey(context: Context): Int {
        retrieveLastKnownKey(context)
        return currentGeneratedKey
    }

    /**
     * Retrieves the last remembered key
     */
    private fun retrieveLastKnownKey(context: Context) {
        currentGeneratedKey =
            keyRange?.first ?: getSharedPreferences(context).getInt(sharedPreferencesKey, 0)
    }

    private fun getSharedPreferences(context: Context) : SharedPreferences {
       return  context.getSharedPreferences(
            context.getString(R.string.image_key_gen_cache_name),
            Context.MODE_PRIVATE
        )
    }
}