package com.wendorochena.poetskingdom.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.text.Editable
import android.util.TypedValue
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import com.google.android.material.imageview.ShapeableImageView
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

class PdfPrintAdapter(
    private val context: Context,
    private val editableArrayList: ArrayList<Editable>,
    private val currentPage: FrameLayout,
    private val strokeMargin: Int,
    private val poemTheme: PoemTheme
) : PrintDocumentAdapter() {
    private var editTextToPrint = ArrayList<EditText>()
    private var pdfDocument: PrintedPdfDocument? = null
    private var reshapedBitmap: Bitmap? = null
    private var isTextCentred = false
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        pdfDocument = newAttributes?.let { PrintedPdfDocument(context, it) }
        // Respond to cancellation request
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        val width = pdfDocument?.pageWidth ?: 0
        val height = pdfDocument?.pageHeight ?: 0
        val imageView = currentPage.getChildAt(0) as ShapeableImageView
        val tag: String = if (imageView.tag != null)
            imageView.tag as String
        else
            ""
        val startAndEndMargins =
            (poemTheme.textMarginUtil.marginLeft) + (poemTheme.textMarginUtil.marginRight)
        val topAndBottomMargins =
            (poemTheme.textMarginUtil.marginTop) + (poemTheme.textMarginUtil.marginBottom)
        val pdfPrinterHelper =
            PdfPrinterHelper(
                height - topAndBottomMargins,
                width - startAndEndMargins,
                poemTheme.textSize,
                ArrayList(),
                poemTheme.outline,
                tag,
            )
        val pages = pdfPrinterHelper.calculatePages(
            editableArrayList,
            context,
            currentPage,
            strokeMargin
        ) + 1
        reshapedBitmap = pdfPrinterHelper.getReshapedBitmapIfAny()
        editTextToPrint = pdfPrinterHelper.getEditTextsToPrint()
        if (pages > 0) {
            PrintDocumentInfo.Builder(poemTheme.poemTitle)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(pages).build()
                .also { info ->
                    // Content layout reflow is complete
                    callback?.onLayoutFinished(info, true)
                }

        } else {
            // Otherwise report an error to the print framework
            callback?.onLayoutFailed("Page count calculation failed.")
        }
    }

    override fun onWrite(
        pageRange: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        val firstEditText = currentPage.getChildAt(1) as EditText
        val textPixelSize =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                poemTheme.textSize.toFloat(),
                context.resources.displayMetrics
            )
        val textPaint = Paint()
        textPaint.typeface = firstEditText.typeface
        textPaint.textSize = textPixelSize
        textPaint.color = firstEditText.currentTextColor
        textPaint.textAlign = getPaintAlignment()


        pdfDocument?.startPage(0)?.also { page ->
            drawPage(page, firstEditText, true, textPaint)
            pdfDocument?.finishPage(page)
        }

        for ((index, editText) in editTextToPrint.withIndex()) {

            pdfDocument?.startPage(index + 1)?.also { page ->
                // check for cancellation
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    pdfDocument?.close()
                    pdfDocument = null
                    return
                }
                // Draw page content for printing
                drawPage(page, editText, false, textPaint)

                // Rendering is complete, so page can be finalized.
                pdfDocument?.finishPage(page)

            }
        }
        try {
            pdfDocument?.writeTo(FileOutputStream(destination?.fileDescriptor))
        } catch (e: java.lang.Exception) {
            callback?.onWriteFailed(e.toString())
            return
        } finally {
            pdfDocument?.close()
            pdfDocument = null
        }
//        val writtenPages = computeWrittenPages()
        // Signal the print framework the document is complete
        callback?.onWriteFinished(pageRange)
    }

    private fun drawPage(
        page: PdfDocument.Page,
        editText: EditText,
        thumbnail: Boolean,
        textPaint: Paint
    ) {
        page.canvas.apply {
            val xPoint = determineXPoint(this.width)
            if (thumbnail) {
                val thumbnailCreator = ThumbnailCreator(
                    context,
                    poemTheme,
                    this.width,
                    this.height,
                    poemTheme.textMarginUtil,
                    generateBackground = false,
                    typeface = editText.typeface
                )
                thumbnailCreator.pdfInitiateCreateThumbnail()

                val rect = Rect(0, 0, this.width, this.height)
                val thumbnailsFolder = context.getDir(
                    "thumbnails",
                    AppCompatActivity.MODE_PRIVATE
                )

                val encodedTitle = poemTheme.poemTitle.replace(' ', '_')
                val thumbnailFile =
                    File(thumbnailsFolder.absolutePath + File.separator + encodedTitle + ".png")
                try {
                    this.drawBitmap(
                        BitmapFactory.decodeFile(thumbnailFile.absolutePath), null, rect, null
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val imageView = currentPage.getChildAt(0) as ShapeableImageView
                if (imageView.tag != null && imageView.tag.toString().startsWith("/")) {
                    val offset = strokeMargin
                    val imageRect =
                        if (currentPage.background != null)
                            Rect(offset, offset, this.width, this.height)
                        else
                            Rect(0, 0, this.width, this.height)
                    try {
                        if (reshapedBitmap != null)
                            this.drawBitmap(reshapedBitmap!!, null, imageRect, null)
                        else
                            this.drawBitmap(imageView.drawToBitmap(), null, imageRect, null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (currentPage.background != null) {
                    if (currentPage.background is ColorDrawable) {
                        val colorDrawable = currentPage.background as ColorDrawable
                        this.drawColor(colorDrawable.color)
                    } else {
                        val rect = Rect(0, 0, this.width, this.height)
                        val gradientDrawable: GradientDrawable =
                            currentPage.background.constantState?.newDrawable() as GradientDrawable

                        this.drawBitmap(
                            gradientDrawable.toBitmap(
                                this.width,
                                this.height,
                                Bitmap.Config.ARGB_8888
                            ), null, rect, null
                        )
                    }
                }

                val lineHeight =
                    (textPaint.descent() - textPaint.ascent() + textPaint.fontMetrics.leading).roundToInt()
                        .toFloat()

                var yPoint = if (poemTheme.textAlignment.toString().contains("CENTRE_VERTICAL"))
                    determineYPoint(
                        this.height - strokeMargin,
                        editText.text.lines().size,
                        lineHeight
                    )
                else
                    poemTheme.textMarginUtil.marginTop.toFloat() * (3F / 4F)

                for (line in editText.text.lines()) {
                    yPoint += (textPaint.descent() - textPaint.ascent() + textPaint.fontMetrics.leading).roundToInt()
                        .toFloat()
                    if (isTextCentred) {
                        val bounds = Rect()
                        textPaint.getTextBounds(line, 0, line.length, bounds)
                        val xOffset =
                            (this.width / 2F) - (bounds.width() / 2F) - (poemTheme.textMarginUtil.marginLeft.toFloat() * (3F / 4F))
                        val xPointToUse = if (xOffset < 0)
                            xPoint - xOffset
                        else
                            xPoint + xOffset
                        this.drawText(line, xPointToUse, yPoint, textPaint)
                    } else {
                        this.drawText(line, xPoint, yPoint, textPaint)
                    }
                }
            }
        }
    }

    /**
     * Returns alignment of the paint to be used
     */
    private fun getPaintAlignment(): Paint.Align {
        return when (poemTheme.textAlignment) {
            TextAlignment.LEFT, TextAlignment.CENTRE_VERTICAL_LEFT -> {
                Paint.Align.LEFT
            }

            TextAlignment.CENTRE, TextAlignment.CENTRE_VERTICAL -> {
                isTextCentred = true
                Paint.Align.LEFT
            }

            TextAlignment.RIGHT, TextAlignment.CENTRE_VERTICAL_RIGHT -> {
                Paint.Align.RIGHT
            }
        }
    }

    private fun determineXPoint(width: Int): Float {
        return when (poemTheme.textAlignment) {
            TextAlignment.LEFT, TextAlignment.CENTRE_VERTICAL_LEFT -> {
                poemTheme.textMarginUtil.marginLeft.toFloat() * (3F / 4F)
            }

            TextAlignment.RIGHT, TextAlignment.CENTRE_VERTICAL_RIGHT -> {
                (width.toFloat() - (poemTheme.textMarginUtil.marginRight.toFloat() * (3F / 4F)))
            }

            TextAlignment.CENTRE, TextAlignment.CENTRE_VERTICAL -> {
                isTextCentred = true
                poemTheme.textMarginUtil.marginLeft.toFloat() * (3F / 4F)
            }
        }
    }

    /**
     * Determines the beginning Y point
     */
    private fun determineYPoint(
        pageHeight: Int,
        numOfLines: Int,
        landscapeLineHeight: Float
    ): Float {
        val halfOfPage = (pageHeight.toFloat() / 2F)
        val topHalf = (numOfLines.toDouble() / 2.0)

        return (halfOfPage - (landscapeLineHeight * topHalf)).roundToInt().toFloat()
    }
}