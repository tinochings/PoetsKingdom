package com.wendorochena.poetskingdom.utils

import android.text.Editable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.text.getSpans

enum class CustomTextStyle {
    CUSTOM_BOLD, CUSTOM_COLOR_SELECTED_TEXT
}

data class TextFormatInformation(val from: Int, val end: Int, val customTextStyle: CustomTextStyle)

/**
 * This class stores information about the custom formatted text.
 */
class TextFormatUtilitySaver(val poem: ArrayList<Editable>) {
    val textFormatInformation = ArrayList<TextFormatInformation>()
    val foregroundColorSpanArrayList = ArrayList<ForegroundColorSpan>()
    val boldSpanArrayList = ArrayList<StyleSpan>()

    fun getFormattedPoemText() {

    }

    fun saveFormattedPoemText() {
        for (editable in poem) {
            val customTextColorSpans = editable.getSpans<ForegroundColorSpan>()
            val boldSpans = editable.getSpans<StyleSpan>()
            if (customTextColorSpans.isNotEmpty()) {
                customTextColorSpans.forEach {
                    textFormatInformation.add(
                        TextFormatInformation(
                            editable.getSpanStart(it),
                            editable.getSpanEnd(it),
                            CustomTextStyle.CUSTOM_COLOR_SELECTED_TEXT
                        )
                    )
                    foregroundColorSpanArrayList.add(it)
                }
            }
            if (boldSpans.isNotEmpty()) {
                boldSpans.forEach {
                    textFormatInformation.add(
                        TextFormatInformation(
                            editable.getSpanStart(it),
                            editable.getSpanEnd(it),
                            CustomTextStyle.CUSTOM_COLOR_SELECTED_TEXT
                        )
                    )
                    boldSpanArrayList.add(it)
                }
            }
        }
    }

    companion object {

        /**
         * Removes any formatting on selected text
         *
         * @param editable the editable to remove text formatting from
         * @param start the start index of the text
         * @param end the end index of the text
         */
        fun removeSpannableText(editable: Editable, start: Int, end: Int) {
            val boldSpan = editable.getSpans<StyleSpan>(start, end)
            val colorSpan = editable.getSpans<ForegroundColorSpan>(start, end)
            if (boldSpan.isNotEmpty()) {
                boldSpan.forEach {
                    editable.removeSpan(it)
                }
            }
            if (colorSpan.isNotEmpty()) {
                colorSpan.forEach {
                    editable.removeSpan(it)
                }
            }
        }

        /**
         *
         * @return A hashMap containing spannable text for each poem
         */
        fun spannedEditableHashMap(editableArray: ArrayList<Editable>): HashMap<Int, ArrayList<Triple<CustomTextStyle, SpannableString, Int?>>> {
            val hashMap = HashMap<Int, ArrayList<Triple<CustomTextStyle, SpannableString, Int?>>>()
            val spanIndexHelper = ArrayList<Int>()
            for ((index, editable) in editableArray.withIndex()) {
                val customTextColorSpans = editable.getSpans<ForegroundColorSpan>()
                val boldSpans = editable.getSpans<StyleSpan>()
                if (customTextColorSpans.isNotEmpty()) {
                    hashMap[index] = ArrayList()
                    customTextColorSpans.forEach {
                        val spanStart = editable.getSpanStart(it)
                        val spannableToAdd = SpannableString(
                            editable.substring(
                                editable.getSpanStart(it)until editable.getSpanEnd(it)
                            )
                        )
                        spannableToAdd.setSpan(
                            it,
                            0,
                            spannableToAdd.length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        hashMap[index]?.add(Triple(CustomTextStyle.CUSTOM_COLOR_SELECTED_TEXT,spannableToAdd, it.foregroundColor))
                        spanIndexHelper.add(spanStart)
                    }
                }
                if (boldSpans.isNotEmpty()) {
                    if (hashMap[index] == null)
                        hashMap[index] = ArrayList()
                    boldSpans.forEach {
                        val spanStart = editable.getSpanStart(it)
                        val spannableToAdd = SpannableString(
                            editable.substring(
                                editable.getSpanStart(it) until editable.getSpanEnd(it)
                            )
                        )
                        spannableToAdd.setSpan(
                            it,
                            0,
                            spannableToAdd.length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        //sort bold insertions as they are added second
                        if (spanIndexHelper.isNotEmpty()) {
                            var counter = 0
                            while (counter < spanIndexHelper.size) {
                                if (spanIndexHelper[counter] > spanStart){
                                    spanIndexHelper.add(counter, spanStart)
                                    hashMap[index]?.add(counter, Triple(CustomTextStyle.CUSTOM_BOLD,spannableToAdd, null))
                                    break
                                }
                                counter++
                            }
                            if (counter == spanIndexHelper.size)
                                hashMap[index]?.add(
                                    Triple(
                                        CustomTextStyle.CUSTOM_BOLD,
                                        spannableToAdd,
                                        null
                                    )
                                )
                        } else {
                            hashMap[index]?.add(
                                Triple(
                                    CustomTextStyle.CUSTOM_BOLD,
                                    spannableToAdd,
                                    null
                                )
                            )
                            spanIndexHelper.add(spanStart)
                        }
                    }
                }
            }
            return hashMap
        }
    }
}