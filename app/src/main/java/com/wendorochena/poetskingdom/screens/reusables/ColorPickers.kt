package com.wendorochena.poetskingdom.screens.reusables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.screens.reusables.layouts.DialogLayout

@Composable
fun CircleColorPicker(
    modifier: Modifier,
    controller: ColorPickerController,
    initialColor : Color = Color.Red,
    onColorChanged: (ColorEnvelope) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 15.dp)) {
        Row(modifier = Modifier.padding(15.dp)) {
            Text(text = stringResource(id = R.string.color_picker_title),
                style = MaterialTheme.typography.h1,
                color = Color.White)
        }
        HsvColorPicker(modifier = modifier, controller = controller, onColorChanged = onColorChanged,
            initialColor = initialColor)
        AlphaSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(35.dp),
            controller = controller,
        )
        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(35.dp),
            controller = controller,
        )
        AlphaTile(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(70.dp)
                .clip(RoundedCornerShape(10.dp)),
            controller = controller,
        )
    }
}

@Preview
@Composable
fun ColorPreview(){
    DialogLayout(dialogBody =  {
        CircleColorPicker(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(), controller = rememberColorPickerController()) {
        }
    }, positiveButtonText = "", negativeButtonText = "", onNegativeAction = {true}, onPositiveAction = {true}, onDismiss = {})
}