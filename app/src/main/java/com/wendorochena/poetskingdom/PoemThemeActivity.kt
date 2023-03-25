package com.wendorochena.poetskingdom

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.setMargins
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.slider.Slider
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.PoemThemeXmlParser
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.recyclerViews.ImageRecyclerViewAdapter
import com.wendorochena.poetskingdom.utils.ShapeAppearanceModelHelper
import com.wendorochena.poetskingdom.utils.TypefaceHelper
import kotlinx.coroutines.*
import java.io.File
import kotlin.math.round
import kotlin.math.roundToInt

class PoemThemeActivity : AppCompatActivity() {
    private lateinit var mainLinearLayout: LinearLayout
    private var currentView = "Outline"
    private var recyclerView: RecyclerView? = null
    private var outlineChosen: View? = null
    private var backgroundImageChosen: String? = null
    private var backgroundColorChosen: String? = null
    private var backgroundColorChosenAsInt: Int? = null
    private lateinit var recyclerViewAdapter: ImageRecyclerViewAdapter
    private lateinit var poemTheme: PoemTheme


    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poem_theme)
        mainLinearLayout = findViewById(R.id.mainLinearLayoutContainer)
        findViewById<RelativeLayout>(R.id.backgroundPreview).bringChildToFront(
            findViewById<TextView>(
                R.id.previewText
            )
        )
        findViewById<FrameLayout>(R.id.text).setOnClickListener(
            populateView("Text", "$packageName:id/text")
        )
        findViewById<FrameLayout>(R.id.outline).setOnClickListener(
            populateView("Outline", "$packageName:id/outline")
        )
        findViewById<FrameLayout>(R.id.background).setOnClickListener(
            populateView("Background", "$packageName:id/images")
        )
        setupCreatePoemListener()
        setupOutlineListeners()
        setupTextListeners()
        setupBackgroundListeners()
        setupPreviewListeners()
        poemTheme = PoemTheme(BackgroundType.DEFAULT, applicationContext)
        val intentExtras = intent.extras
        if (intentExtras?.getString("poemThemeName") != null) {
            val poemName = intentExtras.getString("poemThemeName")
            val poemThemeXmlParser = PoemThemeXmlParser(
                PoemTheme(BackgroundType.DEFAULT, applicationContext),
                applicationContext
            )
            val handler = CoroutineExceptionHandler { _, exception ->
                exception.printStackTrace()
                val builder = MaterialAlertDialogBuilder(this@PoemThemeActivity)
                builder.setTitle(R.string.failed_poem_load_title)
                    .setMessage(R.string.failed_poem_load_message)
                    .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
            }

            GlobalScope.launch(Dispatchers.Main + handler) {
                if (poemThemeXmlParser.parseTheme(poemName) == 0) {
                    initialisePoemTheme(poemThemeXmlParser)
                    findViewById<Button>(R.id.startPoemCreation).setText(R.string.edit_button_theme)
                } else {
                    val builder = MaterialAlertDialogBuilder(this@PoemThemeActivity)
                    builder.setTitle(R.string.failed_poem_load_title)
                        .setMessage(R.string.failed_poem_load_message)
                        .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }.show()
                }
            }
        }
        setupSliderListener()
        val sharedPreferences =
            applicationContext.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("outlineFirstUse", false)) {
            onFirstUseGuide("outline")
            sharedPreferences.edit().putBoolean("outlineFirstUse", true).apply()
        }
    }

    /**
     * This is for testing purposes I REPEAT DO NOT UNCOMMENT IT IS HIGHLY INSECURE
     */
//    fun setBackground(string: String) {
//        backgroundImageChosen = string
//    }
//
//    fun getBackgroundImage(): String? {
//        return backgroundImageChosen
//    }
//
//    fun getAdapter(): ImageRecyclerViewAdapter {
//        return recyclerViewAdapter
//    }
//
//    //These getters are here for testing purposes
//    fun getOutlineChosen(): View? {
//        return outlineChosen
//    }
//
//    fun getBackgroundColorChosen(): Int? {
//        return backgroundColorChosenAsInt
//    }
//
//    fun getPoemTheme(): PoemTheme {
//        return poemTheme
//    }
//
//    fun getCurrentView(): String {
//        return currentView
//    }

    private fun onFirstUseGuide(headerName: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.guide_title)
            .setPositiveButton(R.string.builder_understood) { dialog, _ ->
                dialog.dismiss()
            }
        when (headerName) {
            "outline" -> {
                alertDialogBuilder.setMessage(R.string.guide_outline)
            }
            "background" -> {
                alertDialogBuilder.setMessage(R.string.guide_background)
            }
            "text" -> {
                alertDialogBuilder.setMessage(R.string.guide_text)
            }
        }
        alertDialogBuilder.show()
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
     * This function launches a new activity but firsts writes poem data to a local file
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun setupCreatePoemListener() {

        findViewById<Button>(R.id.startPoemCreation).setOnClickListener { newButton ->
            val currButton = newButton as Button
            if (currButton.text == getString(R.string.edit_button_theme)) {
                val poemThemeXmlParser =
                    PoemThemeXmlParser(poemTheme, applicationContext)
                poemThemeXmlParser.setIsEditTheme(true)

                val exceptionHandler = CoroutineExceptionHandler { _, exception ->
                    exception.printStackTrace()
                    //add a better wayy to manage failure
                    println("Error saving file")
                }

                GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
                    if (poemThemeXmlParser.savePoemThemeToLocalFile(
                        backgroundImageChosen,
                        backgroundColorChosen,
                        outlineChosen
                    ) == 0
                ) {
                    val newActivityIntent =
                        Intent(applicationContext, CreatePoem::class.java)
                    newActivityIntent.putExtra("loadPoem", true)
                    newActivityIntent.putExtra(
                        "poemTitle",
                        poemTheme.getTitle()
                    )
                    finish()
                    startActivity(newActivityIntent)

                }
                }
            } else {
                val builder: AlertDialog.Builder = this@PoemThemeActivity.let {
                    AlertDialog.Builder(it)
                }
                //create textview
                val textView = TextView(this)
                textView.setTextColor(resources.getColor(R.color.white, null))
                textView.text = resources.getString(R.string.create_poem_title)
                textView.setTypeface(null, Typeface.BOLD)
                textView.gravity = Gravity.CENTER
                textView.textSize = resources.getDimension(R.dimen.normal_text_size)
                builder.setCustomTitle(textView)
                //user input
                val editText = EditText(this)
                editText.setHint(R.string.create_poem_edit_text_hint)
                editText.setHintTextColor(Color.WHITE)
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q)
                    editText.textCursorDrawable = null

                editText.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_CLASS_TEXT)
                editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(60))
                editText.setTextColor(resources.getColor(R.color.white, null))
                builder.setView(editText)
                builder.apply {
                    setPositiveButton(R.string.confirm, null)
                }

                val dialog: AlertDialog = builder.create()
                dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                    ?.setTextColor(resources.getColor(R.color.white, null))
                dialog.setOnShowListener {
                    val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    button.setTextColor(resources.getColor(R.color.white, null))
                    button.setOnClickListener {
                        if (button.text == resources.getString(R.string.retry)) {
                            textView.text = resources.getString(R.string.create_poem_title)
                            button.text = resources.getString(R.string.confirm)
                            editText.visibility = View.VISIBLE
                            editText.isEnabled = true
                            editText.setText("")
                        } else if (isValidatedInput(editText.text.toString().replace(' ', '_'))) {
                            poemTheme.setTitle(editText.text.toString())
                            val poemThemeXmlParser =
                                PoemThemeXmlParser(poemTheme, applicationContext)

                            val exceptionHandler = CoroutineExceptionHandler { _, exception ->
                                exception.printStackTrace()
                                // add a better wayy to manage failure
                                println("Error saving file")
                            }

                            GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
                                if (poemThemeXmlParser.savePoemThemeToLocalFile(
                                        backgroundImageChosen,
                                        backgroundColorChosen,
                                        outlineChosen
                                    ) == 0
                                ) {
                                    dialog.dismiss()
                                    val newActivityIntent =
                                        Intent(applicationContext, CreatePoem::class.java)
                                    newActivityIntent.putExtra(getString(R.string.load_poem_argument_name), false)
                                    newActivityIntent.putExtra(
                                        getString(R.string.poem_title_argument_name),
                                        poemTheme.getTitle()
                                    )
                                    finish()
                                    startActivity(newActivityIntent)

                                } else {
                                    textView.text = resources.getString(R.string.invalid_input)
                                    button.text = resources.getString(R.string.retry)
                                    editText.isEnabled = false
                                    editText.setText(resources.getString(R.string.file_already_exists))
                                }
                            }
                        } else {
                            textView.text = resources.getString(R.string.invalid_input)
                            button.text = resources.getString(R.string.retry)
                            editText.isEnabled = false
                            editText.setText(resources.getString(R.string.invalid_input_message))
                        }
                    }
                }
                dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_rectangle)
                dialog.show()
            }
        }
    }

    /**
     *
     */
    private fun populateView(viewToPopulate: String, idPrefix: String): View.OnClickListener {
        return View.OnClickListener {
            val sharedPreferences =
                applicationContext.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            if (currentView != viewToPopulate) {
                when (currentView) {
                    "Outline" -> {
                        findViewById<FrameLayout>(R.id.outline).background = null
                        disableView("$packageName:id/outline")
                    }
                    "Background" -> {
                        findViewById<FrameLayout>(R.id.background).background = null
                        disableView("$packageName:id/images")
                    }
                    "Text" -> {
                        findViewById<FrameLayout>(R.id.text).background = null
                        disableView("$packageName:id/text")
                    }
                }
                currentView = viewToPopulate
                when (viewToPopulate) {
                    "Background" -> {
                        if (!sharedPreferences.getBoolean("backgroundFirstUse", false)) {
                            sharedPreferences.edit().putBoolean("backgroundFirstUse", true).apply()
                            onFirstUseGuide("background")

                        }
                        val drawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.heading_background,
                            null
                        )
                        findViewById<FrameLayout>(R.id.background).background = drawable
                    }
                    "Outline" -> {
                        val drawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.heading_background,
                            null
                        )
                        findViewById<FrameLayout>(R.id.outline).background = drawable
                    }
                    "Text" -> {
                        if (!sharedPreferences.getBoolean("textFirstUse", false)) {
                            sharedPreferences.edit().putBoolean("textFirstUse", true).apply()
                            onFirstUseGuide("text")
                        }
                        val drawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.heading_background,
                            null
                        )
                        findViewById<FrameLayout>(R.id.text).background = drawable
                    }
                }
                enableCorrectView(idPrefix)
                if (currentView == "Background")
                    populateBackground()
            }
        }
    }

    /**
     * @param idPrefix the prefix of the Id's to enable
     */
    private fun enableCorrectView(idPrefix: String) {
        var hasEncounteredFirstIdOccurrence = false
        for (child in mainLinearLayout.children) {
            val isCorrectIdPrefix =
                resources.getResourceName(child.id).startsWith(idPrefix)
            // if we have reached an id starting with text enable it
            if (!hasEncounteredFirstIdOccurrence && isCorrectIdPrefix ||
                hasEncounteredFirstIdOccurrence && isCorrectIdPrefix
            ) {
                child.visibility = View.VISIBLE
                if (!hasEncounteredFirstIdOccurrence)
                    hasEncounteredFirstIdOccurrence = true
            } else if (hasEncounteredFirstIdOccurrence)
                break
        }
    }

    /**
     * @param idPrefix the prefix of the Id's to disable
     */
    private fun disableView(idPrefix: String) {
        var hasEncounteredFirstIdOccurrence = false
        for (child in mainLinearLayout.children) {
            val isCorrectIdPrefix =
                resources.getResourceName(child.id).startsWith(idPrefix)
            // if we have encountered the first id starting with outline disable its view
            if (!hasEncounteredFirstIdOccurrence && isCorrectIdPrefix ||
                (hasEncounteredFirstIdOccurrence && isCorrectIdPrefix)
            ) {

                child.visibility = View.GONE
                if (!hasEncounteredFirstIdOccurrence)
                    hasEncounteredFirstIdOccurrence = true
            }
            // This means we have reached the ent of outline ids and no need to continue
            else if (hasEncounteredFirstIdOccurrence)
                break
        }
        if (idPrefix == "$packageName:id/images")
            findViewById<RecyclerView>(R.id.recyclerImageContainer).visibility = View.GONE
    }

    /**
     * This function sets the recycler view with the locally known images.
     */
    private fun populateBackground() {
        if (recyclerView != null) {
            recyclerView?.visibility = View.VISIBLE
        } else {
            recyclerView = findViewById(R.id.recyclerImageContainer)
            recyclerView?.visibility = View.VISIBLE

            val myImagesFolder = applicationContext?.getDir("myImages", Context.MODE_PRIVATE)

            if (myImagesFolder != null) {
                if (myImagesFolder.listFiles() != null) {
                    val imagesFolder = myImagesFolder.listFiles()
                    imagesFolder?.sortByDescending { it.lastModified() }
                    if (imagesFolder != null) {
                        recyclerViewAdapter =
                            ImageRecyclerViewAdapter(
                                this.javaClass.name,
                                imagesFolder.toCollection(ArrayList()),
                                applicationContext
                            )
                    }

                    val gridLayoutManager = GridLayoutManager(applicationContext, 4)
                    recyclerView?.layoutManager = gridLayoutManager
                    recyclerView?.adapter = recyclerViewAdapter
                    recyclerViewAdapter.onItemClick = { imageFileClicked, _ ->
                        //reset background color
                        if (backgroundColorChosen != null) {
                            backgroundColorChosen = null
                            backgroundColorChosenAsInt = null
                            if (outlineChosen != null) {
                                val background =
                                    outlineChosen?.background?.constantState?.newDrawable() as GradientDrawable
                                background.color = null
                                findViewById<FrameLayout>(R.id.outlinePreview).background =
                                    background
                            }
                            findViewById<RelativeLayout>(R.id.backgroundPreview).setBackgroundColor(
                                getColor(R.color.white)
                            )
                        }

                        backgroundImageChosen = imageFileClicked.absolutePath
                        preparePoemView("Background")
                        if (outlineChosen != null)
                            poemTheme.setBackGroundType(BackgroundType.OUTLINE_WITH_IMAGE)
                        else
                            poemTheme.setBackGroundType(BackgroundType.IMAGE)
                    }
                }
            }
        }
    }

    /**
     * Adjust the bounds of the text view so it does not intrude into the boarder of the outline
     */
    private fun adjustPreviewTextBounds(outlineShape: String): RelativeLayout.LayoutParams {
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(
            resources.getDimensionPixelSize(R.dimen.previewWithOutlineTextMarginRectangle)
        )
        when (outlineShape) {
            getString(R.string.rounded_rectangle_outline_description) -> {
                layoutParams.setMargins(
                    resources.getDimensionPixelSize(R.dimen.previewWithOutlineTextMarginRectangle)
                )
            }
            getString(R.string.teardrop_description_outline) -> {
                layoutParams.setMargins(
                    resources.getDimensionPixelSize(R.dimen.previewWithOutlineTextMarginTeardrop)
                )
            }
            getString(R.string.rotated_teardrop_outline) -> {
                layoutParams.setMargins(
                    resources.getDimensionPixelSize(R.dimen.previewWithOutlineTextMarginTeardrop)
                )
            }
            getString(R.string.lemon_outline_description) -> {
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
     * @param selection: The subcategory adding to the preview
     * This function shows the preview of how the poem will look
     */
    private fun preparePoemView(selection: String) {
        val previewLayout = findViewById<RelativeLayout>(R.id.backgroundPreview)
        val outlineLayout = findViewById<FrameLayout>(R.id.outlinePreview)
        val imageBackground = findViewById<ShapeableImageView>(R.id.imagePreview)
        val previewCardView = findViewById<CardView>(R.id.imagePreviewCard)
        val previewText = findViewById<TextView>(R.id.previewText)
        val strokeSize = resources.getDimensionPixelSize(R.dimen.strokeSize)

        when (selection) {
            "Outline" -> {
                if (outlineChosen != null) {
                    if (backgroundImageChosen != null) {
                        try {
                            imageBackground.shapeAppearanceModel =
                                ShapeAppearanceModelHelper.shapeImageView(
                                    outlineChosen!!.contentDescription as String,
                                    resources,
                                    strokeSize.toFloat()
                                )
                            Glide.with(applicationContext).load(backgroundImageChosen).into(imageBackground)
                            val colorDrawable =
                                ColorDrawable(getColor(R.color.default_background_color))

                            val layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                            layoutParams.setMargins(strokeSize)
                            imageBackground.layoutParams = layoutParams
                            previewCardView.background = colorDrawable
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    outlineLayout.background =
                        outlineChosen?.background?.constantState?.newDrawable()
                    previewText.layoutParams =
                        adjustPreviewTextBounds(outlineChosen!!.contentDescription as String)
                    previewLayout.bringChildToFront(outlineLayout)
                }
            }
            "Background" -> {
                if (backgroundImageChosen != null) {
                    val backGroundFile = backgroundImageChosen?.let { File(it) }

                    if (backGroundFile != null) {
                        if (previewCardView.visibility != View.VISIBLE) {
                            previewCardView.visibility = View.VISIBLE
                        }

                        if (backGroundFile.exists()) {
                            if (outlineChosen != null) {
                                imageBackground.shapeAppearanceModel =
                                    ShapeAppearanceModelHelper.shapeImageView(
                                        outlineChosen!!.contentDescription as String,
                                        resources,
                                        strokeSize.toFloat()
                                    )

                                val layoutParams = FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT
                                )
                                layoutParams.setMargins(strokeSize)
                                imageBackground.layoutParams = layoutParams
                                val colorDrawable =
                                    ColorDrawable(getColor(R.color.default_background_color))
                                previewCardView.background = colorDrawable
                            } else
                                resetImageView()
                            Glide.with(applicationContext).load(backGroundFile.absolutePath).into(imageBackground)
                            previewLayout.bringChildToFront(previewText)
                        }
                    }
                }
            }
            "OutlineWithColor" -> {
                if (outlineChosen != null) {
//                    outlineChosen?.id?.let { outlinesChanged.add(it) }
                    val gradientDrawable: GradientDrawable =
                        outlineChosen?.background?.constantState?.newDrawable() as GradientDrawable
                    backgroundColorChosenAsInt?.let { gradientDrawable.setColor(it) }
                    outlineLayout.background = gradientDrawable
                    previewText.layoutParams =
                        adjustPreviewTextBounds(outlineChosen!!.contentDescription as String)
                    previewLayout.bringChildToFront(outlineLayout)
                }
            }
        }
    }

    /**
     * Changes the colour of the outline
     */
    private fun changeLayoutColor(frameLayout: FrameLayout) {

        ColorPickerDialog
            .Builder(this)                        // Pass Activity Instance
            .setTitle(getString(R.string.color_picker_title))            // Default "Choose Color" // Default ColorShape.CIRCLE
            .setDefaultColor(R.color.madzinza_green)     // Pass Default Color
            .setColorListener { color, _ ->
                // for performance reasons we have to do this
                when (frameLayout.contentDescription as String) {
                    getString(R.string.rounded_rectangle_outline_description) -> {
                        val gradientDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.rounded_rectangle_outline,
                            null
                        ) as GradientDrawable
                        gradientDrawable.setStroke(
                            resources.getDimensionPixelSize(R.dimen.strokeSize), color
                        )
                        frameLayout.background = gradientDrawable.mutate()
                    }
                    getString(R.string.teardrop_description_outline) -> {
                        val gradientDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.teardrop_outline,
                            null
                        ) as GradientDrawable
                        gradientDrawable.setStroke(
                            resources.getDimensionPixelSize(R.dimen.strokeSize), color
                        )
                        frameLayout.background = gradientDrawable.mutate()
                    }
                    getString(R.string.rotated_teardrop_outline) -> {
                        val gradientDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.rotated_teardrop,
                            null
                        ) as GradientDrawable
                        gradientDrawable.setStroke(
                            resources.getDimensionPixelSize(R.dimen.strokeSize), color
                        )
                        frameLayout.background = gradientDrawable.mutate()
                    }
                    getString(R.string.rectangle_outline_description) -> {
                        val gradientDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.rectangle_outline,
                            null
                        ) as GradientDrawable
                        gradientDrawable.setStroke(
                            resources.getDimensionPixelSize(R.dimen.strokeSize), color
                        )
                        frameLayout.background = gradientDrawable.mutate()
                    }
                    getString(R.string.lemon_outline_description) -> {
                        val gradientDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.lemon_outline,
                            null
                        ) as GradientDrawable
                        gradientDrawable.setStroke(
                            resources.getDimensionPixelSize(R.dimen.strokeSize), color
                        )
                        frameLayout.background = gradientDrawable.mutate()
                    }
                }
                poemTheme.setOutlineColor(color)
            }
            .show()
    }

    /**
     * This function finds all outline instances and sets an onclick listener
     * An onLongClick Listener is also
     */
    private fun setupOutlineListeners() {

        for (child in mainLinearLayout.children) {
            if (resources.getResourceName(child.id).startsWith("$packageName:id/outline")) {
                if (child is LinearLayout) {
                    if (child.childCount > 0) {
                        for (frameLayoutChild in child.children) {
                            if (frameLayoutChild is FrameLayout) {
                                frameLayoutChild.setOnClickListener {
                                    outlineChosen = frameLayoutChild
                                    if (backgroundImageChosen == null && backgroundColorChosen == null) {
                                        poemTheme.setBackGroundType(BackgroundType.OUTLINE)
                                        preparePoemView("Outline")
                                    } else if (backgroundImageChosen != null) {
                                        poemTheme.setBackGroundType(BackgroundType.OUTLINE_WITH_IMAGE)
                                        preparePoemView("Outline")
                                    } else {
                                        findViewById<RelativeLayout>(R.id.backgroundPreview).setBackgroundColor(
                                            getColor(R.color.white)
                                        )
                                        preparePoemView("OutlineWithColor")
                                        poemTheme.setBackGroundType(BackgroundType.OUTLINE_WITH_COLOR)
                                    }
                                }

                                frameLayoutChild.setOnLongClickListener {
                                    changeLayoutColor(frameLayoutChild)
                                    true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This function updates the text to show the current text size
     * Further it updates the poem theme data structure
     */
    private fun setupSliderListener() {
        val textSlider = findViewById<Slider>(R.id.textSizeSlider)

        if (poemTheme.getTextSize() != 14) {
            findViewById<TextView>(R.id.textSizeText).text = String.format(
                resources.getString(R.string.text_size_changeable),
                poemTheme.getTextSize()
            )
            textSlider.value = poemTheme.getTextSize().toFloat()
        }

        textSlider.addOnChangeListener { _: Slider, value: Float, _: Boolean ->
            findViewById<TextView>(R.id.textSizeText).text = String.format(
                resources.getString(R.string.text_size_changeable),
                value.roundToInt()
            )
            findViewById<TextView>(R.id.previewText).setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                round(value)
            )
            poemTheme.setTextSize(value.roundToInt())
        }
    }

    /**
     * Creates a font parent for each even element in the strings array element
     * If an odd one is then left out at the end it gets its own layout
     */
    private fun createFontParent(fontFamily: String): LinearLayout {
        val linearToRet = LinearLayout(applicationContext)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics)
                .roundToInt()
        linearToRet.layoutParams = layoutParams
        when (fontFamily) {
            "ariana_violeta_font" -> {
                linearToRet.id = R.id.textariana_violeta_font
            }
            "cabal_bold_font" -> {
                linearToRet.id = R.id.textcabal_bold_font
            }
            "flaemische_font" -> {
                linearToRet.id = R.id.textflaemische_font
            }
            "opensansregular_font" -> {
                linearToRet.id = R.id.textopensansregular_font
            }
            "scriptin_font" -> {
                linearToRet.id = R.id.textscriptin_font
            }
            "thesignature_font" -> {
                linearToRet.id = R.id.textthesignature_font
            }
            "ayuma_font" -> {
                linearToRet.id = R.id.textayuma_font
            }
            "clicker_script_font" -> {
                linearToRet.id = R.id.textclicker_script_font
            }
            "crimson_bold_font" -> {
                linearToRet.id = R.id.textcrimson_bold_font
            }
            "dense_regular_font" -> {
                linearToRet.id = R.id.textdense_regular_font
            }
            "honey_script_font" -> {
                linearToRet.id = R.id.texthoney_script_font
            }
            "libre_baskerville_font" -> {
                linearToRet.id = R.id.textlibre_baskerville_font
            }
            "lucian_schoenschrift_font" -> {
                linearToRet.id = R.id.textlucian_schoenschrift_font
            }
            "medula_one_font" -> {
                linearToRet.id = R.id.textmedula_one_font
            }
            "nickainley_font" -> {
                linearToRet.id = R.id.textnickainely_font
            }
            "quattrocento_font" -> {
                linearToRet.id = R.id.textquattrocento_font
            }
            "sexsmith_font" -> {
                linearToRet.id = R.id.textsexsmith_font
            }
            "righteous_regular_font" -> {
                linearToRet.id = R.id.textrighteous_regular_font
            }
            "chopin_script_font" -> {
                linearToRet.id = R.id.textchopin_script_font
            }
        }

        return linearToRet
    }

    /**
     * hard coded for performance
     */
    private fun createFontChild(fontFamily: String): FrameLayout {
        val frameToRet = FrameLayout(applicationContext)
        val textViewChild = TextView(applicationContext)
        textViewChild.setTextColor(getColor(R.color.black))
        val width =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics)
                .roundToInt()
        val height =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, resources.displayMetrics)
                .roundToInt()
        val layoutParams = LinearLayout.LayoutParams(width, height)
        val textViewLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        textViewChild.layoutParams = textViewLayoutParams
        val typefaceNameArr = fontFamily.split('_')
        var fontText = ""
        for ((index, word) in typefaceNameArr.withIndex()) {
            if (index != typefaceNameArr.size - 1) {
                val wordToAdd = word[0].uppercase() + word.substring(1, word.length)
                fontText += "$wordToAdd "
            }
        }
        textViewChild.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        textViewChild.typeface = TypefaceHelper.getTypeFace(fontFamily, applicationContext)
        textViewChild.text = fontText
        textViewChild.gravity = Gravity.CENTER
        layoutParams.weight = 1f
        frameToRet.layoutParams = layoutParams
        frameToRet.contentDescription = fontFamily
        frameToRet.addView(textViewChild)

        return frameToRet
    }

    /**
     * We systemically add text views in orders of two. If we reach a case where the amount of array
     * elements is odd we singularly add it to the DOM
     * The variable counter is used to keep track of the odd element to set as parent. When counter
     * + 1 is equal to the array size it means counter is the last element in the array
     * When the variable createParentNextIteration is true it means we have reached the odd element
     * therefore create a view for the previous element and the current
     * @param idPrefix
     */
    private fun setupFontFamilyListeners(idPrefix: String) {
        if (findViewById<LinearLayout>(R.id.textariana_violeta_font) == null) {
            var counter = 0
            val totalSize = resources.getStringArray(R.array.customFontNames).size
            var createParentNextIteration = false
            val viewsToAdd = ArrayList<View>()
            for (fontName in resources.getStringArray(R.array.customFontNames)) {
                if ((counter + 1) % 2 != 0 && (counter + 1) < totalSize) {
                    createParentNextIteration = true
                    counter++
                } else if (createParentNextIteration) {
                    val createChild1 =
                        createFontChild(resources.getStringArray(R.array.customFontNames)[counter - 1])
                    val createChild2 =
                        createFontChild(fontName)
                    val linearLayoutParentOfChildren =
                        createFontParent(fontName)
                    linearLayoutParentOfChildren.addView(createChild1)
                    linearLayoutParentOfChildren.addView(createChild2)
                    linearLayoutParentOfChildren.visibility = View.GONE
                    viewsToAdd.add(linearLayoutParentOfChildren)
                    createParentNextIteration = false
                    counter++
                    mainLinearLayout.addView(linearLayoutParentOfChildren)
                } else if ((counter + 1) == totalSize) {
                    val createChild1 =
                        createFontChild(fontName)
                    val linearLayoutParentOfChildren =
                        createFontParent(fontName)
                    linearLayoutParentOfChildren.addView(createChild1)
                    linearLayoutParentOfChildren.visibility = View.GONE
                    viewsToAdd.add(linearLayoutParentOfChildren)
                    mainLinearLayout.addView(linearLayoutParentOfChildren)
                }
            }
        }

        for (child in mainLinearLayout.children) {
            if (resources.getResourceName(child.id).startsWith(idPrefix)) {
                if (child is LinearLayout) {
                    for (frameChild in child.children) {
                        if (frameChild is FrameLayout) {
                            frameChild.setOnClickListener {
                                findViewById<TextView>(R.id.previewText).typeface =
                                    (frameChild.contentDescription as String?)?.let { it1 ->
                                        TypefaceHelper.getTypeFace(
                                            it1, applicationContext
                                        )
                                    }
                                (frameChild.contentDescription as String?)?.let { it1 ->
                                    poemTheme.setTextFont(
                                        it1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This function sets up the listeners for formatting text.
     */
    private fun setupTextListeners() {
        val previewText = findViewById<TextView>(R.id.previewText)
        val textColorView = findViewById<ImageView>(R.id.textColorView)
        val leftAlign = findViewById<ImageView>(R.id.leftAlign)
        val centreAlign = findViewById<ImageView>(R.id.centreAlign)
        val rightAlign = findViewById<ImageView>(R.id.rightAlign)
        val centreVerticalAlign = findViewById<ImageView>(R.id.centreVerticalAlign)

        textColorView.setOnClickListener {
            ColorPickerDialog
                .Builder(this)                        // Pass Activity Instance
                .setTitle(getString(R.string.color_picker_title))            // Default "Choose Color"
                .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                .setDefaultColor(R.color.black)     // Pass Default Color
                .setColorListener { color, colorHex ->
                    textColorView.setColorFilter(color)
                    findViewById<TextView>(R.id.textColorText).setTextColor(color)
                    previewText.setTextColor(color)
                    poemTheme.setTextColor(colorHex)
                    poemTheme.setTextColorAsInt(color)
                }
                .show()
        }

        leftAlign.setOnClickListener {
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            previewText.layoutParams = layoutParams
            previewText.gravity = Gravity.START
            poemTheme.setTextAlignment(TextAlignment.LEFT)
        }
        centreAlign.setOnClickListener {
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            previewText.layoutParams = layoutParams
            previewText.gravity = Gravity.CENTER
            poemTheme.setTextAlignment(TextAlignment.CENTRE)
        }
        rightAlign.setOnClickListener {
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            previewText.layoutParams = layoutParams
            previewText.gravity = Gravity.END
            poemTheme.setTextAlignment(TextAlignment.RIGHT)
        }
        centreVerticalAlign.setOnClickListener {
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            previewText.layoutParams = layoutParams
            previewText.gravity = Gravity.CENTER
            poemTheme.setTextAlignment(TextAlignment.CENTRE_VERTICAL)
        }

        setupFontFamilyListeners("$packageName:id/text")
    }

    /**
     * This function  sets up a listener for the colors icon on press event
     */
    private fun setupBackgroundListeners() {
        val colorsIcon = findViewById<FrameLayout>(R.id.backgroundColorsIcon)

        colorsIcon.setOnClickListener {
            if (recyclerView != null) {
                recyclerView!!.visibility = View.GONE
            }

            ColorPickerDialog
                .Builder(this)                        // Pass Activity Instance
                .setTitle(getString(R.string.color_picker_title))            // Default "Choose Color"
                .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                .setDefaultColor(R.color.black)     // Pass Default Color
                .setColorListener { color, colorHex ->
                    if (outlineChosen != null)
                        poemTheme.setBackGroundType(BackgroundType.OUTLINE_WITH_COLOR)
                    else
                        poemTheme.setBackGroundType(BackgroundType.COLOR)

                    val imagePreviewCard = findViewById<CardView>(R.id.imagePreviewCard)
                    if (imagePreviewCard.visibility == View.VISIBLE) {
                        imagePreviewCard.visibility = View.GONE
                        backgroundImageChosen = null
                    }
                    poemTheme.setBackgroundColor(colorHex)
                    poemTheme.setBackgroundColorAsInt(color)
                    backgroundColorChosen = colorHex
                    backgroundColorChosenAsInt = color
                    if (outlineChosen != null) {
                        preparePoemView("OutlineWithColor")
                    } else
                        findViewById<RelativeLayout>(R.id.backgroundPreview).setBackgroundColor(
                            color
                        )
                }.setDismissListener {
                    if (recyclerView != null) {
                        recyclerView!!.visibility = View.VISIBLE
                    }
                }.show()
        }
    }

    /**
     * This function is responsible for removing how the poem will look. This works by pressing an
     * element for a prolonged period i.e. (on
     */
    private fun setupPreviewListeners() {
        val imagePreviewCard = findViewById<CardView>(R.id.imagePreviewCard)
        val imagePreview = findViewById<ShapeableImageView>(R.id.imagePreview)
        val outlineLayout = findViewById<FrameLayout>(R.id.outlinePreview)
        val backGroundPreviewContainer = findViewById<RelativeLayout>(R.id.backgroundPreview)

        imagePreviewCard.isLongClickable = true

        imagePreviewCard.setOnLongClickListener {

            if (outlineChosen != null && backgroundImageChosen != null) {
                val builder: AlertDialog.Builder = this@PoemThemeActivity.let {
                    AlertDialog.Builder(it)
                }
                builder.setMessage(R.string.remove_background_image_outline_popup)
                builder.setTitle(R.string.remove_background_title)
                builder.apply {
                    setPositiveButton(
                        R.string.positive_background_color_outline_button
                    ) { _, _ ->
                        outlineChosen = null
                        outlineLayout.background = null
                        updateThemeOnRemoval("image|outline")
                    }
                    setNegativeButton(
                        R.string.negative_background_image_outline_button
                    ) { _, _ ->
                        //remove background image
                        backgroundImageChosen = null
                        imagePreview.setImageBitmap(null)
                        imagePreviewCard.visibility = View.GONE
                        updateThemeOnRemoval("outline|image")
                    }

                }.show()
            } else {
                backgroundImageChosen = null
                imagePreview.setImageBitmap(null)
                imagePreviewCard.visibility = View.GONE
                updateThemeOnRemoval("image")
            }
            true
        }

        backGroundPreviewContainer.setOnLongClickListener {
            if (outlineChosen != null && backgroundColorChosen == null) {
                outlineLayout.background = null
                updateThemeOnRemoval("outline")
            } else if (backgroundColorChosen != null && outlineChosen == null) {
                backGroundPreviewContainer.background = null
                updateThemeOnRemoval("color")
            } else if (outlineChosen != null) {
                val builder: AlertDialog.Builder = this@PoemThemeActivity.let {
                    AlertDialog.Builder(it)
                }
                builder.setMessage(R.string.remove_background_color_outline_popup)
                builder.setTitle(R.string.remove_background_title)
                builder.apply {
                    setPositiveButton(
                        R.string.positive_background_color_outline_button
                    ) { _, _ ->
                        //remove background
                        outlineLayout.background = null
                        backgroundColorChosenAsInt?.let { it1 ->
                            backGroundPreviewContainer.setBackgroundColor(
                                it1
                            )
                        }
                        updateThemeOnRemoval("color|outline")
                    }
                    setNegativeButton(
                        R.string.negative_background_color_outline_button
                    ) { _, _ ->
                        backGroundPreviewContainer.setBackgroundColor(getColor(R.color.white))
                        updateThemeOnRemoval("outline|color")
                    }

                }.show()
            }
            true
        }
    }

    /**
     * Sets the text view parameters back to zero when outline removed
     */
    private fun resetTextViewParams() {
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0)
        findViewById<TextView>(R.id.previewText).layoutParams = layoutParams
    }

    /**
     * Restores image view to default settings
     */
    private fun resetImageView() {
        val layoutParams = FrameLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        layoutParams.setMargins(0)
        val shapeableImageView = findViewById<ShapeableImageView>(R.id.imagePreview)
        shapeableImageView.shapeAppearanceModel =
            ShapeAppearanceModel().toBuilder().build()
        shapeableImageView.layoutParams = layoutParams
    }

    /**
     * @param toRemove the string containing what to remove
     */
    private fun updateThemeOnRemoval(toRemove: String) {
        findViewById<RelativeLayout>(R.id.backgroundPreview)

        when (toRemove) {
            "image" -> {
                backgroundImageChosen = null
                poemTheme.backgroundType = BackgroundType.DEFAULT
            }
            "image|outline" -> {
                outlineChosen = null
                resetTextViewParams()
                resetImageView()
                poemTheme.backgroundType = BackgroundType.IMAGE
            }
            "outline" -> {
                outlineChosen = null
                resetTextViewParams()
                poemTheme.backgroundType = BackgroundType.DEFAULT
            }
            "outline|color" -> {
                backgroundColorChosen = null
                backgroundColorChosenAsInt = null
                val gradientDrawable: GradientDrawable =
                    outlineChosen?.background?.mutate() as GradientDrawable
                gradientDrawable.setColor(getColor(R.color.white))
                preparePoemView("Outline")
                poemTheme.backgroundType = BackgroundType.OUTLINE
            }
            "outline|image" -> {
                backgroundImageChosen = null
                poemTheme.backgroundType = BackgroundType.OUTLINE
            }
            "color" -> {
                backgroundColorChosenAsInt = null
                backgroundColorChosen = null
                poemTheme.backgroundType = BackgroundType.DEFAULT
            }
            "color|outline" -> {
                resetTextViewParams()
                outlineChosen = null
                poemTheme.backgroundType = BackgroundType.COLOR
            }
        }
    }

    /**
     * Sets the outline chosen by user
     */
    private fun setOutlineChosen() {
        var shouldBreak = false
        for (child in mainLinearLayout.children) {
            if (shouldBreak)
                break
            if (resources.getResourceName(child.id).startsWith("$packageName:id/outline")) {
                if (child is LinearLayout) {
                    if (child.childCount > 0) {
                        for (frameLayoutChild in child.children) {
                            if (frameLayoutChild is FrameLayout) {
                                if (frameLayoutChild.contentDescription == poemTheme.getOutline()) {
                                    outlineChosen = frameLayoutChild
                                    shouldBreak = true
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Create  memory for current data container
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

        val previewText = findViewById<TextView>(R.id.previewText)
        previewText.setTextColor(poemTheme.getTextColorAsInt())
        previewText.textSize = poemTheme.getTextSize().toFloat()

        previewText.typeface =
            TypefaceHelper.getTypeFace(poemTheme.getTextFont(), applicationContext)

        when (poemTheme.getTextAlignment()) {
            TextAlignment.CENTRE -> {
                previewText.gravity = Gravity.CENTER
            }
            TextAlignment.LEFT -> {
                previewText.gravity = Gravity.START
            }
            TextAlignment.RIGHT -> {
                previewText.gravity = Gravity.END
            }
            TextAlignment.CENTRE_VERTICAL -> {
                previewText.gravity = Gravity.CENTER_HORIZONTAL
            }
            else -> {
                previewText.gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        when (poemTheme.backgroundType) {
            BackgroundType.COLOR -> {
                findViewById<RelativeLayout>(R.id.backgroundPreview).setBackgroundColor(
                    poemTheme.getBackgroundColorAsInt()
                )
                backgroundColorChosen = poemTheme.getBackgroundColor()
                backgroundColorChosenAsInt = poemTheme.getBackgroundColorAsInt()
            }
            BackgroundType.IMAGE -> {
                backgroundImageChosen = poemTheme.getImagePath()
                preparePoemView("Background")
            }
            BackgroundType.OUTLINE -> {
                setOutlineChosen()
                preparePoemView("Outline")
            }
            BackgroundType.OUTLINE_WITH_IMAGE -> {
                setOutlineChosen()
                preparePoemView("Outline")
                backgroundImageChosen = poemTheme.getImagePath()
                preparePoemView("Background")
            }
            BackgroundType.OUTLINE_WITH_COLOR -> {
                setOutlineChosen()
                backgroundColorChosenAsInt = poemTheme.getBackgroundColorAsInt()
                backgroundColorChosen = poemTheme.getBackgroundColor()
                preparePoemView("OutlineWithColor")
            }
            BackgroundType.DEFAULT -> {

            }
        }

    }
}