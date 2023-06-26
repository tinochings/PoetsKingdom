package com.wendorochena.poetskingdom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class TextColorOptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val highlightedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        val isEditText = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)

        if (!isEditText && highlightedText != null){
            val outgoingIntent = Intent()
            val customColorText = SpannableString(highlightedText)
            com.skydoves.colorpickerview.ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.color_picker_title))
                .setPositiveButton(R.string.confirm, object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        if (envelope != null) {
                            customColorText[0..highlightedText.length] = ForegroundColorSpan(envelope.color)
                            outgoingIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, customColorText)
                            setResult(Activity.RESULT_OK, outgoingIntent)
                            finish()
                        } else {
                            //default case
                            finish()
                        }
                    }

                }).setNegativeButton(
                    R.string.title_change_cancel
                ) { dialog, _ ->
                    dialog?.dismiss()
                }.setOnDismissListener { finish() }.show()
        }
    }
}