package com.wendorochena.poetskingdom.screens.reusables.loaders

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
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
            .clickable(enabled = false, interactionSource = null, indication = null, onClick = {}),
        contentAlignment = Alignment.BottomCenter
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

@Composable
fun ImagesLoading(
    modifier: Modifier = Modifier,
    widthOfBrush: Int = 700,
    durationMillis: Int = 1000,
    angleOfAxisY: Float = 0f,
    gridRowWidth : Int = 3
) {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.3f),
        Color.White.copy(alpha = 0.5f),
        Color.White.copy(alpha = 1.0f),
        Color.White.copy(alpha = 0.5f),
        Color.White.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition()
    val screenHeight = LocalWindowInfo.current.containerSize.height
    val imageSize = LocalWindowInfo.current.containerSize.width / 3
    val totalRows = screenHeight / imageSize
    val totalImages = totalRows * gridRowWidth

    val transitionAnimation = transition.animateFloat(
        initialValue = 0f, targetValue = (durationMillis + widthOfBrush).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ), repeatMode = RepeatMode.Restart
        )
    )
    val brush = Brush.linearGradient(colors = shimmerColors, start = Offset(x = transitionAnimation.value - widthOfBrush, y = 0.0f),
        end = Offset(x = transitionAnimation.value, y = angleOfAxisY))

        LazyVerticalGrid(modifier = modifier.padding(top = 5.dp).fillMaxSize(), columns = GridCells.Fixed(gridRowWidth),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            items(totalImages){
                Box(modifier = modifier) {
                    Spacer(modifier = Modifier
                        .padding(3.dp)
                        .aspectRatio(1f)
                        .background(brush = brush))
                }
            }
        }
}

@Preview
@Composable
fun ImagesLoadingScreens() {
    ImagesNotification(imagesNotificationModel = ImagesNotificationModel())
}
