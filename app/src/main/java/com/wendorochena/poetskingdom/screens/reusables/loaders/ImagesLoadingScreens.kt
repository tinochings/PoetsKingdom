package com.wendorochena.poetskingdom.screens.reusables.loaders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wendorochena.poetskingdom.ui.theme.DefaultBackgroundColor
import com.wendorochena.poetskingdom.ui.theme.DefaultColor
import com.wendorochena.poetskingdom.ui.theme.RoundedRectangleOutline

@Composable
fun ImagesNotification(
    modifier: Modifier = Modifier,
    imagesNotificationModel: ImagesNotificationModel,
    fontColor: Color = Color.Black
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.3f))
            .padding(15.dp)
            .clickable(enabled = false, interactionSource = null, indication = null, onClick = {}), contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = modifier.fillMaxWidth(), shape = RoundedRectangleOutline,
            CardDefaults.cardColors(containerColor = DefaultBackgroundColor)
        ) {
            Row(modifier = Modifier.padding(15.dp)) {
                Column {
                    Text(
                        text = imagesNotificationModel.notificationHeader,
                        color = fontColor,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(25.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = { imagesNotificationModel.progress.value },
                        trackColor = Color.White,
                        color = DefaultColor
                    )
                    Spacer(modifier = Modifier.height(25.dp))
                    Row {
                        Row(modifier = Modifier.weight(1f)) {
                            Text(
                                "${imagesNotificationModel.totalImagesProcessed.value} / ${imagesNotificationModel.totalImages}",
                                color = fontColor,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Light
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "${imagesNotificationModel.percentage.value}%",
                                color = fontColor,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ImagesLoadingScreens() {
    ImagesNotification(imagesNotificationModel = ImagesNotificationModel())
}
