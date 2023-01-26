package com.wendorochena.poetskingdom

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CreatePoemUnitTest {

    private val pageNumberAndId : HashMap<Int, Int> = HashMap()

    private fun clearHashMap() {
        pageNumberAndId.clear()
    }

//    private fun putHashMapValues(arrayList: ArrayList<Pair<Int,Int>>) {
//        for (pair in arrayList) {
//
//        }
//    }
//    @Test
//    fun deleteMainPage() {
//        val createPoem = CreatePoem()
//
//        pageNumberAndId[1] = 1
//        createPoem.setPagesAndId(pageNumberAndId)
//
//        createPoem.deleteNum(1)
//        val createPoemPageNumberAndId = createPoem.getPagesAndId()
//        assertEquals(createPoemPageNumberAndId.size,1)
//        assertEquals(createPoemPageNumberAndId[1],1)
//    }
}