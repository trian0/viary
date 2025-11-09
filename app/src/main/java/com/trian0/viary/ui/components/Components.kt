package com.trian0.viary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.trian0.viary.ui.theme.ViaryPrimaryContainer

@Composable
fun ElevatedOutlinedTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(20.dp),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .height(60.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxSize(),
            state = state,
            lineLimits = TextFieldLineLimits.SingleLine,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            textStyle = MaterialTheme.typography.labelLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ViaryPrimaryContainer,
                unfocusedBorderColor = ViaryPrimaryContainer,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
            ),
            enabled = enabled,
            readOnly = readOnly
        )
    }
}