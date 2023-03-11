package com.wendorochena.poetskingdom.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.util.TypedValue
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.drawToBitmap
import com.google.android.material.imageview.ShapeableImageView
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

class ImageSaverUtil(
    private val context: Context,
    private val currentPage: FrameLayout,
    private val textSize: Int
) {
    private lateinit var textPaintAlignment: Paint.Align
    private var missingLineTag = "MISSING LINE/LINES AT INDEX: "

    /**
     * Sets the text alignment to be drawn on bitmap
     *
     * @param textAlignment the text alignment of the poem
     */
    fun setPaintAlignment(textAlignment: TextAlignment) {
        textPaintAlignment = when (textAlignment) {

            TextAlignment.LEFT -> {
                Paint.Align.LEFT
            }
            TextAlignment.CENTRE -> {
                Paint.Align.CENTER
            }
            TextAlignment.RIGHT -> {
                Paint.Align.RIGHT
            }
            TextAlignment.CENTRE_VERTICAL -> {
                Paint.Align.CENTER
            }
            TextAlignment.CENTRE_VERTICAL_RIGHT -> {
                Paint.Align.RIGHT
            }
            TextAlignment.CENTRE_VERTICAL_LEFT -> {
                Paint.Align.LEFT
            }
        }
    }


    /**
     *  Creates an EditText object that is to be added to an arraylist
     *
     *  @param editText the editTextBox to be added
     *  @param editable the text to be added
     */
    private fun editTextToRet(editText: EditText, editable: Editable): EditText {
        val editTextToAdd = EditText(context)
        editTextToAdd.layoutParams = editText.layoutParams
        editTextToAdd.setTextColor(editText.currentTextColor)
        editTextToAdd.typeface = editText.typeface
        editTextToAdd.text = editable
        return editTextToAdd
    }

    /**
     * One entire edit line could be an extremely long word or just gibberish therefore it needs to be
     * split this method returns the split lines
     *
     * @param longWord the word to be split if necessary
     * @param paint the paint of the text
     * @param width width of the screen
     *
     * @return an arrayList containing lines to be saved as an image
     */
    private fun updateLongWordBounds(
        longWord: String,
        paint: Paint,
        width: Int
    ): ArrayList<String> {
        val lines = ArrayList<String>()
        val bounds = Rect()

        paint.getTextBounds(longWord, 0, longWord.length, bounds)

        if (bounds.width() > width) {
            var charIndex = 0
            var accumulatedChars = ""
            while (charIndex < longWord.length) {

                accumulatedChars += longWord[charIndex]
                paint.getTextBounds(accumulatedChars, 0, accumulatedChars.length, bounds)
                if (bounds.width() > width) {
                    accumulatedChars =
                        accumulatedChars.slice(0 until accumulatedChars.length - 1)
                    accumulatedChars += "\n"
                    lines.add(accumulatedChars)
                    accumulatedChars = ""
                    charIndex++
                } else if (charIndex + 1 == longWord.length) {
                    accumulatedChars += "\n"
                    lines.add(accumulatedChars)
                    charIndex++
                } else
                    charIndex++
            }
        }
        return lines
    }

    //use this as a helper function to adjust text bounds so that it fits

    /**
     * Helper function that adjusts text bounds and makes sure that it fits
     *
     * @param lineToSplit the line to split
     * @param width width of the screen
     * @param lineHeight the height of a given line
     * @param height height of the viewport
     * @param isLastLine true if it is last line false if not
     *
     * @return an editable arrayList that has properly split lines that fit viewport width
     */
    private fun updateTextBounds(
        lineToSplit: String,
        width: Int,
        lineHeight: Int,
        height: Int,
        isLastLine: Boolean
    ): ArrayList<Editable> {
        val textPixelSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            textSize.toFloat(),
            context.resources.displayMetrics
        )

        val firstEditText = currentPage.getChildAt(1) as EditText
        val editableArrayListToReturn = ArrayList<Editable>()
        val lines = ArrayList<String>()
        // indexing starts at 0
        val numOfPages = (height / lineHeight)
        val paint = Paint()
        paint.typeface = firstEditText.typeface
        paint.textSize = textPixelSize
        val bounds = Rect()

        paint.getTextBounds(lineToSplit, 0, lineToSplit.length, bounds)
        if (bounds.width() < width) {
            if (!isLastLine)
                lines.add(lineToSplit + "\n")
            else {
                if (lineToSplit.isEmpty())
                    lines.add("\n")
                else
                    lines.add(lineToSplit)
            }
        } else {
            val words = lineToSplit.split(" ")
            var indexCounter = 0
            val wordsSize = words.size

            var currentLine = ""

            while (indexCounter < wordsSize) {
                val currentWord = words[indexCounter]
                paint.getTextBounds(currentWord, 0, currentWord.length, bounds)
                if (bounds.width() > width) {
                    if (!lines.addAll(updateLongWordBounds(currentWord, paint, width)))
                        Log.e(missingLineTag, indexCounter.toString())
                    indexCounter++
                } else {
                    currentLine += if (currentLine.isNotEmpty())
                        " $currentWord"
                    else
                        currentWord
                    paint.getTextBounds(currentLine, 0, currentLine.length, bounds)
                    //this means the last word added spills into a new line
                    if (bounds.width() > width) {
                        val fullLineLength = currentLine.length - currentWord.length
                        currentLine = currentLine.slice(0 until fullLineLength)
                        currentLine += "\n"
                        lines.add(currentLine)
                        currentLine = ""
                    } else if (indexCounter + 1 >= wordsSize) {
                        if (!isLastLine)
                            currentLine += "\n"

                        lines.add(currentLine)
                        indexCounter++
                    } else {
                        indexCounter++
                    }
                }
            }

        }

        var tempSpannableString = ""
        for ((counter, line) in lines.withIndex()) {
            tempSpannableString += line
            if (counter % numOfPages == 0) {
                editableArrayListToReturn.add(SpannableStringBuilder(tempSpannableString))
                tempSpannableString = ""
            }
        }
        if (tempSpannableString != "")
            editableArrayListToReturn.add(SpannableStringBuilder(tempSpannableString))

        return editableArrayListToReturn
    }


    /**
     * @param lastEditText
     * @return the lapping lines that lead to an entire page not being filled
     */
    private fun lappingLines(lastEditText: EditText): String {
        var tempSpannableString = ""

        for ((index, line) in lastEditText.text.lines().withIndex()) {
            tempSpannableString += if (index == lastEditText.text.lines().size - 1)
                line
            else
                line + "\n"
        }
        return tempSpannableString
    }

    /**
     * Appends the new lines to the previous lapping lines
     *
     * @param tempSpannableString the string con
     * @param linesToFill a list containing the lines to fill in
     * @return new lines to append to an arrayList that didn't fill the viewport
     */
    private fun newLinesToAdd(tempSpannableString: String, linesToFill: List<String>): String {
        var stringToRet = tempSpannableString

        for ((index, line) in linesToFill.withIndex()) {
            stringToRet += if (index == linesToFill.size - 1)
                line
            else
                line + "\n"
        }

        return stringToRet
    }

    /**
     * Formats the pages to save by formatting text to make sure it fits on the page
     *
     * @param editableArrayList an arrayList with all the text to be printed
     * @param height the height of the view port in PX
     * @param width the width of the view port in PX
     * @param lineHeight the height of the line in PX
     */
    fun formatPagesToSave(
        editableArrayList: Editable,
        height: Int,
        width: Int,
        lineHeight: Int
    ): ArrayList<EditText> {
        val editText = currentPage.getChildAt(1) as EditText
        val linesPerPage = (height / lineHeight)

        val updatedEditableList = ArrayList<Editable>()

        for ((replaceIndex, line) in editableArrayList.lines().withIndex()) {
            var tempSpannableString = ""
            if (replaceIndex == editableArrayList.lines().size - 1) {
                for (subEditable in updateTextBounds(line, width, lineHeight, height, true)) {
                    tempSpannableString += subEditable.toString()
                }
            } else
                for (subEditable in updateTextBounds(line, width, lineHeight, height, false)) {
                    tempSpannableString += subEditable.toString()
                }
            if (updatedEditableList.isNotEmpty()) {
                if (updatedEditableList[updatedEditableList.size - 1].lines().size < (linesPerPage + 1)) {
                    var previousLines = updatedEditableList[updatedEditableList.size - 1].toString()
                    previousLines += tempSpannableString
                    updatedEditableList[updatedEditableList.size - 1] =
                        SpannableStringBuilder(previousLines)
                } else {
                    updatedEditableList.add(SpannableStringBuilder(tempSpannableString))
                }
            } else
                updatedEditableList.add(SpannableStringBuilder(tempSpannableString))
        }

        val editTextsToRet = ArrayList<EditText>()
        var lappingLines = 0
        var counter = -1


        for (editable in updatedEditableList) {
            counter++
            val linesInCurrentEditable = editable.lines().size
            var linesFilledIfAny = 0

            if (lappingLines > 0) {
                var numOfLinesToFill = linesPerPage - lappingLines
                val indexToFill = editTextsToRet.size - 1
                if (numOfLinesToFill > 0) {

                    val lastEditText = editTextsToRet[indexToFill]

                    numOfLinesToFill = linesPerPage - lastEditText.text.lines().size
                    var tempSpannableString = lappingLines(lastEditText)

                    val linesToFill = if (numOfLinesToFill > editable.lines().size)
                        editable.lines().slice(0 until editable.lines().size)
                    else
                        if (editable.lines().size > numOfLinesToFill)
                            editable.lines().slice(0..numOfLinesToFill)
                        else
                            editable.lines().slice(0 until numOfLinesToFill)

                    tempSpannableString = newLinesToAdd(tempSpannableString, linesToFill)

                    lappingLines = if (numOfLinesToFill > linesToFill.size)
                        lastEditText.text.lines().size + 1
                    else
                        0
                    linesFilledIfAny = linesToFill.size
                    if (lappingLines == 0)
                        tempSpannableString += "\n"

                    lastEditText.text = SpannableStringBuilder(tempSpannableString)

                } else {
                    lappingLines = 0
                }
            }


            if (linesInCurrentEditable <= linesPerPage) {
                if (linesFilledIfAny == 0) {
                    editTextsToRet.add(editTextToRet(editText, editable))
                    lappingLines = linesInCurrentEditable
                } else if (linesInCurrentEditable - linesFilledIfAny > 0) {
                    var tempSpannableString = ""
                    val linesToAdd =
                        editable.lines().slice(linesFilledIfAny until linesInCurrentEditable)

                    if (lappingLines == 0) {
                        tempSpannableString = newLinesToAdd(tempSpannableString, linesToAdd)
                    } else {
                        val indexToFill = editTextsToRet.size - 1
                        tempSpannableString = lappingLines(editTextsToRet[indexToFill])
                        tempSpannableString = newLinesToAdd(tempSpannableString, linesToAdd)
                    }

                    val editTextToAdd =
                        editTextToRet(editText, SpannableStringBuilder(tempSpannableString))
                    editTextsToRet.add(editTextToAdd)
                    lappingLines = if (editTextToAdd.text.lines().size < linesPerPage)
                        editTextToAdd.text.lines().size
                    else
                        0
                }
            } else {
                var totalLinesInEditable = linesInCurrentEditable - linesFilledIfAny
                var currentIndex = linesFilledIfAny

                // it should never be the case that there are lapping lines when there are more
                // lines than are permitted per page
                // if this is ever printed there is a logical problem
                if (lappingLines != 0)
                    println("$lappingLines why oh why ")

                while (totalLinesInEditable > 0) {

                    val upperBound = if (totalLinesInEditable < linesPerPage) {
                        currentIndex + totalLinesInEditable
                    } else
                        currentIndex + linesPerPage

                    val editTextToAddLines =
                        editable.lines().slice(currentIndex until upperBound)

                    val stringBuilder =
                        SpannableStringBuilder(newLinesToAdd("", editTextToAddLines))

                    if (totalLinesInEditable < linesPerPage) {
                        lappingLines = linesInCurrentEditable % linesPerPage
                    }

                    currentIndex += linesPerPage
                    totalLinesInEditable -= linesPerPage
                    editTextsToRet.add(editTextToRet(editText, stringBuilder))
                }
            }
        }

        return editTextsToRet
    }

    /**
     *
     */
    private fun landscapeLineHeight(typeface: Typeface, textPixelSize: Float): Float {
        val textPaint = Paint()
        textPaint.typeface = typeface
        textPaint.textSize = textPixelSize

        return (textPaint.descent() - textPaint.ascent() + textPaint.fontMetrics.leading).roundToInt()
            .toFloat()
    }

    /**
     * Determines the YPoint of where the text should start
     *
     * @param pageHeight the height of page to be printed on in pixels
     * @param numOfLines the number of lines on the page
     * @param lineHeight the height of each line
     */
    private fun determineYPoint(
        pageHeight: Int,
        numOfLines: Int,
        lineHeight: Float
    ): Float {
        val halfOfPage = (pageHeight.toFloat() / 2f)
        val topHalf = (numOfLines.toDouble() / 2.0)
        val result = (halfOfPage - (lineHeight * topHalf)).roundToInt().toFloat()

        return if (result < 0)
            0F
        else
            result
    }

    /**
     * Creates a folder with the pictures that is accessible in the application
     * @param editableArrayList Arraylist containing text user entered per page
     * @param title of the poem
     * @param textMarginPixels the size of the text margins in pixels
     * @param isLandscape
     * @return 0 when we successfully generated images
     * @return -1 when we failed to
     */
    fun savePagesAsImages(
        editableArrayList: ArrayList<Editable>,
        title: String,
        textMarginPixels: Int,
        imageMargins: Int,
        isLandscape: Boolean,
        isCentreVertical: Boolean
    ): Int {
        val firstEditText = currentPage.getChildAt(1) as EditText
        val textPixelSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            textSize.toFloat(),
            context.resources.displayMetrics
        )

        val shapeAbleImageView = currentPage.getChildAt(0) as ShapeableImageView

        var counter = 0
        val imagesFolder = context.getDir("savedImages", MODE_PRIVATE)
        try {
            if (imagesFolder.exists()) {
                val subFolderWithTitleAsName = File(
                    imagesFolder.absolutePath + File.separator + title
                        .replace(' ', '_')
                )
                if (subFolderWithTitleAsName.exists() && subFolderWithTitleAsName.listFiles() != null && subFolderWithTitleAsName.listFiles()?.size!! > 0) {
                    for (file in subFolderWithTitleAsName.listFiles()!!) {
                        file.delete()
                    }
                }
                if (subFolderWithTitleAsName.exists() || subFolderWithTitleAsName.mkdir()) {
                    for (editable in editableArrayList) {
                        val editTextsToPrint = if (!isLandscape)
                            formatPagesToSave(
                                editable,
                                currentPage.height - (textMarginPixels * 2),
                                currentPage.width - (textMarginPixels * 2),
                                firstEditText.lineHeight
                            )
                        else
                            formatPagesToSave(
                                editable,
                                1080 - (textMarginPixels * 2),
                                1080 - (textMarginPixels * 2),
                                firstEditText.lineHeight
                            )
                        for (editTextBox in editTextsToPrint) {
                            val imageToAdd =
                                File(subFolderWithTitleAsName.absolutePath + File.separator + "stanza$counter" + ".png")
                            if (imageToAdd.exists() || imageToAdd.createNewFile()) {
                                val outStream = FileOutputStream(imageToAdd)

                                val bitmap = if (isLandscape)
                                    Bitmap.createBitmap(
                                        1080,
                                        1080,
                                        Bitmap.Config.ARGB_8888
                                    ) else
                                    Bitmap.createBitmap(
                                        currentPage.width,
                                        currentPage.height,
                                        Bitmap.Config.ARGB_8888
                                    )
                                val canvas = Canvas(bitmap)

                                if (currentPage.background != null) {
                                    if (isLandscape) {
                                        val background =
                                            if (currentPage.background.constantState?.newDrawable() is ColorDrawable)
                                                currentPage.background.constantState?.newDrawable() as ColorDrawable
                                            else
                                                currentPage.background.constantState?.newDrawable() as GradientDrawable
                                        background.setBounds(0, 0, 1080, 1080)
                                        background.draw(canvas)
                                    } else {
                                        currentPage.background.draw(canvas)
                                    }
                                } else if (currentPage.getTag(1) != null) {
                                    canvas.drawColor(currentPage.getTag(1) as Int)
                                } else {
                                    canvas.drawColor(context.getColor(R.color.white))
                                }

                                if (shapeAbleImageView.tag != null && shapeAbleImageView.tag.toString()
                                        .startsWith("/")
                                ) {
                                    val rect = if (!isLandscape)
                                        Rect(
                                            shapeAbleImageView.left,
                                            shapeAbleImageView.top,
                                            shapeAbleImageView.right,
                                            shapeAbleImageView.bottom
                                        )
                                    else if (currentPage.background != null)
                                        Rect(
                                            imageMargins,
                                            imageMargins,
                                            1080 - imageMargins,
                                            1080 - imageMargins
                                        )
                                    else
                                        Rect(0, 0, 1080, 1080)

                                    canvas.drawBitmap(
                                        shapeAbleImageView.drawToBitmap(),
                                        null,
                                        rect,
                                        null
                                    )
                                }

                                val xPoint = when (textPaintAlignment) {
                                    Paint.Align.LEFT -> if (!isLandscape)
                                        firstEditText.x
                                    else textMarginPixels.toFloat()
                                    Paint.Align.CENTER -> if (!isLandscape)
                                        (currentPage.width / 2).toFloat()
                                    else
                                        (1080 / 2).toFloat()
                                    else -> if (!isLandscape)
                                        currentPage.width.toFloat() - firstEditText.x
                                    else
                                        currentPage.width.toFloat() - textMarginPixels.toFloat()
                                }

                                var yPoint = if (!isLandscape && !isCentreVertical)
                                    firstEditText.y
                                else if (!isLandscape)
                                    determineYPoint(
                                        currentPage.height - imageMargins,
                                        editTextBox.text.lines().size,
                                        firstEditText.lineHeight.toFloat()
                                    )
                                else
                                    if (isCentreVertical)
                                        determineYPoint(
                                            1080 - imageMargins,
                                            editTextBox.text.lines().size,
                                            firstEditText.lineHeight.toFloat()
                                        )
                                    else
                                        firstEditText.y

                                val lineHeight = if (!isLandscape)
                                    firstEditText.lineHeight.toFloat()
                                else
                                    landscapeLineHeight(firstEditText.typeface, textPixelSize)

                                for (line in editTextBox.text.lines()) {
                                    val textPaint = Paint()
                                    textPaint.typeface = editTextBox.typeface
                                    textPaint.textSize = textPixelSize
                                    textPaint.color = editTextBox.currentTextColor
                                    textPaint.textAlign = textPaintAlignment
                                    yPoint += lineHeight
                                    canvas.drawText(line, xPoint, yPoint, textPaint)
                                }

                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                                outStream.close()
                                counter++
                            }
                        }
                    }
                    return 0
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return -1
        }
        return -1
    }
}