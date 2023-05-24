package com.wendorochena.poetskingdom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import java.io.File
import kotlin.math.min

class ImageViewer : AppCompatActivity() {
    private val imagesArrayList = ArrayList<Bitmap>()
    private var mScaleFactor = 1f
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var mainImage: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        val intentExtras = intent.extras
        mainImage = findViewById(R.id.mainImageView)
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        if (intentExtras != null) {
            val loadType = intentExtras.getString("imageLoadType")
            if (loadType == "image") {
                val imagePath = intentExtras.getString("imagePath")
                loadSingleImageView(imagePath)
                setupTextView(1,1)
            } else if (loadType == "poem saved image") {
                val poemName = intentExtras.getString("poemName")
                loadSavedPoemImage(poemName)
            }
        }

        val sharedPreferences = applicationContext.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("imageViewerFirstUse",false)) {
            onFirstUse()
            sharedPreferences.edit().putBoolean("imageViewerFirstUse",true).apply()
        }
    }

    private fun onFirstUse() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        val customTitleView = TextView(this)
        customTitleView.setTextColor(resources.getColor(R.color.white, null))
        customTitleView.text = resources.getString(R.string.guide_title)

        customTitleView.setTypeface(null, Typeface.BOLD)
        customTitleView.textSize = resources.getDimension(R.dimen.normal_text_size)

        val customMessageView = TextView(this)
        customMessageView.setTextColor(resources.getColor(R.color.white, null))
        customMessageView.text = resources.getString(R.string.guide_image_viewer)
        val typedValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics).toInt()
        customTitleView.setPadding(typedValue, typedValue, typedValue,0)
        customMessageView.setPadding(typedValue)

        alertDialogBuilder.setCustomTitle(customTitleView)
        alertDialogBuilder.setView(customMessageView)
        alertDialogBuilder.setPositiveButton(R.string.builder_understood) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = alertDialogBuilder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.selected_rounded_rectangle)
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(R.color.off_white))
//        alertDialogBuilder.setTitle(R.string.guide_title).setPositiveButton(R.string.builder_understood) { dialog, _ ->
//            dialog.dismiss()
//        }.setMessage(R.string.guide_image_viewer).show()
    }

    private fun setupTextView(currentIndex : Int, totalImages : Int){
        val textView = findViewById<TextView>(R.id.pageNumberTextView)

        textView.text = "$currentIndex / $totalImages"
    }
    /**
     *
     */
    private fun setupImageButtons() {
        val buttonLeft = findViewById<ImageButton>(R.id.imageLeft)
        val buttonRight = findViewById<ImageButton>(R.id.imageRight)
        buttonLeft.visibility = View.VISIBLE
        buttonRight.visibility = View.VISIBLE

        buttonLeft.setOnClickListener {
            val currentNum = mainImage.tag as Int

            if (currentNum > 0) {
                val newIndex = currentNum - 1
                mainImage.setImageBitmap(imagesArrayList[newIndex])
                setupTextView(newIndex + 1, imagesArrayList.size)
                mainImage.tag = newIndex
            }
        }

        buttonRight.setOnClickListener {
            val currentNum = mainImage.tag as Int

            if (currentNum < imagesArrayList.size - 1) {
                val newIndex = currentNum + 1
                mainImage.setImageBitmap(imagesArrayList[newIndex])
                setupTextView(newIndex + 1, imagesArrayList.size)
                mainImage.tag = newIndex
            }
        }
    }

    /**
     *
     */
    private fun loadSavedPoemImage(poemName: String?) {
        val encodedPoemName = poemName?.replace(' ', '_')

        val savedImagesFolder = getDir(getString(R.string.saved_images_folder_name), MODE_PRIVATE)
        val subFolder = File(savedImagesFolder.absolutePath + File.separator + encodedPoemName)
        val thumbnailFolder = getDir(getString(R.string.thumbnails_folder_name), MODE_PRIVATE)

        if (subFolder.exists()) {
            if (subFolder.listFiles() != null && subFolder.listFiles()?.size == 1) {
                val thumbnail =
                    File(thumbnailFolder.absolutePath + File.separator + encodedPoemName + ".png")
                if (thumbnail.exists()) {
                    imagesArrayList.add(BitmapFactory.decodeFile(thumbnail.absolutePath))
                    imagesArrayList.add(
                        BitmapFactory.decodeFile(
                            subFolder.listFiles()?.get(0)?.absolutePath
                        )
                    )
                    mainImage.setImageBitmap(imagesArrayList[0])
                    mainImage.tag = 0
                    setupTextView(1,imagesArrayList.size)
                    setupImageButtons()
                } else {
                    loadSingleImageView(subFolder.listFiles()?.get(0)?.absolutePath)
                    setupTextView(1,1)
                }
            } else if (subFolder.listFiles() != null && subFolder.listFiles()?.size!! > 1) {
                val thumbnail =
                    File(thumbnailFolder.absolutePath + File.separator + encodedPoemName + ".png")
                if (thumbnail.exists())
                    imagesArrayList.add(BitmapFactory.decodeFile(thumbnail.absolutePath))
                for (file in subFolder.listFiles()!!) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    imagesArrayList.add(bitmap)
                }
                setupTextView(1,imagesArrayList.size)
                mainImage.setImageBitmap(imagesArrayList[0])
                mainImage.tag = 0

                setupImageButtons()
            }
        }

    }

    /**
     *
     */
    private fun loadSingleImageView(imagePath: String?) {
        val mainImage = findViewById<ImageView>(R.id.mainImageView)
        val file = imagePath?.let { File(it) }
        try {
            if (file?.exists() == true) {
                Glide.with(applicationContext).load(file.absolutePath).into(mainImage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            scaleGestureDetector.onTouchEvent(event)
        }
        return true
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= scaleGestureDetector.scaleFactor
            mScaleFactor = 0.1f.coerceAtLeast(min(mScaleFactor, 10.0f))
            mainImage.scaleX = mScaleFactor
            mainImage.scaleY = mScaleFactor
            return true
        }
    }
}