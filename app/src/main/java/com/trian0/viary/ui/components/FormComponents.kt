package com.trian0.viary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.colors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trian0.viary.ui.theme.ActionOrangeGradient
import com.trian0.viary.ui.theme.Neutral90
import com.trian0.viary.ui.theme.Primary10
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Primary30
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
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector? = null,
    onClicked: () -> Unit = {},
    isLoading: Boolean = false
) {
    Button(
        modifier = modifier
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
