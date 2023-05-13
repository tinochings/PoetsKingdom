package com.wendorochena.poetskingdom.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.OutlineTypes
import com.wendorochena.poetskingdom.viewModels.HeadingSelection

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoundedRectangleOutline(
    modifier: Modifier,
    color: Int,
    onOutlineClicked: (OutlineTypes) -> Unit,
    onOutlineLongClicked: @Composable (HeadingSelection) -> Unit
) {
    var showColorDialog  by remember { mutableStateOf(false) }
    Column(modifier = modifier.padding()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    BorderStroke(dimensionResource(id = R.dimen.strokeSize), Color(color)),
                    shape = RoundedCornerShape(15.dp)
                )
                .combinedClickable(enabled = true,
                    onClick = { onOutlineClicked.invoke(OutlineTypes.ROUNDED_RECTANGLE) },
                    onLongClick = { showColorDialog = true })
        )
        if (showColorDialog) {
            onOutlineLongClicked.invoke(HeadingSelection.OUTLINE)
            showColorDialog = false
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RectangleOutline(
    modifier: Modifier,
    color: Int,
    onOutlineClicked: (OutlineTypes) -> Unit,
    onOutlineLongClicked: @Composable (HeadingSelection) -> Unit
) {
    var showColorDialog  by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    BorderStroke(dimensionResource(id = R.dimen.strokeSize), Color(color)),
                    shape = RectangleShape
                )
                .combinedClickable(enabled = true,
                    onClick = { onOutlineClicked.invoke(OutlineTypes.RECTANGLE) },
                    onLongClick = { showColorDialog = true })
        )
        if (showColorDialog) {
            onOutlineLongClicked.invoke(HeadingSelection.OUTLINE)
            showColorDialog = false
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeardropOutline(
    modifier: Modifier,
    color: Int,
    onOutlineClicked: (OutlineTypes) -> Unit,
    onOutlineLongClicked: @Composable (HeadingSelection) -> Unit
) {
    var showColorDialog  by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    BorderStroke(dimensionResource(id = R.dimen.strokeSize), Color(color)),
                    shape = com.wendorochena.poetskingdom.ui.theme.TeardropOutline
                )
                .combinedClickable(enabled = true,
                    onClick = { onOutlineClicked.invoke(OutlineTypes.TEARDROP) },
                    onLongClick = { showColorDialog = true })
        )
        if (showColorDialog) {
            onOutlineLongClicked.invoke(HeadingSelection.OUTLINE)
            showColorDialog = false
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RotatedTeardropOutline(
    modifier: Modifier,
    color: Int,
    onOutlineClicked: (OutlineTypes) -> Unit,
    onOutlineLongClicked: @Composable (HeadingSelection) -> Unit
) {
    var showColorDialog  by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    BorderStroke(dimensionResource(id = R.dimen.strokeSize), Color(color)),
                    shape = com.wendorochena.poetskingdom.ui.theme.RotatedTeardropOutline
                )
                .combinedClickable(enabled = true,
                    onClick = { onOutlineClicked.invoke(OutlineTypes.ROTATED_TEARDROP) },
                    onLongClick = { showColorDialog = true })
        )
        if (showColorDialog) {
            onOutlineLongClicked.invoke(HeadingSelection.OUTLINE)
            showColorDialog = false
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LemonOutline(
    modifier: Modifier,
    color: Int,
    onOutlineClicked: (OutlineTypes) -> Unit,
    onOutlineLongClicked: @Composable (HeadingSelection) -> Unit
) {
    var showColorDialog  by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    BorderStroke(dimensionResource(id = R.dimen.strokeSize), Color(color)),
                    shape = com.wendorochena.poetskingdom.ui.theme.LemonOutline
                )
                .combinedClickable(enabled = true,
                    onClick = { onOutlineClicked.invoke(OutlineTypes.LEMON) },
                    onLongClick = { showColorDialog = true })
        )
        if (showColorDialog) {
            onOutlineLongClicked.invoke(HeadingSelection.OUTLINE)
            showColorDialog = false
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RotatedLemonOutline(
    modifier: Modifier,
    color: Int,
    onOutlineClicked: (OutlineTypes) -> Unit,
    onOutlineLongClicked: @Composable (HeadingSelection) -> Unit
) {
    var showColorDialog  by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    BorderStroke(dimensionResource(id = R.dimen.strokeSize), Color(color)),
                    shape = com.wendorochena.poetskingdom.ui.theme.RotatedLemonOutline
                )
                .combinedClickable(enabled = true,
                    onClick = { onOutlineClicked.invoke(OutlineTypes.LEMON) },
                    onLongClick = { showColorDialog = true })
        )
        if (showColorDialog) {
            onOutlineLongClicked.invoke(HeadingSelection.OUTLINE)
            showColorDialog = false
        }
    }
}