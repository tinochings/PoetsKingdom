package com.wendorochena.poetskingdom

import android.content.Context
import com.wendorochena.poetskingdom.viewModels.CurrentSelection
import com.wendorochena.poetskingdom.viewModels.FloatingButtonState
import com.wendorochena.poetskingdom.viewModels.MyImagesViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File

class MyImagesViewModelTest {

    private var myImagesViewModel = MyImagesViewModel()

    @Mock
    var mockContext: Context = mock {
        on {
            getString(R.string.thumbnails_folder_name)
        } doReturn ("thumbnails")
        on {
            getString(R.string.saved_images_folder_name)
        } doReturn ("savedImages")
         on {
            getString(R.string.my_images_folder_name)
        } doReturn ("myImages")

        on {
            getString(R.string.poems_folder_name)
        } doReturn ("poems")
        on {
            this.getDir("myImages", Context.MODE_PRIVATE)
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images")

        on {
            this.getDir("savedImages", Context.MODE_PRIVATE)
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images")

        on {
            this.getDir("thumbnails", Context.MODE_PRIVATE)
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails")

        on {
            this.getDir("poems", Context.MODE_PRIVATE)
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder")
    }

    @Before
    fun createPoemsFolders() {
        myImagesViewModel = MyImagesViewModel()
    }
    @After
    fun reAddFiles() {
        //re-add image files
        if (!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/Boundary_Test_1.png").exists()) {
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/temp_thumbnails/Boundary_Test_1.png").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/Boundary_Test_1.png")
            )
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/temp_thumbnails/dfsdfsdf.png").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/dfsdfsdf.png")
            )
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/temp_thumbnails/HELLO.png").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/HELLO.png")
            )
        }

        //re-add thumbnails
        if (!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png").exists()) {
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/temp_thumbnails/Boundary_Test_1.png").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png")
            )
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/temp_thumbnails/dfsdfsdf.png").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png")
            )
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/temp_thumbnails/HELLO.png").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png")
            )
        }
        //delete copied images
        if (File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/copy1.png").exists()) {
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/copy1.png").delete()
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/copy2.png").delete()
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/copy3.png").delete()
        }
        //re-add poems
        if (!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").exists()) {
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/poems/HELLO.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml")
            )
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/poems/Boundary_Test_1.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml")
            )
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/poems/dfsdfsdf.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml")
            )
        }

        // delete album poem files
        if (File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml").exists()){
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml").delete()
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/Boundary_Test_1.xml").delete()
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/dfsdfsdf.xml").delete()
        }

        //re-add directories
        if (!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists()) {
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").mkdir()
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").mkdir()
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").mkdir()
        }
    }
    /**
     * Tests all files are found
     */
    @Test
    fun testListedAllFiles() {
        assert(myImagesViewModel.imageFiles.isEmpty())
        assert(myImagesViewModel.getImageFiles(mockContext).size == 3)
        assert(myImagesViewModel.imageFiles.isNotEmpty())
        for (map in myImagesViewModel.getImageFiles(mockContext)) {
            assert(!map.value)
        }
    }

    /**
     * Tests listed thumbnails
     */
    @Test
    fun testListedPoems() {
        assert(myImagesViewModel.savedPoemImages.isEmpty())
        assert(myImagesViewModel.getThumbnails(mockContext).size == 3)
        assert(myImagesViewModel.imageFiles.isEmpty())
        assert(myImagesViewModel.savedPoemImages.isNotEmpty())
        for (map in myImagesViewModel.getThumbnails(mockContext)) {
            assert(!map.value)
        }
    }

    @Test
    fun testSetSelection() {
        myImagesViewModel.setSelection(CurrentSelection.POEMS)
        assert(myImagesViewModel.currentSelection == CurrentSelection.POEMS)
    }

    @Test
    fun testSetFloatingButtonState(){
        myImagesViewModel.setFloatingButtonState(FloatingButtonState.ADDIMAGE)
        assert(myImagesViewModel.floatingButtonStateVar == FloatingButtonState.ADDIMAGE)
        myImagesViewModel.setFloatingButtonState(FloatingButtonState.DELETEIMAGE)
        assert(myImagesViewModel.floatingButtonStateVar == FloatingButtonState.DELETEIMAGE)
    }

    @Test
    fun testOnLongClickAllImages() {
        myImagesViewModel.setSelection(CurrentSelection.IMAGES)
        myImagesViewModel.setOnLongClick(true)
        for (map in myImagesViewModel.imageFiles)
            myImagesViewModel.imageFiles[map.key] = true
        assert(myImagesViewModel.onImageLongPressed)
        for (map in myImagesViewModel.imageFiles)
            assert(map.value)

        for (map in myImagesViewModel.imageFiles)
            myImagesViewModel.imageFiles[map.key] = false

        for (map in myImagesViewModel.imageFiles)
            assert(!map.value)
    }

    @Test
    fun testDeleteImages(){
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/Boundary_Test_1.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/dfsdfsdf.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/HELLO.png").exists())
        myImagesViewModel.setSelection(CurrentSelection.IMAGES)
        assert(myImagesViewModel.getImageFiles(mockContext).size == 3)
        myImagesViewModel.setOnLongClick(true)

        for (map in myImagesViewModel.imageFiles)
            myImagesViewModel.imageFiles[map.key] = true
        myImagesViewModel.deleteImages()
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/Boundary_Test_1.png").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/dfsdfsdf.png").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/HELLO.png").exists())
    }

    @Test
    fun testDeleteSavedPoems() {
        assert( File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists())
        myImagesViewModel.setSelection(CurrentSelection.POEMS)
        assert(myImagesViewModel.currentSelection == CurrentSelection.POEMS)
        assert(myImagesViewModel.savedPoemImages.isEmpty())
        assert(myImagesViewModel.getThumbnails(mockContext).size == 3)
        assert(myImagesViewModel.imageFiles.isEmpty())
        assert(myImagesViewModel.savedPoemImages.isNotEmpty())
        myImagesViewModel.setOnLongClick(true)
        assert(myImagesViewModel.onImageLongPressed)
        for (map in myImagesViewModel.getThumbnails(mockContext)) {
            myImagesViewModel.savedPoemImages[map.key] = true
        }
        myImagesViewModel.deleteSavedPoems(mockContext)
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists())

        // poem files should be there
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").exists())
        //thumbnails not deleted
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png").exists())
    }

    @Test
    fun testDeleteSavedPoemsWithSavedAlbumPoems() {
        assert( File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists())

        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").copyTo(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml")).exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").copyTo(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/dfsdfsdf.xml")).exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").copyTo(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/Boundary_Test_1.xml")).exists())

        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").delete())
        myImagesViewModel.setSelection(CurrentSelection.POEMS)
        assert(myImagesViewModel.currentSelection == CurrentSelection.POEMS)
        assert(myImagesViewModel.savedPoemImages.isEmpty())
        assert(myImagesViewModel.getThumbnails(mockContext).size == 3)
        assert(myImagesViewModel.imageFiles.isEmpty())
        assert(myImagesViewModel.savedPoemImages.isNotEmpty())
        myImagesViewModel.setOnLongClick(true)
        assert(myImagesViewModel.onImageLongPressed)
        for (map in myImagesViewModel.getThumbnails(mockContext)) {
            myImagesViewModel.savedPoemImages[map.key] = true
        }
        myImagesViewModel.deleteSavedPoems(mockContext)
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists())

        //thumbnails not deleted
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png").exists())
    }

    @Test
    fun testDeleteSavedPoemsWithNoSavedPoems() {
        assert( File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists())

        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").delete())

        myImagesViewModel.setSelection(CurrentSelection.POEMS)
        assert(myImagesViewModel.currentSelection == CurrentSelection.POEMS)
        assert(myImagesViewModel.savedPoemImages.isEmpty())
        assert(myImagesViewModel.getThumbnails(mockContext).size == 3)
        assert(myImagesViewModel.imageFiles.isEmpty())
        assert(myImagesViewModel.savedPoemImages.isNotEmpty())
        myImagesViewModel.setOnLongClick(true)
        assert(myImagesViewModel.onImageLongPressed)
        for (map in myImagesViewModel.getThumbnails(mockContext)) {
            myImagesViewModel.savedPoemImages[map.key] = true
        }
        myImagesViewModel.deleteSavedPoems(mockContext)
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists())

        // poem files should not be there
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").exists())
        //thumbnails not deleted
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png").exists())
    }
    /**
     * Test that a back press should deselect all images
     */
    @Test
    fun testInvokeBackOnLongPressImages() {
        myImagesViewModel.setOnLongClick(true)
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/Boundary_Test_1.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/dfsdfsdf.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/HELLO.png").exists())

        for (map in myImagesViewModel.imageFiles)
            myImagesViewModel.imageFiles[map.key] = true

        assert(myImagesViewModel.onImageLongPressed)

        for (map in myImagesViewModel.imageFiles)
            assert(myImagesViewModel.imageFiles[map.key] == true)

        //immitate back press
        myImagesViewModel.setOnLongClick(false)
        myImagesViewModel.resetSelectedImages()

        for (map in myImagesViewModel.imageFiles)
            assert(myImagesViewModel.imageFiles[map.key] == false)
    }

    /**
     * Test that a back press should deselect all images
     */
    @Test
    fun testInvokeBackOnLongPressPoems() {
        myImagesViewModel.setSelection(CurrentSelection.POEMS)
        myImagesViewModel.setOnLongClick(true)
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists())

        for (map in myImagesViewModel.savedPoemImages)
            myImagesViewModel.savedPoemImages[map.key] = true

        assert(myImagesViewModel.onImageLongPressed)

        for (map in myImagesViewModel.savedPoemImages)
            assert(myImagesViewModel.savedPoemImages[map.key] == true)

        //immitate back press
        myImagesViewModel.setOnLongClick(false)
        myImagesViewModel.resetSelectedImages()

        for (map in myImagesViewModel.savedPoemImages)
            assert(myImagesViewModel.savedPoemImages[map.key] == false)
    }

    @Test
    fun testCopyToLocalFolder(){
        val toCopyFrom = File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/folder_to_copy_from")
        var copy1Size = 0L
        var copy2Size = 0L
        var copy3Size = 0L
        for (file in toCopyFrom.listFiles()!!){
            myImagesViewModel.copyToLocalFolder(file.absolutePath, mockContext)
            if (file.name == "copy1.png")
                copy1Size = file.totalSpace
            else if (file.name == "copy2.png")
                copy2Size = file.totalSpace
            else
                copy3Size = file.totalSpace
        }

        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/copy1.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/copy1.png").totalSpace == copy1Size)
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/copy2.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/copy2.png").totalSpace == copy2Size)
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/copy3.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_images/copy3.png").totalSpace == copy3Size)
    }
}