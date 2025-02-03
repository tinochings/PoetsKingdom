package com.wendorochena.poetskingdom.viewModels.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import coil3.request.ImageRequest
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.poemdata.PoemTheme
import com.wendorochena.poetskingdom.poemdata.TextAlignment
import com.wendorochena.poetskingdom.ui.theme.MadzinzaGreen
import com.wendorochena.poetskingdom.utils.TextMarginUtil
import com.wendorochena.poetskingdom.viewModels.HeadingSelection

data class PoemThemeViewModelModel(
    val headingSelection : HeadingSelection = HeadingSelection.OUTLINE,
    val backgroundType : BackgroundType = BackgroundType.DEFAULT,
    val backgroundImageChosen: String? = null,
    val backgroundColorChosen: String? = null,
    val backgroundColorChosenAsInt: Int? = null,
    val outlineType: OutlineTypes? = null,
    val outlineColor: Int = MadzinzaGreen.toArgb(),
    val fontColor: Int = Color.Black.toArgb(),
    val textFontFamily: FontFamily= FontFamily(android.graphics.Typeface.DEFAULT),
    val textFontFamilyString: String = "default",
    val fontSize: Float = 14f,
    val textAlignment: TextAlignment = TextAlignment.LEFT,
    val shouldDisplayDialog : Boolean = false,
    val poemThemeResult : Int = -2,
    val textMarginUtil : TextMarginUtil = TextMarginUtil(),
    val poemTitle : String = "",
    val isBold : Boolean = false,
    val isItalic : Boolean = false,
    val isEditTheme : Boolean = false,
    val savedAlbumName: String? = null,
    val poemThemeState : PoemTheme = PoemTheme(),
    val imageRequests : List<ImageRequest> = ArrayList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PoemThemeViewModelModel

        if (headingSelection != other.headingSelection) return false
        if (backgroundType != other.backgroundType) return false
        if (backgroundImageChosen != other.backgroundImageChosen) return false
        if (backgroundColorChosen != other.backgroundColorChosen) return false
        if (backgroundColorChosenAsInt != other.backgroundColorChosenAsInt) return false
        if (outlineType != other.outlineType) return false
        if (outlineColor != other.outlineColor) return false
        if (fontColor != other.fontColor) return false
        if (textFontFamily != other.textFontFamily) return false
        if (textFontFamilyString != other.textFontFamilyString) return false
        if (fontSize != other.fontSize) return false
        if (textAlignment != other.textAlignment) return false
        if (shouldDisplayDialog != other.shouldDisplayDialog) return false
        if (poemThemeResult != other.poemThemeResult) return false
        if (textMarginUtil != other.textMarginUtil) return false
        if (poemTitle != other.poemTitle) return false
        if (isBold != other.isBold) return false
        if (isItalic != other.isItalic) return false
        if (isEditTheme != other.isEditTheme) return false
        if (savedAlbumName != other.savedAlbumName) return false
        if (poemThemeState != other.poemThemeState) return false
        if (imageRequests != other.imageRequests) return false

        return true
    }

    override fun hashCode(): Int {
        var result = headingSelection.hashCode()
        result = 31 * result + backgroundType.hashCode()
        result = 31 * result + (backgroundImageChosen?.hashCode() ?: 0)
        result = 31 * result + (backgroundColorChosen?.hashCode() ?: 0)
        result = 31 * result + (backgroundColorChosenAsInt ?: 0)
        result = 31 * result + (outlineType?.hashCode() ?: 0)
        result = 31 * result + outlineColor
        result = 31 * result + fontColor
        result = 31 * result + textFontFamily.hashCode()
        result = 31 * result + textFontFamilyString.hashCode()
        result = 31 * result + fontSize.hashCode()
        result = 31 * result + textAlignment.hashCode()
        result = 31 * result + shouldDisplayDialog.hashCode()
        result = 31 * result + poemThemeResult
        result = 31 * result + textMarginUtil.hashCode()
        result = 31 * result + poemTitle.hashCode()
        result = 31 * result + isBold.hashCode()
        result = 31 * result + isItalic.hashCode()
        result = 31 * result + isEditTheme.hashCode()
        result = 31 * result + (savedAlbumName?.hashCode() ?: 0)
        result = 31 * result + poemThemeState.hashCode()
        return result
    }
}


