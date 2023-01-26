package com.wendorochena.poetskingdom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.preference.EditTextPreference
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
            preferenceManager.sharedPreferencesName = getString(R.string.personalisation_sharedpreferences_key)
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            setupPoemPreferencesListeners()
            val sharedPreferences = context?.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            if (sharedPreferences != null) {
                if (!sharedPreferences.getBoolean("personalisationFirstUse",false)) {
                    onFirstUse()
                    sharedPreferences.edit().putBoolean("personalisationFirstUse",true).apply()
                }
            }
        }

        private fun onFirstUse() {
            val alertDialogBuilder = context?.let { AlertDialog.Builder(it) }
            alertDialogBuilder?.setTitle(R.string.guide_title)
                ?.setPositiveButton(R.string.builder_understood) { dialog, _ ->
                    dialog.dismiss()
                }?.setMessage(R.string.guide_personalisation)?.show()
        }
        private fun setupPoemPreferencesListeners() {
            val nicknamePref = findPreference<EditTextPreference>("appNickname")

            nicknamePref?.setOnBindEditTextListener {
                it.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(30))
            }
            nicknamePref?.setOnPreferenceChangeListener { pref,p ->
                restartApp()
                true
            }
        }

        private fun restartApp() {
            val intent = Intent(
                context,
                MainActivity::class.java
            )
            startActivity(intent)
            finishAffinity(this.requireActivity())
        }
    }
}