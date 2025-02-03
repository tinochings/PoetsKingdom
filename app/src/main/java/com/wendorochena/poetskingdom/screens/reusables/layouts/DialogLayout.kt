package com.wendorochena.poetskingdom.screens.reusables.layouts

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wendorochena.poetskingdom.screens.reusables.buttons.DialogActionButton
import com.wendorochena.poetskingdom.ui.theme.DefaultColor
import com.wendorochena.poetskingdom.ui.theme.DefaultStatusBarColor

/**
 * @param modifier
 * @param dialogBody the composable function that describes the body of the dialog
 * @param onNegativeAction determines what to do when the negative button has been clicked. Returns
 * true if the dialog should be dismissed
 * @param onPositiveAction determines what do when the positive button has been clicked. Returns
 * true if the dialog should be dismissed
 * @param onDismiss cleanup any state necessary when dismissing a function
 */
@Composable
fun DialogLayout(
    modifier: Modifier = Modifier,
    dialogBody: @Composable () -> Unit,
    positiveButtonText : String,
    negativeButtonText : String,
    onNegativeAction: () -> Boolean,
    onPositiveAction: () -> Boolean,
    onDismiss : () -> Unit
) {
    var shouldDismiss by remember { mutableStateOf(false) }

    if (!shouldDismiss) {
        Dialog(
            onDismissRequest = {
                shouldDismiss = true
                onDismiss.invoke() },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true,
                usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(15.dp)
                    .border(
                        width = 3.dp,
                        color = DefaultStatusBarColor,
                        com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline
                    ),
                shape = com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline,
                colors = CardDefaults.cardColors(containerColor = DefaultColor)
            ) {
                dialogBody()
                Spacer(modifier = Modifier.height(15.dp))
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End)) {
                    DialogActionButton(modifier = Modifier.weight(1f), buttonText = negativeButtonText
                    ) {
                        shouldDismiss = onNegativeAction.invoke()
                    }

                    DialogActionButton(modifier = Modifier.weight(1f), buttonText = positiveButtonText
                    ) {
                        shouldDismiss = onPositiveAction.invoke()
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleTextDialogBody(title : String, textBody : String, fontColor : Color = Color.White) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(text = title, style = MaterialTheme.typography.h1, color = fontColor)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = textBody, style = MaterialTheme.typography.body1, color = fontColor)
    }
}


@Preview
@Composable
fun DialogPreview() {
    DialogLayout(
        dialogBody = { SimpleTextDialogBody(title = "sdsddssdfsdfdsfdsf", textBody = "sdjfgdkjfgjkdsfgskd fdsjkgfdskjfdsgfkdsfkdsjgf kdsgfkd sfgkdsf kdsgf kdsf")},
        positiveButtonText = "Confirm",
        negativeButtonText = "Cancel",
        onNegativeAction = { true },
        onPositiveAction = { true }) {

    }
}