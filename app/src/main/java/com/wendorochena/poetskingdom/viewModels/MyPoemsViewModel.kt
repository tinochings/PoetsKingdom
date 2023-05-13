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
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MyPoemsViewModel : ViewModel() {
    var savedPoems = mutableStateMapOf<File, Boolean>()
        private set
    var onImageLongPressed by mutableStateOf(false)
        private set
    var permissionsResultLauncher = MutableLiveData<String>()
    var shareIntent = MutableLiveData<Intent>()
    var searchButtonClicked by mutableStateOf(false)
    var displayNoResultsFound by mutableStateOf(false)
    var hitsFound by mutableStateOf(false)
    var stanzaIndexAndText = HashMap<String, ArrayList<Pair<Int, String>>>()
    val poemBackgroundTypeArrayList = mutableStateListOf<Pair<BackgroundType, Int>>()
    val substringLocations = mutableStateListOf<Pair<String, String>>()
    var searchResultFiles = mutableListOf<File>()


    /**
     * Adds all files and sets to long press as false to to the savedPoems Map
     */
    private fun addAllFiles(
        arrayList: ArrayList<File>,
    ) {
        for (file in arrayList) {
            savedPoems[file] = false
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

    fun setOnLongClick(boolean: Boolean) {
        onImageLongPressed = boolean
    }

    /**
     * Gets an arraylist containing thumbnails to display
     */
    fun getThumbnails(context: Context): MutableMap<File, Boolean> {
        val arrayListToRet = ArrayList<File>()

        if (savedPoems.isEmpty()) {
            val savedPoemsFolder =
                context.getDir(
                    context.getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )
            val thumbnailsFolder =
                context.getDir(
                    context.getString(R.string.thumbnails_folder_name),
                    Context.MODE_PRIVATE
                )
            try {
                val savedImageFiles = savedPoemsFolder.listFiles()?.toMutableList()
                if (savedImageFiles != null) {
                    for (file in savedImageFiles) {
                        val thumbnailFile = File(
                            thumbnailsFolder?.absolutePath + File.separator + file.name.split(".")[0] + ".png"
                        )
                        if (thumbnailFile.exists()) {
                            arrayListToRet.add(File(thumbnailFile.absolutePath))
                        } else {
                            Log.e("No Such Thumbnail", file.name)
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            addAllFiles(arrayListToRet)
        }
        return savedPoems
    }


    /**
     * Deletes saved poem images
     *
     * @param context application context
     */
    fun deleteSavedPoems(context: Context) {
        val filesToDelete = savedPoems.filter { it.value }
        for (entry in filesToDelete) {
            try {
                val file = entry.key
                val splitString = file.name.split(File.separator)
                val poemName = splitString[splitString.size - 1]
                val savedPoemsPath = context.getDir(
                    context.getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )
                val fullPathToDelete = File(
                    savedPoemsPath.absolutePath + File.separator + poemName.replace(
                        ".png",
                        ".xml"
                    )
                )
                if (fullPathToDelete.exists())
                    if (fullPathToDelete.deleteRecursively())
                        savedPoems.remove(file)
                    else {
                        Log.e("Failed to remove file: ", entry.key.name)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Failed to remove file: ", entry.key.name)
            }
        }
        onImageLongPressed = false
    }

    /**
     * Shares images
     */
    fun shareIntent(applicationContext: Context) {
        val selectedElements = savedPoems.filter { it.value }
        if (selectedElements.size > 1) {
            Toast.makeText(applicationContext, R.string.share_as_image_toast, Toast.LENGTH_LONG)
                .show()
        } else {
            val savedImagesFolder =
                applicationContext.getDir(
                    applicationContext.getString(R.string.saved_images_folder_name),
                    AppCompatActivity.MODE_PRIVATE
                )
            val poemName = selectedElements.keys.first().name.split(".")[0].replace(' ', '_')
            val poemSavedImagesFolder =
                File(savedImagesFolder.absolutePath + File.separator + poemName)

            if (poemSavedImagesFolder.exists()) {
                val filesToShare = poemSavedImagesFolder.listFiles()
                if (filesToShare != null && filesToShare.isNotEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        shareIntentAndroidQPlus(
                            filesToShare,
                            poemName,
                            applicationContext = applicationContext
                        )
                    } else {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && applicationContext.checkSelfPermission(
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val imageUris: java.util.ArrayList<Uri> = java.util.ArrayList()
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

                                for (file in poemSavedImagesFolder.listFiles()!!) {
                                    val inputStream = FileInputStream(file)
                                    val newFile =
                                        File(subDirectory.absolutePath + File.separator + file.name)
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
                                e.printStackTrace()
                            }
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
                    }
                }
            }
        }
    }

    /**
     * Starts a share intent
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun shareIntentAndroidQPlus(
        filesToShare: Array<File>,
        poemName: String,
        applicationContext: Context
    ) {
        val imageUris = kotlin.collections.ArrayList<Uri>()

        for (file in filesToShare) {
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    poemName.replace('_', ' ') + " " + file.name
                )
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = applicationContext.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

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
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
            type = "image/*"
        }
        this.shareIntent.value = shareIntent
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
                exception.printStackTrace()
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
                    viewModelScope.launch(Dispatchers.Main + handler) {
                        if (sender != null) {
                            substringLocations.addAll(sender)
                            if (displayNoResultsFound)
                                displayNoResultsFound = false
                            stanzaIndexAndText = searchUtil.getStanzaAndText()
                            val backgroundImageDrawableFolder = applicationContext.getDir(
                                applicationContext.getString(R.string.background_image_drawable_folder),
                                Context.MODE_PRIVATE
                            )

                            for (filePair in subStringLocations) {
                                val backgroundFileImage =
                                    File(
                                        backgroundImageDrawableFolder.absolutePath + File.separator + filePair.first.split(
                                            "."
                                        )[0] + ".png"
                                    )
                                if (backgroundFileImage.exists())
                                    searchResultFiles.add(backgroundFileImage)
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
}