package com.wendorochena.poetskingdom

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade

open class PoetsKingdomApplication : Application(), SingletonImageLoader.Factory {
    /** Return a new [ImageLoader]. */
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context).crossfade(true)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context,0.45).build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve(getString(R.string.coil_image_cache_name)))
                    .maxSizeBytes(500L * 1024 * 1024).build()
            }.build()
    }

    override fun onCreate() {
        SingletonImageLoader.setSafe { context ->
            return@setSafe newImageLoader(context)
        }
        super.onCreate()
    }
}