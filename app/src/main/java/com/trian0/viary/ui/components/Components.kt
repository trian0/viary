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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.colors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.trian0.viary.R
import com.trian0.viary.ui.theme.ActionOrangeGradient
import com.trian0.viary.ui.theme.Neutral90
import com.trian0.viary.ui.theme.Primary10
import com.trian0.viary.ui.theme.Primary100
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Primary30
import com.trian0.viary.ui.theme.Primary50
import com.trian0.viary.ui.theme.Primary80
import com.trian0.viary.ui.theme.Secondary90
import com.trian0.viary.ui.theme.White

@Composable
fun ElevatedOutlinedTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    inputTransformation: InputTransformation = InputTransformation.maxLength(50),
    outputTransformation: OutputTransformation? = null,
    icon: ImageVector? = null,
    label: String = "",
    isError: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .height(56.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxSize(),
            scrollState = rememberScrollState(),
            leadingIcon = if (icon != null) {
                {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = icon,
                        contentDescription = null,
                        tint = Primary30
                    )
                }
            } else null,
            inputTransformation = inputTransformation,
            outputTransformation = outputTransformation,
            trailingIcon = if (isError) {
                {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Filled.Error,
                        tint = Color.Red,
                        contentDescription = null
                    )
                }
            } else null,
            state = state,
            lineLimits = TextFieldLineLimits.SingleLine,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = Primary20,
                fontWeight = FontWeight.Normal
            ),
            placeholder = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Primary20.copy(0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            },
            colors = colors(
                focusedBorderColor = Primary30,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Neutral90,
                focusedContainerColor = Neutral90,
            ),
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
        )
    }
}

@Composable
fun ViaryButton(
    label: String,
    icon: ImageVector? = null,
    onClicked: () -> Unit = {},
    isLoading: Boolean = false
) {
    Button(
        modifier = Modifier
            .padding(top = 30.dp)
            .fillMaxWidth()
            .height(55.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(30.dp)
            )
            .background(ActionOrangeGradient, RoundedCornerShape(20.dp)),
        onClick = {
            onClicked()
        },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = White
        ),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = White,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                icon?.let {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = label,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = White,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ViarySecondaryButton(
    modifier: Modifier,
    label: String,
    icon: ImageVector? = null,
    onClicked: () -> Unit = {},
    isLoading: Boolean = false
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp),
        onClick = {
            onClicked()
        },
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Neutral90,
            contentColor = Primary10
        ),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = White,
                modifier = Modifier.height(24.dp)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                icon?.let {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Primary10,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = label,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary10,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ClimateViary(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(65.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = if (isSelected) Primary30 else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Neutral90,
            contentColor = Primary30,
            disabledContainerColor = Neutral90,
            disabledContentColor = Primary30
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    vertical = 18.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                imageVector = icon,
                contentDescription = null,
                tint = Primary30
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = label,
                color = Primary30,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

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
            ViaryButton(labelConfirm, onClicked = onConfirm)
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
            ViaryButton(stringResource(labelConfirm), onClicked = onDismiss)
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
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

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
            if (selectedImageUri != null) {
                onImageSelected(selectedImageUri!!)
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = selectedImageUri,
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