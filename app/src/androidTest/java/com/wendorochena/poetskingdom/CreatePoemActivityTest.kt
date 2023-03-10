package com.wendorochena.poetskingdom

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.widget.EditText
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.Color
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

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

        onView(withId(R.id.parent)).perform(doubleClick())
        onView(withId(R.id.bottomDrawer)).check(matches(isDisplayed()))
    }
}