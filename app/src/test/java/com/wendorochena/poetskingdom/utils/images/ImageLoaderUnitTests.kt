package com.wendorochena.poetskingdom.utils.images

import android.content.Context
import coil3.Image
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.imageLoader
import coil3.memory.MemoryCache
import com.wendorochena.poetskingdom.utils.generators.ImageCacheKeyGen
import com.wendorochena.poetskingdom.utils.generators.contracts.ImageFolderType
import com.wendorochena.poetskingdom.utils.images.loaders.ImageLoaderUtility
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

/**
 * For simplicity sake, both cache folders have the exact same amount of images.
 *
 *
 */
class ImageLoaderUnitTests {
    private val imageThumbnailsCacheName = "compressed_image_thumbnails"
    private val myPoemsThumbnailsCacheName = "compressed_my_image_thumbnails"

    private val imagesFolder = File(
            "../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectoryImageLoader/$imageThumbnailsCacheName"
            )

    /**
     * cached images are sorted in ascending order of last modified
     */
    private val sortedImagesFolderFiles = imagesFolder.listFiles()!!.sortedBy { it.lastModified() }

    @Test
    fun testInvalidDirectorySize() = runTest{
        val dispatcher = StandardTestDispatcher(testScheduler)
        every { anyConstructed<ImageCacheKeyGen>().retrieveLastKnownCachedKey(any()) } returns 0
        val imageLoaderUtility = ImageLoaderUtility(dispatcher, ImageFolderType.IMAGES)
        val imageRequests = imageLoaderUtility.loadAllImages(mockedContext, dispatcher)

        assert(imageRequests.isEmpty())
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun testValidDirectorySizeInvalidImageFolder()= runTest{
        every { anyConstructed<ImageCacheKeyGen>().retrieveLastKnownCachedKey(any()) } returns sortedImagesFolderFiles.size - 1
        val invalidContext : Context = mockk<Context>()
        setupMemoryCache(imageThumbnailsCacheName, 0 until imagesFolder.listFiles()!!.size, true, sortedImagesFolderFiles.size - 1)
        every { invalidContext.cacheDir } returns File("invalid path")
        assert(!invalidContext.cacheDir.exists())
        val dispatcher = StandardTestDispatcher(testScheduler)
        val imageLoaderUtility = ImageLoaderUtility(dispatcher, ImageFolderType.IMAGES)
        val imageRequests = imageLoaderUtility.loadAllImages(invalidContext, dispatcher)

        assert(imageRequests.isEmpty())
    }
    /**
     * Tests the path where every image is found in the cache
     * N.B POSITIVE PATH TESTING
     */
    @Test
    fun testLoadImagesCacheHit() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        every { anyConstructed<ImageCacheKeyGen>().retrieveLastKnownCachedKey(any()) } returns sortedImagesFolderFiles.size - 1
        setupMemoryCache(imageThumbnailsCacheName, 0 until imagesFolder.listFiles()!!.size, false, sortedImagesFolderFiles.size - 1)
        val imageLoaderUtility = ImageLoaderUtility(dispatcher, ImageFolderType.IMAGES)
        val imageRequests = imageLoaderUtility.loadAllImages(mockedContext, dispatcher)
        testScheduler.advanceUntilIdle()
        assert(imageRequests.size == sortedImagesFolderFiles.size)
        for ((imageRequestsCounter, i) in (sortedImagesFolderFiles.size - 1 downTo 0).withIndex()){
            assert(imageRequests[imageRequestsCounter].data is Image)
            val dataAsImage = imageRequests[imageRequestsCounter].data as Image
            assert(dataAsImage.toString() == sortedImagesFolderFiles[i].absolutePath)
        }
    }

    /**
     * Tests the path where every image is found in the cache when an arbitrarily large
     * cache key i stored. A large cache key should not change the functionality of the cache
     * retrieval
     * N.B POSITIVE PATH TESTING
     */
    @Test
    fun testLoadImagesCacheHitSmallRangeLargeLastKnownKey() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        every { anyConstructed<ImageCacheKeyGen>().retrieveLastKnownCachedKey(any()) } returns 10000
        setupMemoryCache(imageThumbnailsCacheName, 10000 - sortedImagesFolderFiles.size + 1 .. 10000, false, 10000)
        val imageLoaderUtility = ImageLoaderUtility(dispatcher, ImageFolderType.IMAGES)
        val imageRequests = imageLoaderUtility.loadAllImages(mockedContext, dispatcher)

        assert(imageRequests.size == sortedImagesFolderFiles.size)
        testScheduler.advanceUntilIdle()
        for ((imageRequestsCounter, i) in (sortedImagesFolderFiles.size - 1 downTo 0).withIndex()){
            assert(imageRequests[imageRequestsCounter].data is Image)
            val dataAsImage = imageRequests[imageRequestsCounter].data as Image
            assert(dataAsImage.toString() == sortedImagesFolderFiles[i].absolutePath)
        }
    }

    /**
     * Tests that loading from disk finds the correct image in the correct order.
     */
    @Test
    fun testLoadImagesCacheMiss() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        every { anyConstructed<ImageCacheKeyGen>().retrieveLastKnownCachedKey(any()) } returns sortedImagesFolderFiles.size - 1
        setupMemoryCache(imageThumbnailsCacheName, 0 until imagesFolder.listFiles()!!.size, true, sortedImagesFolderFiles.size - 1)
        val imageLoaderUtility = ImageLoaderUtility(dispatcher, ImageFolderType.IMAGES)
        val imageRequests = imageLoaderUtility.loadAllImages(mockedContext, dispatcher)
        testScheduler.advanceUntilIdle()

        assert(imageRequests.size == sortedImagesFolderFiles.size)
        for ((imageRequestsCounter, i) in (sortedImagesFolderFiles.size - 1 downTo 0).withIndex()){
            assert(imageRequests[imageRequestsCounter].data is String)
            val dataAsString = imageRequests[imageRequestsCounter].data

            assert(dataAsString == sortedImagesFolderFiles[i].absolutePath)
        }
    }

    /**
     * Tests that loading from disk finds the correct image in the correct order when given a
     * large range
     */
    @Test
    fun testLoadImagesCacheMissLargeStartingRange() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        every { anyConstructed<ImageCacheKeyGen>().retrieveLastKnownCachedKey(any()) } returns 20000
        setupMemoryCache(imageThumbnailsCacheName, 20000 - imagesFolder.listFiles()!!.size + 1 .. 20000, true, 20000)
        val imageLoaderUtility = ImageLoaderUtility(dispatcher, ImageFolderType.IMAGES)
        val imageRequests = imageLoaderUtility.loadAllImages(mockedContext, dispatcher)
        testScheduler.advanceUntilIdle()

        assert(imageRequests.size == sortedImagesFolderFiles.size)
        for ((imageRequestsCounter, i) in (sortedImagesFolderFiles.size - 1 downTo 0).withIndex()){
            assert(imageRequests[imageRequestsCounter].data is String)
            val dataAsString = imageRequests[imageRequestsCounter].data

            assert(dataAsString == sortedImagesFolderFiles[i].absolutePath)
        }
    }

    /**
     * Tests that the ImageLoaderUtility can retrieve files from the cache and the disk in a
     * single iteration
     */
    @Test
    fun testLoadImagesHalfCacheHitHalfCacheMissLargeStartingRange() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        every { anyConstructed<ImageCacheKeyGen>().retrieveLastKnownCachedKey(any()) } returns 20000
        setupMemoryCache(imageThumbnailsCacheName, 19998 .. 19999, false, 20000)
        setupMemoryCache(imageThumbnailsCacheName, 20000 .. 20000, true, 20000)
        setupMemoryCache(imageThumbnailsCacheName, 19997 .. 19997, true, 20000)
        val imageLoaderUtility = ImageLoaderUtility(dispatcher, ImageFolderType.IMAGES)
        val imageRequests = imageLoaderUtility.loadAllImages(mockedContext, dispatcher)
        testScheduler.advanceUntilIdle()

        assert(imageRequests.size == sortedImagesFolderFiles.size)
        assert(imageRequests[0].data.toString() == sortedImagesFolderFiles[sortedImagesFolderFiles.size - 1].absolutePath)
        assert(imageRequests[1].data.toString() == sortedImagesFolderFiles[sortedImagesFolderFiles.size - 2].absolutePath)
        assert(imageRequests[2].data.toString() == sortedImagesFolderFiles[sortedImagesFolderFiles.size - 3].absolutePath)
        assert(imageRequests[3].data.toString() == sortedImagesFolderFiles[0].absolutePath)
    }
    /**
     * Create memory cache values in the correct  ascending order based on lastModified. The earliest
     * created file is the first file and the latest edited file is the last
     *
     * In order to verify accuracy of an image we use the size of the file of each image as the
     * uniquely identifying feature accessed by the width param
     *
     * At time of testing, no two files had the exact same file length
     * @param range the range of cache keys from the start index until the end index
     * @param nullify true if the range should be found in memory cache else false
     * @param lastKnownKey the last known cache key
     */
    private fun setupMemoryCache(folder: String, range: IntRange, nullify : Boolean, lastKnownKey : Int) {
        val memoryCacheKeys : HashSet<MemoryCache.Key> = HashSet()

        range.forEach{ step ->
            val currImageMock = mockk<Image>()
            val index = step - lastKnownKey + sortedImagesFolderFiles.size - 1
            every { currImageMock.toString() } returns sortedImagesFolderFiles[index].absolutePath
            every { memoryCache[MemoryCache.Key("$step")] } returns if (nullify) null else MemoryCache.Value(currImageMock)
            memoryCacheKeys.add(MemoryCache.Key("$step"))
        }

        every { memoryCache.keys } returns memoryCacheKeys
    }

    companion object {

        var memoryCache: MemoryCache = mockk<MemoryCache>()
        var mockedContext : Context = mockk<Context>()
        @JvmStatic
        @BeforeClass
        fun setup() {
            imageLoaderSetup()
            mockkConstructor(ImageCacheKeyGen::class)
            every { mockedContext.cacheDir } returns File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectoryImageLoader")
        }

        @OptIn(DelicateCoilApi::class)
        private fun imageLoaderSetup() {
            mockkConstructor(ImageLoader::class)
            val imageLoader = mockk<ImageLoader>()
            every { imageLoader.memoryCache } returns memoryCache
            every { mockedContext.imageLoader } returns imageLoader
            every { anyConstructed<ImageLoader>().memoryCache } returns memoryCache
            SingletonImageLoader.setUnsafe(imageLoader)
        }
    }
}