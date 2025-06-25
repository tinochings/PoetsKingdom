package com.wendorochena.poetskingdom.utils.generators

import android.content.Context
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.generators.contracts.NonRandomKeyGenContract
import java.io.File

class FileNameImageCacheKeyGen(
    override val imageFolderType: ImageFolderType,
    private val files: List<File>
) : NonRandomKeyGenContract<String> {

    private var filesImageIndex = -1

    override fun generateKey(context: Context): String {
        filesImageIndex++
        return files[filesImageIndex].name
    }

    override fun generateKeys(context: Context, numOfKeys: Int): Pair<String, String> {
        /* no-op */
        return Pair("", "")
    }

    override fun cacheLastKnownKey(context: Context) {
        /* no-op */
    }

    override fun invalidateCache(context: Context) {
        /* no-op */
    }

    override fun retrieveLastKnownCachedKey(context: Context): String {
        /* no-op */
        return ""
    }
}