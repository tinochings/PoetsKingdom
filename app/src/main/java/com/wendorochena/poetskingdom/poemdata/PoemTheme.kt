package com.wendorochena.poetskingdom.poemdata

import android.content.Context

data class PoemTheme(var backgroundType: BackgroundType, private val applicationContext: Context) {
    private  var backgroundColor: String = "#FFFFFFFF"
    private  var imagePath: String = ""
    private  var outline: String =""
    private var textSize = 14
    private var textColor: String = "#000000"
    private var textColorAsInt = -16777216
//        applicationContext.resources.getColor(R.color.black, null)
    private var backgroundColorAsInt = -1
//        applicationContext.resources.getColor(R.color.white,null)
    private var textAlignment: TextAlignment = TextAlignment.LEFT
    private var textFontFamily: String = "Default"
    private var outlineColor: Int = -7821273
//        applicationContext.resources.getColor(R.color.madzinza_green, null)
    private var poemTitle: String = ""


    fun setBackGroundType(backgroundType: BackgroundType) {
        this.backgroundType = backgroundType
    }

    fun getBackgroundColor(): String {
        return backgroundColor
    }

    fun setBackgroundColor(color: String) {
        backgroundColor = color
    }

    fun getImagePath(): String {
        return imagePath
    }

    fun setImagePath(imagePath: String) {
        this.imagePath = imagePath
    }

    fun getOutline(): String {
        return outline
    }

    fun setOutline(outline: String) {
        this.outline = outline
    }

    fun getTextSize(): Int {
        return textSize
    }

    fun setTextSize(textSize: Int) {
        this.textSize = textSize
    }

    fun getTextColor(): String {
        return textColor
    }

    fun setTextColor(textColor: String) {
        this.textColor = textColor
    }

    fun getTextFont(): String {
        return textFontFamily
    }

    fun setTextFont(textFontFamily: String) {
        this.textFontFamily = textFontFamily
    }

    fun getTextAlignment(): TextAlignment {
        return textAlignment
    }

    fun setTextAlignment(textAlignment: TextAlignment) {
        this.textAlignment = textAlignment
    }

    fun getTitle(): String {
        return poemTitle
    }

    fun setTitle(poemTitle: String) {
        this.poemTitle = poemTitle
    }

    fun getTextColorAsInt(): Int {
        return textColorAsInt
    }

    fun setTextColorAsInt(color: Int) {
        textColorAsInt = color
    }

    fun getBackgroundColorAsInt(): Int {
        return backgroundColorAsInt
    }

    fun setBackgroundColorAsInt(color: Int) {
        backgroundColorAsInt = color
    }

    fun getOutlineColor(): Int {
        return outlineColor
    }

    fun setOutlineColor(color: Int) {
        this.outlineColor = color
    }

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
    }
}