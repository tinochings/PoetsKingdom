package com.wendorochena.poetskingdom.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ThumbnailCreator(
    private val context: Context,
    private val poemTheme: PoemTheme,
    private val width: Int,
    private val height: Int,
    private val textMarginUtil: TextMarginUtil,
    private val generateBackground : Boolean
) {

    private var poemName = "Default"
    private var poetsName = "Default"
    private var poetsSignature = "Default"
    private var poemTextSize = 12f
    private lateinit var textPaintAlignment: Paint.Align
    private lateinit var paint: Paint
    private var xPoint: Float = 0f
    private var yPoint: Float = 0f


    /**
     * Sets the text alignment to be drawn on bitmap
     *
     * @param textAlignment the text alignment of the poem
     */
    private fun setPaintAlignment(textAlignment: TextAlignment) {
        when (textAlignment) {

            TextAlignment.LEFT -> {
                xPoint = textMarginUtil.marginLeft.toFloat()
                textPaintAlignment = Paint.Align.LEFT
            }
            TextAlignment.CENTRE -> {
                xPoint = width / 2f
                textPaintAlignment = Paint.Align.CENTER
            }
            TextAlignment.RIGHT -> {
                xPoint = width - textMarginUtil.marginRight.toFloat()
                textPaintAlignment = Paint.Align.RIGHT
            }
            TextAlignment.CENTRE_VERTICAL -> {
                xPoint = width.toFloat() / 2f
                textPaintAlignment = Paint.Align.CENTER
            }
            TextAlignment.CENTRE_VERTICAL_RIGHT -> {
                xPoint = width - textMarginUtil.marginRight.toFloat()
                textPaintAlignment = Paint.Align.RIGHT
            }
            TextAlignment.CENTRE_VERTICAL_LEFT -> {
                xPoint = textMarginUtil.marginLeft.toFloat()
                textPaintAlignment = Paint.Align.LEFT
            }
        }
    }


    private fun setupPaint() {
        paint = Paint()
        paint.color = poemTheme.getTextColorAsInt()
        paint.textSize = poemTextSize
        paint.typeface = TypefaceHelper.getTypeFace(poemTheme.getTextFont(), context)
    }

    /**
     * Only allow a maximum of two lines
     */
    private fun validateLines() {
        val poemNameLines = ImageSaverUtil.updateLongWordBounds(
            poemName, paint, width -
                    textMarginUtil.marginLeft - textMarginUtil.marginRight
        )
        if (poemNameLines.size >= 2) {
            poemName = poemNameLines[0] + "\\p" + poemNameLines[1]
        }

        val poetsLines = ImageSaverUtil.updateLongWordBounds(
            poetsName, paint, width -
                    textMarginUtil.marginLeft - textMarginUtil.marginRight
        )
        if (poetsLines.size >= 2) {
            poetsName = poetsLines[0] + "\\p" + poetsLines[1]
        }

        val poetsSignatureLines = ImageSaverUtil.updateLongWordBounds(
            poetsSignature, paint, width -
                    textMarginUtil.marginLeft - textMarginUtil.marginRight
        )
        if (poetsSignatureLines.size >= 2) {
            poetsSignature = poetsSignatureLines[0] + "\\p" + poetsSignatureLines[1]
        }
    }

    /**
     * Needed to evade coroutine context
     */
    fun pdfInitiateCreateThumbnail() {
        val personalisationPreferences = context.getSharedPreferences(
            context.getString(R.string.personalisation_sharedpreferences_key),
            AppCompatActivity.MODE_PRIVATE
        )

        poemName = poemTheme.getTitle()
        poetsName = personalisationPreferences.getString("author", "Default").toString()
        poetsSignature = personalisationPreferences.getString("signature", "Default").toString()
        poemTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            poemTheme.getTextSize().toFloat(),
            context.resources.displayMetrics
        )
        setupPaint()
        setPaintAlignment(poemTheme.getTextAlignment())
        validateLines()
        drawToBitmap()
    }

    private fun centreAlign(string: String) {
        val bounds = Rect()
        paint.getTextBounds(string, 0, string.length, bounds)
        xPoint = ((width / 2) -  (bounds.width() / 2)).toFloat()
    }


    private fun rightAlign(string: String) {
        val bounds = Rect()
        paint.getTextBounds(string, 0, string.length, bounds)
        xPoint = (width - textMarginUtil.marginRight - bounds.width()).toFloat()
    }

    /**
     * Sets up the thumbnail information
     */
    suspend fun initiateCreateThumbnail() {
        withContext(Dispatchers.IO) {
            val personalisationPreferences = context.getSharedPreferences(
                context.getString(R.string.personalisation_sharedpreferences_key),
                AppCompatActivity.MODE_PRIVATE
            )

            poemName = poemTheme.getTitle()
            poetsName = personalisationPreferences.getString("author", "Default").toString()
            poetsSignature = personalisationPreferences.getString("signature", "Default").toString()
            poemTextSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                poemTheme.getTextSize().toFloat(),
                context.resources.displayMetrics
            )
            setupPaint()
            setPaintAlignment(poemTheme.getTextAlignment())
            validateLines()
            drawToBitmap()
        }
    }

    /**
     * Draws the thumbnail to a bitmap canvas
     */
    private fun drawToBitmap() {
        try {
            val thumbnailFolder = context.getDir(
                context.getString(R.string.thumbnails_folder_name),
                Context.MODE_PRIVATE
            )
            val thumbnailFile = File(
                thumbnailFolder.absolutePath + File.separator + poemTheme.getTitle().replace(
                    ' ',
                    '_'
                ) + ".png"
            )
            if (!thumbnailFile.exists())
                thumbnailFile.createNewFile()

            val outputStream = FileOutputStream(thumbnailFile)
            val thumbnailBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(thumbnailBitmap)

            when (poemTheme.backgroundType) {
                BackgroundType.DEFAULT -> {
                    val colorDrawable = ColorDrawable(context.getColor(R.color.white))
                    colorDrawable.setBounds(0, 0, width, height)
                    canvas.drawColor(context.getColor(R.color.white))
                }

                BackgroundType.OUTLINE -> {
                    val backgroundDrawable = PoemTheme.getOutlineAndColor(
                        "landscape",
                        poemTheme,
                        0,
                        width,
                        0,
                        height,
                        context
                    ) as GradientDrawable
                    backgroundDrawable.setColor(context.getColor(R.color.white))
                    backgroundDrawable.setBounds(0, 0, width, height)
                    backgroundDrawable.draw(canvas)
                }

                BackgroundType.OUTLINE_WITH_IMAGE -> {
                    val strokeSize: Int =
                        context.resources.getDimensionPixelSize(R.dimen.strokeSize)
                    val backgroundDrawable = PoemTheme.getOutlineAndColor(
                        "landscape",
                        poemTheme,
                        0,
                        width,
                        0,
                        height,
                        context
                    ) as GradientDrawable
                    backgroundDrawable.setStroke(strokeSize, poemTheme.getOutlineColor())

                    val image = ShapeableImageView(context)
                    val file = File(poemTheme.getImagePath())
                    image.layout(strokeSize, strokeSize, width, height)
                    image.left = strokeSize
                    image.top = strokeSize
                    image.right = width
                    image.bottom = height
                    image.scaleType = ImageView.ScaleType.FIT_XY

                    image.shapeAppearanceModel =
                        ShapeAppearanceModelHelper.shapeImageView(
                            poemTheme.getOutline(),
                            context.resources,
                            strokeSize.toFloat()
                        )
                    if (file.exists()) {
                        val bitmap = Bitmap.createBitmap(
                            width,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        val tempCanvas = Canvas(bitmap)
                        image.setImageBitmap(BitmapFactory.decodeFile(poemTheme.getImagePath()))
                        image.draw(tempCanvas)
                        canvas.drawBitmap(
                            bitmap, null,
                            Rect(strokeSize, strokeSize, width, height), null
                        )
                    }



                    backgroundDrawable.draw(canvas)
                }

                BackgroundType.OUTLINE_WITH_COLOR -> {
                    val backgroundDrawable = PoemTheme.getOutlineAndColor(
                        "landscape",
                        poemTheme,
                        0,
                        width,
                        0,
                        height,
                        context
                    ) as GradientDrawable
                    val gradientDrawable: GradientDrawable =
                        backgroundDrawable.constantState?.newDrawable() as GradientDrawable
                    gradientDrawable.setColor(poemTheme.getBackgroundColorAsInt())

                    gradientDrawable.setBounds(0, 0, width, height)
                    gradientDrawable.draw(canvas)
                }

                BackgroundType.IMAGE -> {
                    val file = File(poemTheme.getImagePath())
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(poemTheme.getImagePath())
                        canvas.drawBitmap(bitmap, null, Rect(0, 0, width, height), null)
                    }
                }

                BackgroundType.COLOR -> {
                    val colorDrawable = ColorDrawable(poemTheme.getBackgroundColorAsInt())
                    colorDrawable.setBounds(0, 0, width, height)
                    colorDrawable.draw(canvas)
                }
            }

            if (generateBackground){
                val backgroundImageDrawableFolder = context.getDir(
                    context.getString(R.string.background_image_drawable_folder),
                    Context.MODE_PRIVATE
                )
                val backgroundFile =
                    File(backgroundImageDrawableFolder.absolutePath + File.separator + poemTheme.getTitle().replace(
                        ' ',
                        '_'
                    ) + ".png")

                if (backgroundFile.exists() || backgroundFile.createNewFile()) {
                    val backgroundOutputStream = FileOutputStream(backgroundFile)
                    thumbnailBitmap.compress(Bitmap.CompressFormat.PNG,100,backgroundOutputStream)
                    backgroundOutputStream.close()
                }
            }

            yPoint = if (poemTheme.getOutline() != "")
                (paint.descent() - paint.ascent() + paint.fontMetrics.leading) +
                        context.resources.getDimensionPixelSize(R.dimen.strokeSize).toFloat()
            else
                paint.descent() - paint.ascent() + paint.fontMetrics.leading

            val isCentreVertical =
                poemTheme.getTextAlignment() == TextAlignment.CENTRE_VERTICAL || poemTheme.getTextAlignment() == TextAlignment.CENTRE
            val isRightAlign =
                poemTheme.getTextAlignment() == TextAlignment.CENTRE_VERTICAL_RIGHT || poemTheme.getTextAlignment() == TextAlignment.RIGHT

            val poemNameSplit = poemName.split("\\p")
            val poetsNameSplit = poetsName.split("\\p")
            val poetsSignatureSplit = poetsSignature.split("\\p")

            if (poemNameSplit.size == 1) {
                if (isCentreVertical) {
                    centreAlign(poemName)
                }
                if (isRightAlign)
                    rightAlign(poemName)

                canvas.drawText(poemName, xPoint, yPoint, paint)
            } else {
                if (isCentreVertical) {
                    centreAlign(poetsNameSplit[0])
                }
                if (isRightAlign)
                    rightAlign(poetsNameSplit[0])
                canvas.drawText(poemNameSplit[0], xPoint, yPoint, paint)


                yPoint += (paint.descent() - paint.ascent() + paint.fontMetrics.leading)

                if (isCentreVertical) {
                    centreAlign(poetsNameSplit[1])
                }

                if (isRightAlign)
                    rightAlign(poemNameSplit[1])
                canvas.drawText(poemNameSplit[1], xPoint, yPoint, paint)

            }


            yPoint = height / 2f

            if (poetsNameSplit.size == 1) {
                if (isCentreVertical) {
                    centreAlign(poetsName)
                }
                if (isRightAlign)
                    rightAlign(poetsName)

                canvas.drawText(poetsName, xPoint, yPoint, paint)

            } else {
                if (isCentreVertical) {
                    centreAlign(poetsNameSplit[0])
                }
                if (isRightAlign)
                    rightAlign(poetsNameSplit[0])

                canvas.drawText(poetsNameSplit[0], xPoint, yPoint, paint)

                yPoint += (paint.descent() - paint.ascent() + paint.fontMetrics.leading)

                if (isCentreVertical) {
                    centreAlign(poetsNameSplit[1])
                }
                if (isRightAlign)
                    rightAlign(poetsNameSplit[1])

                canvas.drawText(poetsNameSplit[1], xPoint, yPoint, paint)
            }

            yPoint = height - (paint.descent() - paint.ascent() + paint.fontMetrics.leading)

            if (poetsSignatureSplit.size == 1) {
                if (isCentreVertical) {
                    centreAlign(poetsSignature)
                }
                if (isRightAlign)
                    rightAlign(poetsSignature)
                canvas.drawText(poetsSignature, xPoint, yPoint, paint)
            } else {
                if (isCentreVertical) {
                    centreAlign(poetsSignatureSplit[0])
                }
                if (isRightAlign)
                    rightAlign(poetsSignatureSplit[0])
                yPoint =
                    height - ((paint.descent() - paint.ascent() + paint.fontMetrics.leading) * 2)

                canvas.drawText(poetsSignatureSplit[0], xPoint, yPoint, paint)

                yPoint += (paint.descent() - paint.ascent() + paint.fontMetrics.leading)

                if (isCentreVertical) {
                    centreAlign(poetsSignatureSplit[1])
                }
                if (isRightAlign)
                    rightAlign(poetsSignatureSplit[1])

                canvas.drawText(poetsSignatureSplit[1], xPoint, yPoint, paint)
            }

            thumbnailBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(this::javaClass.name, "Failed to save thumbnail")
        }
    }
}