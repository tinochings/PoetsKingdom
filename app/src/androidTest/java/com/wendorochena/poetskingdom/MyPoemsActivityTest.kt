package com.wendorochena.poetskingdom

import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.imageview.ShapeableImageView
import org.hamcrest.CoreMatchers.not
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MyPoemsActivityTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val poemFolder =
        appContext.getDir(appContext.getString(R.string.poems_folder_name), Context.MODE_PRIVATE)
    private val thumbnailFolder = appContext.getDir(
        appContext.getString(R.string.thumbnails_folder_name),
        Context.MODE_PRIVATE
    )

    @get:Rule
    val activityRule = ActivityScenarioRule(MyPoems::class.java)


    @Test
    fun useAppContext() {
        // Context of the app under test.
        Assert.assertEquals("com.wendorochena.poetskingdom", appContext.packageName)
    }


    /**
     * Tested with 8 saved poems
     *
     * Checks to see whether all poems saved in the poems folder are active in the recycler view
     */
    @Test
    fun testLoadAllSavedPoems() {
        activityRule.scenario.onActivity {
            val recyclerView = it.findViewById<RecyclerView>(R.id.recyclerView)
            assert(recyclerView.adapter?.itemCount == poemFolder.listFiles()?.size)
        }
    }

    /**
     * Tests whether the recycler view successfully reconfigures when a deletion has happened
     *
     * Index 1 is deleted hence index 2 should take index 1s place
     *
     * NB tested with more than 3 poems created
     */
    @Test
    fun testSuccessfulDeletion() {
        val indexToDelete = 1
        var recyclerViewCount = -1
        var frameLayoutViewToDelete = FrameLayout(appContext)
        var indexTwoName = ""
        activityRule.scenario.onActivity {
            val recyclerView = it.findViewById<RecyclerView>(R.id.recyclerView)
            val recyclerViewAdapter = recyclerView.adapter!!

            assert(recyclerViewAdapter.itemCount > 0)
            recyclerViewCount = recyclerViewAdapter.itemCount
            frameLayoutViewToDelete = recyclerView[indexToDelete] as FrameLayout
            indexTwoName = recyclerView[indexToDelete + 1].findViewById<TextView>(R.id.listViewText).text.toString()
            val frameLayout = recyclerView[indexToDelete] as FrameLayout
            val imageView = frameLayout.findViewById<ShapeableImageView>(R.id.listViewImage)
            imageView.performLongClick()
        }
        onView(withId(R.id.bottomDrawer)).check(matches(isDisplayed()))
        val titleName = frameLayoutViewToDelete.findViewById<TextView>(R.id.listViewText).text.toString()
        onView(withId(R.id.deleteButton)).perform(click())

        activityRule.scenario.onActivity {
            val recyclerView = it.findViewById<RecyclerView>(R.id.recyclerView)
            val recyclerViewAdapter = recyclerView.adapter!!

            assert(recyclerViewCount > recyclerViewAdapter.itemCount)
            assert(recyclerView[indexToDelete].findViewById<TextView>(R.id.listViewText).text == indexTwoName)
            assert(titleName != recyclerView[indexToDelete].findViewById<TextView>(R.id.listViewText).text)
            val encodedTitle = titleName.replace(" ", "_")
            assert(!File(poemFolder.absolutePath + File.separator + encodedTitle + ".xml").exists())
            assert(!File(thumbnailFolder.absolutePath + File.separator + encodedTitle + ".xml").exists())
        }
    }

//    /**
//     * Tests that 3 successful results containing the exact sub phrase I want you is found.
//     *
//     * First poem "Search Test 1" has "I want you" on the first poem line
//     * Second poem "Search Test 2" has "I want you" on the tenth stanza where each stanza has been completely filled
//     * Third poem "Search Test 3" has "I want you" on the fifth stanza where each stanza is full of text
//     */
//    @Test
//    fun testSearchIWantYou() {
//        //click search and check if search comes out
//        onView(withId(R.id.searchButton)).perform(click())
//        onView(withId(R.id.advancedSearchContainer)).check(matches(isDisplayed()))
//
//        // click on the edit text and type
//        onView(withId(R.id.advancedSearchText)).perform(click())
//        onView(withId(R.id.advancedSearchText)).perform(typeText("I want you"))
//        onView(withId(R.id.advancedSearchText)).perform(pressImeActionButton())
//
//        // confirm that the search view is no longer there and that the searchRecycler view is
//        onView(withId(R.id.advancedSearchContainer)).check(matches(not(isDisplayed())))
//        onView(withId(R.id.searchRecyclerView)).check(matches(isDisplayed()))
//        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))
//
//        activityRule.scenario.onActivity {
//            val searchRecyclerView = it.findViewById<RecyclerView>(R.id.searchRecyclerView)
//
//            assert(searchRecyclerView.adapter != null)
//            assert(searchRecyclerView.adapter!!.itemCount == 3)
//
//            for (searchItemCont in searchRecyclerView.children) {
//                val searchItemName = searchItemCont.findViewById<TextView>(R.id.searchTitle).text
//                val searchItemStanzas = searchItemCont.findViewById<TextView>(R.id.searchStanzas).text
//
//                if (searchItemName == "Search Test 1") {
//                    assert(searchItemStanzas == appContext.getString(R.string.search_stanza_text, "1"))
//                } else if (searchItemName == "Search Test 2") {
//                    assert(searchItemStanzas == appContext.getString(R.string.search_stanza_text, "10"))
//                } else if (searchItemName == "Search Test 3") {
//                    assert(searchItemStanzas == appContext.getString(R.string.search_stanza_text, "5"))
//                }
//            }
//        }
//    }

    /**
     * Tests the functionality of the search button and back button, asserting the correct views are
     * visible
     */
    @Test
    fun testFunctionality() {
        // recycler view should not be visible when search clicked
        onView(withId(R.id.searchButton)).perform(click())
        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))

        onView(withId(R.id.advancedSearchContainer)).check(matches(isDisplayed()))

        //search recycler view should not be visible
        onView(withId(R.id.searchRecyclerView)).check(matches(not(isDisplayed())))

        Espresso.pressBack()

        //recycler view should be visible and only recycler view
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.searchRecyclerView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.advancedSearchContainer)).check(matches(not(isDisplayed())))

        onView(withId(R.id.searchButton)).perform(click())
        onView(withId(R.id.advancedSearchContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.advancedSearchText)).perform(click())
        onView(withId(R.id.advancedSearchText)).perform(pressImeActionButton())
        onView(withId(R.id.advancedSearchText)).check(matches(withHint(appContext.getString(R.string.no_input_entered))))

        Espresso.pressBack()
        Espresso.pressBack()

        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.searchRecyclerView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.advancedSearchContainer)).check(matches(not(isDisplayed())))
    }

}