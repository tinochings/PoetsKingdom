package com.wendorochena.poetskingdom.poemdata

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.content.res.ResourcesCompat
import com.wendorochena.poetskingdom.R

data class PoemTheme(var backgroundType: BackgroundType, private val applicationContext: Context) {
    var backgroundColor: String = "#FFFFFFFF"
    var imagePath: String = ""
    var outline: String = ""
    var textSize = 14
    var textColor: String = "#000000"
    var textColorAsInt = -16777216
    //        applicationContext.resources.getColor(R.color.black, null)
    var backgroundColorAsInt = -1
    //        applicationContext.resources.getColor(R.color.white,null)
    var textAlignment: TextAlignment = TextAlignment.LEFT
    var textFontFamily: String = "Default"
    var outlineColor: Int = -7821273
    //        applicationContext.resources.getColor(R.color.madzinza_green, null)
    var poemTitle: String = ""

    override fun toString(): String {
        return "PoemTitle: $poemTitle, BackgroundType $backgroundType, BackgroundColor: " +
                "$backgroundColor,  BackGroundColorAsInt: $backgroundColorAsInt ImagePath:" +
                " $imagePath, Outline: $outline, OutlineColor: $outlineColor TextSize: $textSize, " +
                "TextColor: $textColor, TextColorAsInt: $textColorAsInt, TextAlignment: " +
                "$textAlignment, TextFontFamily: $textFontFamily"
    }

    companion object PoemThemeHelper {

        fun parseBackgroundType(backgroundType: String): BackgroundType {
            when (backgroundType) {
                BackgroundType.COLOR.toString() -> {
                    return BackgroundType.COLOR
                }
                BackgroundType.IMAGE.toString() -> {
                    return BackgroundType.IMAGE
                }
                BackgroundType.OUTLINE.toString() -> {
                    return BackgroundType.OUTLINE
                }
                BackgroundType.OUTLINE_WITH_COLOR.toString() -> {
                    return BackgroundType.OUTLINE_WITH_COLOR
                }
                BackgroundType.OUTLINE_WITH_IMAGE.toString() -> {
                    return BackgroundType.OUTLINE_WITH_IMAGE
                }
            }
            return BackgroundType.DEFAULT
        }

        /**
         * Multiple arguments per background type are returned as a destring delimited by space char
         * They will further be processed as split arrays
         */
        fun determineBackgroundTypeAsString(backgroundType: BackgroundType): String {
            when (backgroundType) {
                BackgroundType.IMAGE -> {
                    return "imagePath"
                }
                BackgroundType.COLOR -> {
                    return "backgroundColor backgroundColorAsInt"
                }
                BackgroundType.OUTLINE -> {
                    return "backgroundOutline backgroundOutlineColor"
                }
                BackgroundType.OUTLINE_WITH_IMAGE -> {
                    return "imagePath backgroundOutline backgroundOutlineColor"
                }
                BackgroundType.OUTLINE_WITH_COLOR -> {
                    return "backgroundOutlineWithColor backgroundOutlineColor backgroundColor backgroundColorAsInt"
                }
                BackgroundType.DEFAULT -> {
                    return "backgroundColor backgroundColorAsInt"
                }
            }
        }


        /**
         * Determines the TextAlignment value of @param string
         * @param string string to be determined
         * @return the TextAlignment class value of left as default
         */
        fun determineTextAlignment(string: String): TextAlignment {
            when (string) {
                TextAlignment.LEFT.toString() -> {
                    return TextAlignment.LEFT
                }
                TextAlignment.RIGHT.toString() -> {
                    return TextAlignment.RIGHT
                }
                TextAlignment.CENTRE.toString() -> {
                    return TextAlignment.CENTRE
                }
                TextAlignment.CENTRE_VERTICAL.toString() -> {
                    return TextAlignment.CENTRE_VERTICAL
                }
                TextAlignment.CENTRE_VERTICAL_LEFT.toString() -> {
                    return TextAlignment.CENTRE_VERTICAL_LEFT
                }
                TextAlignment.CENTRE_VERTICAL_RIGHT.toString() -> {
                    return TextAlignment.CENTRE_VERTICAL_RIGHT
                }
            }
            return TextAlignment.LEFT
        }

        /**
         * @param orientation orientation of the poem
         * @param poemTheme the PoemTheme instance
         * @param leftMargin the left margin
         * @param rightMargin the right margin
         * @param topMargin the top margin
         * @param bottomMargin the bottom margin
         * @return Returns outline and the color selected
         */

        fun getOutlineAndColor(
            orientation: String,
            poemTheme: PoemTheme,
            leftMargin: Int,
            rightMargin: Int,
            topMargin: Int,
            bottomMargin: Int,
            applicationContext: Context
        ): Drawable {

            val strokeSize: Int = if (orientation == "portrait")
                applicationContext.resources.getDimensionPixelSize(R.dimen.portraitStrokeSize)
            else
                applicationContext.resources.getDimensionPixelSize(R.dimen.strokeSize)

            val defaultDrawable = ResourcesCompat.getDrawable(
                applicationContext.resources,
                R.drawable.rounded_rectangle_outline,
                null
            ) as GradientDrawable

            defaultDrawable.setStroke(
                strokeSize, poemTheme.outlineColor
            )

            defaultDrawable.setBounds(
                leftMargin,
                topMargin,
                rightMargin,
                bottomMargin
            )
            when (poemTheme.outline) {
                OutlineTypes.ROUNDED_RECTANGLE.toString() -> {
                    return defaultDrawable
                }

                OutlineTypes.TEARDROP.toString() -> {
                    val gradientDrawable = ResourcesCompat.getDrawable(
                        applicationContext.resources,
                        R.drawable.teardrop_outline,
                        null
                    ) as GradientDrawable
                    gradientDrawable.setBounds(
                        leftMargin,
                        topMargin,
                        rightMargin,
                        bottomMargin
                    )
                    gradientDrawable.setStroke(
                        strokeSize, poemTheme.outlineColor
                    )
                    return gradientDrawable
                }

                OutlineTypes.ROTATED_TEARDROP.toString() -> {
                    val gradientDrawable = ResourcesCompat.getDrawable(
                        applicationContext.resources,
                        R.drawable.rotated_teardrop,
                        null
                    ) as GradientDrawable
                    gradientDrawable.setBounds(
                        leftMargin,
                        topMargin,
                        rightMargin,
                        bottomMargin
                    )
                    gradientDrawable.setStroke(
                        strokeSize, poemTheme.outlineColor
                    )
                    return gradientDrawable
                }

                OutlineTypes.RECTANGLE.toString() -> {
                    val gradientDrawable = ResourcesCompat.getDrawable(
                        applicationContext.resources,
                        R.drawable.rectangle_outline,
                        null
                    ) as GradientDrawable
                    gradientDrawable.setBounds(
                        leftMargin,
                        topMargin,
                        rightMargin,
                        bottomMargin
                    )
                    gradientDrawable.setStroke(
                        strokeSize, poemTheme.outlineColor
                    )
                    return gradientDrawable
                }
                OutlineTypes.LEMON.toString() -> {
                    val gradientDrawable = ResourcesCompat.getDrawable(
                        applicationContext.resources,
                        R.drawable.lemon_outline,
                        null
                    ) as GradientDrawable
                    gradientDrawable.setBounds(
                        leftMargin,
                        topMargin,
                        rightMargin,
                        bottomMargin
                    )
                    gradientDrawable.setStroke(
                        strokeSize, poemTheme.outlineColor
                    )
                    return gradientDrawable
                }
            }
            return defaultDrawable
        }
    }
}