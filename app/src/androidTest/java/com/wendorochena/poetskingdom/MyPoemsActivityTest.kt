package com.wendorochena.poetskingdom

import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import kotlin.random.Random

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
     */
    @Test
    fun testSuccessfulDeletion() {
        val indexToDelete: Int = 1
        var recyclerViewCount = -1
        var frameLayoutViewToDelete: FrameLayout = FrameLayout(appContext)
        activityRule.scenario.onActivity {
            val recyclerView = it.findViewById<RecyclerView>(R.id.recyclerView)
            val recyclerViewAdapter = recyclerView.adapter!!

            assert(recyclerViewAdapter.itemCount > 0)
            recyclerViewCount = recyclerViewAdapter.itemCount
//            indexToDelete = (0..recyclerViewAdapter.itemCount).random()
            frameLayoutViewToDelete = recyclerView[indexToDelete] as FrameLayout

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
            assert(titleName != recyclerView[indexToDelete].findViewById<TextView>(R.id.listViewText).text)
            val encodedTitle = titleName.replace(" ", "_")
            assert(!File(poemFolder.absolutePath + File.separator + encodedTitle + ".xml").exists())
            assert(!File(thumbnailFolder.absolutePath + File.separator + encodedTitle + ".xml").exists())
        }
    }



}