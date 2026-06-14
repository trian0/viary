package com.trian0.viary.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.trian0.viary.R
import com.trian0.viary.ui.theme.Neutral90
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Primary100
import com.trian0.viary.ui.theme.Primary50
import com.trian0.viary.ui.theme.Primary80
import com.trian0.viary.ui.theme.Secondary90
import com.trian0.viary.ui.theme.White
import java.io.File

@Composable
fun SuccessDialog(
    icon: ImageVector = Icons.Filled.CheckCircle,
    labelTitle: String,
    labelSubtitle: String,
    labelConfirm: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = White,
        tonalElevation = 8.dp,
        icon = {
            val shadowColor = Primary80
            val shadowElevation = 12.dp

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .padding(shadowElevation)
                    .graphicsLayer {
                        this.shadowElevation = shadowElevation.toPx()
                        this.shape = CircleShape
                        this.ambientShadowColor = shadowColor
                        this.spotShadowColor = shadowColor
                    }
                    .clip(CircleShape)
                    .background(Primary100)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Primary50,
                    modifier = Modifier.size(40.dp)
                )
            }
        },

        title = {
            Text(
                textAlign = TextAlign.Center,
                text = labelTitle,
                style = MaterialTheme.typography.headlineMedium
            )
        },

        text = {
            Text(
                text = labelSubtitle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = Primary20
            )
        },

        confirmButton = {
            ViaryButton(modifier = Modifier, labelConfirm, onClicked = onConfirm)
        },
    )
}

@Composable
fun ErrorDialog(
    icon: ImageVector = Icons.Outlined.LocationOff,
    labelTitle: Int = R.string.dialog_error_title,
    labelSubtitle: Int,
    labelConfirm: Int = R.string.dialog_error_button_label,
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = White,
        tonalElevation = 8.dp,
        icon = {
            val shadowColor = Primary80
            val shadowElevation = 12.dp

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .padding(shadowElevation)
                    .graphicsLayer {
                        this.shadowElevation = shadowElevation.toPx()
                        this.shape = CircleShape
                        this.ambientShadowColor = shadowColor
                        this.spotShadowColor = shadowColor
                    }
                    .clip(CircleShape)
                    .background(Primary100)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Primary50,
                    modifier = Modifier.size(32.dp)
                )
            }
        },

        title = {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(labelTitle),
                style = MaterialTheme.typography.headlineMedium
            )
        },

        text = {
            Text(
                text = stringResource(labelSubtitle),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = Primary20
            )
        },

        confirmButton = {
            ViaryButton(modifier = Modifier, stringResource(labelConfirm), onClicked = onDismiss)
        },
    )
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Neutral90,
            White,
            Neutral90,
        ),
        start = Offset(shimmerTranslate - 300f, 0f),
        end = Offset(shimmerTranslate, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(
    onDismiss: () -> Unit = {}
) {
    val permissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val showPermissionDialog = !permissionState.status.isGranted

    if (showPermissionDialog) {
        SuccessDialog(
            icon = Icons.Filled.LocationOff,
            labelTitle = stringResource(R.string.dialog_location_title),
            labelSubtitle = stringResource(R.string.dialog_location_subtitle),
            labelConfirm = stringResource(R.string.dialog_location_confirm),
            onDismiss = onDismiss,
            onConfirm = {
                permissionState.launchPermissionRequest()
            }
        )
    }
}

@Composable
fun ImagePicker(
    modifier: Modifier = Modifier,
    label: String,
    imageSelectedTitle: String,
    imageSelectedSubtitle: String,
    onImageSelected: (Uri) -> Unit,
    currentImagePath: String? = null,
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { onImageSelected(it) }
    }

    val displayModel: Any? = selectedImageUri ?: currentImagePath?.let { File(it) }

    Column(
        modifier = modifier
            .wrapContentSize()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardColors(
                containerColor = Secondary90,
                contentColor = Primary20,
                disabledContainerColor = Secondary90,
                disabledContentColor = Primary20
            ),
            onClick = { launcher.launch("image/*") }
        ) {
            if (displayModel != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = displayModel,
                        contentDescription = "Imagem selecionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = imageSelectedTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = imageSelectedSubtitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Primary100.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddAPhoto,
                                contentDescription = "Adicionar imagem",
                                modifier = Modifier.size(30.dp),
                                tint = Primary20
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = stringResource(R.string.create_screen_image_picker_subtitle),
                            style = MaterialTheme.typography.labelLarge,
                            color = Primary20
                        )
                    }
                }
            }
        }
    }
}
