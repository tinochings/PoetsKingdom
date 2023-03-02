package com.wendorochena.poetskingdom

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.wendorochena.poetskingdom.recyclerViews.MyPoemsRecyclerViewAdapter
import com.wendorochena.poetskingdom.utils.SearchUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.collections.sortByDescending
import kotlin.collections.toMutableList
import kotlin.collections.withIndex

class MyPoems : AppCompatActivity() {

    private lateinit var recyclerViewAdapter: MyPoemsRecyclerViewAdapter
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


    private fun onFirstUse() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.guide_title)
            .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                dialog.dismiss()
            }.setMessage(R.string.guide_my_poems).show()
    }

    /**
     *
     */
    private fun setupOnBackPressed() {
        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val optionsContainer = findViewById<LinearLayout>(R.id.optionsContainer)
                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                if (optionsContainer.isVisible) {
                    optionsContainer.visibility = View.GONE
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
        recyclerViewAdapter.onItemLongClick = { frameLayout, i ->
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
        recyclerViewAdapter.onItemClick = { frameLayout, i ->
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
    private fun createFrameLayout(fileName: String): FrameLayout {
        val frameToRet = layoutInflater.inflate(R.layout.list_view_layout, null) as FrameLayout
        frameToRet.id = View.generateViewId()
        val shapeableImageView = frameToRet.getChildAt(1) as ShapeableImageView
        val textView = frameToRet.getChildAt(2) as TextView

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

        return frameToRet
    }

    /**
     *
     */
    private fun setupToolBarButtons() {
        val searchOptionsImage = findViewById<ImageButton>(R.id.optionsButton)
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.autoCompleteText)
        val advancedSearchEditText = findViewById<EditText>(R.id.advancedSearchText)
        val advancedSearchCont = findViewById<FrameLayout>(R.id.advancedSearchContainer)
        val searchCont = findViewById<FrameLayout>(R.id.searchButtonContainer)
        val simpleSearchTextView = findViewById<TextView>(R.id.simpleTextSearchTextView)
        val advancedSearchTextView = findViewById<TextView>(R.id.advancedSearchTextView)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        searchOptionsImage.setOnClickListener {
            val optionsContainer = findViewById<LinearLayout>(R.id.optionsContainer)

            if (recyclerView.isVisible)
                recyclerView.visibility = View.GONE
            else
                recyclerView.visibility = View.VISIBLE

            if (!advancedSearchCont.isVisible)
                advancedSearchCont.visibility = View.VISIBLE

            if (!searchCont.isVisible)
                searchCont.visibility = View.VISIBLE

            if (optionsContainer.isVisible)
                optionsContainer.visibility = View.GONE
            else
                optionsContainer.visibility = View.VISIBLE
            if (!advancedSearchTextView.isVisible)
                advancedSearchTextView.visibility = View.VISIBLE

            if (!simpleSearchTextView.isVisible)
                simpleSearchTextView.visibility = View.VISIBLE

            if (advancedSearchCont.tag == "selected") {
                advancedSearchCont.background = ColorDrawable(Color.WHITE)
                advancedSearchCont.tag = "deselected"
            }
        }

        autoCompleteTextView.setOnClickListener {
            advancedSearchCont.visibility = View.GONE
            advancedSearchTextView.visibility = View.GONE

            autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    if (p0 != null && p0.isNotEmpty()) {
                        val arrayList = if (searchHashMap.containsKey(p0[0]))
                            searchHashMap[p0[0]]!!
                        else
                            ArrayList()
                        val arrayAdapter = ArrayAdapter(
                            applicationContext,
                            android.R.layout.simple_list_item_1,
                            arrayList
                        )
                        autoCompleteTextView.setAdapter(arrayAdapter)
                        autoCompleteTextView.onItemClickListener =
                            AdapterView.OnItemClickListener { _, view, _, _ ->
                                val textView = view as TextView
                                launchCreatePoem(textView.text as String)
                            }
                    }
                }

            })
        }

        advancedSearchEditText.setOnClickListener {
            if (advancedSearchCont.tag != "selected") {
                advancedSearchCont.background =
                    AppCompatResources.getDrawable(this, R.drawable.selected_rounded_rectangle)
                advancedSearchCont.tag = "selected"
                simpleSearchTextView.visibility = View.GONE
                searchCont.visibility = View.GONE
            }
        }

        advancedSearchEditText.setOnKeyListener { _, _, event ->

            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val searchUtil =
                    SearchUtil(advancedSearchEditText.text.toString(), applicationContext)
                searchUtil.initiateLuceneSearch()

                if (!searchUtil.getSubStringLocations().isEmpty()) {
                    val subStringLocations = searchUtil.getSubStringLocations()

                    for (pair in subStringLocations) {
                        val fileName = pair.first.split(".")[0]
                        println(fileName)
                    }
                }
            }
            true
        }
    }

    /**
     * Creates a hashmap with the first letter as a key and the file name as a value
     */
    private fun populateSearchHashMap(fileName: String) {
        val key = fileName[0].lowercaseChar()

        val fileNameToAdd = fileName.replace(".xml", "").replace('_', ' ')
        if (searchHashMap.containsKey(key)) {
            searchHashMap[key]?.add(fileNameToAdd)
        } else {
            val arrayListToAdd = ArrayList<String>()
            arrayListToAdd.add(fileNameToAdd)
            searchHashMap[key] = arrayListToAdd
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
                    populateSearchHashMap(file.name)
                    val frameLayout = createFrameLayout(file.name)
                    frameLayout.tag = index
                    recyclerViewAdapter.addItem(frameLayout)
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun initialiseRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(applicationContext, 2)
        recyclerView.adapter = recyclerViewAdapter

        populateRecyclerView()
    }
}