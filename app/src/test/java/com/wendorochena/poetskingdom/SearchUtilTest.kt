package com.wendorochena.poetskingdom

import android.content.Context
import com.wendorochena.poetskingdom.utils.SearchUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.io.File


/**
 * Tests that all correct results are displayed when searching for a substring.
 *
 * Each search keyword is planted strategically into a poem. There are three exact instances in which
 * a keyword can be found. It can be found on the first stanza as the exact match. It can be found
 * on the tenth stanza with a lot of convoluted text. Finally it can be found on the fifth stanza
 * with a lot of convoluted text.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(RobolectricTestRunner::class)
class SearchUtilTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var searchUtil: SearchUtil
    private val exactSearch = "Exact Search"


    @Before
    fun setUp() {
        mockContext = mock {
            on {
                getDir(
                    "index",
                    Context.MODE_PRIVATE
                )
            } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/mock_index")
            on {
                getDir(
                    getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )
            } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/poems")
            on { getString(R.string.exact_phrase_search) } doReturn exactSearch
        }
    }

    /**
     * Search Keyword "I want you"
     *
     * Files ("Search_Test_1.xml", "Search_Test_2.xml", "Search_Test_3.xml") are the only files
     * containing the keyword
     *
     * This test tests that the correct files are found and that the correct
     * highlighted text positions are found
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testIWantYou(): Unit = runTest {

        val dispatcher = StandardTestDispatcher(testScheduler)
        searchUtil = SearchUtil("I want you", mockContext, exactSearch, dispatcher, dispatcher)
        searchUtil.initiateLuceneSearch()
        advanceUntilIdle()
        val results: ArrayList<String> = searchUtil.getTitleSearchResults()
        val substringLocation = searchUtil.getSubStringLocations()

        assert(results.isNotEmpty())

        assert(substringLocation.isNotEmpty())

        assert(results.size == 3)

        assert(results.contains("Search Test 1"))
        assert(results.contains("Search Test 2"))
        assert(results.contains("Search Test 3"))

        for (pair in substringLocation) {

            when (pair.first) {
                "Search Test 1" -> {
                    val location = pair.second.split(" ")
                    val stanzaNum = location[0].toInt()
                    val startIndex = location[1].toInt()
                    val endIndex = location[2].split("\n")[0].toInt()

                    assert(stanzaNum == 1)
                    assert(startIndex == 0)
                    assert(endIndex == 10)
                }
                "Search Test 2" -> {
                    val location = pair.second.split(" ")
                    val stanzaNum = location[0].toInt()
                    val startIndex = location[1].toInt()
                    val endIndex = location[2].split("\n")[0].toInt()
                    // there are 35 lines in each stanza with the search keyword and a line is a character
                    val lines = 35

                    assert(stanzaNum == 10)
                    assert(startIndex == 936 + lines)
                    assert(endIndex == 946 + lines)
                }
                "Search Test 3" -> {
                    val location = pair.second.split(" ")
                    val stanzaNum = location[0].toInt()
                    val startIndex = location[1].toInt()
                    val endIndex = location[2].split("\n")[0].toInt()
                    // there are 35 lines in each stanza with the search keyword and a line is a character
                    val lines = 35

                    assert(stanzaNum == 5)
                    assert(startIndex == 936 + lines)
                    assert(endIndex == 946 + lines)
                }
            }
        }

    }

    /**
     * Search keyword "This is an example of a very long exact search string that will be tested in the same manner as the previous tests"
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testLongStringExactMatch(): Unit = runTest {

        val dispatcher = StandardTestDispatcher(testScheduler)
        searchUtil = SearchUtil(
            "This is an example of a very long exact search string that will be tested in the same manner as the previous tests",
            mockContext,
            exactSearch,
            dispatcher,
            dispatcher
        )
        searchUtil.initiateLuceneSearch()
        advanceUntilIdle()
        val results: ArrayList<String> = searchUtil.getTitleSearchResults()
        val substringLocation = searchUtil.getSubStringLocations()

        assert(results.isNotEmpty())

        assert(substringLocation.isNotEmpty())
        assert(results.size == 3)

        assert(results.contains("This is search 1"))
        assert(results.contains("This is search 2"))
        assert(results.contains("This is search 3"))

        for (pair in substringLocation) {

            when (pair.first) {
                "This is search 1" -> {
                    val location = pair.second.split(" ")
                    val stanzaNum = location[0].toInt()
                    val startIndex = location[1].toInt()
                    val endIndex = location[2].split("\n")[0].toInt()

                    assert(stanzaNum == 1)
                    assert(startIndex == 0)
                    assert(endIndex == 114)
                }
                "This is search 2" -> {
                    val location = pair.second.split(" ")
                    val stanzaNum = location[0].toInt()
                    val startIndex = location[1].toInt()
                    val endIndex = location[2].split("\n")[0].toInt()
                    // there are 35 lines in each stanza with the search keyword and a line is a character
                    val lines = 35

                    assert(stanzaNum == 10)
                    assert(startIndex == 936 + lines)
                    assert(endIndex == 1050 + lines)
                }
                "This is search 3" -> {
                    val location = pair.second.split(" ")
                    val stanzaNum = location[0].toInt()
                    val startIndex = location[1].toInt()
                    val endIndex = location[2].split("\n")[0].toInt()
                    // there are 35 lines in each stanza with the search keyword and a line is a character
                    val lines = 35

                    assert(stanzaNum == 5)
                    assert(startIndex == 936 + lines)
                    assert(endIndex == 1050 + lines)
                }
            }
        }
    }

    /**
     *
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testExtremeBoundary(): Unit = runTest {

        val dispatcher = StandardTestDispatcher(testScheduler)
        searchUtil = SearchUtil(
            "K-nearest neighbors, abbreviated to KNN for future mentions is a classification technique that assigns a class to the k nearest points in regards to a point x. The prevailing class in all chosen k points is the class that is assigned. Before we start attempting to classify with the KNN method we split training data and test data. This helps us avoid over-fitting, in this way when we test the data it is tested on data that has never been seen before and as such gives us more conclusive results. For our KNN classifier we split the data to 70% training data and 30% test data. Generally to find the most optimal value of k to use we use the formula k = n0.5, where k is the number of nearest points and n is the number of instances. However for our hyper parameter k we decided for a much lower value than such k that would have been deduced by square rooting the number of our instances. Due to a general higher bias the larger the value k is we decided to go with the value 5.",
            mockContext,
            exactSearch,
            dispatcher,
            dispatcher
        )
        searchUtil.initiateLuceneSearch()
        advanceUntilIdle()
        val results: ArrayList<String> = searchUtil.getTitleSearchResults()

        val substringLocation = searchUtil.getSubStringLocations()

        assert(results.isNotEmpty())

        assert(substringLocation.isNotEmpty())

        assert(results.size == 3)

        assert(results.contains("Boundary Test 1"))
        assert(results.contains("Boundary Test 2"))
        assert(results.contains("Boundary Test 3"))
        for (pair in substringLocation) {

            when (pair.first) {
                "Boundary Test 1" -> {
                    val location = pair.second.split(" ")
                    val stanzaNum = location[0].toInt()
                    val startIndex = location[1].toInt()
                    val endIndex = location[2].split("\n")[0].toInt()

                    assert(stanzaNum == 1)
                    assert(startIndex == 0)
                    assert(endIndex == 981)
                }
                "Boundary Test 2" -> {
                    val location = pair.second.split(" ")
                    val stanzaNum = location[0].toInt()
                    val startIndex = location[1].toInt()
                    val endIndex = location[2].split("\n")[0].toInt()
                    // there are 35 lines in each stanza with the search keyword and a line is a character
                    val lines = 35

                    assert(stanzaNum == 10)
                    assert(startIndex == 936 + lines)
                    assert(endIndex == 1917 + lines)
                }
                "Boundary Test 3" -> {
                    val location = pair.second.split(" ")
                    val stanzaNum = location[0].toInt()
                    val startIndex = location[1].toInt()
                    val endIndex = location[2].split("\n")[0].toInt()
                    // there are 35 lines in each stanza with the search keyword and a line is a character
                    val lines = 35

                    assert(stanzaNum == 5)
                    assert(startIndex == 936 + lines)
                    assert(endIndex == 1917 + lines)
                }
            }
        }
    }
}