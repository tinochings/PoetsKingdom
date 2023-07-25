package com.wendorochena.poetskingdom.utils

import android.content.res.Resources
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.OutlineTypes

class TextMarginUtil(
    var marginLeft: Int = 0,
    var marginRight: Int = 0,
    var marginTop: Int = 0,
    var marginBottom: Int = 0
) {


    /**
     * Determines the bounds for text for a given outline.
     *
     * @param outline The outline to determine
     * @param resources resources of activity
     * @param strokeSize the size of the stroke of the outline
     */
    fun determineTextMargins(outline: String, resources: Resources, strokeSize: Int) {
        when (outline) {
            OutlineTypes.ROUNDED_RECTANGLE.toString(), OutlineTypes.RECTANGLE.toString() -> {
                marginLeft = strokeSize + 4
                marginRight = strokeSize + 4
                marginTop = strokeSize + 4
                marginBottom = strokeSize + 4
            }

            OutlineTypes.TEARDROP.toString() -> {
                marginLeft = resources.getDimension(R.dimen.teardropCornerSizeTopLeft)
                    .toInt() + strokeSize - 3
                marginRight = strokeSize
                marginTop = strokeSize
                marginBottom =
                    resources.getDimension(R.dimen.teardropCornerSizeTopLeft).toInt() - strokeSize
            }

            OutlineTypes.ROTATED_TEARDROP.toString() -> {
                marginLeft = strokeSize
                marginRight = resources.getDimension(R.dimen.rotatedTeardropCornerSizeTopRight)
                    .toInt() - strokeSize
                marginTop = strokeSize
                marginBottom = resources.getDimension(R.dimen.rotatedTeardropCornerSizeTopRight)
                    .toInt() - strokeSize
            }

            OutlineTypes.LEMON.toString() -> {
                // 2 is an offset
                marginLeft = resources.getDimension(R.dimen.lemonCornerSizeTopLeft)
                    .toInt() - (strokeSize * 2) - 2
                marginRight = strokeSize + 3
                marginTop = strokeSize
                marginBottom = resources.getDimension(R.dimen.lemonCornerSizeBottomRight)
                    .toInt() - (strokeSize * 2) - 2
            }
        }
    }

    override fun toString(): String {
        return "$marginLeft $marginRight $marginTop $marginBottom"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextMarginUtil

        if (marginLeft != other.marginLeft) return false
        if (marginRight != other.marginRight) return false
        if (marginTop != other.marginTop) return false
        if (marginBottom != other.marginBottom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = marginLeft
        result = 31 * result + marginRight
        result = 31 * result + marginTop
        result = 31 * result + marginBottom
        return result
    }

}