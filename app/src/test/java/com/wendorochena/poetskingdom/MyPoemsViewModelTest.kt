package com.wendorochena.poetskingdom

import android.content.Context
import android.content.SharedPreferences
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.viewModels.MyPoemsViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File

class MyPoemsViewModelTest {

    private var myPoemsViewModel = MyPoemsViewModel()
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
            getString(R.string.saved_images_folder_name)
        } doReturn ("savedImages")

        on {
            this.getDir(
                "poems",
                Context.MODE_PRIVATE
            )
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder")

        on {
            this.getDir("thumbnails", Context.MODE_PRIVATE)
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails")

        on {
            this.getDir("savedImages", Context.MODE_PRIVATE)
        } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images")
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

        val file =
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dummyFolder")
        val rename =
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dummyFolderRename")
        if (file.exists())
            file.delete()
        if (rename.exists())
            rename.delete()
        myPoemsViewModel = MyPoemsViewModel()
        myPoemsViewModel.getThumbnails(mockContext, myPoemsViewModel.allPoemsString)
    }

    @After
    fun reAddFiles() {
        //re-add poem files
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


        //re-add directories
        if (!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists()) {
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").mkdir()
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").mkdir()
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").mkdir()
        }
    }

    @Test
    fun testAddAlbum() {
        assert(myPoemsViewModel.addAlbumName("dummyFolder", mockContext))
    }

    /**
     * Selecting an album whilst on long pressed is enabled should deselect images and turn off long
     * press
     */
    @Test
    fun testAlbumSelectionWithOnLongClick() {
        myPoemsViewModel.setOnLongClick(true)
        myPoemsViewModel.setAlbumSelection("album1")
        assert(myPoemsViewModel.albumNameSelection == "album1")
        assert(!myPoemsViewModel.onImageLongPressed)
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
     * Tests that poems only in albums are displayed
     */
    @Test
    fun testDisplayAlbumThumbnails() {
        myPoemsViewModel.allSavedPoems.clear()
        //copy files to album
        assert(
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml")
            ).exists()
        )
        assert(
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/Boundary_Test_1.xml")
            ).exists()
        )
        assert(
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/dfsdfsdf.xml")
            ).exists()
        )
        //delete main folder files
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").delete())

        assert(
            myPoemsViewModel.getThumbnails(
                mockContext,
                myPoemsViewModel.allPoemsString
            ).size == 3
        )

        assert(myPoemsViewModel.savedPoemAndAlbum["HELLO"] == "album1")
        assert(myPoemsViewModel.savedPoemAndAlbum["Boundary Test 1"] == "album1")
        assert(myPoemsViewModel.savedPoemAndAlbum["dfsdfsdf"] == "album1")

        //delete album folder files
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/dfsdfsdf.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/Boundary_Test_1.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml").delete())
    }

    /**
     * Moves HELLO Poem into album1 and verifies state and then returns it back into all saved poems
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

    /**
     * Tests that deleting a poem does not delete a thumbnail since there is a saved poem
     */
    @Test
    fun testDeletePoemsButKeepThumbnail() {
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
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png").exists())

        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").exists())
        assert(myPoemsViewModel.allSavedPoems.size == 3)
        myPoemsViewModel.setOnLongClick(true)
        assert(myPoemsViewModel.onImageLongPressed)
        for (map in myPoemsViewModel.allSavedPoems)
            myPoemsViewModel.allSavedPoems[map.key] = true

        myPoemsViewModel.deleteSavedPoems(mockContext)

        assert(myPoemsViewModel.allSavedPoems[hello] == null)
        assert(myPoemsViewModel.allSavedPoems[dfsdfsdf] == null)
        assert(myPoemsViewModel.allSavedPoems[boundaryTest1] == null)

        //poems deleted
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").exists())

        //saved images are there
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists())
        //thumbnails not deleted
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png").exists())

        assert(!myPoemsViewModel.onImageLongPressed)
    }

    /**
     * Tests that deleting a poem from an album does not delete a thumbnail since there is a saved poem
     */
    @Test
    fun testDeleteAlbumPoemsButKeepThumbnail() {
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
        myPoemsViewModel.setAlbumSelection("album1")
        assert(myPoemsViewModel.albumNameSelection == "album1")
        assert(
            myPoemsViewModel.getThumbnails(
                mockContext,
                myPoemsViewModel.albumNameSelection
            ).size == 3
        )
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png").exists())

        assert(myPoemsViewModel.albumSavedPoems.size == 3)
        myPoemsViewModel.setOnLongClick(true)
        assert(myPoemsViewModel.onImageLongPressed)
        for (map in myPoemsViewModel.albumSavedPoems)
            myPoemsViewModel.albumSavedPoems[map.key] = true

        myPoemsViewModel.deleteSavedPoems(mockContext)

        assert(myPoemsViewModel.allSavedPoems[hello] == null)
        assert(myPoemsViewModel.allSavedPoems[dfsdfsdf] == null)
        assert(myPoemsViewModel.allSavedPoems[boundaryTest1] == null)

        assert(myPoemsViewModel.albumSavedPoems[hello] == null)
        assert(myPoemsViewModel.albumSavedPoems[dfsdfsdf] == null)
        assert(myPoemsViewModel.albumSavedPoems[boundaryTest1] == null)

        //poems deleted
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/dfsdfsdf.xml").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/Boundary_Test_1.xml").exists())

        //saved images are there
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists())
        //thumbnails not deleted
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png").exists())

        assert(!myPoemsViewModel.onImageLongPressed)
    }

    /**
     * Tests that deleting a poem with no saved images also deletes the thumbnail
     */
    @Test
    fun testDeletePoemsButDeletesThumbnail() {
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
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png").exists())

        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").exists())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").exists())

        //saved images are not there
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").delete())

        assert(myPoemsViewModel.allSavedPoems.size == 3)
        myPoemsViewModel.setOnLongClick(true)
        assert(myPoemsViewModel.onImageLongPressed)
        for (map in myPoemsViewModel.allSavedPoems)
            myPoemsViewModel.allSavedPoems[map.key] = true

        myPoemsViewModel.deleteSavedPoems(mockContext)

        assert(myPoemsViewModel.allSavedPoems[hello] == null)
        assert(myPoemsViewModel.allSavedPoems[dfsdfsdf] == null)
        assert(myPoemsViewModel.allSavedPoems[boundaryTest1] == null)

        //poems deleted
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").exists())

        //saved images are not there
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/Boundary_Test_1").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/dfsdfsdf").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/saved_images/HELLO").exists())
        //thumbnails deleted
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/HELLO.png").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/dfsdfsdf.png").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails/Boundary_Test_1.png").exists())

        assert(!myPoemsViewModel.onImageLongPressed)
    }

    /**
     * Tests that clearing search options should work as expected
     */
    @Test
    fun testOnBackSearchPressed() {
        myPoemsViewModel.searchButtonClicked = true
        myPoemsViewModel.hitsFound = true
        myPoemsViewModel.substringLocations.add(Pair("dummy", "dummy"))
        myPoemsViewModel.poemBackgroundTypeArrayList.add(Pair(BackgroundType.DEFAULT, 1))
        myPoemsViewModel.searchResultFiles.add(File("sdjhfksdjf"))

        assert(myPoemsViewModel.substringLocations.isNotEmpty())
        assert(myPoemsViewModel.poemBackgroundTypeArrayList.isNotEmpty())
        assert(myPoemsViewModel.searchResultFiles.isNotEmpty())

        myPoemsViewModel.clearSearchOptions()

        assert(myPoemsViewModel.substringLocations.isEmpty())
        assert(myPoemsViewModel.poemBackgroundTypeArrayList.isEmpty())
        assert(myPoemsViewModel.searchResultFiles.isEmpty())
        assert(!myPoemsViewModel.searchButtonClicked)
        assert(!myPoemsViewModel.hitsFound)
    }

    /**
     * Test that the correct poem file is returned
     */
    @Test
    fun testGetPoemFiles() {
        val file = File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails")
        var poemName: String
        val poemsFolder = mockContext.getDir("poems", Context.MODE_PRIVATE)
        for (thumbnail in file.listFiles()!!) {
            poemName = thumbnail.name.split(".")[0]
            assert(
                myPoemsViewModel.getPoemsFile(
                    thumbnail,
                    mockContext
                ).path == poemsFolder.absolutePath + File.separator + "$poemName.xml"
            )
        }
    }

    /**
     * Tests getting a file from an album yields correct result
     */
    @Test
    fun testGetPoemFilesAlbum() {
        val file = File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/thumbnails")
        var poemName: String
        val poemsFolder = mockContext.getDir("poems", Context.MODE_PRIVATE)

        myPoemsViewModel.allSavedPoems.clear()
        //copy files to album
        assert(
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml")
            ).exists()
        )
        assert(
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/Boundary_Test_1.xml")
            ).exists()
        )
        assert(
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/dfsdfsdf.xml")
            ).exists()
        )
        //delete main folder files
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").delete())

        assert(
            myPoemsViewModel.getThumbnails(
                mockContext,
                myPoemsViewModel.allPoemsString
            ).size == 3
        )

        assert(myPoemsViewModel.savedPoemAndAlbum["HELLO"] == "album1")
        assert(myPoemsViewModel.savedPoemAndAlbum["Boundary Test 1"] == "album1")
        assert(myPoemsViewModel.savedPoemAndAlbum["dfsdfsdf"] == "album1")

        for (thumbnail in file.listFiles()!!) {
            poemName = thumbnail.name.split(".")[0]
            assert(
                myPoemsViewModel.getPoemsFile(
                    thumbnail,
                    mockContext
                ).path == poemsFolder.absolutePath + File.separator + "album1" + File.separator + "$poemName.xml"
            )
        }

        //delete album folder files
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/dfsdfsdf.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/Boundary_Test_1.xml").delete())
        assert(File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/album1/HELLO.xml").delete())
    }

    /**
     * Tests multiple renames with allowed and not allowed strings
     */
    @Test
    fun testRenameAlbum() {
        //add album
        assert(myPoemsViewModel.addAlbumName("dummyFolder", mockContext))
        assert(!myPoemsViewModel.renameAlbum("dummyFolder", "dummyFolder", mockContext))
        assert(myPoemsViewModel.renameAlbum("dummyFolder", "dummyFolderRename", mockContext))
        assert(!myPoemsViewModel.renameAlbum("dummyFolderRename", "dummyFolderRename", mockContext))
        assert(
            !myPoemsViewModel.renameAlbum(
                "dummyFolderRenamesdfsadsa",
                "dummyFolderRename",
                mockContext
            )
        )
        assert(myPoemsViewModel.renameAlbum("dummyFolderRename", "dummyFolderRename2", mockContext))
        assert(
            myPoemsViewModel.renameAlbum(
                "dummyFolderRename2",
                "dummyFolderRename2 12 2392",
                mockContext
            )
        )
        assert(myPoemsViewModel.renameAlbum("dummyFolderRename2 12 2392", "p", mockContext))
        assert(!myPoemsViewModel.renameAlbum("p", "All Poems", mockContext))
        assert(myPoemsViewModel.renameAlbum("p", "dummyFolderRename", mockContext))
    }

    @Test
    fun testAlbumDelete() {
        assert(myPoemsViewModel.addAlbumName("dummyFolder", mockContext))
        assert(myPoemsViewModel.deleteAlbum(myPoemsViewModel.allPoemsString, mockContext))
        assert(!myPoemsViewModel.deleteAlbum("album does not exist", mockContext))
        assert(myPoemsViewModel.deleteAlbum("dummyFolder", mockContext))
        assert(myPoemsViewModel.addAlbumName("dummy Folder with spaces and 1 2 3", mockContext))
        assert(myPoemsViewModel.deleteAlbum("dummy Folder with spaces and 1 2 3", mockContext))

        assert(myPoemsViewModel.addAlbumName("dummyFolder", mockContext))

        //copy files to album
        assert(
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/HELLO.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dummyFolder/HELLO.xml")
            ).exists()
        )
        assert(
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dfsdfsdf.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dummyFolder/Boundary_Test_1.xml")
            ).exists()
        )
        assert(
            File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/Boundary_Test_1.xml").copyTo(
                File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dummyFolder/dfsdfsdf.xml")
            ).exists()
        )

        assert(myPoemsViewModel.deleteAlbum("dummyFolder", mockContext))

        //delete album folder files
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dummyFolder/dfsdfsdf.xml").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dummyFolder/Boundary_Test_1.xml").exists())
        assert(!File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/my_poems_folder/dummyFolder/HELLO.xml").exists())
    }

    @Test
    fun testReselectImages() {
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

        myPoemsViewModel.resetSelectedImages()

        assert(myPoemsViewModel.allSavedPoems[hello] == false)
        assert(myPoemsViewModel.allSavedPoems[boundaryTest1] == false)
        assert(myPoemsViewModel.allSavedPoems[dfsdfsdf] == false)
    }


    @Test
    fun testUpdateSearchHistory() {
        myPoemsViewModel.updateSearchHistory("1")
        myPoemsViewModel.updateSearchHistory("2")
        myPoemsViewModel.updateSearchHistory("3")
        myPoemsViewModel.updateSearchHistory("4")
        myPoemsViewModel.updateSearchHistory("5")
        myPoemsViewModel.updateSearchHistory("6")
        myPoemsViewModel.updateSearchHistory("7")
        myPoemsViewModel.updateSearchHistory("8")
        myPoemsViewModel.updateSearchHistory("9")
        myPoemsViewModel.updateSearchHistory("10")
        myPoemsViewModel.updateSearchHistory("11")

        assert(myPoemsViewModel.getSearchHistory(mockContext).size == 10)
        assert(myPoemsViewModel.getSearchHistory(mockContext)[0] == "2")
        assert(myPoemsViewModel.getSearchHistory(mockContext)[1] == "3")
        assert(myPoemsViewModel.getSearchHistory(mockContext)[2] == "4")
        assert(myPoemsViewModel.getSearchHistory(mockContext)[3] == "5")
        assert(myPoemsViewModel.getSearchHistory(mockContext)[4] == "6")
        assert(myPoemsViewModel.getSearchHistory(mockContext)[5] == "7")
        assert(myPoemsViewModel.getSearchHistory(mockContext)[6] == "8")
        assert(myPoemsViewModel.getSearchHistory(mockContext)[7] == "9")
        assert(myPoemsViewModel.getSearchHistory(mockContext)[8] == "10")
        assert(myPoemsViewModel.getSearchHistory(mockContext)[9] == "11")
        assert(myPoemsViewModel.getSearchHistory(mockContext).size == 10)
        myPoemsViewModel.getSearchHistory(mockContext).clear()
        myPoemsViewModel.updateSearchHistory("1")
        myPoemsViewModel.updateSearchHistory("2")

        assert(myPoemsViewModel.getSearchHistory(mockContext)[0] == "1")
        assert(myPoemsViewModel.getSearchHistory(mockContext)[1] == "2")
        assert(myPoemsViewModel.getSearchHistory(mockContext).size == 2)
    }
}