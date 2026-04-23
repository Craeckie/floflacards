/*
 * Copyright (C) 2026 FloFla Dev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.floflacards.app.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.floflacards.app.data.entity.FlashcardEntity
import com.floflacards.app.data.entity.CategoryEntity
import com.floflacards.app.domain.model.FlashcardRating
import com.floflacards.app.data.source.FlashcardUiPreferences
import com.floflacards.app.data.model.FlashcardTheme

/**
 * Flashcard overlay container. The header is always draggable; the bottom-right
 * corner carries an always-visible resize grip. Overlay alpha is driven by the
 * user's saved opacity (configured in the main app settings).
 */
@Composable
fun FlashcardContainer(
    flashcard: FlashcardEntity,
    category: CategoryEntity?,
    uiState: FlashcardUiPreferences.FlashcardUiState,
    theme: FlashcardTheme = FlashcardTheme.DEFAULT_THEME,
    onPositionChange: (Int, Int) -> Unit,
    onSizeChange: (Int, Int) -> Unit,
    onRating: (FlashcardRating) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAnswer by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .alpha(uiState.getAlpha()),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp,
                pressedElevation = 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = FlashcardColors.getBackgroundColor(theme)
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                FlashcardHeader(
                    category = category,
                    theme = theme,
                    onPositionChange = onPositionChange,
                    onClose = onClose
                )

                // Content area, 3-sided border (left/right/bottom) — the top of
                // the card is the title bar and gets no border.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .then(contentBorder(FlashcardColors.getTextColor(theme).copy(alpha = 0.3f)))
                ) {
                    FlashcardContent(
                        flashcard = flashcard,
                        showAnswer = showAnswer,
                        onShowAnswer = { showAnswer = true },
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )

                    if (showAnswer) {
                        FlashcardControls(
                            onRating = onRating,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        ResizeCornerGrip(
            theme = theme,
            onSizeChange = onSizeChange,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

/**
 * 3-sided thin border (left, right, bottom) that frames the content area beneath
 * the title bar. The path curves through the card's bottom corners so the
 * border stays visible all the way around those corners.
 */
internal fun contentBorder(color: Color): Modifier = Modifier.drawBehind {
    val stroke = 1.dp.toPx()
    val half = stroke / 2f
    // Match the Card's RoundedCornerShape(20.dp); inset by half-stroke so the
    // stroke's outer edge sits on the card's edge instead of being clipped.
    val r = (20.dp.toPx() - half).coerceAtLeast(0f)
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(half, 0f)
        // Down the left side to where the bottom-left curve starts.
        lineTo(half, h - r - half)
        // Bottom-left corner: left-mid (180°) → bottom-mid (90°).
        arcTo(
            rect = Rect(half, h - 2f * r - half, 2f * r + half, h - half),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false
        )
        // Across the bottom.
        lineTo(w - r - half, h - half)
        // Bottom-right corner: bottom-mid (90°) → right-mid (0°).
        arcTo(
            rect = Rect(w - 2f * r - half, h - 2f * r - half, w - half, h - half),
            startAngleDegrees = 90f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false
        )
        // Up the right side.
        lineTo(w - half, 0f)
    }
    drawPath(path = path, color = color, style = Stroke(width = stroke))
}

/**
 * Resize affordance in the bottom-right corner. Two diagonal strokes in the
 * theme text color at low alpha — subtle but discoverable. The whole box
 * captures drag gestures.
 */
@Composable
internal fun ResizeCornerGrip(
    theme: FlashcardTheme,
    onSizeChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokeColor = FlashcardColors.getTextColor(theme).copy(alpha = 0.35f)
    Box(
        modifier = modifier
            .size(40.dp)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    onSizeChange(dragAmount.x.toInt(), dragAmount.y.toInt())
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pad = 8.dp.toPx()
            val strokeWidth = 2.dp.toPx()
            val outer = 16.dp.toPx()
            val inner = 9.dp.toPx()
            val w = size.width
            val h = size.height
            // Two diagonal strokes in the bottom-right corner.
            drawLine(
                color = strokeColor,
                start = Offset(w - pad, h - pad - outer),
                end = Offset(w - pad - outer, h - pad),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = strokeColor,
                start = Offset(w - pad, h - pad - inner),
                end = Offset(w - pad - inner, h - pad),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
