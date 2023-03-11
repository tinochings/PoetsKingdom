package com.wendorochena.poetskingdom.utils

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.util.TypedValue
import android.widget.EditText
import android.widget.FrameLayout
import kotlin.math.roundToInt

class PdfPrinterHelper(
    private val height: Int,
    private val pageWidth: Int,
    private val textSize: Int,
    private var editTextsToPrint: ArrayList<EditText>,
    private val textSizeMargin : Int
) {

    /**
     * Initiates the calculating of pages of a PDF document
     *
     * @param editables the arrayList containing all the text in the poem
     * @param context the context of the calling poem
     * @param currentPage the layout of the currently selected page in the calling Activity
     *
     * @return the number of pages
     */
    fun calculatePages(
        editables: ArrayList<Editable>,
        context: Context,
        currentPage: FrameLayout
    ): Int {
        val imageSaverUtil = ImageSaverUtil(context, currentPage, textSize * 4/3)
        val textPixelSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            textSize.toFloat(),
            context.resources.displayMetrics
        ) * 4/3

        val textPaint = Paint()
        textPaint.textSize = textPixelSize

        for (edit in editables) {
            val editTexts = imageSaverUtil.formatPagesToSave(edit,height - textSizeMargin, pageWidth,(textPaint.descent() - textPaint.ascent() + textPaint.fontMetrics.leading).roundToInt())
            editTextsToPrint.addAll(editTexts)
        }

        return editTextsToPrint.size
    }

    /**
     * @return the EditTextBoxes to print where each element represents a page
     */
    fun getEditTextsToPrint() : ArrayList<EditText> {
        return this.editTextsToPrint
    }
}