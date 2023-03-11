package com.wendorochena.poetskingdom

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wendorochena.poetskingdom.recyclerViews.CreatePoemRecyclerViewAdapter
import org.hamcrest.CoreMatchers.*
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Testing this class is very taxing, and quite frankly too expensive. Tests will normally consist
 * of long pieces of code to test minimal things.
 *
 * As a compromise, the basic core tests are tested to make sure things work as should
 *
 * NB No boundary tests were done
 */
@RunWith(AndroidJUnit4::class)
class CreatePoemActivityTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val poemFolder =
        appContext.getDir(appContext.getString(R.string.poems_folder_name), Context.MODE_PRIVATE)
    private val thumbnailFolder = appContext.getDir(
        appContext.getString(R.string.thumbnails_folder_name),
        Context.MODE_PRIVATE
    )
    private val poemThemeFolder = appContext.getDir(
        appContext.getString(R.string.poem_themes_folder_name),
        Context.MODE_PRIVATE
    )
    private val defaultPoemFileName = "Load Default Test"
    private lateinit var createPoemActivityRule : ActivityScenario<CreatePoem>

//    lateinit var createPoemActivityRule : ActivityScenario<CreatePoem>

    @get:Rule
    val poemThemeActivityRule = ActivityScenarioRule(PoemThemeActivity::class.java)


    @Test
    fun useAppContext() {
//        i.putExtra(appContext.getString(R.string.poem_title_argument_name), defaultFile)
        // Context of the app under test.
        Assert.assertEquals("com.wendorochena.poetskingdom", appContext.packageName)
    }

    /**
     * Test that a poem is loaded successfully and that it saves after  input is entered
     *
     * NB TESTED WITH Portrait orientation
     */
    @Test
    fun testLoadDefaultPoem() {
        val defaultFile = File(
            poemThemeFolder.absolutePath + File.separator + defaultPoemFileName.replace(
                ' ',
                '_'
            ) + ".xml"
        )

        val activity = ActivityScenario.launch(PoemThemeActivity::class.java)

        if (!defaultFile.exists()) {
            onView(withText(appContext.getString(R.string.create_poem))).perform(click())
            onView(withHint(R.string.create_poem_edit_text_hint)).perform(click())
            onView(withHint(R.string.create_poem_edit_text_hint)).perform(
                typeText(
                    defaultPoemFileName
                )
            )
            onView(withText(R.string.confirm)).perform(click())
        }
        assert(defaultFile.exists())
        activity.close()
        val i = Intent(appContext, CreatePoem::class.java)
        i.putExtra(appContext.getString(R.string.load_poem_argument_name), false)
        i.putExtra(appContext.getString(R.string.poem_title_argument_name), defaultPoemFileName)
        val createPoemActivityRule = ActivityScenario.launch<CreatePoem>(i)
        createPoemActivityRule.onActivity {
            val extras = it.intent.extras

            assert(!extras!!.getBoolean(appContext.getString(R.string.load_poem_argument_name)))
            assert(extras.getString(appContext.getString(R.string.poem_title_argument_name)) == defaultPoemFileName)

            val background =
                it.findViewById<FrameLayout>(R.id.portraitPoemContainer).background as ColorDrawable
            val textColor = it.findViewById<EditText>(R.id.portraitTextView).textColors.defaultColor

            assert(background.color == android.graphics.Color.WHITE)
            assert(textColor == android.graphics.Color.BLACK)
        }
        createPoemActivityRule.close()
    }

    /**
     * Test functionality of the bottom drawer
     */
    @Test
    fun testBottomDrawer() {
        val defaultFile = File(
            poemThemeFolder.absolutePath + File.separator + defaultPoemFileName.replace(
                ' ',
                '_'
            ) + ".xml"
        )
        if (!defaultFile.exists())
            testLoadDefaultPoem()
        val i = Intent(appContext, CreatePoem::class.java)
        i.putExtra(appContext.getString(R.string.load_poem_argument_name), false)
        i.putExtra(appContext.getString(R.string.poem_title_argument_name), defaultPoemFileName)
        val createPoemActivityRule = ActivityScenario.launch<CreatePoem>(i)
//        val i = Intent(appContext, CreatePoem::class.java)
//        i.putExtra(appContext.getString(R.string.load_poem_argument_name), false)
//        i.putExtra(appContext.getString(R.string.poem_title_argument_name), defaultPoemFileName)
//        val createPoemActivityRule = ActivityScenario.launch<CreatePoem>(i)

        createPoemActivityRule.onActivity {
            val extras = it.intent.extras

            assert(!extras!!.getBoolean(appContext.getString(R.string.load_poem_argument_name)))
            assert(extras.getString(appContext.getString(R.string.poem_title_argument_name)) == defaultPoemFileName)

            val background =
                it.findViewById<FrameLayout>(R.id.portraitPoemContainer).background as ColorDrawable
            val textColor = it.findViewById<EditText>(R.id.portraitTextView).textColors.defaultColor

            assert(background.color == android.graphics.Color.WHITE)
            assert(textColor == android.graphics.Color.BLACK)
        }
        //assert that a double click brings out bottom drawer
        onView(withId(R.id.parent)).perform(doubleClick())
        onView(withId(R.id.bottomDrawer)).check(matches(isDisplayed()))

        //assert all buttons work as they should
        onView(withId(R.id.textOptions)).perform(click())
        onView(withId(R.id.textOptionsContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.leftAlign)).perform(click())

        createPoemActivityRule.onActivity {
            val editText = it.findViewById<EditText>(R.id.portraitTextView)
            assert(editText.textAlignment == View.TEXT_ALIGNMENT_TEXT_START)
        }

        onView(withId(R.id.centreAlign)).perform(click())

        createPoemActivityRule.onActivity {
            val editText = it.findViewById<EditText>(R.id.portraitTextView)
            assert(editText.textAlignment == View.TEXT_ALIGNMENT_CENTER)
        }

        onView(withId(R.id.rightAlign)).perform(click())

        createPoemActivityRule.onActivity {
            val editText = it.findViewById<EditText>(R.id.portraitTextView)
            assert(editText.textAlignment == View.TEXT_ALIGNMENT_TEXT_END)
        }

        onView(withId(R.id.centerVerticalAlign)).perform(click())

        createPoemActivityRule.onActivity {
            val editText = it.findViewById<EditText>(R.id.portraitTextView)
            assert(editText.textAlignment == View.TEXT_ALIGNMENT_CENTER)
            assert(editText.gravity == Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL)
        }

        onView(withId(R.id.centerVerticalLeftAlign)).perform(click())

        createPoemActivityRule.onActivity {
            val editText = it.findViewById<EditText>(R.id.portraitTextView)
            assert(editText.textAlignment == View.TEXT_ALIGNMENT_TEXT_START)
            val editTextParams = editText.layoutParams as FrameLayout.LayoutParams
            assert(editTextParams.gravity == (Gravity.CENTER_VERTICAL or Gravity.START))
        }

        onView(withId(R.id.centerVerticalRightAlign)).perform(click())

        createPoemActivityRule.onActivity {
            val editText = it.findViewById<EditText>(R.id.portraitTextView)
            assert(editText.textAlignment == View.TEXT_ALIGNMENT_TEXT_END)
            val editTextParams = editText.layoutParams as FrameLayout.LayoutParams
            assert(editTextParams.gravity == (Gravity.CENTER_VERTICAL or Gravity.END))
        }

        //text color dialogue
        onView(withId(R.id.textColor)).perform(click())
        onView(withText("Select Color")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        onView(withId(R.id.textSize)).perform(click())
        onView(withId(R.id.textSizeSlider)).check(matches(isDisplayed()))

        //recycler view for pages
        onView(withId(R.id.pagesOptions)).perform(click())
        onView(withId(R.id.recyclerPagesContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.backgroundDim)).check(matches(isDisplayed()))

        onView(withId(R.id.saveOptions)).perform(click())
        onView(withId(R.id.recyclerPagesContainer)).check(matches(not(isDisplayed())))
        onView(withId(R.id.backgroundDim)).check(matches(not(isDisplayed())))
        onView(withId(R.id.saveOptionsContainer)).check(matches(isDisplayed()))

    }

    /**
     *
     */
    @Test
    fun testChangeTitle() {
        val newTitle = "Change Of Title"
        val changedFileTheme = File(
            poemThemeFolder.absolutePath + File.separator + newTitle.replace(
                ' ',
                '_'
            ) + ".xml"
        )
        val changedFilePoem = File(
            poemFolder.absolutePath + File.separator + newTitle.replace(
                ' ',
                '_'
            ) + ".xml"
        )

        val changedFileThumbnail = File(
            thumbnailFolder.absolutePath + File.separator + newTitle.replace(
                ' ',
                '_'
            ) + ".png"
        )
        val defaultFile = File(
            poemThemeFolder.absolutePath + File.separator + defaultPoemFileName.replace(
                ' ',
                '_'
            ) + ".xml"
        )
        if (!defaultFile.exists())
            testLoadDefaultPoem()
        val i = Intent(appContext, CreatePoem::class.java)
        i.putExtra(appContext.getString(R.string.load_poem_argument_name), false)
        i.putExtra(appContext.getString(R.string.poem_title_argument_name), defaultPoemFileName)
        val createPoemActivityRule = ActivityScenario.launch<CreatePoem>(i)

        onView(withId(R.id.titleTextView)).perform(longClick())
        onView(withText(R.string.title_change)).check(matches(isDisplayed()))
        onView(withHint(R.string.change_title_hint)).perform(click())
        onView(withHint(R.string.change_title_hint)).perform(typeText(newTitle))
        onView(withText(R.string.title_change_confirm)).perform(click())

        assert(changedFileTheme.exists())
        assert(changedFilePoem.exists())
        assert(changedFileThumbnail.exists())
    }


    /**
     * Tests that one page is successfully deleted
     * A successful deletion consists of the pages being re-ordered correctly and the recycler view
     * being re-ordered correctly
     */
    @Test
    fun testRecyclerViewDeletePageFiveOfTen() {
        val defaultFile = File(
            poemThemeFolder.absolutePath + File.separator + defaultPoemFileName.replace(
                ' ',
                '_'
            ) + ".xml"
        )
        if (!defaultFile.exists())
            testLoadDefaultPoem()
        val i = Intent(appContext, CreatePoem::class.java)
        i.putExtra(appContext.getString(R.string.load_poem_argument_name), false)
        i.putExtra(appContext.getString(R.string.poem_title_argument_name), defaultPoemFileName)
        val createPoemActivityRule = ActivityScenario.launch<CreatePoem>(i)

        onView(withHint(R.string.create_poem_text_view_hint)).perform(typeText("1"))
        onView(withId(R.id.portraitPoemContainer)).perform(doubleClick())
        onView(withId(R.id.pagesOptions)).perform(click())
        var counter = 0

        while (counter < 9) {
            onView(withId(R.id.addPageRecyclerViewId)).perform(click())
            onView(withId(R.id.recyclerPagesContainer)).check(matches(not(isDisplayed())))
            onView(withTagValue(`is`(counter + 2))).perform(click())
            onView(allOf(withParent(withTagValue(`is`(counter + 2))), withParentIndex(1))).perform(
                typeText("${counter + 2}")
            )
            Espresso.pressBack()
            onView(withId(R.id.pagesOptions)).perform(click())
            counter++
        }

        onView(withId(R.id.recyclerPagesContainer)).check(matches(isDisplayed()))
        //delete page five
        onView(withId(R.id.recyclerPagesContainer)).perform(
            RecyclerViewActions.actionOnItemAtPosition<CreatePoemRecyclerViewAdapter.ViewHolder>(
                5,
                longClick()
            )
        )
//        onView(withId(R.id.pagesOptions)).perform(click())
//        onView(withId(R.id.recyclerPagesContainer)).check(matches(not(isDisplayed())))
        var countNumber = 1

        // assert that pages 1 to 4 are still the same
        while (countNumber < 5) {
            onView(
                allOf(
                    withTagValue(`is`(countNumber)), allOf(
                        withParent(withId(R.id.parent))
                    )
                )
            ).check { view, _ ->
                val frameLayout = view as FrameLayout
                val editText = frameLayout.getChildAt(1) as EditText
                assert(editText.text.toString() == "$countNumber")
                countNumber++
            }
        }

        //assert pages 5 to 9 have been reconfigured correctly
        // page 5 should have text 6
        //page 6 should have text 7 etc etc
        counter = 6
        while (countNumber < 10) {
            onView(
                allOf(
                    withTagValue(`is`(countNumber)), allOf(withParent(withId(R.id.parent))
            ))).check { view, _ ->
                val frameLayout = view as FrameLayout
                val editText = frameLayout.getChildAt(1) as EditText
                assert(editText.text.toString() == "$counter")
            }
            countNumber++
            counter++
        }

                onView(withId(R.id.pagesOptions)).perform(click())
        onView(withId(R.id.recyclerPagesContainer)).check(matches(not(isDisplayed())))

        onView(withId(R.id.pagesOptions)).perform(click())
        onView(withId(R.id.recyclerPagesContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerPagesContainer)).perform(RecyclerViewActions.scrollToLastPosition<CreatePoemRecyclerViewAdapter.ViewHolder>())
        createPoemActivityRule.onActivity {
            val recyclerView = it.findViewById<RecyclerView>(R.id.recyclerPagesContainer)
            val recyclerViewAdapter = recyclerView.adapter!! as CreatePoemRecyclerViewAdapter

            var count = 1

            while (count < recyclerViewAdapter.itemCount) {
                val frameLayout = recyclerViewAdapter.getElement(count)
                val editText = frameLayout[1] as EditText
                if (count < 5)
                    assert(editText.text.toString() == "$count")
                else
                    assert(editText.text.toString() == "${count + 1}")
                count++
            }
        }
    }

}