package com.erdevelopments.powerpath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun WorkoutImage(
    imagePath: String?,
    modifier: Modifier = Modifier,
    onClick: ((String) -> Unit)? = null
) {
    if (!imagePath.isNullOrBlank()) {
        val path = imagePath
        AsyncImage(
            model = imageModel(path),
            contentDescription = null,
            modifier = modifier
                .clip(CircleShape)
                .let { base ->
                    if (onClick == null) base else base.clickable { onClick(path) }
                },
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "-",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FullscreenImageDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageModel(imagePath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    )
}

private fun imageModel(imagePath: String): Any =
    if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
        imagePath
    } else {
        File(imagePath)
    }
