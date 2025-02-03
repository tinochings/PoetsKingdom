package com.wendorochena.poetskingdom.screens.reusables.buttons

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wendorochena.poetskingdom.ui.theme.DefaultColor

@Composable
fun DialogActionButton(modifier : Modifier = Modifier, buttonText : String, onClick : () -> Unit){
    Button(
        modifier = modifier
            .padding(5.dp),
        onClick = {
            onClick.invoke()
        },
        colors = ButtonDefaults.buttonColors(containerColor = DefaultColor)
    ) {
        Text(text = buttonText, style = MaterialTheme.typography.h2)
    }
}