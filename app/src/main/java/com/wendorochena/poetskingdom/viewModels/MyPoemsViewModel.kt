package com.wendorochena.poetskingdom.viewModels

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.content.FileProvider
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.utils.SearchUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Collections

class MyPoemsViewModel : ViewModel() {

    val allPoemsString = "All Poems"
    var albumNameSelection by mutableStateOf(allPoemsString)
    var allSavedPoems = mutableStateMapOf<File, Boolean>()
        private set
    var albumSavedPoems = mutableStateMapOf<File, Boolean>()
        private set
    var onImageLongPressed by mutableStateOf(false)
        private set
    var permissionsResultLauncher = MutableLiveData<String>()
    var shareIntent = MutableLiveData<Intent>()
    var searchButtonClicked by mutableStateOf(false)
    var displayNoResultsFound by mutableStateOf(false)
    var displayAlbumsDialog by mutableStateOf(false)
    var shouldRenameAlbum by mutableStateOf(false)
    var displayAlbumSelector by mutableStateOf(false)
    var albumSaveResult = -2
    var hitsFound by mutableStateOf(false)
    var oldAlbumName = ""
    var stanzaIndexAndText = HashMap<String, ArrayList<Pair<Int, String>>>()
    val poemBackgroundTypeArrayList = mutableStateListOf<Pair<BackgroundType, Int>>()
    val substringLocations = mutableStateListOf<Pair<String, String>>()
    var searchResultFiles = mutableListOf<File>()
    private val searchHistory = mutableStateListOf<String>()
    private var initialisedSearchHistory = false
    private val albumFolderNames = mutableStateListOf<String>()

    // poem name as key and album as value
    val savedPoemAndAlbum = HashMap<String, String>()
    val searchResultIndexToUse = HashMap<String, Int>()


    /**
     * Sets album selection to the selected user album and clears state if state was previously
     * invoked
     *
     * @param albumName the name of the album to set as selected
     */
    fun setAlbumSelection(albumName: String) {
        if (onImageLongPressed) {
            onImageLongPressed = false
            resetSelectedImages()
        }

        albumNameSelection = albumName
        if (albumSavedPoems.isNotEmpty())
            albumSavedPoems.clear()
    }

    /**
     * Adds all files and sets to long press as false to to the savedPoems Map
     * @param arrayList the arraylist containing files in a map
     * @param isAlbumAdd true when adding poems to an album
     */
    private fun addAllFiles(
        arrayList: ArrayList<File>,
        isAlbumAdd: Boolean
    ) {
        for (file in arrayList) {
            if (isAlbumAdd)
                albumSavedPoems[file] = false
            else
                allSavedPoems[file] = false
        }
    }

    /**
     * Clears Search View options
     */
    fun clearSearchOptions() {
        searchButtonClicked = false
        displayNoResultsFound = false
        hitsFound = false
        stanzaIndexAndText.clear()
        poemBackgroundTypeArrayList.clear()
        substringLocations.clear()
        searchResultFiles.clear()
    }

    /**
     * Sets long click to boolean value
     * @param boolean true if user long pressed else false
     */
    fun setOnLongClick(boolean: Boolean) {
        onImageLongPressed = boolean
    }

    /**
     * Gets an arraylist containing thumbnails to display
     * @param context the activity context
     * @param albumName the name of the album to get thumbnails for
     * @return a map containing file locations of thumbnails and their long click value
     */
    fun getThumbnails(context: Context, albumName: String): MutableMap<File, Boolean> {
        val arrayListToRet = ArrayList<File>()
        val encodedAlbumName = albumName.replace(' ', '_')
        val mapToUse = if (albumName == allPoemsString)
            allSavedPoems
        else
            albumSavedPoems

        if (mapToUse.isEmpty()) {
            var savedPoemsFolder =
                context.getDir(
                    context.getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )
            if (albumName != allPoemsString)
                savedPoemsFolder =
                    File(savedPoemsFolder.absolutePath + File.separator + encodedAlbumName)

            val thumbnailsFolder =
                context.getDir(
                    context.getString(R.string.thumbnails_folder_name),
                    Context.MODE_PRIVATE
                )
            try {
                val savedPoemsFiles = savedPoemsFolder.listFiles()?.toMutableList()
                if (savedPoemsFiles != null) {
                    for (file in savedPoemsFiles) {
                        if (file.isDirectory) {
                            val allFiles = file.listFiles()
                            if (allFiles != null) {
                                for (albumFile in allFiles) {
                                    val albumFileName = albumFile.name.split(".")[0]
                                    savedPoemAndAlbum[albumFileName.replace(
                                        '_',
                                        ' '
                                    )] = file.name.replace('_', ' ')
                                    val thumbnailFile = File(
                                        thumbnailsFolder?.absolutePath + File.separator + albumFileName + ".png"
                                    )
                                    if (thumbnailFile.exists()) {
                                        arrayListToRet.add(File(thumbnailFile.absolutePath))
                                    }
                                }
                            }
                        }
                        if (file.isFile) {
                            val thumbnailFile = File(
                                thumbnailsFolder?.absolutePath + File.separator + file.name.split(".")[0] + ".png"
                            )
                            if (thumbnailFile.exists()) {
                                arrayListToRet.add(File(thumbnailFile.absolutePath))
                            }
                        }
                    }
                }
            } catch (exception: Exception) {
            }
            addAllFiles(arrayListToRet, albumName != allPoemsString)
        }
        return mapToUse
    }


    /**
     * Deletes saved poem images
     *
     * @param context application context
     */
    fun deleteSavedPoems(context: Context) {
        val mapToUse = if (albumNameSelection == allPoemsString)
            allSavedPoems
        else
            albumSavedPoems
        val savedImagesFolder = context.getDir(
            context.getString(R.string.saved_images_folder_name),
            Context.MODE_PRIVATE
        )
        val filesToDelete = mapToUse.filter { it.value }
        for (entry in filesToDelete) {
            try {
                val file = entry.key
                val poemName = file.name
                val decodedPoemName = poemName.split(".")[0].replace('_', ' ')
                val encodedAlbumName = savedPoemAndAlbum[decodedPoemName]
                val savedPoemsPath = context.getDir(
                    context.getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )
                val fullPoemPathToDelete = if (encodedAlbumName != null)
                    File(
                        savedPoemsPath.absolutePath + File.separator + encodedAlbumName + File.separator + poemName.replace(
                            ".png",
                            ".xml"
                        )
                    )
                else
                    File(
                        savedPoemsPath.absolutePath + File.separator + poemName.replace(
                            ".png",
                            ".xml"
                        )
                    )
                val fullSavedImagesPathToDelete =
                    File(savedImagesFolder.absolutePath + File.separator + poemName.split(".")[0])

                if (fullPoemPathToDelete.exists())
                    if (fullPoemPathToDelete.deleteRecursively()) {
                        if (albumNameSelection != allPoemsString)
                            allSavedPoems.remove(file)
                        mapToUse.remove(file)
                        if (!fullSavedImagesPathToDelete.exists())
                            file.delete()
                    }
            } catch (e: Exception) {
            }
        }
        onImageLongPressed = false
    }

    /**
     * Shares images outside the application
     *
     * @param applicationContext context of the application
     */
    fun shareIntent(applicationContext: Context, poemToShare: File?): ArrayList<Uri>? {
        val mapToUse = if (albumNameSelection == allPoemsString)
            allSavedPoems
        else
            albumSavedPoems
        val selectedElements = mapToUse.filter { it.value }
        if (selectedElements.size > 1) {
            Toast.makeText(applicationContext, R.string.share_as_image_toast, Toast.LENGTH_LONG)
                .show()
        } else {
            val savedImagesFolder =
                applicationContext.getDir(
                    applicationContext.getString(R.string.saved_images_folder_name),
                    AppCompatActivity.MODE_PRIVATE
                )
            val poemName = if (poemToShare == null)
                selectedElements.keys.first().name.split(".")[0].replace(' ', '_')
            else
                poemToShare.name.split(".")[0]

            val poemSavedImagesFolder =
                File(savedImagesFolder.absolutePath + File.separator + poemName)

            if (poemSavedImagesFolder.exists()) {
                val filesToShare = poemSavedImagesFolder.listFiles()
                if (filesToShare != null && filesToShare.isNotEmpty()) {
                    var toShare = if (poemToShare == null)
                        arrayOf(selectedElements.keys.first())
                    else
                        arrayOf(poemToShare)

                    toShare += filesToShare
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        return shareIntentAndroidQPlus(
                            toShare,
                            poemName,
                            applicationContext = applicationContext,
                            poemToShare != null
                        )
                    } else {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && applicationContext.checkSelfPermission(
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val imageUris: ArrayList<Uri> = ArrayList()
                            try {
                                val directory = File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                                    "The Poets Kingdom"
                                )
                                if (!directory.exists())
                                    directory.mkdir()

                                val subDirectory =
                                    File(directory.absolutePath + File.separator + poemName)

                                if (!subDirectory.exists())
                                    subDirectory.mkdir()

                                for ((counter, file) in toShare.withIndex()) {
                                    val inputStream = FileInputStream(file)
                                    val newFile = if (counter == 0)
                                        File(
                                            subDirectory.absolutePath + File.separator + file.name.replace(
                                                '_',
                                                ' '
                                            ) + " thumbnail"
                                        )
                                    else
                                        File(
                                            subDirectory.absolutePath + File.separator + poemName + " Stanza $counter"
                                        )
                                    if (newFile.createNewFile()) {
                                        val outputStream = FileOutputStream(newFile)
                                        outputStream.channel.transferFrom(
                                            inputStream.channel,
                                            0,
                                            file.totalSpace
                                        )
                                        outputStream.close()
                                        inputStream.close()
                                        imageUris.add(
                                            FileProvider.getUriForFile(
                                                applicationContext,
                                                "${applicationContext.packageName}.provider",
                                                newFile
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                            }
                            if (poemToShare != null)
                                return imageUris

                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND_MULTIPLE
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
                                type = "image/*"
                            }
                            this.shareIntent.value = shareIntent
                        } else {
                            permissionsResultLauncher.value =
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }
                        onImageLongPressed = false
                    }
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    applicationContext.getString(
                        R.string.failed_to_share,
                        selectedElements.keys.first().name.split(".")[0].replace('_', ' ')
                    ),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
        return null
    }

    /**
     * Starts a share intent for Android Q and above
     * @param filesToShare the files to share outside the application
     * @param poemName the name of the poem
     * @param applicationContext context of the application
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun shareIntentAndroidQPlus(
        filesToShare: Array<File>,
        poemName: String,
        applicationContext: Context,
        isExternalShareIntent: Boolean
    ): ArrayList<Uri>? {
        try {
            val imageUris = kotlin.collections.ArrayList<Uri>()
            for ((counter, file) in filesToShare.withIndex()) {
                val contentValues = if (counter == 0)
                    ContentValues().apply {
                        put(
                            MediaStore.MediaColumns.DISPLAY_NAME,
                            "Thumbnail"
                        )
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + File.separator + applicationContext.getString(
                                R.string.app_name
                            ) + File.separator + poemName.replace('_', ' ')
                        )
                    }
                else
                    ContentValues().apply {
                        put(
                            MediaStore.MediaColumns.DISPLAY_NAME,
                            "Stanza $counter"
                        )
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + File.separator + applicationContext.getString(
                                R.string.app_name
                            ) + File.separator + poemName.replace('_', ' ')
                        )
                    }
                val resolver = applicationContext.contentResolver
                val uri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                val inputStream = FileInputStream(file)

                if (uri != null) {
                    resolver.openOutputStream(uri).use { outStream ->
                        if (outStream != null) {
                            inputStream.copyTo(outStream, DEFAULT_BUFFER_SIZE)
                        }
                    }
                }

                if (uri != null) {
                    imageUris.add(uri)
                }
            }
            if (isExternalShareIntent)
                return imageUris

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
                type = "image/*"
            }

            this.shareIntent.value = shareIntent
            onImageLongPressed = false
        } catch (e: IOException) {
        }
        return null
    }

    /**
     * Invokes a search for users input
     *
     * @param searchPhrase The phrase the user desires to search
     * @param applicationContext The context of the application
     * @param searchType The kind of search the user wishes to query
     * @param mainDispatcher The Main Thread
     * @param ioDispatcher The IO Thread
     */
    fun invokeSearch(
        searchPhrase: String,
        applicationContext: Context,
        searchType: String,
        mainDispatcher: CoroutineDispatcher,
        ioDispatcher: CoroutineDispatcher
    ) {
        val searchUtil =
            SearchUtil(
                searchPhrase,
                applicationContext,
                searchType,
                mainDispatcher,
                ioDispatcher
            )
        searchUtil.initiateLuceneSearch()

        if (searchUtil.getItemCount() > -1) {
            val poemThemeXmlParser =
                PoemThemeXmlParser(
                    PoemTheme(BackgroundType.DEFAULT, applicationContext),
                    applicationContext
                )

            val subStringLocations = searchUtil.getSubStringLocations()

            val handler = CoroutineExceptionHandler { _, exception ->
            }

            subStringLocations.addOnListChangedCallback(object :
                ObservableList.OnListChangedCallback<ObservableList<Pair<String, String>>>() {
                override fun onChanged(sender: ObservableList<Pair<String, String>>?) {
                }

                override fun onItemRangeChanged(
                    sender: ObservableList<Pair<String, String>>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                }

                override fun onItemRangeInserted(
                    sender: ObservableList<Pair<String, String>>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    // in Search Util we use an addAll to the observed item so we ony iterate once
                    viewModelScope.launch(mainDispatcher + handler) {
                        if (sender != null) {
                            if (itemCount == 1 && sender[0].first == "null" && sender[0].second == "null") {
                                displayNoResultsFound = true
                                hitsFound = false
                            } else {
                                substringLocations.addAll(sender)
                                if (displayNoResultsFound)
                                    displayNoResultsFound = false
                                stanzaIndexAndText = searchUtil.getStanzaAndText()
                                val backgroundImageDrawableFolder = applicationContext.getDir(
                                    applicationContext.getString(R.string.background_image_drawable_folder),
                                    Context.MODE_PRIVATE
                                )

                                for ((index, filePair) in subStringLocations.withIndex()) {
                                    val backgroundFileImage =
                                        File(
                                            backgroundImageDrawableFolder.absolutePath + File.separator + filePair.first.split(
                                                "."
                                            )[0] + ".png"
                                        )
                                    searchResultFiles.add(backgroundFileImage)
                                    searchResultIndexToUse[filePair.first] = index
                                }
                                hitsFound = true
                                val results =
                                    poemThemeXmlParser.parseMultipleThemes(sender as ObservableArrayList)


                                for (result in results) {
                                    poemBackgroundTypeArrayList.add(
                                        Pair(
                                            result.first,
                                            result.second
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                override fun onItemRangeMoved(
                    sender: ObservableList<Pair<String, String>>?,
                    fromPosition: Int,
                    toPosition: Int,
                    itemCount: Int
                ) {

                }

                override fun onItemRangeRemoved(
                    sender: ObservableList<Pair<String, String>>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    subStringLocations.removeOnListChangedCallback(this)
                }

            })

        } else {
            displayNoResultsFound = true
            hitsFound = false
        }
    }

    /**
     * Highlights hits found
     *
     * @param subStringLocations The locations to highlight
     * @return Highlighted string as the first element. The second is the stanzas it was found in
     */
    fun highlightedText(subStringLocations: Pair<String, String>): Pair<AnnotatedString, String> {
        var stanzaNumbersText = ""
        val stanzaIndexAndText = this.stanzaIndexAndText[subStringLocations.first]
        if (stanzaIndexAndText != null) {
            // when there are many stanzas with the search sub phrase use a triple to store information
            // highlight the subsequent items and append to the text
            val tripleArrayList = HashMap<Int, ArrayList<Pair<Int, Int>>>()
            for (substringLocation in subStringLocations.second.lines()) {
                val delimitedString = substringLocation.split(" ")

                // must be an error
                if (delimitedString.size != 3)
                    break

                val stanzaNum = delimitedString[0].toIntOrNull()
                val startIndex = delimitedString[1].toIntOrNull()
                val endIndex = delimitedString[2].toIntOrNull()

                if (stanzaNum != null && startIndex != null && endIndex != null) {
                    if (tripleArrayList[stanzaNum] == null)
                        tripleArrayList[stanzaNum] = ArrayList()

                    tripleArrayList[stanzaNum]?.add(Pair(startIndex, endIndex))
                }
            }

            val text = buildAnnotatedString {
                var textCharCounter = 0
                for ((counter, pair) in stanzaIndexAndText.withIndex()) {
                    if (counter != stanzaIndexAndText.lastIndex)
                        append(pair.second + "\n\n")
                    else
                        append(pair.second)

                    for (indices in tripleArrayList[pair.first]!!) {
                        if (indices.first > -1) {
                            addStyle(
                                style = SpanStyle(
                                    background = androidx.compose.ui.graphics.Color(
                                        Color.LTGRAY
                                    )
                                ),
                                start = indices.first + textCharCounter,
                                end = indices.second + textCharCounter
                            )
                        }
                    }
                    textCharCounter += pair.second.length
                }
            }
            // obtain locations and format as string
            for (line in subStringLocations.second.lines()) {
                if (line.split(" ").size == 3) {
                    val number = line.split(" ")[0].toIntOrNull()
                    val secondNumber = line.split(" ")[1].toIntOrNull()
                    if (secondNumber != null) {
                        if (number != null && secondNumber > -1) {
                            if (stanzaNumbersText.isEmpty() || stanzaNumbersText[stanzaNumbersText.length - 2].digitToInt() != number) {
                                stanzaNumbersText += "$number "
                            }
                        }
                    }
                }
            }
            return Pair(text, stanzaNumbersText)

        }
        return Pair(buildAnnotatedString { }, "")
    }

    operator fun Spannable.plus(other: Spannable): Spannable {
        return SpannableStringBuilder(this).append(other)
    }

    /**
     * Resets all selected images if there were any
     */
    fun resetSelectedImages() {
        val mapToUse = if (albumNameSelection == allPoemsString)
            allSavedPoems
        else
            albumSavedPoems
        val keys = mapToUse.filter { it.value }
        for (selectedKeys in keys) {
            mapToUse[selectedKeys.key] = false
        }
    }

    /**
     * Loads the search history from shared preferences if necessary
     * @param context context of the activity
     */
    fun getSearchHistory(context: Context): SnapshotStateList<String> {
        if (searchHistory.isEmpty() && !initialisedSearchHistory) {
            val sharedPreferences =
                context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            val savedHistory = sharedPreferences.getStringSet("search_history", HashSet())
            if (savedHistory?.isNotEmpty() == true) {
                searchHistory.addAll(savedHistory)
            } else {
                saveSearchHistory(context)
            }
            initialisedSearchHistory = true
        }
        return searchHistory
    }

    /**
     * Adds a new item to the search history
     * @param searchToAdd the search item to add
     */
    fun updateSearchHistory(searchToAdd: String) {
        if (!searchHistory.contains(searchToAdd)) {
            if (searchHistory.size < 10) {
                searchHistory.add(searchToAdd)
            } else {
                var nextValue = searchHistory[9]
                var counter = 9
                while (counter > 0) {
                    val tempNextVal = searchHistory[counter - 1]
                    searchHistory[counter - 1] = nextValue
                    nextValue = tempNextVal
                    counter--
                }
                searchHistory[9] = searchToAdd
            }
        }
    }

    /**
     * Saves the search history to shared preferences
     * @param context the context of MyPoemsScreen
     */
    fun saveSearchHistory(context: Context) {
        if (searchHistory.isNotEmpty()) {
            val sharedPreferences =
                context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            sharedPreferences.edit().putStringSet("search_history", searchHistory.toSet()).apply()
        } else {
            val sharedPreferences =
                context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            sharedPreferences.edit().putStringSet("search_history", HashSet()).apply()
        }
    }

    /**
     * Deletes search history item
     * @param toDelete the index to delete
     */
    fun deleteHistoryItem(toDelete: Int) {
        searchHistory.removeAt(toDelete)
    }

    /**
     * Gets all albums
     */
    fun getAlbums(context: Context): SnapshotStateList<String> {
        if (albumFolderNames.isEmpty()) {
            albumFolderNames.add(context.getString(R.string.all_poems_album_name))
            val sharedPreferences =
                context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)

            sharedPreferences.getStringSet("albums", Collections.emptySet())
                ?.let { albumFolderNames.addAll(it) }
        }
        return albumFolderNames
    }

    suspend fun addAlbumName(albumName: String, context: Context): Boolean {
        val handler = CoroutineExceptionHandler { _, exception ->
        }
        return withContext(viewModelScope.coroutineContext + Dispatchers.IO + handler) {
            albumSaveResult = -1
            val encodedAlbumName = albumName.replace(' ', '_')
            val poemFolder =
                context.getDir(
                    context.getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )
            if (poemFolder.exists()) {
                try {
                    val newAlbum = File(poemFolder.absolutePath + File.separator + encodedAlbumName)
                    if (newAlbum.exists())
                        return@withContext false
                    if (newAlbum.mkdir()) {
                        albumFolderNames.add(albumName)
                        updateAlbums(context)
                        albumSaveResult = 0
                        return@withContext true
                    }
                } catch (e: IOException) {
                    return@withContext false
                }
            }
            return@withContext false
        }
    }

    private fun updateAlbums(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        val albumsToSave = albumFolderNames.filterIndexed { index, _ -> index != 0 }

        sharedPreferences.edit().putStringSet("albums", albumsToSave.toSet()).apply()
    }

    /**
     * Moves a poem from its current location to a new album
     */
    fun addPoemToAlbum(context: Context, albumName: String): Boolean {
        val mapToUse = if (albumNameSelection == allPoemsString)
            allSavedPoems
        else
            albumSavedPoems
        val albumsToMove = mapToUse.filter { it.value }
        if (albumsToMove.isEmpty())
            return false
        val encodedAlbumName = if (albumName == allPoemsString)
            ""
        else
            albumName.replace(' ', '_')
        val poemFolder =
            context.getDir(
                context.getString(R.string.poems_folder_name),
                Context.MODE_PRIVATE
            )

        val albumFolder = if (albumNameSelection != allPoemsString && albumName == allPoemsString)
            poemFolder
        else
            File(poemFolder.absolutePath + File.separator + encodedAlbumName)

        if (albumFolder.exists()) {
            try {
                for (poemFilePair in albumsToMove) {
                    val poemFileName = poemFilePair.key.name.split(".")[0]
                    val poemFileNameDecoded = poemFileName.replace('_', ' ')
                    val albumFolderToMoveFrom = savedPoemAndAlbum[poemFileNameDecoded]
                    val encodedAlbumFolderToMoveFrom = albumFolderToMoveFrom?.replace(' ', '_')

                    val sourceFile = if (albumFolderToMoveFrom == null)
                        File(poemFolder.absolutePath + File.separator + poemFileName + ".xml")
                    else
                        File(poemFolder.absolutePath + File.separator + encodedAlbumFolderToMoveFrom + File.separator + poemFileName + ".xml")
                    val destinationFile =
                        File(albumFolder.absolutePath + File.separator + poemFileName + ".xml")
                    if (sourceFile.exists() && destinationFile.createNewFile()) {
                        if (Build.VERSION.SDK_INT >= 26) {
                            Files.move(
                                sourceFile.toPath(),
                                destinationFile.toPath(),
                                StandardCopyOption.ATOMIC_MOVE
                            )
                        } else {
                            val inputStream = FileInputStream(sourceFile)
                            val outputStream = FileOutputStream(destinationFile)

                            outputStream.channel.transferFrom(
                                inputStream.channel,
                                0,
                                sourceFile.totalSpace
                            )
                            outputStream.close()
                            inputStream.close()
                            sourceFile.delete()
                        }
                        if (albumName == allPoemsString)
                            savedPoemAndAlbum.remove(poemFileNameDecoded)
                        else
                            savedPoemAndAlbum[poemFileNameDecoded] = albumName
                        if (albumSavedPoems[poemFilePair.key] != null)
                            albumSavedPoems.remove(poemFilePair.key)
                    } else {
                        // add notification for failure of partial additions
                    }
                }
            } catch (e: IOException) {
                return false
            }
        } else {
            return false
        }
        for (albumFileToDeselect in albumsToMove) {
            mapToUse[albumFileToDeselect.key] = false
        }
        return true
    }

    fun resolveAlbumName(file: File): String? {
        val fileName = file.name.split(".")[0].replace('_', ' ')
        return savedPoemAndAlbum[fileName]
    }

    /**
     * Deletes an album
     */
    suspend fun deleteAlbum(albumName: String, context: Context): Boolean {
        val handler = CoroutineExceptionHandler { _, exception ->
        }
        return withContext(viewModelScope.coroutineContext + Dispatchers.IO + handler) {
            if (albumName == allPoemsString)
                return@withContext true
            val poemFolder =
                context.getDir(
                    context.getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )

            val encodedAlbumName = albumName.replace(' ', '_')

            val folderToDelete = File(poemFolder.absolutePath + File.separator + encodedAlbumName)

            try {
                if (folderToDelete.exists()) {
                    if (folderToDelete.deleteRecursively()) {
                        savedPoemAndAlbum.remove(albumName)
                        albumFolderNames.remove(albumName)
                        updateAlbums(context)
                        return@withContext true
                    }
                }
            } catch (e: IOException) {
                return@withContext false
            }
            return@withContext false
        }
    }

    /**
     * Renames an album
     */
    suspend fun renameAlbum(albumName: String, albumRename: String, context: Context): Boolean {
        val handler = CoroutineExceptionHandler { _, exception ->
        }
        return withContext(viewModelScope.coroutineContext + Dispatchers.IO + handler) {
            val poemFolder =
                context.getDir(
                    context.getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )

            if (albumRename == allPoemsString)
                return@withContext false

            if (albumName != albumRename) {

                val encodedAlbumName = albumName.replace(' ', '_')
                val encodedAlbumRename = albumRename.replace(' ', '_')

                val folderToDelete = File(poemFolder.absolutePath, encodedAlbumName)

                val folderToRename = File(poemFolder.absolutePath, encodedAlbumRename)

                try {
                    if (folderToDelete.exists() && !folderToRename.exists()) {
                        if (folderToDelete.renameTo(folderToRename)) {
                            albumFolderNames[albumFolderNames.indexOf(albumName)] = albumRename
                            for (pair in savedPoemAndAlbum) {
                                if (pair.value == albumName)
                                    pair.setValue(albumRename)
                            }
                            albumSaveResult = 0
                            updateAlbums(context)
                            return@withContext true
                        }
                    }
                } catch (e: IOException) {
                    albumSaveResult = -1
                    return@withContext false
                }
            }
            albumSaveResult = -1
            return@withContext false
        }
    }

    fun getPoemsFile(file: File, context: Context): File {
        val poemFolder =
            context.getDir(
                context.getString(R.string.poems_folder_name),
                Context.MODE_PRIVATE
            )
        val decodedPoemName = file.name.split(".")[0].replace('_', ' ')
        val encodedPoemName = file.name.split(".")[0]
        val albumName = savedPoemAndAlbum[decodedPoemName]
        return if (albumName != null) {
            val encodedAlbumName = albumName.replace(' ', '_')
            File(poemFolder.absolutePath + File.separator + encodedAlbumName + File.separator + encodedPoemName + ".xml")
        } else {
            File(poemFolder.absolutePath + File.separator + encodedPoemName + ".xml")
        }
    }
}