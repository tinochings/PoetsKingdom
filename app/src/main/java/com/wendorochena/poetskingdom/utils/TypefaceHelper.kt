package com.wendorochena.poetskingdom.utils

import android.content.Context
import android.graphics.Typeface
import android.graphics.Typeface.DEFAULT
import android.graphics.Typeface.MONOSPACE
import android.graphics.Typeface.SANS_SERIF
import android.graphics.Typeface.SERIF
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
                    return MONOSPACE
                }
                "sans-serif" -> {
                    return SANS_SERIF
                }
                "serif" -> {
                    return SERIF
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
                "rochester_regular_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.rochester_regular_font)
                }
                "steelfish_rounded_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.steelfish_rounded_font)
                }
                "vinque_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.vinque_font)
                }
                "rm_typerighter_old_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.rm_typerighter_old_font)
                }
                "lm_monocaps_regular_font" -> {
                    return ResourcesCompat.getFont(applicationContext,R.font.lm_monocaps_regular_font)
                }
                "monofonto_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.monofonto_font)
                }
                "code_new_roman_font" -> {
                    return ResourcesCompat.getFont(applicationContext, R.font.code_new_roman_font)
                }
                "attic_font" -> {
                    return ResourcesCompat.getFont(applicationContext,R.font.attic_font)
                }
            }
            return DEFAULT
        }

        /**
         * Jetpack Compose helper
         */
        fun getTypeFace(typeFaceName: String): FontFamily {

            when (typeFaceName) {
                "ariana_violeta" -> {
                    return FontFamily(Font(R.font.ariana_violeta))
                }
                "cabal_bold" -> {
                    return FontFamily(Font( R.font.cabal_bold))
                }
                "cabal" -> {
                    return FontFamily(Font(R.font.cabal))
                }
                "lobster_regular" -> {
                    return FontFamily(Font(R.font.lobster_regular))
                }
                "monospace" -> {
                    return FontFamily(MONOSPACE)
                }
                "sans-serif" -> {
                    return FontFamily(SANS_SERIF)
                }
                "serif" -> {
                    return FontFamily(SERIF)
                }
                "adinekirnberg" -> {
                    return FontFamily(Font(R.font.adinekirnberg))
                }
                "bangers" -> {
                    return FontFamily(Font(R.font.bangers))
                }
                "flaemische" -> {
                    return FontFamily(Font(R.font.flaemische))
                }
                "opensansregular" -> {
                    return FontFamily(Font(R.font.opensansregular))
                }
                "sacramento" -> {
                    return FontFamily(Font(R.font.sacramento))
                }
                "scriptin" -> {
                    return FontFamily(Font(R.font.scriptin))
                }
                "sourcesansproregular" -> {
                    return FontFamily(Font(R.font.sourcesansproregular))
                }
                "thesignature" -> {
                    return FontFamily(Font(R.font.thesignature))
                }
                "alegreya_sans_sc_medium" -> {
                    return FontFamily(Font(R.font.alegreya_sans_sc_medium))
                }
                "ayuma" -> {
                    return FontFamily(Font(R.font.ayuma))
                }
                "bungasai" -> {
                    return FontFamily(Font(R.font.bungasai))
                }
                "clicker_script" -> {
                    return FontFamily(Font(R.font.clicker_script))
                }
                "comfortaa" -> {
                    return FontFamily(Font(R.font.comfortaa))
                }
                "crimson_bold" -> {
                    return FontFamily(Font(R.font.crimson_bold))
                }
                "crimson_roman" -> {
                    return FontFamily(Font(R.font.crimson_roman))
                }
                "dense_regular" -> {
                    return FontFamily(Font(R.font.dense_regular))
                }
                "glass_antiqua" -> {
                    return FontFamily(Font(R.font.glass_antiqua))
                }
                "honey_script" -> {
                    return FontFamily(Font(R.font.honey_script))
                }
                "josefin_sans" -> {
                    return FontFamily(Font(R.font.josefin_sans))
                }
                "libre_baskerville" -> {
                    return FontFamily(Font(R.font.libre_baskerville))
                }
                "life_savers" -> {
                    return FontFamily(Font(R.font.life_savers))
                }
                "lucian_schoenschrift" -> {
                    return FontFamily(Font(R.font.lucian_schoenschrift))
                }
                "magnolia_script" -> {
                    return FontFamily(Font(R.font.magnolia_script))
                }
                "medula_one" -> {
                    return FontFamily(Font(R.font.medula_one))
                }
                "nautilus" -> {
                    return FontFamily(Font(R.font.nautilus))
                }
                "nickainley" -> {
                    return FontFamily(Font(R.font.nickainley))
                }
                "oldenglishfive" -> {
                    return FontFamily(Font(R.font.oldenglishfive))
                }
                "quattrocento" -> {
                    return FontFamily(Font(R.font.quattrocento))
                }
                "rogue_script" -> {
                    return FontFamily(Font(R.font.rouge_script))
                }
                "sexsmith" -> {
                    return FontFamily(Font(R.font.sexsmith))
                }
                "typerighter_medium" -> {
                    return FontFamily(Font(R.font.typerighter_medium))
                }
                "righteous_regular" -> {
                    return FontFamily(Font(R.font.righteous_regular))
                }
                "chopin_script" -> {
                    return FontFamily(Font(R.font.chopin_script))
                }
                "rochester_regular" -> {
                    return FontFamily(Font(R.font.rochester_regular))
                }
                "steelfish_rounded" -> {
                    return FontFamily(Font(R.font.steelfish_rounded))
                }
                "vinque" -> {
                    return FontFamily(Font(R.font.vinque))
                }
                "rm_typerighter_old" -> {
                    return FontFamily(Font(R.font.rm_typerighter_old))
                }
                "lm_monocaps_regular" -> {
                    return FontFamily(Font(R.font.lm_monocaps_regular))
                }
                "monofonto" -> {
                    return FontFamily(Font(R.font.monofonto))
                }
                "code_new_roman" -> {
                    return FontFamily(Font(R.font.code_new_roman))
                }
                "attic" -> {
                    return FontFamily(Font(R.font.attic))
                }
            }
            return FontFamily(DEFAULT)
        }
    }
}