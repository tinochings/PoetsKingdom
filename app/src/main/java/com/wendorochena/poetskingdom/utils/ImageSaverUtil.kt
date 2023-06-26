package com.wendorochena.poetskingdom.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.text.getSpans
import androidx.core.view.drawToBitmap
import androidx.databinding.ObservableInt
import com.google.android.material.imageview.ShapeableImageView
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

/**
 * @param context context of the activity
 * @param currentPage the frame layout currently displayed
 * @param textSize the size of the text in SP
 * @param outline the outline set
 * @param widthAndHeight a Pair with the width as the first value, height as second value
 */
class ImageSaverUtil(
    private val context: Context,
    private val currentPage: FrameLayout,
    private val textSize: Int,
    private val outline: String,
    private val widthAndHeight: Pair<Int, Int>
) {
    private lateinit var textPaintAlignment: Paint.Align
    private var missingLineTag = "MISSING LINE/LINES AT INDEX: "

    //    private var spannableStringMetaData
    var progressTracker: ObservableInt = ObservableInt(0)
    var totalPages = ObservableInt(0)

    companion object {
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
        fun updateLongWordBounds(
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
    }

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
        isLastLine: Boolean,
        allSpannableStringsLocations: ArrayList<Triple<Int, Triple<Pair<Int, Int>, StyleSpan?, ForegroundColorSpan?>, String>>
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
        if (numOfPages > 0) {
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
                //key is word number to track
                //value is a pair with the index number of item in allSpannableStringsLocations as first
                // value is a pair with the exact coordinates in the subtring as second
                val wordNumbersToTrack = HashMap<Int, Pair<Int, Pair<Int, Int>>>()
                if (allSpannableStringsLocations.isNotEmpty()) {
                    for ((index, spannableItem) in allSpannableStringsLocations.withIndex()) {
                        wordNumbersToTrack[locateWordNumber(
                            lineToSplit,
                            spannableItem.second.first.first
                        )] = Pair(index, spannableItem.second.first)
                    }
                }
                var indexCounter = 0
                val wordsSize = words.size

                var currentLine = ""

                while (indexCounter < wordsSize) {
                    val currentWord = words[indexCounter]
                    paint.getTextBounds(currentWord, 0, currentWord.length, bounds)
                    if (bounds.width() > width) {
                        val updatedSplitWord = updateLongWordBounds(currentWord, paint, width)
                        if (wordNumbersToTrack.contains(indexCounter)) {
                            //add this to updated
                            val updatedWordBounds = updateNewWordBounds(
                                updatedSplitWord,
                                lineToSplit,
                                wordNumbersToTrack[indexCounter]!!.second
                            )
                        }
                        if (!lines.addAll(updatedSplitWord))
                            println("$missingLineTag $indexCounter")
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
                            if (wordNumbersToTrack.contains(indexCounter)){
                                val startIndex = currentLine.length - currentWord.length
                                val endIndex = startIndex + currentWord.length
                                val toUpdate = allSpannableStringsLocations[wordNumbersToTrack[indexCounter]!!.first]
                                val updated = Triple(lines.size, Triple(Pair(startIndex,endIndex), toUpdate.second.second, toUpdate.second.third), toUpdate.third)

                                allSpannableStringsLocations[wordNumbersToTrack[indexCounter]!!.first] = updated
                            }
                            if (!isLastLine)
                                currentLine += "\n"

                            lines.add(currentLine)
                            indexCounter++
                        } else {
                            if (wordNumbersToTrack.contains(indexCounter)){
                                val startIndex = currentLine.length - currentWord.length
                                val endIndex = startIndex + currentWord.length
                                val toUpdate = allSpannableStringsLocations[wordNumbersToTrack[indexCounter]!!.first]
                                val updated = Triple( lines.size, Triple(Pair(startIndex,endIndex), toUpdate.second.second, toUpdate.second.third), toUpdate.third)
                                allSpannableStringsLocations[wordNumbersToTrack[indexCounter]!!.first] = updated
                            }
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
        return ArrayList()
    }

    /**
     * This function updates meta data for a spannable string that has been split
     * into multiple lines
     */
    private fun updateNewWordBounds(
        updatedSplitWord: ArrayList<String>,
        lineToSplit: String,
        pair: Pair<Int, Int>
    ) : ArrayList<Pair<Int, Pair<Int, Int>>>{
        val exactSubstring = lineToSplit.substring(pair.first, pair.second)
        val updatedLocations = ArrayList<Pair<Int, Pair<Int, Int>>>()
        var numOfLeftChars = 0
        var leftCharCounter = pair.first
        while (leftCharCounter >= 0 && lineToSplit[leftCharCounter].isLetterOrDigit()) {
            numOfLeftChars++
            leftCharCounter--
        }

        var counterForCharsBeforeSubString = 0
        var counterForCharsWhenSubstringFound = 0
        var startIndex = 0
        for ((index, splitSubStr) in updatedSplitWord.withIndex()) {
            var shouldBreak = false
            if (splitSubStr.length + counterForCharsBeforeSubString < numOfLeftChars) {
                counterForCharsBeforeSubString += splitSubStr.length
            } else {
                for ((charIndex, _) in splitSubStr.withIndex()) {
                    //found sub string
                    if (counterForCharsBeforeSubString == numOfLeftChars) {
                        startIndex = charIndex
                        updatedLocations.add(Pair(index, Pair(charIndex, charIndex)))
                        counterForCharsBeforeSubString++
                    } else if (counterForCharsBeforeSubString > numOfLeftChars && counterForCharsWhenSubstringFound != exactSubstring.length) {
                        if (charIndex == splitSubStr.length - 1)
                            updatedLocations.add(Pair(index, Pair(startIndex, charIndex)))
                        counterForCharsWhenSubstringFound++
                    } else if (counterForCharsWhenSubstringFound == exactSubstring.length) {
                        updatedLocations.add(Pair(index, Pair(startIndex, charIndex)))
                        shouldBreak = true
                        break
                    } else
                        counterForCharsBeforeSubString++

                    if (charIndex == splitSubStr.length - 1 && startIndex != 0)
                        startIndex = 0

                }
                if (shouldBreak)
                    break
            }
        }
//        println(updatedLocations)
        return updatedLocations
    }

    /**
     * locates word number to follow
     */
    private fun locateWordNumber(lineToSplit: String, spanStartIndex: Int): Int {
        var counter = 0
        var wordCounter = 0
        while (counter <= spanStartIndex) {
            if (lineToSplit[counter].isWhitespace())
                wordCounter++

            counter++
        }
        return wordCounter
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
     * @param initialEditableToFormat an arrayList with all the text to be printed
     * @param height the height of the view port in PX
     * @param width the width of the view port in PX
     * @param lineHeight the height of the line in PX
     */
    fun formatPagesToSave(
        initialEditableToFormat: Editable,
        height: Int,
        width: Int,
        lineHeight: Int
    ): ArrayList<EditText> {
        val editText = currentPage.getChildAt(1) as EditText
        val linesPerPage = (height / lineHeight)
        val foregroundColorSpans = initialEditableToFormat.getSpans<ForegroundColorSpan>()
        val boldStyleSpans = initialEditableToFormat.getSpans<StyleSpan>()
        val updatedEditableList = ArrayList<Editable>()
        // first element is the line number
        //second element is the precise coordinates and type of span
        //third is the full word
        val allSpannableStringsLocations =
            ArrayList<Triple<Int, Triple<Pair<Int, Int>, StyleSpan?, ForegroundColorSpan?>, String>>()

        for ((replaceIndex, line) in initialEditableToFormat.lines().withIndex()) {
            if (foregroundColorSpans.isNotEmpty()) {
                foregroundColorSpans.forEach {
                    val substring = initialEditableToFormat.substring(
                        initialEditableToFormat.getSpanStart(it),
                        initialEditableToFormat.getSpanEnd(it)
                    )
                    val fullWord = findFullWord(
                        initialEditableToFormat,
                        initialEditableToFormat.getSpanStart(it),
                        initialEditableToFormat.getSpanEnd(it)
                    )
                    val startLineIndex = initialEditableToFormat.indexOf(line)
                    val endLineIndex = startLineIndex + line.length
                    val spanStartIndex = initialEditableToFormat.getSpanStart(it)
                    val spanEndIndex = initialEditableToFormat.getSpanEnd(it)

                    if (line.contains(substring) && startLineIndex <= spanStartIndex && endLineIndex >= spanEndIndex) {
                        val spanStartIndexInLine = spanStartIndex - startLineIndex
                        val spanEndIndexInLine = spanStartIndexInLine + substring.length

                        allSpannableStringsLocations.add(
                            (Triple(
                                replaceIndex,
                                Triple(
                                    Pair(spanStartIndexInLine, spanEndIndexInLine),
                                    null,
                                    it
                                ),
                                fullWord
                            ))
                        )
                    }
                }
            }
            if (boldStyleSpans.isNotEmpty()) {
                boldStyleSpans.forEach {
                    val substring = initialEditableToFormat.substring(
                        initialEditableToFormat.getSpanStart(it),
                        initialEditableToFormat.getSpanEnd(it)
                    )
                    val fullWord = findFullWord(
                        initialEditableToFormat,
                        initialEditableToFormat.getSpanStart(it),
                        initialEditableToFormat.getSpanEnd(it)
                    )
                    val startLineIndex = initialEditableToFormat.indexOf(line)
                    val endLineIndex = startLineIndex + line.length
                    val spanStartIndex = initialEditableToFormat.getSpanStart(it)
                    val spanEndIndex = initialEditableToFormat.getSpanEnd(it)


                    if (line.contains(substring) && startLineIndex <= spanStartIndex && endLineIndex >= spanEndIndex) {
                        val spanStartIndexInLine = spanStartIndex - startLineIndex
                        val spanEndIndexInLine = spanStartIndexInLine + substring.length

                        allSpannableStringsLocations.add(
                            (Triple(
                                replaceIndex,
                                Triple(
                                    Pair(spanStartIndexInLine, spanEndIndexInLine),
                                    it,
                                    null
                                ),
                                fullWord
                            ))
                        )
                    }
                }
            }
            var tempSpannableString = ""
            if (replaceIndex == initialEditableToFormat.lines().size - 1) {
                for (subEditable in updateTextBounds(
                    line,
                    width,
                    lineHeight,
                    height,
                    true,
                    allSpannableStringsLocations
                )) {
                    tempSpannableString += subEditable.toString()
                }
            } else
                for (subEditable in updateTextBounds(
                    line,
                    width,
                    lineHeight,
                    height,
                    false,
                    allSpannableStringsLocations
                )) {
                    tempSpannableString += subEditable.toString()
                }
            if (updatedEditableList.isNotEmpty()) {
                if (updatedEditableList[updatedEditableList.size - 1].lines().size < (linesPerPage + 1)) {
                    var previousLines =
                        updatedEditableList[updatedEditableList.size - 1].toString()
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
        println(allSpannableStringsLocations)
        return editTextsToRet
    }

    /**
     * Finds the full word containing the spannable string
     */
    private fun findFullWord(initialEditableToFormat: Editable, start: Int, end: Int): String {
        val previousSubstringChar = if (start == 0)
            initialEditableToFormat.substring(0, 0)
        else
            initialEditableToFormat.substring(start - 1, start)
        val nextSubStringChar = if (end == initialEditableToFormat.length)
            initialEditableToFormat.substring(end, end)
        else
            initialEditableToFormat.substring(end, end + 1)

        //is a substring
        if (start == 0 || end == initialEditableToFormat.length || previousSubstringChar[0].isLetterOrDigit() || nextSubStringChar[0].isLetterOrDigit()) {
            var hasFoundStart = false
            var textStart = -1
            var textEnd = -2
            var hasFoundEnd = false
            var counter = 1
            if (start == 0) {
                textStart = 0
                hasFoundStart = true
            }
            while (!hasFoundStart) {
                if (counter >= 0) {
                    val charToInspect = initialEditableToFormat.substring(
                        start - counter,
                        start - counter + 1
                    )
                    //word found
                    if (charToInspect[0].isWhitespace() || charToInspect[0] == '\n') {
                        textStart = start - counter + 1
                        hasFoundStart = true
                    } else {
                        counter++
                    }
                } else
                    break
            }
            counter = 1
            if (end == initialEditableToFormat.length) {
                textEnd = end
                hasFoundEnd = true
            }
            while (!hasFoundEnd) {
                if (counter <= initialEditableToFormat.length - 1) {
                    val charToInspect = initialEditableToFormat.substring(
                        end + counter - 1,
                        end + counter
                    )
                    //word found
                    if (charToInspect[0].isWhitespace() || charToInspect[0] == '\n') {
                        textEnd = end + counter - 1
                        hasFoundEnd = true
                    } else {
                        counter++
                    }
                } else
                    break
            }
            return initialEditableToFormat.substring(textStart, textEnd)
        }
        return initialEditableToFormat.substring(start, end)
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
     * Determines the YPoint of where the text should start for centreVertical alignment
     *
     * @param pageHeight the height of page to be printed on in pixels
     * @param numOfLines the number of lines on the page
     * @param lineHeight the height of each line
     */
    private fun determineCentreVerticalYPoint(
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
     * Determines the X point coordinate for the bitmap to be written on
     */
    private fun determineXPoint(
        isLandscape: Boolean,
        firstEditText: EditText,
        textMarginUtil: TextMarginUtil
    ): Float {
        return when (textPaintAlignment) {
            Paint.Align.LEFT -> if (!isLandscape)
                firstEditText.x
            else textMarginUtil.marginLeft.toFloat()

            Paint.Align.CENTER -> if (!isLandscape)
                (currentPage.width / 2).toFloat()
            else
                (widthAndHeight.first / 2).toFloat()

            else -> if (!isLandscape)
                currentPage.width.toFloat() - firstEditText.x
            else
                widthAndHeight.first - textMarginUtil.marginRight.toFloat()
        }
    }

    /**
     * Determines the Y point of the bitmap to be drawn on
     */
    private fun determineYPoint(
        isLandscape: Boolean,
        isCentreVertical: Boolean,
        firstEditText: EditText,
        editTextBox: EditText,
        imageStrokeMargins: Int,
        textMarginUtil: TextMarginUtil,
        lineHeight: Float
    ): Float {
        if (!isLandscape && !isCentreVertical)
            return firstEditText.y
        else if (!isLandscape)
            return determineCentreVerticalYPoint(
                currentPage.height - imageStrokeMargins,
                editTextBox.text.lines().size,
                lineHeight
            )
        else
            if (isCentreVertical)
                return determineCentreVerticalYPoint(
                    widthAndHeight.second - imageStrokeMargins,
                    editTextBox.text.lines().size,
                    lineHeight
                )
            else
                return textMarginUtil.marginTop.toFloat()
    }

    /**
     * Rebuilds the image shape if the image
     *
     * @param imagePath the path for the image
     * @param imageStrokeMargins the size of the stroke
     */
    fun rebuildImageShape(imageStrokeMargins: Int, imagePath: String): Bitmap {
        val imageView = ShapeableImageView(context)
        val bitmap = Bitmap.createBitmap(
            widthAndHeight.first,
            widthAndHeight.second,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        imageView.layout(
            imageStrokeMargins,
            imageStrokeMargins,
            widthAndHeight.first,
            widthAndHeight.second
        )
        imageView.left = imageStrokeMargins
        imageView.top = imageStrokeMargins
        imageView.right = widthAndHeight.first
        imageView.bottom = widthAndHeight.second
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        imageView.shapeAppearanceModel = ShapeAppearanceModelHelper.shapeImageView(
            outline,
            context.resources,
            imageStrokeMargins.toFloat()
        )
        imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath))

        imageView.draw(canvas)

        return bitmap
    }

    /**
     * Creates a folder with the pictures that is accessible in the application
     * @param editableArrayList Arraylist containing text user entered per page
     * @param title of the poem
     * @param textMarginUtil the utility class containing the text margins
     * @param isLandscape
     * @return 0 when we successfully generated images
     * @return -1 when we failed to
     */
    suspend fun savePagesAsImages(
        editableArrayList: ArrayList<Editable>,
        title: String,
        textMarginUtil: TextMarginUtil,
        imageStrokeMargins: Int,
        isLandscape: Boolean,
        isCentreVertical: Boolean
    ): Int {
        return withContext(Dispatchers.IO) {
            val firstEditText = currentPage.getChildAt(1) as EditText
            val textPixelSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                textSize.toFloat(),
                context.resources.displayMetrics
            )
            val shapeAbleImageView = currentPage.getChildAt(0) as ShapeableImageView
            var counter = 0
            val imagesFolder = context.getDir("savedImages", MODE_PRIVATE)
            val lineHeight = if (!isLandscape)
                firstEditText.lineHeight.toFloat()
            else
                landscapeLineHeight(firstEditText.typeface, textPixelSize)

            val spannableHashMap =
                TextFormatUtilitySaver.spannedEditableHashMap(editableArrayList)
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
                        val editTextArrayList = ArrayList<Pair<Int, ArrayList<EditText>>>()
                        var sizeCounter = 0
                        for ((index, editable) in editableArrayList.withIndex()) {
                            val editTextsToPrint = if (!isLandscape)
                                formatPagesToSave(
                                    editable,
                                    currentPage.height - textMarginUtil.marginBottom,
                                    currentPage.width - textMarginUtil.marginLeft - textMarginUtil.marginRight - imageStrokeMargins,
                                    firstEditText.lineHeight
                                )
                            else
                                formatPagesToSave(
                                    editable,
                                    height = widthAndHeight.second - textMarginUtil.marginTop - textMarginUtil.marginBottom,
                                    width = widthAndHeight.first - textMarginUtil.marginLeft - textMarginUtil.marginRight - imageStrokeMargins,
                                    lineHeight.toInt()
                                )
                            sizeCounter += editTextsToPrint.size
                            editTextArrayList.add(Pair(index, editTextsToPrint))
                        }
                        totalPages.set(sizeCounter)
                        for (editTextBoxPair in editTextArrayList) {
                            for (editTextBox in editTextBoxPair.second) {
                                if (spannableHashMap[editTextBoxPair.first] != null) {
                                    for ((index, styleTextAndTextColor) in spannableHashMap[editTextBoxPair.first]!!.withIndex()) {
                                        updateSpannableString(
                                            isLandscape = isLandscape,
                                            firstEditText = firstEditText,
                                            lineHeight = lineHeight,
                                            spannableHashMap = spannableHashMap,
                                            imageStrokeMargins = imageStrokeMargins,
                                            textMarginUtil = textMarginUtil,
                                            styleTextAndTextColor = styleTextAndTextColor,
                                            spannableHashMapKey = editTextBoxPair.first,
                                            spannableHashMapValueIndex = index
                                        )
                                    }
                                }
                                val imageToAdd =
                                    File(subFolderWithTitleAsName.absolutePath + File.separator + "stanza$counter" + ".png")
                                if (imageToAdd.exists() || imageToAdd.createNewFile()) {
                                    val outStream = FileOutputStream(imageToAdd)

                                    val bitmap = if (isLandscape)
                                        Bitmap.createBitmap(
                                            widthAndHeight.first,
                                            widthAndHeight.second,
                                            Bitmap.Config.ARGB_8888
                                        ) else
                                        Bitmap.createBitmap(
                                            currentPage.width,
                                            currentPage.height,
                                            Bitmap.Config.ARGB_8888
                                        )
                                    val canvas = Canvas(bitmap)

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
                                                imageStrokeMargins,
                                                imageStrokeMargins,
                                                widthAndHeight.first,
                                                widthAndHeight.second
                                            )
                                        else
                                            Rect(
                                                0,
                                                0,
                                                widthAndHeight.first,
                                                widthAndHeight.second
                                            )

                                        if (isLandscape)
                                            canvas.drawBitmap(
                                                rebuildImageShape(
                                                    imageStrokeMargins,
                                                    shapeAbleImageView.tag as String
                                                ),
                                                null,
                                                rect,
                                                null
                                            )
                                        else
                                            canvas.drawBitmap(
                                                shapeAbleImageView.drawToBitmap(),
                                                null,
                                                rect,
                                                null
                                            )
                                    }

                                    if (currentPage.background != null) {
                                        if (isLandscape) {
                                            val background =
                                                if (currentPage.background.constantState?.newDrawable() is ColorDrawable)
                                                    currentPage.background.constantState?.newDrawable() as ColorDrawable
                                                else
                                                    currentPage.background.constantState?.newDrawable() as GradientDrawable
                                            background.setBounds(
                                                0,
                                                0,
                                                widthAndHeight.first,
                                                widthAndHeight.second
                                            )
                                            background.draw(canvas)
                                        } else {
                                            currentPage.background.draw(canvas)
                                        }
                                    } else if (currentPage.getTag(1) != null) {
                                        canvas.drawColor(currentPage.getTag(1) as Int)
                                    } else {
                                        if (shapeAbleImageView.tag == null)
                                            canvas.drawColor(context.getColor(R.color.white))
                                    }
                                    val xPoint =
                                        determineXPoint(
                                            isLandscape,
                                            firstEditText,
                                            textMarginUtil
                                        )

                                    var yPoint = determineYPoint(
                                        isLandscape,
                                        isCentreVertical,
                                        firstEditText,
                                        editTextBox,
                                        imageStrokeMargins,
                                        textMarginUtil,
                                        lineHeight
                                    )

                                    val textPaint = Paint()
                                    textPaint.typeface = editTextBox.typeface
                                    textPaint.textSize = textPixelSize
                                    textPaint.color = editTextBox.currentTextColor
                                    textPaint.textAlign = textPaintAlignment
                                    var linesToSkipOver = 0
                                    for (line in editTextBox.text.lines()) {
                                        var shouldPrintLine = true
                                        if (linesToSkipOver > 0) {
                                            linesToSkipOver--
                                            continue
                                        }
                                        // check if the line is a spanned line
                                        if (spannableHashMap[editTextBoxPair.first] != null) {
                                            var tripleToRemove: Triple<CustomTextStyle, SpannableString, Int?>? =
                                                null
                                            for (spannedTextTriple in spannableHashMap[editTextBoxPair.first]!!) {
                                                val spannedString = spannedTextTriple.second
                                                if (spannedString.lines().size == 1) {
                                                    if (line == spannedString.toString()) {
                                                        yPoint += lineHeight
                                                        drawToCanvas(
                                                            canvas = canvas,
                                                            yPoint = yPoint,
                                                            xPoint = xPoint,
                                                            spannedTextTriple = spannedTextTriple,
                                                            editTextBox = editTextBox,
                                                            textPixelSize = textPixelSize,
                                                            line = line
                                                        )
                                                        shouldPrintLine = false
                                                        tripleToRemove = spannedTextTriple
                                                        break
                                                    }
                                                } else {
                                                    if (spannedString.lines()[0] == line) {
                                                        //one user entered line was split into multiple lines
                                                        for (spannedTextLine in spannedString.lines()) {
                                                            yPoint += lineHeight
                                                            drawToCanvas(
                                                                canvas = canvas,
                                                                yPoint = yPoint,
                                                                xPoint = xPoint,
                                                                spannedTextTriple = spannedTextTriple,
                                                                editTextBox = editTextBox,
                                                                textPixelSize = textPixelSize,
                                                                line = spannedTextLine
                                                            )
                                                            linesToSkipOver++
                                                        }
                                                        shouldPrintLine = false
                                                        tripleToRemove = spannedTextTriple
                                                        break
                                                    }
                                                }
                                            }
                                            if (tripleToRemove != null)
                                                spannableHashMap[editTextBoxPair.first]?.remove(
                                                    tripleToRemove
                                                )
                                        }
                                        if (shouldPrintLine) {
                                            yPoint += lineHeight
                                            canvas.drawText(line, xPoint, yPoint, textPaint)
                                        }
                                    }

                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                                    outStream.close()
                                    counter++
                                    progressTracker.set(progressTracker.get() + 1)
                                }
                            }
                        }
                        return@withContext 0
                    }
                }
            } catch (exception: IOException) {
                exception.printStackTrace()
                return@withContext -1
            }
            return@withContext -1
        }
    }

    /**
     * Updates the spannable string in the spannableHashMap to fit bounds in case it did not
     *
     * @param isLandscape orientation of poem
     * @param firstEditText the first edit text to print
     * @param lineHeight the line height in the edit text
     * @param spannableHashMap the hashmap to edit
     * @param textMarginUtil the text margin utility class containing bounds
     * @param styleTextAndTextColor the pair value to update
     * @param imageStrokeMargins margin size if there is a stroke
     * @param spannableHashMapKey the key to edit
     * @param spannableHashMapValueIndex the arraylist index number to edit
     */
    private fun updateSpannableString(
        isLandscape: Boolean,
        firstEditText: EditText,
        lineHeight: Float,
        spannableHashMap: HashMap<Int, ArrayList<Triple<CustomTextStyle, SpannableString, Int?>>>,
        textMarginUtil: TextMarginUtil,
        styleTextAndTextColor: Triple<CustomTextStyle, SpannableString, Int?>,
        imageStrokeMargins: Int,
        spannableHashMapKey: Int,
        spannableHashMapValueIndex: Int
    ) {
        val spannableEditableArrayList: ArrayList<Editable>
        val tempSpannableString = SpannableStringBuilder("")
        if (!isLandscape)
            spannableEditableArrayList = updateTextBounds(
                styleTextAndTextColor.second.toString(),
                width = currentPage.width - textMarginUtil.marginLeft - textMarginUtil.marginRight - imageStrokeMargins,
                lineHeight = firstEditText.lineHeight,
                height = currentPage.height - textMarginUtil.marginBottom,
                isLastLine = false,
                ArrayList()
            )
        else
            spannableEditableArrayList = updateTextBounds(
                styleTextAndTextColor.second.toString(),
                width = widthAndHeight.first - textMarginUtil.marginLeft - textMarginUtil.marginRight - imageStrokeMargins,
                lineHeight = lineHeight.toInt(),
                height = widthAndHeight.second - textMarginUtil.marginTop - textMarginUtil.marginBottom,
                isLastLine = false,
                ArrayList()
            )

        if (spannableEditableArrayList.size > 1) {
            for (editable in spannableEditableArrayList) {
                tempSpannableString.append(editable)
            }

            spannableHashMap[spannableHashMapKey]?.set(
                spannableHashMapValueIndex,
                Triple(
                    styleTextAndTextColor.first,
                    SpannableString(tempSpannableString),
                    styleTextAndTextColor.third
                )
            )
        }
    }

    /**
     * Draws spanned text to canvas
     *
     * @param canvas the canvas to draw on
     * @param yPoint the current y axis point
     * @param xPoint the current x axis point
     * @param spannedTextTriple the spanned text container
     * @param editTextBox the editText to use when setting typeface
     * @param textPixelSize size of the text in Pixels
     * @param line the line to print
     */
    private fun drawToCanvas(
        canvas: Canvas,
        yPoint: Float,
        xPoint: Float,
        spannedTextTriple: Triple<CustomTextStyle, SpannableString, Int?>,
        editTextBox: EditText,
        textPixelSize: Float,
        line: String
    ) {
        if (spannedTextTriple.first == CustomTextStyle.CUSTOM_BOLD) {
            val tempTextPaint = Paint()
            tempTextPaint.typeface = Typeface.create(editTextBox.typeface, Typeface.BOLD)
            tempTextPaint.textSize = textPixelSize
            tempTextPaint.color = editTextBox.currentTextColor
            tempTextPaint.textAlign = textPaintAlignment
            canvas.drawText(line, xPoint, yPoint, tempTextPaint)
        } else {
            val tempTextPaint = Paint()
            tempTextPaint.typeface = editTextBox.typeface
            tempTextPaint.textSize = textPixelSize
            tempTextPaint.color = spannedTextTriple.third!!
            tempTextPaint.textAlign = textPaintAlignment
            canvas.drawText(line, xPoint, yPoint, tempTextPaint)
        }
    }
}