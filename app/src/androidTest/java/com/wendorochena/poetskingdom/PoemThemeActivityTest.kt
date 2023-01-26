package com.wendorochena.poetskingdom

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.provider.ContactsContract.CommonDataKinds.Im
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class PoemThemeActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(PoemThemeActivity::class.java)

//    @Test
//    fun testCorrectInitialHeading() {
//        activityRule.scenario.onActivity {
//            assertEquals(it.getCurrentView(), "Outline")
//        }
//    }
//
//    /**
//     * Test the unhiding of views
//     */
//    @Test
//    fun testBackgroundHeading() {
//        onView(withId(R.id.background)).perform(click())
//        activityRule.scenario.onActivity {
//            assertEquals(it.getCurrentView(), "Background")
//        }
//        onView(withId(R.id.imagesContainer1)).check(matches(isDisplayed()))
//        onView(withId(R.id.recyclerImageContainer)).check(matches(isDisplayed()))
//        onView(withId(R.id.outlineContainer1)).check(matches(not(isDisplayed())))
//    }
//
//
//    /**
//     * Test that all views with a prefix heading text are visible
//     */
//    @Test
//    fun testTextHeading() {
//        onView(withId(R.id.text)).perform(click())
//        activityRule.scenario.onActivity {
//            assertEquals(it.getCurrentView(), "Text")
//        }
//        onView(withId(R.id.imagesContainer1)).check(matches(not(isDisplayed())))
//        onView(withId(R.id.recyclerImageContainer)).check(matches(not(isDisplayed())))
//        activityRule.scenario.onActivity { activity ->
//            val mainLayout = activity.findViewById<LinearLayout>(R.id.mainLinearLayoutContainer)
//            for (child in mainLayout.children) {
//
//                if (child.visibility == View.VISIBLE) {
//                    if (child.id != activity.findViewById<LinearLayout>(R.id.headingLayoutContainer).id) {
//                        assert(
//                            activity.resources.getResourceName(child.id)
//                                .startsWith(activity.packageName + ":id/text")
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * Tests three different clicks and asserts that the visible views are the actual ones needed
//     */
//    @Test
//    fun testMultipleHeadingClicks() {
//        onView(withId(R.id.text)).perform(click())
//        activityRule.scenario.onActivity {
//            assertEquals(it.getCurrentView(), "Text")
//        }
//        activityRule.scenario.onActivity { activity ->
//            val mainLayout = activity.findViewById<LinearLayout>(R.id.mainLinearLayoutContainer)
//            for (child in mainLayout.children) {
//
//                if (child.visibility == View.VISIBLE) {
//                    if (child.id != activity.findViewById<LinearLayout>(R.id.headingLayoutContainer).id) {
//                        assert(
//                            activity.resources.getResourceName(child.id)
//                                .startsWith(activity.packageName + ":id/text")
//                        )
//                    }
//                }
//            }
//            assert(activity.getCurrentView() == "Text")
//        }
//
//        onView(withId(R.id.outline)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            val mainLayout = activity.findViewById<LinearLayout>(R.id.mainLinearLayoutContainer)
//            for (child in mainLayout.children) {
//
//                if (child.visibility == View.VISIBLE) {
//                    if (child.id != activity.findViewById<LinearLayout>(R.id.headingLayoutContainer).id) {
//                        assert(
//                            activity.resources.getResourceName(child.id)
//                                .startsWith(activity.packageName + ":id/outline")
//                        )
//                    }
//                }
//            }
//            assert(activity.getCurrentView() == "Outline")
//        }
//
//        onView(withId(R.id.background)).perform(click())
//        onView(withId(R.id.imagesContainer1)).check(matches(isDisplayed()))
//        onView(withId(R.id.recyclerImageContainer)).check(matches(isDisplayed()))
//
//        activityRule.scenario.onActivity { activity ->
//            val mainLayout = activity.findViewById<LinearLayout>(R.id.mainLinearLayoutContainer)
//
//            for (child in mainLayout.children) {
//                if (child.visibility == View.VISIBLE) {
//                    if (child.id != activity.findViewById<LinearLayout>(R.id.headingLayoutContainer).id) {
//                        assert(
//                            activity.resources.getResourceName(child.id)
//                                .lowercase(Locale.getDefault())
//                                .contains("image")
//                        )
//                    }
//                }
//            }
//            assert(activity.getCurrentView() == "Background")
//        }
//    }
//
//    /**
//     * Test that the outline correctly changes the preview
//     */
//    @Test
//    fun testOutlineAndPreview() {
//        onView(withId(R.id.outlineContainer1)).check(matches(isDisplayed()))
//        onView(withId(R.id.rectangle_outline)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            val layoutToTest = activity.findViewById<FrameLayout>(R.id.rectangle_outline)
//            val outlineLayout = activity.findViewById<FrameLayout>(R.id.outlinePreview)
//            assertTrue(outlineLayout.background == layoutToTest.background)
//            assertTrue(activity.getCurrentView() == "Outline")
//            assertTrue(activity.getOutlineChosen() == activity.findViewById<FrameLayout>(R.id.rectangle_outline))
//        }
//    }
//
//    @Test
//    fun testRemoveImage() {
//        activityRule.scenario.onActivity { activity ->
//            val outlineLayout = activity.findViewById<FrameLayout>(R.id.outlinePreview)
//            val imageView = activity.findViewById<ImageView>(R.id.imagePreview)
//            val bitmap: Bitmap =
//                BitmapFactory.decodeResource(activity.resources, R.drawable.test)
//            imageView.setImageBitmap(bitmap)
//            activity.findViewById<CardView>(R.id.imagePreviewCard).visibility = View.VISIBLE
//            activity.setBackground("null")
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        onView(withId(R.id.imagePreviewCard)).perform(longClick())
//        onView(withId(R.id.imagePreviewCard)).check(matches(not(isDisplayed())))
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == null)
//        }
//    }
//
//    @Test
//    fun testRemoveOutline() {
//        onView(withId(R.id.rectangle_outline)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getOutlineChosen() == activity.findViewById<ImageView>(R.id.rectangle_outline))
//        }
//        onView(withId(R.id.backgroundPreview)).perform(longClick())
//        activityRule.scenario.onActivity { activity ->
//            println(activity.findViewById<FrameLayout>(R.id.outlinePreview).background)
//            assert(null == activity.findViewById<FrameLayout>(R.id.outlinePreview).background)
//            assert(activity.getOutlineChosen() == null)
//        }
//    }
//
//    @Test
//    fun testRemoveColor() {
//        onView(withId(R.id.background)).perform(click())
//        onView(withId(R.id.backgroundColorsIcon)).perform(click())
//        onView(withId(com.github.dhaval2404.colorpicker.R.id.colorPicker)).check(matches(isDisplayed()))
//        onView(withText("OK")).perform(click())
//        onView(withId(R.id.backgroundPreview)).check(matches(isDisplayed()))
//        onView(withId(R.id.backgroundPreview)).perform(longClick())
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundColorChosen() == null)
//        }
//    }
//
//    /**
//     * Selects a color first and checks that the background of the relative layout has a color
//     * An outline is then added and the relative layout should have the normal background layout
//     * Further the new drawable should have the selected color inside the outline
//     */
////    @Test fun testRemoveOutlineColor() {
////        onView(withId(R.id.background)).perform(click())
////        onView(withId(R.id.backgroundColorsIcon)).perform(click())
////        onView(withId(com.github.dhaval2404.colorpicker.R.id.colorPicker)).check(matches(isDisplayed()))
////        onView(withText("OK")).perform(click())
////        onView(withId(R.id.backgroundPreview)).check(matches(isDisplayed()))
////        activityRule.scenario.onActivity { activity ->
////            assert(activity.getBackgroundColorChosen() != null)
////        }
////
////        onView(withId(R.id.outline)).perform(click())
////        onView(withId(R.id.rectangle_outline)).perform(click())
////
////        activityRule.scenario.onActivity { activity ->
////            assert(activity.getOutlineChosen() != null)
////           val gradient =  activity.getOutlineChosen()?.background as GradientDrawable
////            println(gradient.color?.defaultColor)
////            assert(gradient.color?.defaultColor != R.color.white)
////        }
////        onView(withId(R.id.backgroundPreview)).perform(longClick())
////        onView(withText("Color")).perform(click())
////
////        activityRule.scenario.onActivity { activity ->
////            assert(activity.getOutlineChosen() != null)
////            val gradient =  activity.getOutlineChosen()?.background as GradientDrawable
////            println(gradient.color.toString())
////            assert(gradient.color?.defaultColor != R.color.white)
////        }
////    }
//
//    /**
//     * This checks if poem theme correctly contains an outline
//     */
//    @Test
//    fun testOutlineBackgroundPoemTheme() {
//        onView(withId(R.id.rectangle_outline)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getPoemTheme().getBackGroundType() == BackgroundType.OUTLINE)
//        }
//    }
//
//    /**
//     * Simulate a click by invoking an element in the adapter. The invoke call will find the activity
//     * definition and perform the required action
//     * NOTE AT THE POINT OF TESTING THERE WERE 12 IMAGES IN THE LOCAL FOLDER
//     */
//    @Test
//    fun testRecyclerView() {
//        onView(withId(R.id.background)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(1))
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//    }
//
//    /**
//     * Change the image twice and the latest image should be displayed
//     */
//    @Test fun testChangeImageTwice() {
//        onView(withId(R.id.background)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(0))
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(0).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(1))
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(1).absolutePath)
//        }
//    }
//
//    /**
//     * Stress test the recycler view with the maximum amount of images currently
//     * At each stage the background image should be the invoked element
//     */
//    @Test fun testChangeImageTwelveTimes() {
//        onView(withId(R.id.background)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(0))
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(0).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(1))
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(1).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(2))
//        }
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(2).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(3))
//        }
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(3).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(4))
//        }
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(4).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(5))
//        }
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(5).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(6))
//        }
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(6).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(7))
//        }
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(7).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(8))
//        }
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(8).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(9))
//        }
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(9).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(10))
//        }
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(10).absolutePath)
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(11))
//        }
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == activity.getAdapter().getElement(11).absolutePath)
//        }
//    }
//
//    /**
//     * Set an image and then remove the image. After every image removal the preview card should not
//     * be visible
//     */
//    @Test fun testRemoveFiveImage() {
//        onView(withId(R.id.background)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(0))
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        onView(withId(R.id.imagePreviewCard)).perform(longClick())
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == null)
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(not(isDisplayed())))
//
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(1))
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        onView(withId(R.id.imagePreviewCard)).perform(longClick())
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == null)
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(not(isDisplayed())))
//
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(2))
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        onView(withId(R.id.imagePreviewCard)).perform(longClick())
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == null)
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(not(isDisplayed())))
//
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(3))
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        onView(withId(R.id.imagePreviewCard)).perform(longClick())
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == null)
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(not(isDisplayed())))
//
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(4))
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        onView(withId(R.id.imagePreviewCard)).perform(longClick())
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == null)
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(not(isDisplayed())))
//    }
//
//    /**
//     * Set a background and then remove the background
//     */
//    @Test fun testPoemThemeBackgroundImage() {
//        onView(withId(R.id.background)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(0))
//            assert(activity.getPoemTheme().backgroundType == BackgroundType.IMAGE)
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        onView(withId(R.id.imagePreviewCard)).perform(longClick())
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getPoemTheme().backgroundType == BackgroundType.DEFAULT)
//        }
//    }
//
//    /**
//     * Click on an outline and an image
//     * The poem Theme should be OUTLINE WITH IMAGE
//     */
//    @Test fun testPoemThemeBackgroundAndOutline() {
//        onView(withId(R.id.rectangle_outline)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getPoemTheme().backgroundType == BackgroundType.OUTLINE)
//            assert(activity.getOutlineChosen() != null)
//        }
//        onView(withId(R.id.background)).perform(click())
//
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(0))
//            assert(activity.getPoemTheme().backgroundType == BackgroundType.OUTLINE_WITH_IMAGE)
//            assert(activity.getBackgroundImage() != null)
//        }
//    }
//
//    /**
//     * Remove an image when there is an outline. The poem theme should remain consistent
//     */
//    @Test fun testRemoveImageFromOutline() {
//        onView(withId(R.id.rectangle_outline)).perform(click())
//        onView(withId(R.id.background)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(0))
//            assert(activity.getPoemTheme().backgroundType == BackgroundType.OUTLINE_WITH_IMAGE)
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        onView(withId(R.id.imagePreviewCard)).perform(longClick())
//        onView(withText(R.string.remove_background_image_outline_popup)).check(matches(isDisplayed()))
//        onView(withText(R.string.negative_background_image_outline_button)).perform(click())
//        onView(withId(R.id.imagePreviewCard)).check(matches(not(isDisplayed())))
//
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() == null)
//            assert(activity.getOutlineChosen() != null)
//            assert(activity.getPoemTheme().backgroundType == BackgroundType.OUTLINE)
//        }
//    }
//
//    /**
//     * Remove an image when there is an outline. The poem theme should remain consistent
//     */
//    @Test fun testRemoveOutlineFromImage() {
//        onView(withId(R.id.rectangle_outline)).perform(click())
//        onView(withId(R.id.background)).perform(click())
//        activityRule.scenario.onActivity { activity ->
//            activity.getAdapter().onItemClick?.invoke(activity.getAdapter().getElement(1))
//            assert(activity.getPoemTheme().backgroundType == BackgroundType.OUTLINE_WITH_IMAGE)
//        }
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//        onView(withId(R.id.imagePreviewCard)).perform(longClick())
//        onView(withText(R.string.remove_background_image_outline_popup)).check(matches(isDisplayed()))
//        onView(withText(R.string.positive_background_color_outline_button)).perform(click())
//        onView(withId(R.id.imagePreviewCard)).check(matches(isDisplayed()))
//
//        activityRule.scenario.onActivity { activity ->
//            assert(activity.getBackgroundImage() != null)
//            assert(activity.getOutlineChosen() == null)
//            assert(activity.getPoemTheme().backgroundType == BackgroundType.IMAGE)
//        }
//    }
}