package com.wendorochena.poetskingdom

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.print.PrintManager
import android.text.*
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.slider.Slider
import com.wendorochena.poetskingdom.poemdata.*
import com.wendorochena.poetskingdom.recyclerViews.CreatePoemRecyclerViewAdapter
import com.wendorochena.poetskingdom.utils.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

/**
 * Outlines and background colors seem to lose their constraints when dynamic pages are hidden the visible
 * Resetting the background makes everything normal but it is quite spurious why this happens
 */
class CreatePoem : AppCompatActivity() {
    private lateinit var poemTheme: PoemTheme
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: CreatePoemRecyclerViewAdapter
    private var orientation: String? = null
    private var pages = 1
    private lateinit var currentPage: FrameLayout
    private var hasFileBeenEdited = false
    private var currentContainerView: View? = null
    private var downSizedImage: Bitmap? = null

    //key is the page number value is the id
    private val pageNumberAndId: HashMap<Int, Int> = HashMap()
    private val pageNumberAndText: HashMap<Int, String> = HashMap()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        poemTheme = PoemTheme(BackgroundType.DEFAULT, applicationContext)
        val intentExtras = intent.extras
        val loadPoemArg = getString(R.string.load_poem_argument_name)
        val poemTitleArg = getString(R.string.poem_title_argument_name)

        // parse users theme. If we cant parse it terminate the activity
        if (intentExtras?.getString(poemTitleArg) != null) {
            val poemParser =
                PoemThemeXmlParser(
                    PoemTheme(BackgroundType.DEFAULT, applicationContext),
                    applicationContext
                )

            val exceptionHandler = CoroutineExceptionHandler { _, exception ->
                exception.printStackTrace()

                val builder = MaterialAlertDialogBuilder(this@CreatePoem)
                builder.setTitle(R.string.failed_poem_load_title)
                    .setMessage(R.string.failed_poem_load_message)
                    .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
            }

            val isLoadPoem = intentExtras.getBoolean(loadPoemArg, false)

            // load file on background thread and then populate UI
            GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
                val poemThemeResult = poemParser.parseTheme(intentExtras.getString(poemTitleArg))
                val poemLoadResult =
                    if (isLoadPoem) PoemXMLParser.parseSavedPoem(
                        poemParser.getPoemTheme().getTitle(),
                        applicationContext
                    )
                    else
                        null

                if (poemThemeResult == 0) {
                    initialisePoemTheme(poemParser)
                    inflateUserTheme()
                    initialiseBottomDrawer()
                    if (isLoadPoem) {
                        if (poemLoadResult!!.size > 0)
                            loadSavedPoem(poemLoadResult)
                        else {
                            pageNumberAndText[1] = ""
                            hasFileBeenEdited = true
                        }
                    } else {
                        pageNumberAndText[1] = ""
                        hasFileBeenEdited = true
                    }
                } else {
                    val builder = MaterialAlertDialogBuilder(this@CreatePoem)
                    builder.setTitle(R.string.failed_poem_load_title)
                        .setMessage(R.string.failed_poem_load_message)
                        .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }.show()
                }
            }
        }
        setContentView(R.layout.activity_create_poem)
        orientation = getSharedPreferences(
            getString(R.string.personalisation_sharedpreferences_key),
            MODE_PRIVATE
        ).getString("orientation", null)

        if (orientation == "portrait") {
            pageNumberAndId[1] = R.id.portraitPoemContainer
        } else {
            pageNumberAndId[1] = R.id.landscapePoemContainer
        }
        setupTitle()
        setupRecyclerView()

        setupOnBackPressed()
        val sharedPreferences =
            applicationContext.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("createPoemFirstUse", false)) {
            onFirstUse()
            sharedPreferences.edit().putBoolean("createPoemFirstUse", true).apply()
        }
    }

    private fun onFirstUse() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.guide_title)
            .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                dialog.dismiss()
            }.setMessage(R.string.guide_create_poem).show()
    }


    /**
     * Simple algorithm that checks whether the string typed by a user is safe
     */
    private fun isValidatedInput(toValidate: String): Boolean {
        if (toValidate.isEmpty())
            return false
        for (char in toValidate) {
            if (char == '_')
                continue
            if (!char.isLetterOrDigit() || char.isWhitespace()) {
                return false
            }
        }
        return true
    }

    /**
     * Initiates a save as file in a coroutine
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun actuateSaveAsFile(category: String, createThumbnail: Boolean) {

        turnOnDimmerProgressBar()
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()

            showErrorToast(getString(R.string.error_type_file))
            turnOffDimmerProgressBar()
        }

        GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
            createDataContainer(category,createThumbnail)
            turnOffDimmerProgressBar()
            if (createThumbnail)
                currentPage.visibility = View.VISIBLE
        }
    }
    /**
     * Sets the title typeface
     */
    private fun setupTitle() {
        val textview = findViewById<TextView>(R.id.titleTextView)
        textview.typeface = ResourcesCompat.getFont(applicationContext, R.font.grand_hotel)

        textview.setOnLongClickListener {
            val customTitleView = TextView(this)
            customTitleView.setTextColor(resources.getColor(R.color.white, null))
            customTitleView.text = resources.getString(R.string.title_change)
            customTitleView.setTypeface(null, Typeface.BOLD)
            customTitleView.gravity = Gravity.CENTER
            customTitleView.textSize = resources.getDimension(R.dimen.normal_text_size)
            val editText = EditText(this)
            editText.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(60))
            editText.setHint(R.string.change_title_hint)
            editText.setHintTextColor(Color.WHITE)
            editText.setTextColor(resources.getColor(R.color.white, null))
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setCustomTitle(customTitleView)
            alertDialogBuilder.setView(editText)
            alertDialogBuilder.setTitle(R.string.title_change)
            alertDialogBuilder.setPositiveButton(R.string.title_change_confirm) { dialog, _ ->
                if (isValidatedInput(editText.text.toString().replace(' ', '_'))) {
                    textview.text = editText.text
                    val decodedTitleName = editText.text.toString()
                    val encodedTitleName = editText.text.toString().replace(' ', '_')
                    val oldTitleName = poemTheme.getTitle().replace(' ', '_') + ".xml"
                    val oldTitleFolderName = poemTheme.getTitle().replace(' ', '_')
                    try {
                        val directoryPath = getDir(
                            getString(R.string.poems_folder_name),
                            Context.MODE_PRIVATE
                        ).absolutePath
                        val poemThemePath = getDir(
                            getString(R.string.poem_themes_folder_name),
                            MODE_PRIVATE
                        ).absolutePath
                        val savedImagesPath = getDir(
                            getString(R.string.saved_images_folder_name),
                            MODE_PRIVATE
                        ).absolutePath
                        val thumbnailsPath = getDir(
                            getString(R.string.thumbnails_folder_name),
                            MODE_PRIVATE
                        ).absolutePath
                        val poemFile = File(directoryPath + File.separator + oldTitleName)
                        if (!poemFile.exists())
                            actuateSaveAsFile(Category.NONE.toString(), true)
                        val poemThemeFile = File(poemThemePath + File.separator + oldTitleName)
                        val poemSavedImagesFolder =
                            File(savedImagesPath + File.separator + oldTitleFolderName)
                        val poemThumbnail =
                            File(thumbnailsPath + File.separator + oldTitleFolderName + ".png")
                        val newPoemFile =
                            File(directoryPath + File.separator + encodedTitleName + ".xml")
                        val newThemeFile =
                            File(poemThemePath + File.separator + encodedTitleName + ".xml")
                        val newSavedImagesFile =
                            File(savedImagesPath + File.separator + encodedTitleName)
                        val newThumbnailsFile =
                            File(thumbnailsPath + File.separator + encodedTitleName + ".png")
                        if (poemFile.renameTo(newPoemFile) && poemThemeFile.renameTo(newThemeFile)) {
                            if (poemSavedImagesFolder.exists()) {
                                if (!poemSavedImagesFolder.renameTo(newSavedImagesFile))
                                    Log.e(
                                        "saved images folder failed to change name: ",
                                        poemSavedImagesFolder.name
                                    )
                            }
                            if (poemThumbnail.exists()) {
                                if (!poemThumbnail.renameTo(newThumbnailsFile))
                                    Log.e(
                                        "thumbnail failed to change name: ",
                                        poemSavedImagesFolder.name
                                    )
                            }
                            poemTheme.setTitle(decodedTitleName)
                            hasFileBeenEdited = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    dialog.dismiss()
                } else {
                    editText.text = SpannableStringBuilder("")
                }
            }
            alertDialogBuilder.setNegativeButton(R.string.title_change_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = alertDialogBuilder.create()
            dialog.setOnShowListener {
                val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                val button2 = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                button.setTextColor(resources.getColor(R.color.white, null))
                button2.setTextColor(resources.getColor(R.color.white, null))
            }
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_rectangle)
            dialog.show()
            true
        }
    }

    /**
     * Adds a back pressed listener
     */
    private fun setupOnBackPressed() {
        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentContainerView != null) {
                    turnOffCurrentView()
                    findViewById<LinearLayout>(R.id.allOptionsContainer).visibility = View.GONE
                } else {
                    if (hasFileBeenEdited) {
                        val thumbnailsFolder = applicationContext.getDir(
                            getString(R.string.thumbnails_folder_name),
                            MODE_PRIVATE
                        )
                        val encodedTitle = poemTheme.getTitle().replace(' ', '_') + ".png"
                        if (!File(thumbnailsFolder.absolutePath + File.separator + encodedTitle).exists())
                            actuateSaveAsFile(Category.NONE.toString(), true)
                        else
                            actuateSaveAsFile(Category.NONE.toString(), false)
                    }
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(callBack)
    }

    /**
     * Returns the margin size for text
     */
    private fun getTextMarginSize(): Int {
        return if (orientation == "portrait" && poemTheme.getOutline().contains("TEARDROP"))
            resources.getDimensionPixelSize(R.dimen.previewWithOutlineTextMarginTeardrop)
        else if (orientation == "portrait" && poemTheme.backgroundType.toString()
                .contains("OUTLINE")
        )
            resources.getDimensionPixelSize(R.dimen.portraitStrokeSizeMarginText)
        else
            0

    }

    /**
     * Initialises recycler view and the adapter for it
     */
    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(applicationContext, 2)
        recyclerView = findViewById(R.id.recyclerPagesContainer)
        recyclerView.layoutManager = gridLayoutManager
        recyclerViewAdapter = CreatePoemRecyclerViewAdapter(
            arrayOf(findViewById<FrameLayout>(R.id.addPage)).toCollection(ArrayList()),
            applicationContext
        )
        recyclerView.adapter = recyclerViewAdapter
    }

    /**
     * Replicates the user theme and creates a new frame layout which serves the purpose of a new page
     *
     * @param initialLoad true if the poem is being loaded false if not
     * @return the newly created frame layout
     */
    private fun createNewPage(initialLoad: Boolean): FrameLayout {
        val frameToReturn = FrameLayout(this)
        val defaultLayout: FrameLayout = if (orientation == "portrait")
            findViewById(R.id.portraitPoemContainer)
        else
            findViewById(R.id.landscapePoemContainer)

        val defaultText: EditText = if (orientation == "portrait")
            findViewById(R.id.portraitTextView)
        else
            findViewById(R.id.landscapeTextView)

        val defaultImageView: ShapeableImageView =
            if (orientation == "portrait")
                findViewById(R.id.portraitImageBackground)
            else
                findViewById(R.id.landscapeImageBackground)

        frameToReturn.layoutParams = defaultLayout.layoutParams

        if (defaultLayout.background != null) {
            frameToReturn.background = currentPage.background.current
        }
        val frameId = View.generateViewId()

        frameToReturn.id = frameId
        frameToReturn.tag = pages
        pageNumberAndId[frameToReturn.tag as Int] = frameId

        if (!initialLoad)
            pageNumberAndText[frameToReturn.tag as Int] = ""

        val toRetChildImage = ShapeableImageView(this)
        toRetChildImage.layoutParams = defaultImageView.layoutParams
        toRetChildImage.shapeAppearanceModel = defaultImageView.shapeAppearanceModel
        if (defaultImageView.tag != null && defaultImageView.tag.toString().startsWith("/")) {
            toRetChildImage.tag = defaultImageView.tag
            Glide.with(applicationContext).load(defaultImageView.tag.toString())
                .into(toRetChildImage)
        }
        toRetChildImage.scaleType = ImageView.ScaleType.FIT_XY
        frameToReturn.addView(toRetChildImage)

        val toRetEditTextBox = EditText(this)
        toRetEditTextBox.typeface = defaultText.typeface
        toRetEditTextBox.background = null
        toRetEditTextBox.setTextColor(poemTheme.getTextColorAsInt())
        toRetEditTextBox.textSize = poemTheme.getTextSize().toFloat()
        toRetEditTextBox.inputType = defaultText.inputType
        toRetEditTextBox.isVerticalScrollBarEnabled = true
        toRetEditTextBox.setHint(R.string.create_poem_text_view_hint)
        toRetEditTextBox.setHintTextColor(poemTheme.getTextColorAsInt())

        toRetEditTextBox.isSingleLine = false

        toRetEditTextBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!hasFileBeenEdited && s != null && pageNumberAndText[frameToReturn.tag as Int] != null) {
                    if (s.toString() != pageNumberAndText[frameToReturn.tag as Int])
                        hasFileBeenEdited = true
                }
            }
        })

        if (poemTheme.backgroundType.toString().contains("OUTLINE")) {
            toRetEditTextBox.layoutParams =
                adjustTextBounds(poemTheme.getOutline())
        }

        frameToReturn.addView(toRetEditTextBox)

        when (poemTheme.getTextAlignment()) {
            TextAlignment.LEFT -> {
                toRetEditTextBox.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                toRetEditTextBox.gravity = Gravity.TOP or Gravity.START
            }

            TextAlignment.CENTRE -> {
                toRetEditTextBox.textAlignment = View.TEXT_ALIGNMENT_CENTER
                toRetEditTextBox.gravity = Gravity.TOP or Gravity.CENTER
            }

            TextAlignment.RIGHT -> {
                toRetEditTextBox.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                toRetEditTextBox.gravity = Gravity.TOP or Gravity.END
            }
            TextAlignment.CENTRE_VERTICAL -> {
                val layoutParams = defaultText.layoutParams as FrameLayout.LayoutParams
                layoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
                toRetEditTextBox.layoutParams = defaultText.layoutParams as FrameLayout.LayoutParams
                toRetEditTextBox.textAlignment = View.TEXT_ALIGNMENT_CENTER
                toRetEditTextBox.gravity = Gravity.CENTER
            }
            TextAlignment.CENTRE_VERTICAL_LEFT -> {
                val layoutParams = defaultText.layoutParams as FrameLayout.LayoutParams
                layoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                toRetEditTextBox.layoutParams = layoutParams
                toRetEditTextBox.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                toRetEditTextBox.gravity = Gravity.START
            }
            TextAlignment.CENTRE_VERTICAL_RIGHT -> {
                val layoutParams = defaultText.layoutParams as FrameLayout.LayoutParams
                layoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.END
                toRetEditTextBox.layoutParams = layoutParams
                toRetEditTextBox.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                toRetEditTextBox.gravity = Gravity.END
                toRetEditTextBox.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            }
        }

        frameToReturn.setOnTouchListener(object :
            View.OnTouchListener {
            val bottomDrawer = findViewById<ConstraintLayout>(R.id.bottomDrawer)
            val gestureDetector = GestureDetector(this@CreatePoem, object :
                GestureDetector.SimpleOnGestureListener() {

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (bottomDrawer.isVisible) {
                        val animation = AnimationUtils.loadAnimation(
                            this@CreatePoem,
                            com.google.android.apps.common.testing.accessibility.framework.R.anim.abc_slide_out_bottom
                        )
                        bottomDrawer.startAnimation(animation)
                        bottomDrawer.visibility = View.GONE
                        if (currentContainerView != null)
                            turnOffCurrentView()
                    } else {
                        val animation = AnimationUtils.loadAnimation(
                            this@CreatePoem,
                            com.google.android.apps.common.testing.accessibility.framework.R.anim.abc_slide_in_bottom
                        )
                        bottomDrawer.startAnimation(animation)
                        bottomDrawer.visibility = View.VISIBLE
                    }
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    for (child in frameToReturn.children) {
                        if (child is EditText) {
                            val keyboard =
                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            if (!child.isFocused) {
                                if (child.requestFocus()) {
                                    keyboard.showSoftInput(child, InputMethodManager.SHOW_IMPLICIT)
                                    child.setSelection(child.length())
                                }
                            } else {
                                keyboard.showSoftInput(child, InputMethodManager.SHOW_IMPLICIT)
                            }
                        }
                    }
                    return true
                }
            })

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event != null) {
                    v?.performClick()
                    gestureDetector.onTouchEvent(event)
                }
                return true
            }
        })
        findViewById<ConstraintLayout>(R.id.parent).addView(frameToReturn)
        return frameToReturn
    }

    /**
     *
     */
    private fun setEditText(frameLayout: FrameLayout, boolean: Boolean) {
        for (child in frameLayout.children) {
            if (child is EditText)
                child.isEnabled = boolean
        }
    }

    /**
     * Removes a view and its children and re-orders the current layout if necessary
     *
     * @param pageNumberToDelete the page number to delete
     */
    private fun deleteLayout(pageNumberToDelete: Int) {
        val frameToDelete =
            pageNumberAndId[pageNumberToDelete]?.let { findViewById<FrameLayout>(it) }
        if (pageNumberToDelete == 1) {
            if (pageNumberAndId.contains(2)) {
                val page2 = pageNumberAndId[2]?.let { findViewById<FrameLayout>(it) }
                val editTextPage2 = page2?.getChildAt(1) as EditText
                val editTextPage1 = frameToDelete?.getChildAt(1) as EditText
                editTextPage1.text = editTextPage2.text
                deleteLayout(2)
            }
        } else {
            frameToDelete?.removeAllViews()
        }

        if (pageNumberToDelete != 1) {
            if (pageNumberToDelete == pages) {
                pageNumberAndId.remove(pageNumberToDelete)
                pages--
            } else {
                //update indexes
                var indexCounter = pageNumberToDelete + 1
                val indexesToReplace = ArrayList<Pair<Int, Int>>()
                while (indexCounter <= pages) {
                    val frameToUpdate =
                        pageNumberAndId[indexCounter]?.let { findViewById<FrameLayout>(it) }
                    val previousTagNum = frameToUpdate?.tag as Int
                    val newTagNum = previousTagNum - 1
                    frameToUpdate.tag = newTagNum
                    indexesToReplace.add(Pair(newTagNum, frameToUpdate.id))
                    indexCounter++
                }
                //API 23 does not support replacing so we will manually do it
                for (newKeyAndId in indexesToReplace) {
                    pageNumberAndId[newKeyAndId.first] = newKeyAndId.second
                }

                pageNumberAndId.remove(pages)
                findViewById<ConstraintLayout>(R.id.parent).removeView(frameToDelete)
                pages--
            }
        }
    }

    /**
     * Sets the currently viewed page to the default main page
     */
    private fun setDefaultCurrentPage() {
        currentPage = if (orientation == "portrait")
            findViewById(R.id.portraitPoemContainer)
        else
            findViewById(R.id.landscapePoemContainer)
        currentPage.visibility = View.VISIBLE
    }

    /**
     * Replaces the current view if the page to delete is the currently open page
     * @param pageNumber The page number being deleted
     */
    private fun replaceCurrentView(pageNumber: Int) {
        if (pageNumber == 1 && pages == 1) {
            val editText = currentPage.getChildAt(1) as EditText
            editText.text = SpannableStringBuilder("")
            setDefaultCurrentPage()
        } else if (pageNumber == 1 && pages > 1) {
            setDefaultCurrentPage()
        } else {
            if (pageNumber - 1 == 0) {
                setDefaultCurrentPage()
            } else {
                pageNumberAndId[pageNumber - 1]?.let {
                    currentPage = findViewById(it)
                    turnOffCurrentView()
                    currentPage.visibility = View.VISIBLE
                    currentPage.bringToFront()
                }
            }
        }
    }

    /**
     * Updates the recycler view holder text numbers
     */
    private fun updateRecyclerViewPageNumber(pageNumberToDelete: Int) {
        var counter = pageNumberToDelete + 1
        while (counter < recyclerViewAdapter.itemCount) {
            recyclerView.findViewHolderForLayoutPosition(counter)?.itemView?.findViewById<TextView>(
                R.id.pageNumberTextView
            )?.text = (counter - 1).toString()
            counter++
        }
        recyclerViewAdapter.notifyItemRangeChanged(
            pageNumberToDelete,
            recyclerViewAdapter.itemCount
        )
    }

    /**
     * Sets up the deletion of a page
     */
    private fun setupOnPageLongClickListener() {
        recyclerViewAdapter.onItemLongClick = { clickedLayout ->
            if (clickedLayout.id != R.id.addPageRecyclerViewId) {
                val pageToDelete = clickedLayout.tag as Int
                if (currentPage.tag as Int == pageToDelete || pages == 2) {
                    replaceCurrentView(pageToDelete)
                }
                deleteLayout(pageToDelete)
                updateRecyclerViewPageNumber(pageToDelete)
                recyclerViewAdapter.removeElement(pageToDelete)
                hasFileBeenEdited = true
            }
        }
    }

    /**
     * Sets up the click event for a recycler view
     */
    private fun setupOnPageClickListener() {

        recyclerViewAdapter.onItemClick = { clickedLayout ->
            val dimmer = findViewById<FrameLayout>(R.id.backgroundDim)
            if (clickedLayout.id == R.id.addPage) {
                currentPage.visibility = View.GONE
                pages++
                val newPage = createNewPage(false)
                recyclerViewAdapter.addElement(newPage, newPage.tag as Int)
                recyclerView.visibility = View.GONE
                dimmer.visibility = View.GONE
                currentPage = newPage
                currentPage.bringToFront()
            } else {
                currentPage.visibility = View.GONE
                currentPage = pageNumberAndId[clickedLayout.tag as Int]?.let { findViewById(it) }!!
                recyclerView.visibility = View.GONE
                dimmer.visibility = View.GONE
                currentPage.visibility = View.VISIBLE
                currentPage.bringToFront()
                setEditText(currentPage, true)
            }
        }
    }


    /**
     * Adjust the bounds of the text view so it does not intrude into the boarder of the outline
     */
    private fun adjustTextBounds(outlineTypes: String): FrameLayout.LayoutParams {
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val strokeSize: Int = getTextMarginSize()
        layoutParams.setMargins(
            strokeSize
        )
        when (outlineTypes) {
            OutlineTypes.TEARDROP.toString() -> {
                layoutParams.setMargins(
                    strokeSize
                )
            }
            OutlineTypes.ROTATED_TEARDROP.toString() -> {
                layoutParams.setMargins(
                    strokeSize
                )
            }
            OutlineTypes.LEMON.toString() -> {
                layoutParams.setMargins(
                    resources.getDimensionPixelSize(R.dimen.lemonCornerSizeTopLeft),
                    resources.getDimensionPixelSize(R.dimen.lemonCornerSizeTopRight),
                    resources.getDimensionPixelSize(R.dimen.lemonCornerSizeTopRight),
                    resources.getDimensionPixelSize(R.dimen.lemonCornerSizeTopLeft)
                )
            }
        }
        return layoutParams
    }

    /**
     * Returns outline and the color selected
     */

    private fun getOutlineAndColor(): Drawable {

        val strokeSize: Int = if (orientation == "portrait")
            resources.getDimensionPixelSize(R.dimen.portraitStrokeSize)
        else
            resources.getDimensionPixelSize(R.dimen.strokeSize)

        val defaultDrawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.rounded_rectangle_outline,
            null
        ) as GradientDrawable

        defaultDrawable.setStroke(
            strokeSize, poemTheme.getOutlineColor()
        )

        defaultDrawable.setBounds(
            currentPage.left,
            currentPage.top,
            currentPage.right,
            currentPage.bottom
        )
        when (poemTheme.getOutline()) {
            OutlineTypes.ROUNDED_RECTANGLE.toString() -> {
                val gradientDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.rounded_rectangle_outline,
                    null
                ) as GradientDrawable

                gradientDrawable.setBounds(
                    currentPage.left,
                    currentPage.top,
                    currentPage.right,
                    currentPage.bottom
                )
                gradientDrawable.setStroke(
                    strokeSize, poemTheme.getOutlineColor()
                )
                return gradientDrawable
            }

            OutlineTypes.TEARDROP.toString() -> {
                val gradientDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.teardrop_outline,
                    null
                ) as GradientDrawable
                gradientDrawable.setBounds(
                    currentPage.left,
                    currentPage.top,
                    currentPage.right,
                    currentPage.bottom
                )
                gradientDrawable.setStroke(
                    strokeSize, poemTheme.getOutlineColor()
                )
                return gradientDrawable
            }

            OutlineTypes.ROTATED_TEARDROP.toString() -> {
                val gradientDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.rotated_teardrop,
                    null
                ) as GradientDrawable
                gradientDrawable.setBounds(
                    currentPage.left,
                    currentPage.top,
                    currentPage.right,
                    currentPage.bottom
                )
                gradientDrawable.setStroke(
                    strokeSize, poemTheme.getOutlineColor()
                )
                return gradientDrawable
            }

            OutlineTypes.RECTANGLE.toString() -> {
                val gradientDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.rectangle_outline,
                    null
                ) as GradientDrawable
                gradientDrawable.setBounds(
                    currentPage.left,
                    currentPage.top,
                    currentPage.right,
                    currentPage.bottom
                )
                gradientDrawable.setStroke(
                    strokeSize, poemTheme.getOutlineColor()
                )
                return gradientDrawable
            }

            OutlineTypes.LEMON.toString() -> {
                val gradientDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.lemon_outline,
                    null
                ) as GradientDrawable
                gradientDrawable.setBounds(
                    currentPage.left,
                    currentPage.top,
                    currentPage.right,
                    currentPage.bottom
                )
                gradientDrawable.setStroke(
                    strokeSize, poemTheme.getOutlineColor()
                )
                return gradientDrawable
            }
        }
        return defaultDrawable
    }

    /**
     * Sets up how the text looks
     */
    private fun prepareText() {
        val text = if (orientation == "landscape")
            findViewById(R.id.landscapeTextView)
        else
            findViewById<EditText>(R.id.portraitTextView)

        text.typeface = TypefaceHelper.getTypeFace(poemTheme.getTextFont(), applicationContext)
        text.textSize = poemTheme.getTextSize().toFloat()
        text.setHintTextColor(poemTheme.getTextColorAsInt())
        text.setTextColor(poemTheme.getTextColorAsInt())
        text.setHint(R.string.create_poem_text_view_hint)

        if (poemTheme.backgroundType.toString().contains("OUTLINE")) {
            text.layoutParams = adjustTextBounds(poemTheme.getOutline())
        }

        when (poemTheme.getTextAlignment()) {
            TextAlignment.LEFT -> {
                text.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                text.gravity = Gravity.START
            }

            TextAlignment.CENTRE -> {
                text.textAlignment = View.TEXT_ALIGNMENT_CENTER
                text.gravity = Gravity.CENTER
            }

            TextAlignment.RIGHT -> {
                text.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                text.gravity = Gravity.END
            }
            TextAlignment.CENTRE_VERTICAL -> {
                val layoutParams = text.layoutParams as FrameLayout.LayoutParams
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
                text.layoutParams = layoutParams
                text.textAlignment = View.TEXT_ALIGNMENT_CENTER
                text.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL

            }
            TextAlignment.CENTRE_VERTICAL_LEFT -> {
                val layoutParams = text.layoutParams as FrameLayout.LayoutParams
                layoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                text.layoutParams = layoutParams
                text.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                text.gravity = Gravity.START
            }
            TextAlignment.CENTRE_VERTICAL_RIGHT -> {
                val layoutParams = text.layoutParams as FrameLayout.LayoutParams
                layoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.END
                text.layoutParams = layoutParams
                text.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                text.gravity = Gravity.END
            }
        }

    }

    /**
     * Sets up the cover page
     */
    private fun initiateCoverPage() {
        val personalisationPreferences = getSharedPreferences(
            getString(R.string.personalisation_sharedpreferences_key),
            MODE_PRIVATE
        )

        val coverPage = personalisationPreferences.getString("coverPage", null)
        val author = personalisationPreferences.getString("author", null)
        val signature = personalisationPreferences.getString("signature", null)
        if (coverPage == "true")
            author?.let {
                if (signature != null) {

                    setupCoverPage(it, poemTheme.getTitle(), signature)
                }
            }
    }

    /**
     * Sets the background of the poem
     */
    private fun setBackground() {
        val frame = currentPage
        frame.elevation = 5f

        val image: ShapeableImageView = currentPage.getChildAt(0) as ShapeableImageView

        when (poemTheme.backgroundType) {
            BackgroundType.DEFAULT -> {
                val colorDrawable = ColorDrawable(getColor(R.color.white))
                colorDrawable.setBounds(0, 0, frame.right, frame.bottom)
                frame.background = colorDrawable
                frame.visibility = View.VISIBLE
                initiateCoverPage()
            }

            BackgroundType.OUTLINE -> {
                val backgroundDrawable = getOutlineAndColor() as GradientDrawable
                backgroundDrawable.setColor(getColor(R.color.white))
                frame.background = backgroundDrawable
                frame.visibility = View.VISIBLE
                initiateCoverPage()
            }

            BackgroundType.OUTLINE_WITH_IMAGE -> {
                frame.background = getOutlineAndColor()
                image.shapeAppearanceModel =
                    ShapeAppearanceModelHelper.shapeImageView(poemTheme.getOutline(), resources)
                val file = File(poemTheme.getImagePath())
                if (file.exists()) {
                    Glide.with(applicationContext).load(file.absolutePath)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                e?.printStackTrace()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                downSizedImage = resource?.toBitmap(1080, 1080)
                                initiateCoverPage()
                                return false
                            }

                        }).into(image)
                }
                val layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                val strokeSize: Int = if (orientation == "portrait")
                    resources.getDimensionPixelSize(R.dimen.portraitStrokeSizeMargin)
                else
                    resources.getDimensionPixelSize(R.dimen.strokeSizeMargin)

                layoutParams.setMargins(
                    strokeSize
                )
                image.layoutParams = layoutParams
                image.scaleType = ImageView.ScaleType.FIT_XY
                image.visibility = View.VISIBLE
                image.tag = poemTheme.getImagePath()

            }

            BackgroundType.OUTLINE_WITH_COLOR -> {
                frame.background = getOutlineAndColor()
                val gradientDrawable: GradientDrawable =
                    frame.background.constantState?.newDrawable() as GradientDrawable
                gradientDrawable.setColor(poemTheme.getBackgroundColorAsInt())
                frame.background = gradientDrawable
                frame.visibility = View.VISIBLE
                initiateCoverPage()
            }

            BackgroundType.IMAGE -> {
                val file = File(poemTheme.getImagePath())
                if (file.exists()) {
                    Glide.with(this).load(file.absolutePath)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                e?.printStackTrace()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                downSizedImage = resource?.toBitmap(1080, 1080)
                                initiateCoverPage()
                                return false
                            }

                        }).into(image)
                    image.visibility = View.VISIBLE
                    image.tag = poemTheme.getImagePath()
                }
            }

            BackgroundType.COLOR -> {
                val colorDrawable = ColorDrawable(poemTheme.getBackgroundColorAsInt())
                colorDrawable.setBounds(0, 0, frame.right, frame.bottom)
                frame.background = colorDrawable
                initiateCoverPage()
            }
        }

        currentPage.setOnTouchListener(object :
            View.OnTouchListener {
            val bottomDrawer = findViewById<ConstraintLayout>(R.id.bottomDrawer)
            val gestureDetector = GestureDetector(this@CreatePoem, object :
                GestureDetector.SimpleOnGestureListener() {

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (bottomDrawer.isVisible) {
                        val animation = AnimationUtils.loadAnimation(
                            this@CreatePoem,
                            com.google.android.apps.common.testing.accessibility.framework.R.anim.abc_slide_out_bottom
                        )
                        bottomDrawer.startAnimation(animation)
                        bottomDrawer.visibility = View.GONE
                        if (currentContainerView != null)
                            turnOffCurrentView()
                    } else {
                        val animation = AnimationUtils.loadAnimation(
                            this@CreatePoem,
                            com.google.android.apps.common.testing.accessibility.framework.R.anim.abc_slide_in_bottom
                        )
                        bottomDrawer.startAnimation(animation)
                        bottomDrawer.visibility = View.VISIBLE
                    }
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    for (child in currentPage.children) {
                        if (child is EditText) {
                            val keyboard =
                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            if (!child.isFocused) {
                                if (child.requestFocus()) {
                                    keyboard.showSoftInput(child, InputMethodManager.SHOW_IMPLICIT)
                                    child.setSelection(child.length())
                                }
                            } else {
                                keyboard.showSoftInput(child, InputMethodManager.SHOW_IMPLICIT)
                            }
                        }
                    }
                    return true
                }
            })

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event != null) {
                    v?.performClick()
                    gestureDetector.onTouchEvent(event)
                }
                return true
            }
        })

        for (child in currentPage.children) {
            if (child is EditText) {
                child.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (!hasFileBeenEdited && s != null && pageNumberAndText[currentPage.tag as Int] != null) {
                            if (s.toString() != pageNumberAndText[currentPage.tag as Int])
                                hasFileBeenEdited = true
                        }
                    }
                })
            }
        }
    }

    /**
     *
     */
    private fun setupOrientation() {
        currentPage = if (orientation == "landscape")
            findViewById(R.id.landscapePoemContainer)
        else
            findViewById(R.id.portraitPoemContainer)

        prepareText()

        setBackground()
        currentPage.visibility = View.VISIBLE
        currentPage.tag = 1

    }

    /**
     * Initialises the cover page text views
     */
    private fun setupCoverPage(author: String, title: String, signature: String) {
        if (currentPage.background != null)
            findViewById<FrameLayout>(R.id.coverPageBackground).background =
                currentPage.background.constantState?.newDrawable()

        if (poemTheme.backgroundType.toString().lowercase().contains("image")) {
            if (currentPage.background != null) {
                for (child in currentPage.children) {
                    if (child is ShapeableImageView) {
                        val coverPageImage = findViewById<ShapeableImageView>(R.id.coverPageImage)
                        val params = RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT
                        )
                        params.setMargins(resources.getDimensionPixelSize(R.dimen.strokeSize))
                        coverPageImage.layoutParams = params
                        coverPageImage.shapeAppearanceModel = child.shapeAppearanceModel
                        coverPageImage.scaleType = ImageView.ScaleType.FIT_XY
                        coverPageImage.setImageBitmap(downSizedImage)
                        break
                    }
                }
            } else {
                findViewById<ShapeableImageView>(R.id.coverPageImage).setImageBitmap(
                    downSizedImage
                )
            }
        }

        val coverPageAuthor = findViewById<TextView>(R.id.coverPageAuthor)
        val coverPageTitle = findViewById<TextView>(R.id.coverPageTitle)
        val coverPageSignature = findViewById<TextView>(R.id.coverPageSignature)

        coverPageAuthor.text = author
        coverPageTitle.text = title
        coverPageSignature.text = signature


        coverPageAuthor.setTextColor(poemTheme.getTextColorAsInt())
        coverPageTitle.setTextColor(poemTheme.getTextColorAsInt())
        coverPageSignature.setTextColor(poemTheme.getTextColorAsInt())

        for (child in currentPage.children) {
            if (child is EditText) {
                coverPageAuthor.textAlignment = child.textAlignment
                coverPageSignature.textAlignment = child.textAlignment
                coverPageTitle.textAlignment = child.textAlignment
                coverPageAuthor.typeface = child.typeface
                coverPageTitle.typeface = child.typeface
                coverPageSignature.typeface = child.typeface
            }
        }

        if (currentPage.background != null) {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

            val textMargin = getTextMarginSize()
            params.bottomMargin = textMargin
            params.marginStart = textMargin
            params.marginEnd = textMargin

            coverPageSignature.layoutParams = params
        }
    }

    /**
     * Prepares the background the user selected
     */
    private fun inflateUserTheme() {

        findViewById<TextView>(R.id.titleTextView).text = poemTheme.getTitle()

        setupOrientation()

        findViewById<ConstraintLayout>(R.id.parent).setOnTouchListener(object :
            View.OnTouchListener {
            val bottomDrawer = findViewById<ConstraintLayout>(R.id.bottomDrawer)
            val gestureDetector = GestureDetector(this@CreatePoem, object :
                GestureDetector.SimpleOnGestureListener() {

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (bottomDrawer.isVisible) {
                        val animation = AnimationUtils.loadAnimation(
                            this@CreatePoem,
                            com.google.android.apps.common.testing.accessibility.framework.R.anim.abc_slide_out_bottom
                        )
                        bottomDrawer.startAnimation(animation)
                        bottomDrawer.visibility = View.GONE
                        if (currentContainerView != null)
                            turnOffCurrentView()
                    } else {
                        val animation = AnimationUtils.loadAnimation(
                            this@CreatePoem,
                            com.google.android.apps.common.testing.accessibility.framework.R.anim.abc_slide_in_bottom
                        )
                        bottomDrawer.startAnimation(animation)
                        bottomDrawer.visibility = View.VISIBLE
                    }
                    return true
                }
            })

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event != null) {
                    v?.performClick()
                    gestureDetector.onTouchEvent(event)
                }
                return true
            }
        })
    }

    /**
     * Initialises the listener for the pages button
     */
    private fun setupPageButton() {
        val pagesButton = findViewById<ImageButton>(R.id.pagesOptions)

        pagesButton.setOnLongClickListener {
            Toast.makeText(applicationContext, "Shows the stanzas in this poem", Toast.LENGTH_LONG)
                .show()
            true
        }

        pagesButton.setOnClickListener {
            val dimmer = findViewById<FrameLayout>(R.id.backgroundDim)
            val allOptionsContainer = findViewById<LinearLayout>(R.id.allOptionsContainer)
            if (recyclerView.visibility == View.VISIBLE) {
                recyclerView.visibility = View.GONE
                allOptionsContainer.visibility = View.GONE
                dimmer.visibility = View.GONE
                setEditText(currentPage, true)
                currentContainerView = null
            } else {
                turnOffCurrentView()
                currentContainerView = recyclerView
                setEditText(currentPage, false)
                dimmer.visibility = View.VISIBLE
                dimmer.bringToFront()
                dimmer.z = 5f
                dimmer.foreground.alpha = 180

                if (recyclerViewAdapter.itemCount > currentPage.tag as Int) {
                    for (child in currentPage.children) {
                        if (child is EditText)
                            recyclerViewAdapter.addElement(child.text, currentPage.tag as Int)
                    }

                } else {
                    recyclerViewAdapter.addElement(currentPage, currentPage.tag as Int)
                }

                recyclerView.visibility = View.VISIBLE
                recyclerView.bringToFront()
                recyclerView.z = 5f
            }

        }
        setupOnPageClickListener()
        setupOnPageLongClickListener()
    }

    /**
     * Updates the text alignment for all
     */
    private fun setEditTextAlignment(alignment: Int, gravity: Int) {

        for (key in pageNumberAndId.keys) {
            pageNumberAndId[key]?.let {
                val currFrame = findViewById<FrameLayout>(it)
                for (child in currFrame.children) {
                    if (child is EditText) {
                        when (gravity) {
                            Gravity.CENTER_VERTICAL -> {
                                val layoutParams = child.layoutParams as FrameLayout.LayoutParams
                                layoutParams.gravity = gravity or Gravity.CENTER_HORIZONTAL
                                child.layoutParams = layoutParams
                                child.textAlignment = alignment
                                child.gravity = Gravity.CENTER
                            }
                            Gravity.CENTER_VERTICAL or Gravity.START -> {
                                val layoutParams = child.layoutParams as FrameLayout.LayoutParams
                                layoutParams.gravity = gravity
                                child.layoutParams = layoutParams
                                child.textAlignment = alignment
                                child.gravity = Gravity.START
                            }
                            Gravity.CENTER_VERTICAL or Gravity.END -> {
                                val layoutParams = child.layoutParams as FrameLayout.LayoutParams
                                layoutParams.gravity = gravity
                                child.layoutParams = layoutParams
                                child.textAlignment = alignment
                                child.gravity = Gravity.END
                            }
                            else -> {
                                val layoutParams = child.layoutParams as FrameLayout.LayoutParams
                                layoutParams.gravity = if (gravity == Gravity.CENTER)
                                    Gravity.TOP or gravity
                                else
                                    Gravity.NO_GRAVITY or gravity
                                child.layoutParams = layoutParams
                                child.textAlignment = alignment
                                child.gravity = gravity
                            }
                        }

                        recyclerViewAdapter.addElement(alignment, gravity, currFrame.tag as Int)
                    }
                }
            }
        }
    }

    /**
     * Adjusts the text bounds for the cover page to be generated
     */
    private fun adjustCoverPageBounds() {
        val coverPageAuthor = findViewById<TextView>(R.id.coverPageAuthor)
        val coverPageTitle = findViewById<TextView>(R.id.coverPageTitle)
        val coverPageSignature = findViewById<TextView>(R.id.coverPageSignature)

        val strokeSize: Int = getTextMarginSize()
        val layoutParamAuthor = coverPageAuthor.layoutParams as RelativeLayout.LayoutParams
        val layoutParamTitle = coverPageTitle.layoutParams as RelativeLayout.LayoutParams
        val layoutParamSignature = coverPageSignature.layoutParams as RelativeLayout.LayoutParams
        layoutParamAuthor.marginEnd = strokeSize
        layoutParamTitle.marginEnd = strokeSize
        layoutParamSignature.marginEnd = strokeSize
        coverPageAuthor.layoutParams = layoutParamAuthor
        coverPageSignature.layoutParams = layoutParamSignature
        coverPageTitle.layoutParams = layoutParamTitle

    }

    /**
     *
     * Saves poem as a theme on IO thread
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun actuateSavePoemTheme() {
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()

            showErrorToast(getString(R.string.error_type_poem_theme))
        }

        GlobalScope.launch (Dispatchers.Main + exceptionHandler){
            val poemThemeXmlParser = PoemThemeXmlParser(poemTheme, applicationContext)
            poemThemeXmlParser.setIsEditTheme(true)

            poemThemeXmlParser.savePoemThemeToLocalFile(
                poemTheme.getImagePath(),
                poemTheme.getBackgroundColor(),
                null
            )
        }
    }
    /**
     * Sets up listeners for the text options events
     */
    private fun setupTextOptionsContainerButtons() {
        val leftAlign = findViewById<ImageView>(R.id.leftAlign)
        val centreAlign = findViewById<ImageView>(R.id.centreAlign)
        val rightAlign = findViewById<ImageView>(R.id.rightAlign)
        val centreVerticalAlign = findViewById<ImageView>(R.id.centerVerticalAlign)
        val centreVerticalLeftAlign = findViewById<ImageView>(R.id.centerVerticalLeftAlign)
        val centreVerticalRightAlign = findViewById<ImageView>(R.id.centerVerticalRightAlign)


        leftAlign.setOnClickListener {
            val coverPageAuthor = findViewById<TextView>(R.id.coverPageAuthor)
            val coverPageTitle = findViewById<TextView>(R.id.coverPageTitle)
            val coverPageSignature = findViewById<TextView>(R.id.coverPageSignature)
            coverPageAuthor.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            coverPageTitle.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            coverPageSignature.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            coverPageAuthor.gravity = Gravity.START
            coverPageTitle.gravity = Gravity.START
            coverPageSignature.gravity = Gravity.START
            poemTheme.setTextAlignment(TextAlignment.LEFT)
            setEditTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_START, Gravity.START)
//            val poemThemeXmlParser = PoemThemeXmlParser(poemTheme, applicationContext)
//            poemThemeXmlParser.setIsEditTheme(true)
            actuateSavePoemTheme()
//            poemThemeXmlParser.savePoemThemeToLocalFile(
//                poemTheme.getImagePath(),
//                poemTheme.getBackgroundColor(),
//                null
//            )
        }
        centreAlign.setOnClickListener {
            val coverPageAuthor = findViewById<TextView>(R.id.coverPageAuthor)
            val coverPageTitle = findViewById<TextView>(R.id.coverPageTitle)
            val coverPageSignature = findViewById<TextView>(R.id.coverPageSignature)
            coverPageAuthor.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            coverPageTitle.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            coverPageSignature.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            coverPageAuthor.gravity = Gravity.CENTER
            coverPageTitle.gravity = Gravity.CENTER
            coverPageSignature.gravity = Gravity.CENTER
            poemTheme.setTextAlignment(TextAlignment.CENTRE)
            setEditTextAlignment(TextView.TEXT_ALIGNMENT_CENTER, Gravity.CENTER)
            actuateSavePoemTheme()
//            val poemThemeXmlParser = PoemThemeXmlParser(poemTheme, applicationContext)
//            poemThemeXmlParser.setIsEditTheme(true)
//            poemThemeXmlParser.savePoemThemeToLocalFile(
//                poemTheme.getImagePath(),
//                poemTheme.getBackgroundColor(),
//                null
//            )
        }
        rightAlign.setOnClickListener {
            val coverPageAuthor = findViewById<TextView>(R.id.coverPageAuthor)
            val coverPageTitle = findViewById<TextView>(R.id.coverPageTitle)
            val coverPageSignature = findViewById<TextView>(R.id.coverPageSignature)
            coverPageAuthor.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            coverPageTitle.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            coverPageSignature.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            coverPageAuthor.gravity = Gravity.END
            coverPageTitle.gravity = Gravity.END
            coverPageSignature.gravity = Gravity.END
            adjustCoverPageBounds()
            poemTheme.setTextAlignment(TextAlignment.RIGHT)
            setEditTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END, Gravity.END)
            actuateSavePoemTheme()
//            val poemThemeXmlParser = PoemThemeXmlParser(poemTheme, applicationContext)
//            poemThemeXmlParser.setIsEditTheme(true)
//            poemThemeXmlParser.savePoemThemeToLocalFile(
//                poemTheme.getImagePath(),
//                poemTheme.getBackgroundColor(),
//                null
//            )
        }
        centreVerticalAlign.setOnClickListener {
            val coverPageAuthor = findViewById<TextView>(R.id.coverPageAuthor)
            val coverPageTitle = findViewById<TextView>(R.id.coverPageTitle)
            val coverPageSignature = findViewById<TextView>(R.id.coverPageSignature)
            coverPageAuthor.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            coverPageTitle.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            coverPageSignature.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            coverPageAuthor.gravity = Gravity.CENTER
            coverPageTitle.gravity = Gravity.CENTER
            coverPageSignature.gravity = Gravity.CENTER
            adjustCoverPageBounds()
            poemTheme.setTextAlignment(TextAlignment.CENTRE_VERTICAL)
            setEditTextAlignment(TextView.TEXT_ALIGNMENT_CENTER, Gravity.CENTER_VERTICAL)
            actuateSavePoemTheme()
//            val poemThemeXmlParser = PoemThemeXmlParser(poemTheme, applicationContext)
//            poemThemeXmlParser.setIsEditTheme(true)
//            poemThemeXmlParser.savePoemThemeToLocalFile(
//                poemTheme.getImagePath(),
//                poemTheme.getBackgroundColor(),
//                null
//            )
        }
        centreVerticalRightAlign.setOnClickListener {
            val coverPageAuthor = findViewById<TextView>(R.id.coverPageAuthor)
            val coverPageTitle = findViewById<TextView>(R.id.coverPageTitle)
            val coverPageSignature = findViewById<TextView>(R.id.coverPageSignature)
            coverPageAuthor.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            coverPageTitle.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            coverPageSignature.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            coverPageAuthor.gravity = Gravity.END
            coverPageTitle.gravity = Gravity.END
            coverPageSignature.gravity = Gravity.END
            adjustCoverPageBounds()
            poemTheme.setTextAlignment(TextAlignment.CENTRE_VERTICAL_RIGHT)
            setEditTextAlignment(
                TextView.TEXT_ALIGNMENT_TEXT_END,
                Gravity.CENTER_VERTICAL or Gravity.END
            )
            actuateSavePoemTheme()
//            val poemThemeXmlParser = PoemThemeXmlParser(poemTheme, applicationContext)
//            poemThemeXmlParser.setIsEditTheme(true)
//            poemThemeXmlParser.savePoemThemeToLocalFile(
//                poemTheme.getImagePath(),
//                poemTheme.getBackgroundColor(),
//                null
//            )
        }
        centreVerticalLeftAlign.setOnClickListener {
            val coverPageAuthor = findViewById<TextView>(R.id.coverPageAuthor)
            val coverPageTitle = findViewById<TextView>(R.id.coverPageTitle)
            val coverPageSignature = findViewById<TextView>(R.id.coverPageSignature)
            coverPageAuthor.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            coverPageTitle.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            coverPageSignature.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            coverPageAuthor.gravity = Gravity.START
            coverPageTitle.gravity = Gravity.START
            coverPageSignature.gravity = Gravity.START
            adjustCoverPageBounds()
            poemTheme.setTextAlignment(TextAlignment.CENTRE_VERTICAL_LEFT)
            setEditTextAlignment(
                TextView.TEXT_ALIGNMENT_TEXT_START,
                Gravity.CENTER_VERTICAL or Gravity.START
            )
            actuateSavePoemTheme()
//            val poemThemeXmlParser = PoemThemeXmlParser(poemTheme, applicationContext)
//            poemThemeXmlParser.setIsEditTheme(true)
//            poemThemeXmlParser.savePoemThemeToLocalFile(
//                poemTheme.getImagePath(),
//                poemTheme.getBackgroundColor(),
//                null
//            )
        }
    }

    /**
     *
     */
    private fun setupTextOptions() {
        val textOptions = findViewById<ImageButton>(R.id.textOptions)
        val allOptionsContainer = findViewById<LinearLayout>(R.id.allOptionsContainer)
        val textOptionsContainer = findViewById<LinearLayout>(R.id.textOptionsContainer)

        textOptions.setOnLongClickListener {
            Toast.makeText(
                applicationContext,
                "Shows alignment options on click",
                Toast.LENGTH_LONG
            ).show()
            true
        }
        textOptions.setOnClickListener {
            if (textOptionsContainer.visibility == View.VISIBLE) {
                textOptionsContainer.visibility = View.GONE
                allOptionsContainer.visibility = View.GONE
                currentContainerView = null
            } else {
                turnOffCurrentView()
                currentContainerView = textOptionsContainer
                allOptionsContainer.visibility = View.VISIBLE
                textOptionsContainer.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Sets up the slider that allows for text size change
     */
    private fun setupTextSizeContainer() {
        val textSize = findViewById<ImageButton>(R.id.textSize)
        val allOptionsContainer = findViewById<LinearLayout>(R.id.allOptionsContainer)
        val textSizeContainer = findViewById<FrameLayout>(R.id.textSizeContainer)

        textSize.setOnLongClickListener {
            Toast.makeText(applicationContext, "Allows you to change text size", Toast.LENGTH_LONG)
                .show()
            true
        }
        textSize.setOnClickListener {
            if (textSizeContainer.visibility == View.VISIBLE) {
                textSizeContainer.visibility = View.GONE
                allOptionsContainer.visibility = View.GONE
                currentContainerView = null
            } else {
                turnOffCurrentView()
                currentContainerView = textSizeContainer
                allOptionsContainer.visibility = View.VISIBLE
                textSizeContainer.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Sets up a dialog to choose text color
     */
    private fun setupTextColor() {
        val textColor = findViewById<ImageButton>(R.id.textColor)

        textColor.setOnLongClickListener {
            Toast.makeText(applicationContext, "Changes the text color", Toast.LENGTH_LONG).show()
            true
        }
        textColor.setOnClickListener {
            turnOffCurrentView()
            var currentEditText: EditText? = null

            for (child in currentPage.children)
                if (child is EditText)
                    currentEditText = child

            ColorPickerDialog
                .Builder(this)                        // Pass Activity Instance
                .setTitle(getString(R.string.color_picker_title))            // Default "Choose Color"
                .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                .setDefaultColor(R.color.black)     // Pass Default Color
                .setColorListener { color, colorHex ->
                    currentEditText?.setTextColor(color)
                    poemTheme.setTextColor(colorHex)
                    poemTheme.setTextColorAsInt(color)
                    updateAllEditTextViews(Float.NaN, "textColor", color)
                    actuateSavePoemTheme()
//                    val poemThemeXmlParser = PoemThemeXmlParser(poemTheme, applicationContext)
//                    poemThemeXmlParser.savePoemThemeToLocalFile(
//                        poemTheme.getImagePath(),
//                        poemTheme.getBackgroundColor(),
//                        null
//                    )
                }.setDismissListener {
                    currentContainerView = null
                }.show()
        }
    }

    /**
     * Updates all stanzas in the current poem
     *
     * @param textSize The new size of text
     */
    private fun updateAllEditTextViews(textSize: Float, action: String, color: Int) {
        when (action) {
            "textSize" -> {
                for (key in pageNumberAndId.keys) {
                    pageNumberAndId[key]?.let {
                        val currFrame = findViewById<FrameLayout>(it)
                        for (child in currFrame.children) {
                            if (child is EditText) {
                                child.textSize = textSize
                                continue
                            }
                        }
                    }
                }
                recyclerViewAdapter.addElement(textSize)
            }

            "textColor" -> {
                val coverPageAuthor = findViewById<TextView>(R.id.coverPageAuthor)
                val coverPageTitle = findViewById<TextView>(R.id.coverPageTitle)
                val coverPageSignature = findViewById<TextView>(R.id.coverPageSignature)

                coverPageAuthor.setTextColor(color)
                coverPageTitle.setTextColor(color)
                coverPageSignature.setTextColor(color)
                for (key in pageNumberAndId.keys) {
                    pageNumberAndId[key]?.let {
                        val currFrame = findViewById<FrameLayout>(it)
                        for (child in currFrame.children) {
                            if (child is EditText) {
                                child.setTextColor(color)
                                child.setHintTextColor(color)
                                continue
                            }
                        }
                    }
                }
                recyclerViewAdapter.addElement(color)
            }
        }
    }

    /**
     * Edits the size of the text
     */
    private fun setupTextSlider() {
        val textSlider = findViewById<Slider>(R.id.textSizeSlider)
        val currentEditText: EditText = currentPage.getChildAt(1) as EditText
        findViewById<TextView>(R.id.textSizeText).text = String.format(
            resources.getString(R.string.text_size_changeable),
            poemTheme.getTextSize()
        )
        textSlider.value = poemTheme.getTextSize().toFloat()
        textSlider.addOnChangeListener { _: Slider, value: Float, _: Boolean ->
            findViewById<TextView>(R.id.textSizeText).text = String.format(
                resources.getString(R.string.text_size_changeable),
                value.roundToInt()
            )
            poemTheme.setTextSize(value.roundToInt())

            val newTextSize = poemTheme.getTextSize().toFloat()
            currentEditText.textSize = poemTheme.getTextSize().toFloat()
            updateAllEditTextViews(newTextSize, "textSize", 0)
            actuateSavePoemTheme()
//            val poemThemeXmlParser = PoemThemeXmlParser(poemTheme, applicationContext)
//            if (poemThemeXmlParser.savePoemThemeToLocalFile(
//                    poemTheme.getImagePath(),
//                    poemTheme.getBackgroundColor(),
//                    null
//                ) != 0
//            ) {
//                println("Could not save poem theme")
//            }
        }
    }

    /**
     *
     */
    private fun setupSaveButton() {
        val saveButton = findViewById<ImageButton>(R.id.saveOptions)
        val allOptionsContainer = findViewById<LinearLayout>(R.id.allOptionsContainer)
        val saveContainer = findViewById<LinearLayout>(R.id.saveOptionsContainer)

        saveButton.setOnClickListener {
            Toast.makeText(
                applicationContext,
                "Saves the current poem by desired choice",
                Toast.LENGTH_LONG
            ).show()
        }
        saveButton.setOnClickListener {
            if (saveContainer.visibility == View.VISIBLE) {
                saveContainer.visibility = View.GONE
                allOptionsContainer.visibility = View.GONE
                currentContainerView = null
            } else {
                turnOffCurrentView()
                currentContainerView = saveContainer
                allOptionsContainer.visibility = View.VISIBLE
                saveContainer.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Creates a thumbnail for a poem
     * @param createThumbnail if true a thumbnail is created, if false it is not created.
     */
    private fun createThumbnail(createThumbnail: Boolean, isProgressBarActive: Boolean) {
        if (createThumbnail) {
            currentPage.visibility = View.GONE
            turnOffCurrentView()
            val coverPage = findViewById<RelativeLayout>(R.id.coverPage)
            coverPage.visibility = View.VISIBLE
            val viewToPreview = window.decorView.findViewById<RelativeLayout>(R.id.coverPage)
            findViewById<ShapeableImageView>(R.id.coverPageImage)
            viewToPreview.visibility = View.VISIBLE
            findViewById<FrameLayout>(R.id.coverPageBackground).bringToFront()
            findViewById<TextView>(R.id.coverPageAuthor).bringToFront()
            findViewById<TextView>(R.id.coverPageTitle).bringToFront()
            findViewById<TextView>(R.id.coverPageSignature).bringToFront()

            viewToPreview.post {
                val bitmap = Bitmap.createBitmap(
                    1080,
                    1080,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)


                viewToPreview.draw(canvas)

                try {
                    val thumbnailsFolder = applicationContext.getDir(
                        getString(R.string.thumbnails_folder_name),
                        MODE_PRIVATE
                    )
                    val encodedTitle = poemTheme.getTitle().replace(' ', '_')

                    if (thumbnailsFolder.exists()) {
                        val newThumbnailFile =
                            File(thumbnailsFolder.absolutePath + File.separator + encodedTitle + ".png")
                        if (!newThumbnailFile.exists()) {
                            if (newThumbnailFile.createNewFile()) {
                                val outputStream = FileOutputStream(newThumbnailFile)
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                                outputStream.close()
                            }
                        } else {
                            val outputStream = FileOutputStream(newThumbnailFile, false)
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                        }
                    }
                    coverPage.visibility = View.GONE

                    if (!isProgressBarActive)
                        currentPage.visibility = View.VISIBLE

                } catch (e: Exception) {
                    e.printStackTrace()
                    coverPage.visibility = View.GONE
                    turnOffDimmerProgressBar()
                    currentPage.visibility = View.VISIBLE
                    showErrorToast(getString(R.string.error_type_thumbnail))
                }
            }
        }
    }

    /**
     * Shows an error toast
     */
    private fun showErrorToast(errorType: String) {
        Toast.makeText(
            applicationContext,
            getString(R.string.error_toast, errorType),
            Toast.LENGTH_LONG
        ).show()

    }

    /**
     * Shows that the file was successfully added as saveType
     */
    private fun showSuccessToast(saveType: String) {

        Toast.makeText(
            applicationContext,
            getString(R.string.save_toast, poemTheme.getTitle(), saveType),
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Creates a poem data structure and writes to a file together with the thumbnail
     *
     */
    private suspend fun createDataContainer(category: String, createThumbnail: Boolean) {
        val entirePoem = ArrayList<Editable>()

        for (keys in pageNumberAndId.keys) {
            val frame = pageNumberAndId[keys]?.let { findViewById<FrameLayout>(it) }
            val editText = frame?.getChildAt(1) as EditText
            pageNumberAndText[keys] = editText.text.toString()
            entirePoem.add(editText.text)
        }

        val categoryToAdd = CategoryUtils.stringToCategory(category)
        val poemDataContainer = PoemDataContainer(categoryToAdd, entirePoem, poemTheme)
        poemDataContainer.setPages(pages)

        createThumbnail(createThumbnail, true)
        val poemParser = PoemXMLParser(poemDataContainer, applicationContext)

        if (poemTheme.backgroundType.toString().contains("IMAGE")) {
            for (child in currentPage.children) {
                if (child is ShapeableImageView) {
                    if (!poemParser.saveBackgroundImageDrawable(child.drawToBitmap(Bitmap.Config.ARGB_8888)))
                        println("Could not save image background Drawable error unknown")
                }
            }
        } else if (!poemParser.saveBackgroundImageDrawable(
                currentPage.background.toBitmap(
                    1920,
                    1080,
                    Bitmap.Config.ARGB_8888
                )
            )
        )
            println("Could not save image background Drawable error unknown")

        when (poemParser.saveToXmlFile()) {
            0 -> {
                showSuccessToast(getString(R.string.error_type_file))
            }
            -1 -> {
                turnOffCurrentView()
                showErrorToast(getString(R.string.error_type_file))
            }

        }
    }

    /**
     * Returns all edit texts editable
     */
    private fun getAllTypedText(): ArrayList<Editable> {
        val editableArrayList = ArrayList<Editable>()
        for (key in pageNumberAndId.keys) {
            val currFrame =
                pageNumberAndId[key]?.let { findViewById<FrameLayout>(it) }
            val editText = currFrame?.getChildAt(1) as EditText
            editableArrayList.add(editText.text)
        }
        return editableArrayList
    }

    /**
     * Initiates the saving of pages as PDF
     */
    private fun initiateSavePagesAsPdf() {
        val textMargin: Int = if (orientation == "portrait" && poemTheme.backgroundType.toString()
                .contains("OUTLINE")
        )
            resources.getDimensionPixelSize(R.dimen.portraitStrokeSizeMarginText)
        else
            resources.getDimensionPixelSize(R.dimen.strokeSizeMarginText)

        val strokeMargin = if (orientation == "portrait" && poemTheme.backgroundType.toString()
                .contains("OUTLINE")
        )
            resources.getDimensionPixelSize(R.dimen.portraitStrokeSize)
        else if (orientation == "landscape" && poemTheme.backgroundType.toString()
                .contains("OUTLINE")
        )
            resources.getDimensionPixelSize(R.dimen.strokeSize)
        else
            0

        try {
            this.also {
                // Get a PrintManager instance
                val printManager = this.getSystemService(Context.PRINT_SERVICE) as PrintManager
                // Set job name, which will be displayed in the print queue
                val jobName = "${applicationContext.getString(R.string.app_name)} Document"
                // Start a print job, passing in a PrintDocumentAdapter implementation
                // to handle the generation of a print document
                printManager.print(
                    jobName,
                    PdfPrintAdapter(
                        applicationContext,
                        poemTheme.getTextSize(),
                        getAllTypedText(),
                        poemTheme.getTitle(),
                        currentPage,
                        this,
                        Pair(strokeMargin, textMargin),
                        poemTheme.getTextAlignment()
                    ),
                    null
                )

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Initiates the process of saving an image
     */
    private suspend fun initiateSavePagesAsImages() {
        val editableArrayList = getAllTypedText()
        val strokeSize: Int = getTextMarginSize()
        val imageMargins =
            if (orientation == "portrait" && currentPage.background != null && currentPage.background !is ColorDrawable)
                resources.getDimensionPixelSize(R.dimen.portraitStrokeSize)
            else if (orientation == "landscape" && currentPage.background != null && currentPage.background !is ColorDrawable)
                resources.getDimensionPixelSize(R.dimen.strokeSize)
            else
                0
        val isLandscape = orientation == "landscape"

        val imageSaverUtil =
            ImageSaverUtil(applicationContext, currentPage, poemTheme.getTextSize())

        val isCenterVertical = poemTheme.getTextAlignment().toString().contains("CENTRE_VERTICAL")

        imageSaverUtil.setPaintAlignment(poemTheme.getTextAlignment())
        if (imageSaverUtil.savePagesAsImages(
                editableArrayList,
                poemTheme.getTitle(),
                strokeSize,
                imageMargins,
                isLandscape,
                isCenterVertical
            ) == 0
        )
            showSuccessToast(getString(R.string.error_type_image))
        else
            showErrorToast(getString(R.string.error_type_image))
    }

    /**
     * Turns on Dimmer with progress bar
     */
    private fun turnOnDimmerProgressBar() {
        val progressBar = findViewById<ProgressBar>(R.id.progessBar)
        val dimmer = findViewById<FrameLayout>(R.id.backgroundDim)
        dimmer.visibility = View.VISIBLE
        dimmer.bringToFront()
        dimmer.z = 5f
        progressBar.z = 5f
        progressBar.visibility = View.VISIBLE
        progressBar.bringToFront()
    }

    private fun turnOffDimmerProgressBar(){
        val progressBar = findViewById<ProgressBar>(R.id.progessBar)
        val dimmer = findViewById<FrameLayout>(R.id.backgroundDim)

        progressBar.visibility = View.GONE
        dimmer.visibility = View.GONE
    }
    /**
     * Initialises the listeners for the save container
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun setupSaveContainerButtons() {
        val categoryChoices = arrayOf(
            Category.ADVENTURE.toString(),
            Category.FANTASY.toString(),
            Category.FUNNY.toString(),
            Category.NONE.toString(),
            Category.ROMANTIC.toString(),
            Category.SOUL.toString(),
            Category.VIVID.toString()
        )
        val saveAsFile = findViewById<TextView>(R.id.fileSave)
        val saveAsImage = findViewById<TextView>(R.id.imageSaveContainer)
        val saveAsPdf = findViewById<TextView>(R.id.pdfSaveContainer)
        val editPoemTheme = findViewById<TextView>(R.id.editPoemTheme)


        saveAsFile.setOnClickListener {
            AlertDialog.Builder(this).setTitle(R.string.category)
                .setSingleChoiceItems(categoryChoices, 3) { dialog, chosenInt ->
                    dialog.dismiss()
                    actuateSaveAsFile(categoryChoices[chosenInt], true)
                    hasFileBeenEdited = false
                }.show()
        }

        saveAsImage.setOnClickListener {
            turnOffCurrentView()
            val exceptionHandler = CoroutineExceptionHandler { _, exception ->
                exception.printStackTrace()

                showErrorToast(getString(R.string.error_type_image))
                turnOffDimmerProgressBar()
            }
            GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
                turnOnDimmerProgressBar()
                createThumbnail(createThumbnail = true, isProgressBarActive = true)
                initiateSavePagesAsImages()
                turnOffDimmerProgressBar()
                currentPage.visibility = View.VISIBLE
            }
        }

        saveAsPdf.setOnClickListener {
            turnOffCurrentView()
            createThumbnail(createThumbnail = true, isProgressBarActive = false)
            initiateSavePagesAsPdf()
        }
        editPoemTheme.setOnClickListener {
            //to fix when categories are necessary
            actuateSaveAsFile(Category.NONE.toString(), true)
            val activityIntent = Intent(applicationContext, PoemThemeActivity::class.java)
            activityIntent.putExtra("poemThemeName", poemTheme.getTitle())
            finish()
            startActivity(activityIntent)
        }
    }

    /**
     * Turns off the current containerView if there is any
     */
    private fun turnOffCurrentView() {
        if (currentContainerView != null) {
            currentContainerView?.visibility = View.GONE
        }
        if (currentContainerView is RecyclerView) {
            findViewById<FrameLayout>(R.id.backgroundDim).visibility = View.GONE
            findViewById<ProgressBar>(R.id.progessBar).visibility =View.GONE
            findViewById<RecyclerView>(R.id.recyclerPagesContainer).visibility = View.GONE
            setEditText(currentPage, true)
        }
        currentContainerView = null
    }

    /**
     * Initialises bottom drawer. The bottom drawer has five buttons. Each button has its own layout
     * when it has been clicked. This latter mentioned layout can only belong to one of the buttons
     * No two layouts can be visible at the same time. The global variable currentContainerView is used
     * to keep track of the current displayed view. If another view is active when a button is clicked
     * then the prior layout disappears in favour of the latest clicked button. Secondly clicking on the
     * button once its displayed turns the display off
     */
    private fun initialiseBottomDrawer() {
        setupPageButton()
        setupTextOptions()
        setupTextOptionsContainerButtons()
        setupTextSizeContainer()
        setupTextSlider()
        setupTextColor()
        setupSaveButton()
        setupSaveContainerButtons()
    }

    /**
     * Loads the corresponding frame layout views for each stanza
     * @param stanzas The arrayList containing the each stanzas saved text
     */
    private fun loadSavedPoem(stanzas: ArrayList<String>) {
        val firstPageEditText = currentPage.getChildAt(1) as EditText
        firstPageEditText.text = SpannableStringBuilder(stanzas[0])
        pageNumberAndText[currentPage.tag as Int] = stanzas[0]
        recyclerViewAdapter.addElement(currentPage, currentPage.tag as Int)
        var counter = 1

        while (counter < stanzas.size) {
            pages++
            val frameLayout = createNewPage(true)
            val frameEditText = frameLayout.getChildAt(1) as EditText
            frameEditText.textSize = poemTheme.getTextSize().toFloat()
            frameEditText.text = SpannableStringBuilder(stanzas[counter])
            pageNumberAndText[frameLayout.tag as Int] = stanzas[counter]
            recyclerViewAdapter.addElement(frameLayout, frameLayout.tag as Int)
            frameLayout.visibility = View.GONE
            counter++
        }
        currentPage.bringToFront()
    }

    /**
     * Create  memory for current poem theme container
     * @param poemThemeXmlParser The poem theme parser with users saved theme preference
     */
    private fun initialisePoemTheme(poemThemeXmlParser: PoemThemeXmlParser) {
        poemTheme.setTitle(poemThemeXmlParser.getPoemTheme().getTitle())
        poemTheme.backgroundType = poemThemeXmlParser.getPoemTheme().backgroundType
        poemTheme.setTextFont(poemThemeXmlParser.getPoemTheme().getTextFont())
        poemTheme.setTextAlignment(poemThemeXmlParser.getPoemTheme().getTextAlignment())
        poemTheme.setOutline(poemThemeXmlParser.getPoemTheme().getOutline())
        poemTheme.setOutlineColor(poemThemeXmlParser.getPoemTheme().getOutlineColor())
        poemTheme.setTextColorAsInt(poemThemeXmlParser.getPoemTheme().getTextColorAsInt())
        poemTheme.setTextColor(poemThemeXmlParser.getPoemTheme().getTextColor())
        poemTheme.setTextSize(poemThemeXmlParser.getPoemTheme().getTextSize())
        poemTheme.setBackgroundColorAsInt(
            poemThemeXmlParser.getPoemTheme().getBackgroundColorAsInt()
        )
        poemTheme.setBackgroundColor(poemThemeXmlParser.getPoemTheme().getBackgroundColor())
        poemTheme.setImagePath(poemThemeXmlParser.getPoemTheme().getImagePath())
    }

}