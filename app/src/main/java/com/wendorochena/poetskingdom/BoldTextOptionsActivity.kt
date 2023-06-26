package com.wendorochena.poetskingdom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set

class BoldTextOptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val highlightedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        val isEditText = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)

        if (!isEditText && highlightedText != null){
            val outgoingIntent = Intent()
            val boldSpannedText = SpannableString(highlightedText)
            boldSpannedText[0..highlightedText.length] = StyleSpan(android.graphics.Typeface.BOLD)
            outgoingIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, boldSpannedText)
            setResult(Activity.RESULT_OK, outgoingIntent)
        }
        finish()
    }
}