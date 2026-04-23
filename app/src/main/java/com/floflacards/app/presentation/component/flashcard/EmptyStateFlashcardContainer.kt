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

package com.floflacards.app.presentation.component.flashcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.floflacards.app.R
import com.floflacards.app.data.entity.FlashcardEntity
import com.floflacards.app.data.source.FlashcardUiPreferences
import com.floflacards.app.data.model.FlashcardTheme
import com.floflacards.app.presentation.component.FlashcardColors
import com.floflacards.app.presentation.component.FlashcardHeader
import com.floflacards.app.presentation.component.ResizeCornerGrip
import com.floflacards.app.presentation.component.contentBorder

/**
 * Specialized container for empty-state flashcards (no cards available).
 * Mirrors FlashcardContainer: always-draggable header, always-resizable corner.
 */
@Composable
fun EmptyStateFlashcardContainer(
    flashcard: FlashcardEntity,
    uiState: FlashcardUiPreferences.FlashcardUiState,
    theme: FlashcardTheme = FlashcardTheme.DEFAULT_THEME,
    onPositionChange: (Int, Int) -> Unit,
    onSizeChange: (Int, Int) -> Unit,
    onManageCards: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    require(flashcard.id == -2L) { "EmptyStateFlashcardContainer should only be used with empty state flashcards (ID -2L)" }

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
                    category = null,
                    theme = theme,
                    onPositionChange = onPositionChange,
                    onClose = onClose
                )

                MinimalisticEmptyContent(
                    theme = theme,
                    onManageCards = onManageCards,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .then(contentBorder(FlashcardColors.getTextColor(theme).copy(alpha = 0.3f)))
                )
            }
        }

        ResizeCornerGrip(
            theme = theme,
            onSizeChange = onSizeChange,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun MinimalisticEmptyContent(
    theme: FlashcardTheme,
    onManageCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.empty_state_no_cards_title),
            color = FlashcardColors.getTextColor(theme),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onManageCards,
            colors = FlashcardColors.getShowAnswerButtonColors(theme),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(44.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_manage_flashcards),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
