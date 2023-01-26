package com.wendorochena.poetskingdom.utils

import com.wendorochena.poetskingdom.poemdata.Category

class CategoryUtils {

    companion object {

        fun stringToCategory(string: String): Category {
            when (string) {
                Category.VIVID.toString() -> {
                    return Category.VIVID
                }
                Category.NONE.toString() -> {
                    return Category.NONE
                }
                Category.SOUL.toString() -> {
                    return Category.SOUL
                }
                Category.FUNNY.toString() -> {
                    return Category.FUNNY
                }
                Category.FANTASY.toString() -> {
                    return Category.FANTASY
                }
                Category.ADVENTURE.toString() -> {
                    return Category.ADVENTURE
                }
            }
            return Category.NONE
        }
    }
}