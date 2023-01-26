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
     *
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

//        editTextsToPrint = editTexts
        return editTextsToPrint.size
    }

    fun getEditTextsToPrint() : ArrayList<EditText> {
        return this.editTextsToPrint
    }
}