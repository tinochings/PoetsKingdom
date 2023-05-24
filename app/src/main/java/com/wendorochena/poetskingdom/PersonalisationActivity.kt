package com.wendorochena.poetskingdom

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputFilter
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.view.setPadding
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat

class PersonalisationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }


    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName =
                getString(R.string.personalisation_sharedpreferences_key)
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            setupPoemPreferencesListeners()
            setupLandscapeListener()
            val sharedPreferences =
                context?.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            if (sharedPreferences != null) {
                if (!sharedPreferences.getBoolean("personalisationFirstUse", false)) {
                    onFirstUse()
                    sharedPreferences.edit().putBoolean("personalisationFirstUse", true).apply()
                }
            }
        }

        private fun onFirstUse() {
            val alertDialogBuilder = context?.let { AlertDialog.Builder(it) }
            val customTitleView = TextView(context)
            customTitleView.setTextColor(resources.getColor(R.color.white, null))
            customTitleView.text = resources.getString(R.string.personalisation)

            customTitleView.setTypeface(null, Typeface.BOLD)
            customTitleView.textSize = resources.getDimension(R.dimen.normal_text_size)

            val customMessageView = TextView(context)
            customMessageView.setTextColor(resources.getColor(R.color.white, null))
            customMessageView.text = resources.getString(R.string.guide_personalisation)
            val typedValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics).toInt()
            customTitleView.setPadding(typedValue, typedValue, typedValue,0)
            customMessageView.setPadding(typedValue)

            alertDialogBuilder?.setCustomTitle(customTitleView)
            alertDialogBuilder?.setView(customMessageView)
            alertDialogBuilder?.setPositiveButton(R.string.builder_understood) { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = alertDialogBuilder?.create()
            dialog?.window?.setBackgroundDrawableResource(R.drawable.selected_rounded_rectangle)
            dialog?.show()
            context?.getColor(R.color.off_white)
                ?.let { dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(it) }
        }

        private fun setupPoemPreferencesListeners() {
            val nicknamePref = findPreference<EditTextPreference>("appNickname")

            nicknamePref?.setOnBindEditTextListener {
                it.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(30))
            }
            nicknamePref?.setOnPreferenceChangeListener { _, _ ->
                restartApp()
                true
            }
        }

        /**
         * Sets the resolution visibility if selection is landscape
         */
        private fun setupLandscapeListener() {
            val orientationSelected = findPreference<ListPreference>("orientation")
            val resolution = findPreference<ListPreference>("resolution")
            orientationSelected?.setOnPreferenceChangeListener { _, s ->
                resolution?.isVisible = s == "landscape"
                true
            }
            resolution?.isVisible = orientationSelected?.value == "landscape"
        }

        private fun restartApp() {
            val intent = Intent(
                requireContext(),
                MainActivityCompose::class.java
            )
            startActivity(intent)
            finishAffinity(this.requireActivity())
        }
    }
}