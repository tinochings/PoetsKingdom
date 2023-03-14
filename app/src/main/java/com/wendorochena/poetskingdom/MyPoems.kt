package com.wendorochena.poetskingdom

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.recyclerViews.MyPoemsRecyclerViewAdapter
import com.wendorochena.poetskingdom.recyclerViews.SearchResultsRecyclerViewAdapter
import com.wendorochena.poetskingdom.utils.SearchUtil
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MyPoems : AppCompatActivity() {

    private lateinit var recyclerViewAdapter: MyPoemsRecyclerViewAdapter
    private lateinit var searchResultsViewAdapter: SearchResultsRecyclerViewAdapter
    private lateinit var poemsFolder: File
    private lateinit var thumbnailsFolder: File
    private var isLongClicked = false
    private var numberOfPoems: Int = 0
    private lateinit var searchHashMap: HashMap<Char, ArrayList<String>>
    private lateinit var selectedElements: ArrayList<Int>
    private var permissionsResultLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_poems)
        permissionsResultLauncher = permissionsActivityResult()
        selectedElements = ArrayList()
        poemsFolder = applicationContext.getDir(getString(R.string.poems_folder_name), MODE_PRIVATE)
        thumbnailsFolder =
            applicationContext.getDir(getString(R.string.thumbnails_folder_name), MODE_PRIVATE)
        numberOfPoems = poemsFolder.listFiles()?.size ?: 0
        recyclerViewAdapter = MyPoemsRecyclerViewAdapter(applicationContext)
        searchHashMap = HashMap()
        setupOnBackPressed()
        initialiseRecyclerView()
        setupClickListener()
        setupLongClickListener()
        setupToolBarButtons()
        setupBottomDrawer()
        val sharedPreferences =
            applicationContext.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("myPoemsFirstUse", false)) {
            onFirstUse()
            sharedPreferences.edit().putBoolean("myPoemsFirstUse", true).apply()
        }
    }


    /**
     *
     */
    private fun onFirstUse() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.guide_title)
            .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                dialog.dismiss()
            }.setMessage(R.string.guide_my_poems).show()
    }

    /**
     * Sets up a callback when back is pressed
     */
    private fun setupOnBackPressed() {
        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val advancedSearchContainer =
                    findViewById<LinearLayout>(R.id.advancedSearchContainer)
                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                val searchRecyclerView = findViewById<RecyclerView>(R.id.searchRecyclerView)

                if (advancedSearchContainer.isVisible) {
                    advancedSearchContainer.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                } else if (searchRecyclerView.isVisible) {
                    searchRecyclerView.visibility = View.GONE
                    if (searchResultsViewAdapter.clearData() != 0)
                        println("Error clearing data")
                    else
                        searchResultsViewAdapter.notifyItemRangeRemoved(
                            0,
                            searchResultsViewAdapter.itemCount
                        )
                    recyclerView.visibility = View.VISIBLE
                } else if (isLongClicked) {
                    recyclerViewAdapter.turnOffLongClick()
                    val bottomDrawer = findViewById<ConstraintLayout>(
                        R.id.bottomDrawer
                    )
                    bottomDrawer.visibility = View.GONE
                    selectedElements.clear()
                    isLongClicked = false
                } else {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(callBack)
    }

    /**
     * Deletes all files related to the file names selected
     */
    private fun deleteFiles(fileNames: ArrayList<String>) {
        val poemThemeFolder =
            applicationContext.getDir(getString(R.string.poem_themes_folder_name), MODE_PRIVATE)
        val thumbnailsFolder =
            applicationContext.getDir(getString(R.string.thumbnails_folder_name), MODE_PRIVATE)
        val savedImagesFolder =
            applicationContext.getDir(getString(R.string.saved_images_folder_name), MODE_PRIVATE)
        try {
            for (fileName in fileNames) {
                val theme = File(poemThemeFolder.absolutePath + File.separator + fileName + ".xml")
                val poem = File(poemsFolder.absolutePath + File.separator + fileName + ".xml")
                val thumbnail =
                    File(thumbnailsFolder.absolutePath + File.separator + fileName + ".png")
                val savedImagesDirectory =
                    File(savedImagesFolder.absolutePath + File.separator + fileName)

                theme.delete()
                poem.delete()
                thumbnail.delete()
                if (savedImagesDirectory.exists()) {
                    if (savedImagesDirectory.listFiles() != null && savedImagesDirectory.listFiles()?.size!! > 0) {
                        if (!savedImagesDirectory.deleteRecursively())
                            Log.e(
                                "Failed Images Deletion",
                                savedImagesDirectory.name + " failed to delete"
                            )
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    /**
     * Starts a share intent
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun shareIntentAndroidQPlus(filesToShare: Array<File>, poemName: String) {
//
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
        startActivity(Intent.createChooser(shareIntent, null))

    }

    /**
     * Sets up the bottom drawer when the a thumbnail has been long selected
     */
    private fun setupBottomDrawer() {
        val shareAsImage = findViewById<ImageButton>(R.id.shareAsImage)
        val shareAsPdf = findViewById<ImageButton>(R.id.shareAsPdf)
        val deleteButton = findViewById<ImageButton>(R.id.deleteButton)

        deleteButton.setOnClickListener {
            val fileNamesToDelete = ArrayList<String>()
            for (index in selectedElements) {
                val frameLayout = recyclerViewAdapter.getFrameAtIndex(index)
                val textView = frameLayout.getChildAt(2) as TextView
                fileNamesToDelete.add(textView.text.toString().replace(' ', '_'))
            }
            deleteFiles(fileNamesToDelete)
            for (index in selectedElements) {
                recyclerViewAdapter.removeAtIndex(index)
            }
            selectedElements.clear()
            recyclerViewAdapter.turnOffLongClick()
            isLongClicked = false
            findViewById<ConstraintLayout>(R.id.bottomDrawer).visibility = View.GONE
        }

        shareAsImage.setOnClickListener {


            if (selectedElements.size > 1) {
                Toast.makeText(applicationContext, R.string.share_as_image_toast, Toast.LENGTH_LONG)
                    .show()
            } else {
                val savedImagesFolder =
                    applicationContext.getDir(
                        getString(R.string.saved_images_folder_name),
                        MODE_PRIVATE
                    )
                val frameLayout = recyclerViewAdapter.getFrameAtIndex(selectedElements[0])
                val textView = frameLayout.getChildAt(2) as TextView
                val poemName = textView.text.toString().replace(' ', '_')
                val poemSavedImagesFolder =
                    File(savedImagesFolder.absolutePath + File.separator + poemName)

                if (poemSavedImagesFolder.exists()) {
                    if (poemSavedImagesFolder.listFiles() != null && poemSavedImagesFolder.listFiles()?.size!! > 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            poemSavedImagesFolder.listFiles()
                                ?.let { it1 -> shareIntentAndroidQPlus(it1, poemName) }
                        } else {
//                            if (poemSavedImagesFolder.listFiles()?.size!! > 1) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && checkSelfPermission(
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
                                                    "$packageName.fileprovider", newFile
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
                                startActivity(Intent.createChooser(shareIntent, null))
                            } else {
                                permissionsResultLauncher?.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     */
    private fun permissionsActivityResult(): ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                val bottomDrawer = findViewById<ConstraintLayout>(R.id.bottomDrawer)
                if (!bottomDrawer.isVisible)
                    bottomDrawer.visibility = View.VISIBLE
                val shareAsImage = findViewById<ImageButton>(R.id.shareAsImage)
                shareAsImage.performClick()

            }
        }

    /**
     * Sets up the long click listener for a selected item in the recycler view
     */
    private fun setupLongClickListener() {
        val bottomDrawer = findViewById<ConstraintLayout>(
            R.id.bottomDrawer
        )
        recyclerViewAdapter.onItemLongClick = { frameLayout, _ ->
            if (!bottomDrawer.isVisible)
                bottomDrawer.visibility = View.VISIBLE
            recyclerViewAdapter.updateLongImage(frameLayout.tag as Int, "check")
            selectedElements.add(frameLayout.tag as Int)
            if (!isLongClicked)
                recyclerViewAdapter.initiateOnLongClickImage(frameLayout.id)
            isLongClicked = true
        }
    }

    /**
     * Launches create poem with given string as argument
     */
    private fun launchCreatePoem(poemTitle: String) {
        val newActivityIntent =
            Intent(applicationContext, CreatePoem::class.java)
        newActivityIntent.putExtra("loadPoem", true)
        newActivityIntent.putExtra(
            "poemTitle",
            poemTitle
        )
        startActivity(newActivityIntent)
    }

    /**
     * Sets up the click listener
     */
    private fun setupClickListener() {
        recyclerViewAdapter.onItemClick = { frameLayout, _ ->
            if (isLongClicked) {
                selectedElements.add(frameLayout.tag as Int)
                recyclerViewAdapter.updateLongImage(frameLayout.tag as Int, "check")
            } else {
                val poemTitleTextView = frameLayout.getChildAt(2) as TextView
                val poemTitle = poemTitleTextView.text
                launchCreatePoem(poemTitle.toString())
            }
        }
    }

    /**
     *
     */
    private fun createFrameLayout(fileName: String, dateModified: Long): FrameLayout {
        val frameToRet = layoutInflater.inflate(R.layout.list_view_layout, null) as FrameLayout
        frameToRet.id = View.generateViewId()
        val shapeableImageView = frameToRet.getChildAt(1) as ShapeableImageView
        val textView = frameToRet.getChildAt(2) as TextView
        val dateTextView = frameToRet.getChildAt(3) as TextView

        try {
            val thumbnailFile = File(
                thumbnailsFolder.absolutePath + File.separator + fileName.replace(
                    ".xml",
                    ".png"
                )
            )
            if (thumbnailFile.exists()) {
                shapeableImageView.setImageBitmap(BitmapFactory.decodeFile(thumbnailFile.absolutePath))
                shapeableImageView.tag = thumbnailFile.absolutePath
            } else {
                Log.e("No Such Thumbnail", fileName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        textView.text = fileName.replace('_', ' ').replace(".xml", "")
        val locale = Locale("en")
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", locale)
        val date = Date(dateModified)
        dateTextView.text = simpleDateFormat.format(date)

        return frameToRet
    }

    /**
     *
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun setupToolBarButtons() {
        val searchOptionsImage = findViewById<ImageButton>(R.id.searchButton)
        val advancedSearchEditText = findViewById<EditText>(R.id.advancedSearchText)
        val advancedSearchCont = findViewById<LinearLayout>(R.id.advancedSearchContainer)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val searchRecyclerView = findViewById<RecyclerView>(R.id.searchRecyclerView)
        val progressBar = findViewById<ProgressBar>(R.id.progessBar)

        searchOptionsImage.setOnClickListener {

            if (advancedSearchEditText.tag != null && advancedSearchEditText.tag == resources.getString(
                    R.string.no_results
                )
            ) {
                advancedSearchEditText.setHint(R.string.search_here)
                advancedSearchEditText.tag = null
            }

            if (recyclerView.isVisible) {
                recyclerView.visibility = View.GONE
                advancedSearchCont.visibility = View.VISIBLE
            }


            if (searchRecyclerView.isVisible) {
                advancedSearchCont.visibility = View.VISIBLE
                searchRecyclerView.visibility = View.GONE

                if (searchResultsViewAdapter.clearData() != 0)
                    println("Error clearing data")
                else
                    searchResultsViewAdapter.notifyItemRangeRemoved(
                        0,
                        searchResultsViewAdapter.itemCount
                    )
            }
        }

        advancedSearchEditText.setOnKeyListener { _, _, event ->

            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_BACK) {
                if (advancedSearchEditText.tag != null && advancedSearchEditText.tag == resources.getString(
                        R.string.no_results
                    )
                ) {
                    advancedSearchEditText.setHint(R.string.search_here)
                    advancedSearchEditText.tag = null
                }
                onBackPressedDispatcher.onBackPressed()
                return@setOnKeyListener true
            }

            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {

                if (advancedSearchEditText.text.isNotEmpty()) {
                    val searchUtil =
                        SearchUtil(advancedSearchEditText.text.toString(), applicationContext)
                    searchUtil.initiateLuceneSearch()


                    if (searchUtil.getItemCount() > -1) {
                        progressBar.visibility = View.VISIBLE
                        if (advancedSearchEditText.tag != null && advancedSearchEditText.tag == resources.getString(
                                R.string.no_results
                            )
                        ) {
                            advancedSearchEditText.setHint(R.string.search_here)
                            advancedSearchEditText.tag = null
                        }

                        advancedSearchEditText.setText("", TextView.BufferType.EDITABLE)
                        val poemThemeXmlParser =
                            PoemThemeXmlParser(PoemTheme(BackgroundType.DEFAULT, this), this)
                        advancedSearchCont.visibility = View.GONE
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
                                GlobalScope.launch(Dispatchers.Main + handler) {
                                    if (sender != null) {
                                        val stanzaIndexAndText = searchUtil.getStanzaAndText()
                                        searchResultsViewAdapter = SearchResultsRecyclerViewAdapter(
                                            this@MyPoems,
                                            Pair(sender as ObservableArrayList, stanzaIndexAndText)
                                        )

                                        val results =
                                            poemThemeXmlParser.parseMultipleThemes(sender)

                                        for (result in results) {
                                            searchResultsViewAdapter.addBackgroundTypePair(
                                                Pair(
                                                    result.first,
                                                    result.second
                                                )
                                            )
                                        }


                                        initialiseSearchRecyclerView()

                                        searchResultsViewAdapter.notifyItemRangeInserted(
                                            0,
                                            searchResultsViewAdapter.itemCount
                                        )
                                        progressBar.visibility = View.GONE
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
//                        val stanzaIndexAndText = searchUtil.getStanzaAndText()
//                        searchResultsViewAdapter = SearchResultsRecyclerViewAdapter(
//                            this,
//                            Pair(subStringLocations, stanzaIndexAndText)
//                        )

//                        GlobalScope.launch(Dispatchers.Main + handler) {
//                            val results = poemThemeXmlParser.parseMultipleThemes(subStringLocations)
//
//                            for (result in results) {
//                                searchResultsViewAdapter.addBackgroundTypePair(
//                                    Pair(
//                                        result.first,
//                                        result.second
//                                    )
//                                )
//                            }
//                        }
//                        for (fileNamePair in subStringLocations) {
//                            if (poemThemeXmlParser.parseTheme(fileNamePair.first.split(".")[0]) == 0) {
//                                println(Thread.currentThread().name)
//                                searchResultsViewAdapter.addBackgroundTypePair(
//                                    Pair(
//                                        poemThemeXmlParser.getPoemTheme().backgroundType,
//                                        poemThemeXmlParser.getPoemTheme().getTextColorAsInt()
//                                    )
//                                )
//                            }
//                        }
//                        initialiseSearchRecyclerView()
//
//                        searchResultsViewAdapter.notifyItemRangeInserted(
//                            0,
//                            searchResultsViewAdapter.itemCount
//                        )
                    } else {
                        advancedSearchEditText.tag = resources.getString(R.string.no_results)
                        advancedSearchEditText.hint = resources.getString(R.string.no_results)
                        advancedSearchEditText.setText("", TextView.BufferType.EDITABLE)
                    }
                } else {
                    advancedSearchEditText.tag = resources.getString(R.string.no_results)
                    advancedSearchEditText.hint = resources.getString(R.string.no_input_entered)
                    advancedSearchEditText.setText("", TextView.BufferType.EDITABLE)
                }
                return@setOnKeyListener true
            }
            if (event.action == KeyEvent.ACTION_DOWN)
                advancedSearchEditText.onKeyDown(event.keyCode, event)
            else
                advancedSearchEditText.onKeyUp(event.keyCode, event)
        }
    }

    /**
     *
     */
    private fun populateRecyclerView() {
        try {
            val poemFiles = poemsFolder.listFiles()?.toMutableList()
            if (poemFiles != null) {
                poemFiles.sortByDescending { it.lastModified() }
                for ((index, file) in poemFiles.withIndex()) {
                    val frameLayout = createFrameLayout(file.name, file.lastModified())
                    frameLayout.tag = index
                    recyclerViewAdapter.addItem(frameLayout)
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    /**
     *
     */
    private fun initialiseRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = recyclerViewAdapter

        populateRecyclerView()
    }

    /**
     *
     */
    private fun initialiseSearchRecyclerView() {
        val searchRecyclerView = findViewById<RecyclerView>(R.id.searchRecyclerView)
        searchRecyclerView.visibility = View.VISIBLE
        searchRecyclerView.layoutManager = GridLayoutManager(this, 1)
        searchRecyclerView.adapter = searchResultsViewAdapter

        searchResultsViewAdapter.onItemClick = { name ->
            launchCreatePoem(name)
        }
    }
}