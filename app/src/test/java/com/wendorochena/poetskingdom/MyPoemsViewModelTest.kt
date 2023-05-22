package com.wendorochena.poetskingdom

import android.content.Context
import android.content.SharedPreferences
import com.wendorochena.poetskingdom.viewModels.MyPoemsViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class MyPoemsViewModelTest {

    private val myPoemsViewModel = MyPoemsViewModel()
    private val sharedPreferences = Mockito.mock(SharedPreferences::class.java)
    @Mock
    val mockContext: Context = mock {
        on {
            getString(R.string.poems_folder_name)
        } doReturn ("poems")
        on {
            getString(R.string.thumbnails_folder_name)
        } doReturn ("thumbnails")
        on {
            getDir(
                getString(R.string.poems_folder_name),
                Context.MODE_PRIVATE
            )
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder")

        on {
            this.getDir("poemThemes", Context.MODE_PRIVATE)
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/themes")

        on {
            this.getDir("thumbnails", Context.MODE_PRIVATE)
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails")
    }

    @Before
    fun setupFiles() {
        Mockito.`when`(mockContext.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE))
            .thenReturn(sharedPreferences)
        Mockito.`when`(
            mockContext.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE).edit()
        ).thenReturn(Mockito.mock(SharedPreferences.Editor::class.java))
        Mockito.`when`(
            mockContext.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE).edit()
                .putStringSet("albums", HashSet())
        ).thenReturn(Mockito.mock(SharedPreferences.Editor::class.java))

        val file = File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dummyFolder")
        if (file.exists())
            file.delete()

        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.allPoemsString)
    }

    @Test
    fun testAddAlbum() {
        assert(myPoemsViewModel.addAlbumName("dummyFolder", mockContext))
    }

    /**
     * Three poems with corresponding thumbnails are originally located in the folder to test
     */
    @Test
    fun testCorrectDisplayOfThumbnails() {
        assert(myPoemsViewModel.allSavedPoems.size == 3)
        //no poems in any albums
        assert(myPoemsViewModel.savedPoemAndAlbum.size == 0)
    }

    /**
     * Moves HELLO Poem into album1 and verifies state and then returns it back into all saved poems
     *
     *
     */
    @Test
    fun testMoveHELLOIntoAlbum1() {
        var file =
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png")
        for (files in myPoemsViewModel.allSavedPoems) {
            if (files.key.name == "HELLO.png") {
                file = files.key
                break
            }
        }
        assert(myPoemsViewModel.allSavedPoems.size == 3)
        assert(myPoemsViewModel.allSavedPoems[file] != null)
        assert(myPoemsViewModel.albumNameSelection == myPoemsViewModel.allPoemsString)
        myPoemsViewModel.allSavedPoems[file] = true
        assert(myPoemsViewModel.addPoemToAlbum(mockContext, "album1"))
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml").exists())
        assert(myPoemsViewModel.allSavedPoems[file] == false)
        myPoemsViewModel.setAlbumSelection("album1")
        assert(myPoemsViewModel.albumNameSelection == "album1")
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.albumNameSelection)
        assert(myPoemsViewModel.savedPoemAndAlbum[file.name.split(".")[0]] == "album1")
        assert(myPoemsViewModel.albumSavedPoems.size == 1)
        myPoemsViewModel.albumSavedPoems[file] = true
        assert(myPoemsViewModel.addPoemToAlbum(mockContext, myPoemsViewModel.allPoemsString))
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").exists())
        assert(myPoemsViewModel.albumSavedPoems[file] == false)
        assert(myPoemsViewModel.savedPoemAndAlbum[file.name.split(".")[0]] == null)
        myPoemsViewModel.setAlbumSelection(myPoemsViewModel.allPoemsString)
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.albumNameSelection)
        assert(myPoemsViewModel.allSavedPoems.size == 3)
        for (files in myPoemsViewModel.allSavedPoems) {
            assert(!files.value)
        }
    }

    /**
     * Moves HELLO poem into album1 then album2 then album3 then back into the main directory
     */
    @Test
    fun testMoveHELLOIntoMultipleAlbums() {
        var file =
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png")
        for (files in myPoemsViewModel.allSavedPoems) {
            if (files.key.name == "HELLO.png") {
                file = files.key
                break
            }
        }
        assert(myPoemsViewModel.allSavedPoems.size == 3)
        assert(myPoemsViewModel.allSavedPoems[file] != null)
        assert(myPoemsViewModel.albumNameSelection == myPoemsViewModel.allPoemsString)
        myPoemsViewModel.allSavedPoems[file] = true
        assert(myPoemsViewModel.addPoemToAlbum(mockContext, "album1"))
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml").exists())
        assert(myPoemsViewModel.allSavedPoems[file] == false)
        myPoemsViewModel.setAlbumSelection("album1")
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.albumNameSelection)
        assert(myPoemsViewModel.savedPoemAndAlbum[file.name.split(".")[0]] == "album1")
        assert(myPoemsViewModel.albumSavedPoems[file] == false)
        assert(myPoemsViewModel.albumSavedPoems.size == 1)
        myPoemsViewModel.albumSavedPoems[file] = true
        assert(myPoemsViewModel.addPoemToAlbum(mockContext, "album2"))
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album2/HELLO.xml").exists())
        myPoemsViewModel.setAlbumSelection("album2")
        assert(myPoemsViewModel.albumNameSelection == "album2")
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.albumNameSelection)
        assert(myPoemsViewModel.savedPoemAndAlbum[file.name.split(".")[0]] == "album2")
        assert(myPoemsViewModel.albumSavedPoems[file] == false)
        assert(myPoemsViewModel.albumSavedPoems.size == 1)
        myPoemsViewModel.albumSavedPoems[file] = true
        assert(myPoemsViewModel.addPoemToAlbum(mockContext, "album3"))
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album3/HELLO.xml").exists())
        myPoemsViewModel.setAlbumSelection("album3")
        assert(myPoemsViewModel.albumNameSelection == "album3")
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.albumNameSelection)
        assert(myPoemsViewModel.savedPoemAndAlbum[file.name.split(".")[0]] == "album3")
        assert(myPoemsViewModel.albumSavedPoems[file] == false)
        assert(myPoemsViewModel.albumSavedPoems.size == 1)
        myPoemsViewModel.albumSavedPoems[file] = true

        assert(myPoemsViewModel.addPoemToAlbum(mockContext, myPoemsViewModel.allPoemsString))
        assert(myPoemsViewModel.albumSavedPoems[file] == false)
        assert(myPoemsViewModel.savedPoemAndAlbum[file.name.split(".")[0]] == null)
        myPoemsViewModel.setAlbumSelection(myPoemsViewModel.allPoemsString)
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.albumNameSelection)
        assert(myPoemsViewModel.allSavedPoems.size == 3)
        for (files in myPoemsViewModel.allSavedPoems) {
            assert(!files.value)
        }
    }

    /**
     * Moves three poems into album1 then album2 then album3 then back into the main directory
     */
    @Test
    fun moveAllPoemsIntoMultipleAlbums() {
        var hello =
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png")
        var dfsdfsdf =
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png")
        var boundaryTest1 =
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png")
        for (files in myPoemsViewModel.allSavedPoems) {
            when (files.key.name) {
                "HELLO.png" -> {
                    hello = files.key
                }

                "dfsdfsdf.png" -> {
                    dfsdfsdf = files.key
                }

                "Boundary_Test_1.png" -> {
                    boundaryTest1 = files.key
                }
            }
        }
        assert(myPoemsViewModel.allSavedPoems.size == 3)
        assert(myPoemsViewModel.allSavedPoems[hello] != null)
        assert(myPoemsViewModel.allSavedPoems[dfsdfsdf] != null)
        assert(myPoemsViewModel.allSavedPoems[boundaryTest1] != null)
        assert(myPoemsViewModel.albumNameSelection == myPoemsViewModel.allPoemsString)
        myPoemsViewModel.allSavedPoems[hello] = true
        myPoemsViewModel.allSavedPoems[boundaryTest1] = true
        myPoemsViewModel.allSavedPoems[dfsdfsdf] = true
        assert(myPoemsViewModel.addPoemToAlbum(mockContext, "album1"))
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/dfsdfsdf.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/Boundary_Test_1.xml").exists())
        assert(myPoemsViewModel.allSavedPoems[hello] == false)
        assert(myPoemsViewModel.allSavedPoems[dfsdfsdf] == false)
        assert(myPoemsViewModel.allSavedPoems[boundaryTest1] == false)
        myPoemsViewModel.setAlbumSelection("album1")
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.albumNameSelection)
        assert(myPoemsViewModel.savedPoemAndAlbum[hello.name.split(".")[0]] == "album1")
        assert(myPoemsViewModel.savedPoemAndAlbum[dfsdfsdf.name.split(".")[0]] == "album1")
        assert(
            myPoemsViewModel.savedPoemAndAlbum[boundaryTest1.name.split(".")[0].replace(
                '_',
                ' '
            )] == "album1"
        )
        assert(myPoemsViewModel.albumSavedPoems[hello] == false)
        assert(myPoemsViewModel.albumSavedPoems[boundaryTest1] == false)
        assert(myPoemsViewModel.albumSavedPoems[dfsdfsdf] == false)
        assert(myPoemsViewModel.albumSavedPoems.size == 3)
        myPoemsViewModel.albumSavedPoems[hello] = true
        myPoemsViewModel.albumSavedPoems[boundaryTest1] = true
        myPoemsViewModel.albumSavedPoems[dfsdfsdf] = true
        assert(myPoemsViewModel.addPoemToAlbum(mockContext, "album2"))
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album2/HELLO.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album2/dfsdfsdf.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album2/Boundary_Test_1.xml").exists())
        myPoemsViewModel.setAlbumSelection("album2")
        assert(myPoemsViewModel.albumNameSelection == "album2")
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.albumNameSelection)
        assert(myPoemsViewModel.savedPoemAndAlbum[hello.name.split(".")[0]] == "album2")
        assert(myPoemsViewModel.savedPoemAndAlbum[dfsdfsdf.name.split(".")[0]] == "album2")
        assert(
            myPoemsViewModel.savedPoemAndAlbum[boundaryTest1.name.split(".")[0].replace(
                '_',
                ' '
            )] == "album2"
        )
        assert(myPoemsViewModel.albumSavedPoems[hello] == false)
        assert(myPoemsViewModel.albumSavedPoems[boundaryTest1] == false)
        assert(myPoemsViewModel.albumSavedPoems[dfsdfsdf] == false)
        assert(myPoemsViewModel.albumSavedPoems.size == 3)
        myPoemsViewModel.albumSavedPoems[hello] = true
        myPoemsViewModel.albumSavedPoems[boundaryTest1] = true
        myPoemsViewModel.albumSavedPoems[dfsdfsdf] = true
        assert(myPoemsViewModel.addPoemToAlbum(mockContext, "album3"))
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album3/HELLO.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album3/dfsdfsdf.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album3/Boundary_Test_1.xml").exists())
        myPoemsViewModel.setAlbumSelection("album3")
        assert(myPoemsViewModel.albumNameSelection == "album3")
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.albumNameSelection)
        assert(myPoemsViewModel.savedPoemAndAlbum[hello.name.split(".")[0]] == "album3")
        assert(myPoemsViewModel.savedPoemAndAlbum[dfsdfsdf.name.split(".")[0]] == "album3")
        assert(
            myPoemsViewModel.savedPoemAndAlbum[boundaryTest1.name.split(".")[0].replace(
                '_',
                ' '
            )] == "album3"
        )
        assert(myPoemsViewModel.albumSavedPoems[hello] == false)
        assert(myPoemsViewModel.albumSavedPoems[boundaryTest1] == false)
        assert(myPoemsViewModel.albumSavedPoems[dfsdfsdf] == false)
        assert(myPoemsViewModel.albumSavedPoems.size == 3)
        myPoemsViewModel.albumSavedPoems[hello] = true
        myPoemsViewModel.albumSavedPoems[boundaryTest1] = true
        myPoemsViewModel.albumSavedPoems[dfsdfsdf] = true

        assert(myPoemsViewModel.addPoemToAlbum(mockContext, myPoemsViewModel.allPoemsString))
        assert(myPoemsViewModel.albumSavedPoems[hello] == false)
        assert(myPoemsViewModel.savedPoemAndAlbum[hello.name.split(".")[0]] == null)
        myPoemsViewModel.setAlbumSelection(myPoemsViewModel.allPoemsString)
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.albumNameSelection)
        assert(myPoemsViewModel.allSavedPoems.size == 3)
        for (files in myPoemsViewModel.allSavedPoems) {
            assert(!files.value)
        }
    }

    /**
     * Moves three poems into album1 and then returns to all poems album selection and moves the poems
     * back into all poems
     */
    @Test
    fun testMoveToAllPoemsFromAllPoems() {
        //get the File memory references to use in this context
        var hello =
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png")
        var dfsdfsdf =
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png")
        var boundaryTest1 =
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png")
        for (files in myPoemsViewModel.allSavedPoems) {
            when (files.key.name) {
                "HELLO.png" -> {
                    hello = files.key
                }

                "dfsdfsdf.png" -> {
                    dfsdfsdf = files.key
                }

                "Boundary_Test_1.png" -> {
                    boundaryTest1 = files.key
                }
            }
        }
        //verify correctness of whats to be displayed
        assert(myPoemsViewModel.allSavedPoems.size == 3)
        assert(myPoemsViewModel.allSavedPoems[hello] != null)
        assert(myPoemsViewModel.allSavedPoems[dfsdfsdf] != null)
        assert(myPoemsViewModel.allSavedPoems[boundaryTest1] != null)
        assert(myPoemsViewModel.albumNameSelection == myPoemsViewModel.allPoemsString)
        //select three poems
        myPoemsViewModel.allSavedPoems[hello] = true
        myPoemsViewModel.allSavedPoems[boundaryTest1] = true
        myPoemsViewModel.allSavedPoems[dfsdfsdf] = true
        assert(myPoemsViewModel.addPoemToAlbum(mockContext, "album1"))
        assert(myPoemsViewModel.allSavedPoems[hello] == false)
        assert(myPoemsViewModel.allSavedPoems[dfsdfsdf] == false)
        assert(myPoemsViewModel.allSavedPoems[boundaryTest1] == false)
        assert(myPoemsViewModel.savedPoemAndAlbum[hello.name.split(".")[0]] == "album1")
        assert(myPoemsViewModel.savedPoemAndAlbum[dfsdfsdf.name.split(".")[0]] == "album1")
        assert(
            myPoemsViewModel.savedPoemAndAlbum[boundaryTest1.name.split(".")[0].replace(
                '_',
                ' '
            )] == "album1"
        )
        assert(myPoemsViewModel.albumSavedPoems.isEmpty())

        //copy back to all poems from all poems
        assert(myPoemsViewModel.albumNameSelection == myPoemsViewModel.allPoemsString)
        myPoemsViewModel.allSavedPoems[hello] = true
        myPoemsViewModel.allSavedPoems[boundaryTest1] = true
        myPoemsViewModel.allSavedPoems[dfsdfsdf] = true
        assert(myPoemsViewModel.albumSavedPoems.isEmpty())
        assert(myPoemsViewModel.addPoemToAlbum(mockContext, myPoemsViewModel.allPoemsString))
        assert(myPoemsViewModel.savedPoemAndAlbum[hello.name.split(".")[0]] == null)
        assert(myPoemsViewModel.savedPoemAndAlbum[dfsdfsdf.name.split(".")[0]] == null)
        assert(
            myPoemsViewModel.savedPoemAndAlbum[boundaryTest1.name.split(".")[0].replace(
                '_',
                ' '
            )] == null
        )
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").exists())
    }
}