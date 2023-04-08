package com.wendorochena.poetskingdom.poemdata

import android.text.Editable

data class PoemDataContainer(val category: Category, val poem: ArrayList<Editable>, val poemTheme: PoemTheme) {

    private var pages : Int = 0

    fun getPoemCategory() : Category {
       return category
    }

    fun getPoemTextArray() : ArrayList<Editable> {
        return poem
    }

    fun getTheme() : PoemTheme {
        return poemTheme
    }

    fun setPages(pages : Int) {
        this.pages = pages
    }

    fun getPages() : Int {
        return pages
    }
}
