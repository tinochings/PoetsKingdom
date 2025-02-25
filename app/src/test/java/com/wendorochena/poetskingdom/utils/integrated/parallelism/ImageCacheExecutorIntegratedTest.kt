package com.wendorochena.poetskingdom.utils.integrated.parallelism

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.utils.parallelism.images.executors.ImagesCacheExecutor
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImageCacheExecutorIntegratedTest {
    private val imageThumbnailsCacheName = "compressed_image_thumbnails"
    private val myPoemsThumbnailsCacheName = "compressed_my_image_thumbnails"
    private val myPoemsImagesSize = 18
    private val myImagesSize = 66
    private val myImagesSizeOddUpperBound = 65
    private val myImagesSizeOddLowerBound = 1


    @Mock
    val mockedBitmap: Bitmap = mock {
        on {
            this.compress(any(), any(), any())
        } doReturn true
        on {
            this.copy(any(), any())
        } doReturn this.mock
    }

    @Mock
    val sharedPreferencesEditor: SharedPreferences.Editor = mock {
        on {
            this.putInt(any(), any())
        } doReturn this.mock
    }

    @Mock
    val sharedPreferences: SharedPreferences = mock {
        on {
            this.getInt(any(), any())
        } doReturn (0)
        on {
            this.edit()
        } doReturn sharedPreferencesEditor
    }

    @Mock
    val mockContext: Context = mock {
        on {
            this.getString(R.string.my_images_folder_name)
        } doReturn "myImages"
        on {
            this.getString(R.string.thumbnails_folder_name)
        } doReturn "thumbnails"
        on {
            this.getDir(
                this.getString(R.string.my_images_folder_name),
                Context.MODE_PRIVATE
            )
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/images/myImages")

        on {
            this.getDir(
                this.getString(R.string.thumbnails_folder_name),
                Context.MODE_PRIVATE
            )
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/images/myPoems")

        on {
            this.cacheDir
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory")
        on {
            this.getSharedPreferences(
                this.getString(R.string.image_key_gen_cache_name),
                Context.MODE_PRIVATE
            )
        } doReturn sharedPreferences
    }

    @Before
    fun mockThumbnailsUtils() {
        mockkStatic(ThumbnailUtils::class)
        every { ThumbnailUtils.createImageThumbnail(any(), any(), any()) } returns mockedBitmap
    }

    @After
    fun clearFiles() {
        val imagesFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$imageThumbnailsCacheName")
        val poemsFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$myPoemsThumbnailsCacheName")
        imagesFolder.listFiles()!!.forEach { it.delete() }
        poemsFolder.listFiles()!!.forEach { it.delete() }
    }

    @Test
    fun testAllFilesGetGenerated() = runTest {
        ImagesCacheExecutor(StandardTestDispatcher(testScheduler), mockContext).execute()
        testScheduler.advanceUntilIdle()

        val imagesFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$imageThumbnailsCacheName")
        val poemsFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$myPoemsThumbnailsCacheName")

        assert(imagesFolder.listFiles()!!.size == myImagesSize)
        assert(poemsFolder.listFiles()!!.size == myPoemsImagesSize)

        val nonCachedImagesFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/images/myImages")
        val nonCachedPoemsFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/images/myPoems")
        verifyCachedAndNonCachedImageNames(imagesFolder, nonCachedImagesFolder)
        verifyCachedAndNonCachedImageNames(poemsFolder, nonCachedPoemsFolder)
    }

    /**
     * This tests that the division of work done in ImagesCacheExecutor is able to correctly divide
     * an odd number of files which at the time of testing is 65. This method also uses an empty
     * myPoems folder to test functionality when only images folders need minimising. An empty poems
     * folder should not stop the executor from successfully finishing
     */
    @Test
    fun testAllFilesGetGeneratedWithOddNumber() = runTest {
        updateImageFolderMocks("myImagesOddUpperBound", "myPoemsEmpty")

        val imagesCacheExecutor =
            ImagesCacheExecutor(StandardTestDispatcher(testScheduler), mockContext)
        imagesCacheExecutor.execute()
        testScheduler.advanceUntilIdle()

        val cachedImagesFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$imageThumbnailsCacheName")
        val cachedPoemsFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$myPoemsThumbnailsCacheName")

        assert(cachedPoemsFolder.listFiles()!!.isEmpty())
        assert(cachedImagesFolder.listFiles()!!.size == myImagesSizeOddUpperBound)

        val nonCachedImagesFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/images/myImagesOddUpperBound")

        verifyCachedAndNonCachedImageNames(cachedImagesFolder, nonCachedImagesFolder)

        resetMocksToDefault()
    }

    @Test
    fun testGenerateOneImage() = runTest {
        updateImageFolderMocks("myImagesOddLowerBound", "myPoemsEmpty")

        val imagesCacheExecutor =
            ImagesCacheExecutor(StandardTestDispatcher(testScheduler), mockContext)
        imagesCacheExecutor.execute()
        testScheduler.advanceUntilIdle()

        val cachedImagesFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$imageThumbnailsCacheName")
        val cachedPoemsFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$myPoemsThumbnailsCacheName")

        assert(cachedPoemsFolder.listFiles()!!.isEmpty())
        assert(cachedImagesFolder.listFiles()!!.size == myImagesSizeOddLowerBound)
        val nonCachedImagesFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/images/myImagesOddLowerBound")

        verifyCachedAndNonCachedImageNames(cachedImagesFolder, nonCachedImagesFolder)
        resetMocksToDefault()
    }

    @Test
    fun testEmptyFolders() = runTest {
        updateImageFolderMocks("myImagesEmpty", "myPoemsEmpty")
        val imagesCacheExecutor =
            ImagesCacheExecutor(StandardTestDispatcher(testScheduler), mockContext)
        imagesCacheExecutor.execute()
        testScheduler.advanceUntilIdle()

        val cachedImagesFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$imageThumbnailsCacheName")
        val cachedPoemsFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$myPoemsThumbnailsCacheName")

        assert(cachedImagesFolder.listFiles()!!.isEmpty())
        assert(cachedPoemsFolder.listFiles()!!.isEmpty())
        resetMocksToDefault()
    }

    @Test
    fun testNonExistentFolder() = runTest {
        updateImageFolderMocks("blahblah", "booboo")
        val imagesCacheExecutor =
            ImagesCacheExecutor(StandardTestDispatcher(testScheduler), mockContext)
        imagesCacheExecutor.execute()
        testScheduler.advanceUntilIdle()

        val cachedImagesFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$imageThumbnailsCacheName")
        val cachedPoemsFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$myPoemsThumbnailsCacheName")

        assert(cachedImagesFolder.listFiles()!!.isEmpty())
        assert(cachedPoemsFolder.listFiles()!!.isEmpty())
        resetMocksToDefault()
    }

    @Test
    fun testBitmapFailedToBeCreated() = runTest {
        every { ThumbnailUtils.createImageThumbnail(any(), any(), any()) } throws (IOException())
        val imagesCacheExecutor =
            ImagesCacheExecutor(StandardTestDispatcher(testScheduler), mockContext)
        imagesCacheExecutor.execute()
        testScheduler.advanceUntilIdle()

        val cachedImagesFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$imageThumbnailsCacheName")
        val cachedPoemsFolder =
            File("../app/src/test/java/com/wendorochena/poetskingdom/cacheDirectory/$myPoemsThumbnailsCacheName")

        assert(cachedPoemsFolder.listFiles()!!.isEmpty())
        assert(cachedImagesFolder.listFiles()!!.isEmpty())
    }

    private fun updateImageFolderMocks(imagesFolder : String, myPoemsFolder : String){
        Mockito.`when`(
            mockContext.getDir(
                mockContext.getString(R.string.my_images_folder_name),
                Context.MODE_PRIVATE
            )
        ).doReturn(File("../app/src/test/java/com/wendorochena/poetskingdom/images/$imagesFolder"))

        Mockito.`when`(
            mockContext.getDir(
                mockContext.getString(R.string.thumbnails_folder_name),
                Context.MODE_PRIVATE
            )
        ).doReturn(File("../app/src/test/java/com/wendorochena/poetskingdom/images/$myPoemsFolder"))
    }
    private fun verifyCachedAndNonCachedImageNames(cachedFolder : File, nonCachedFolder : File) {
        val cacheFolderFiles = cachedFolder.listFiles()!!
        nonCachedFolder.listFiles()!!.forEachIndexed { i, f ->
            assert(cacheFolderFiles[i].nameWithoutExtension == f.nameWithoutExtension)
            assert(cacheFolderFiles[i].lastModified() == f.lastModified())
        }
    }
    private fun resetMocksToDefault() {
        //reset mock
        Mockito.`when`(
            mockContext.getDir(
                mockContext.getString(R.string.my_images_folder_name),
                Context.MODE_PRIVATE
            )
        ).doReturn(File("../app/src/test/java/com/wendorochena/poetskingdom/images/myImages"))
        Mockito.`when`(
            mockContext.getDir(
                mockContext.getString(R.string.thumbnails_folder_name),
                Context.MODE_PRIVATE
            )
        ).doReturn(File("../app/src/test/java/com/wendorochena/poetskingdom/images/myPoems"))
    }
}