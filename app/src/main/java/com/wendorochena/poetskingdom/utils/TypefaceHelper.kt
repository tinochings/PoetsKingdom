package com.wendorochena.poetskingdom.utils

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.wendorochena.poetskingdom.R

class TypefaceHelper {

    companion object {
        /**
         * Unfortunately I have decided to hard code this for performance reasons. The class PoemThemeActivity is already
         * too complex and dynamic. Any more dynamic strain is not efficient and might hinder user satisfaction
         * @param typeFaceName The name of the typeface
         * @param applicationContext the context of the calling application
         * @return the typeface if it was found. If none was found the default typeface is returned
         */
        fun getTypeFace(typeFaceName: String, applicationContext : Context): Typeface? {

            when (typeFaceName) {
                "ariana_violeta_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.ariana_violeta_font)
                }
                "cabal_bold_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.cabal_bold_font)
                }
                "cabal_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.cabal_font)
                }
                "lobster_regular_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.lobster_regular_font)
                }
                "monospace" -> {
                    return Typeface.MONOSPACE
                }
                "sans-serif" -> {
                    return Typeface.SANS_SERIF
                }
                "serif" -> {
                    return Typeface.SERIF
                }
                "adinekirnberg_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.adinekirnberg_font)
                }
                "bangers_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.bangers_font)
                }
                "flaemische_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.flaemische_font)
                }
                "opensansregular_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.opensansregular_font)
                }
                "sacramento_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.sacramento_font)
                }
                "scriptin_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.scriptin_font)
                }
                "sourcesansproregular_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.sourcesansproregular_font)
                }
                "thesignature_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.thesignature_font)
                }
                "alegreya_sans_sc_medium_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.alegreya_sans_sc_medium_font)
                }
                "ayuma_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.ayuma_font)
                }
                "bungasai_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.bungasai_font)
                }
                "clicker_script_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.clicker_script_font)
                }
                "comfortaa_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.comfortaa_font)
                }
                "crimson_bold_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.crimson_bold_font)
                }
                "crimson_roman_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.crimson_roman_font)
                }
                "dense_regular_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.dense_regular_font)
                }
                "glass_antiqua_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.glass_antiqua_font)
                }
                "honey_script_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.honey_script_font)
                }
                "josefin_sans_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.josefin_sans_font)
                }
                "libre_baskerville_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.libre_baskerville_font)
                }
                "life_savers_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.life_savers_font)
                }
                "lucian_schoenschrift_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.lucian_schoenschrift_font)
                }
                "magnolia_script_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.magnolia_script_font)
                }
                "medula_one_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.medula_one_font)
                }
                "nautilus_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.nautilus_font)
                }
                "nickainley_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.nickainley_font)
                }
                "oldenglishfive_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.oldenglishfive_font)
                }
                "quattrocento_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.quattrocento_font)
                }
                "rogue_script_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.rouge_script_font)
                }
                "sexsmith_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.sexsmith_font)
                }
                "typerighter_medium_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.typerighter_medium_font)
                }
                "righteous_regular_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.righteous_regular_font)
                }
                "chopin_script_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.chopin_script_font)
                }
            }
            return Typeface.DEFAULT
        }
    }
}