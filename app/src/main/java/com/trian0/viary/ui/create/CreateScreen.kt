package com.trian0.viary.ui.create

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trian0.viary.ui.components.ElevatedOutlinedTextField
import com.trian0.viary.ui.components.ImagePicker
import com.trian0.viary.ui.theme.ViaryOnPrimary
import com.trian0.viary.ui.theme.ViaryPrimary
import org.koin.compose.viewmodel.koinViewModel
import java.util.Locale

@Composable
fun CreateScreen(
    viewModel: CreateViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val viaryNameState = rememberTextFieldState()
    val locateState = rememberTextFieldState()
    val kmState = rememberTextFieldState()

    LaunchedEffect(viaryNameState.text) {
        viewModel.onIntent(CreateContract.CreateIntent.OnViaryNameChanged(viaryNameState.text.toString()))
    }

    LaunchedEffect(locateState.text) {
        viewModel.onIntent(CreateContract.CreateIntent.OnDepartureLocationChanged(locateState.text.toString()))
    }

    LaunchedEffect(kmState.text) {
        viewModel.onIntent(CreateContract.CreateIntent.OnCurrentKmChanged(kmState.text.toString()))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreateContract.CreateEffect.TripCreatedSuccessfully -> {
                    Toast.makeText(context, "Viagem iniciada com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                }

                is CreateContract.CreateEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Sucesso!") },
            text = { Text(text = "Sua viagem foi iniciada com sucesso. Boa jornada!") },
            confirmButton = {
                Button(
                    onClick = {
                        onNavigateBack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .border(
                width = 2.dp,
                color = ViaryOnPrimary,
                shape = RoundedCornerShape(30.dp)
            )
            .clip(RoundedCornerShape(30.dp))
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .background(Color.Transparent),
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Criar Viagem",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                modifier = Modifier.padding(top = 40.dp),
                text = "Nome da sua Viary",
                style = MaterialTheme.typography.bodyMedium
            )

            ElevatedOutlinedTextField(
                state = viaryNameState,
                modifier = Modifier.padding(top = 10.dp)
            )

            if (uiState.viaryNameError != null) {
                Text(
                    text = uiState.viaryNameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Text(
                modifier = Modifier.padding(top = 20.dp),
                text = "Localização de Saída",
                style = MaterialTheme.typography.bodyMedium
            )

            ElevatedOutlinedTextField(
                state = locateState,
                modifier = Modifier.padding(top = 10.dp)
            )

            if (uiState.departureLocationError != null) {
                Text(
                    text = uiState.departureLocationError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Text(
                modifier = Modifier.padding(top = 20.dp),
                text = "Kilometragem Atual (Km)",
                style = MaterialTheme.typography.bodyMedium
            )

            ElevatedOutlinedTextField(
                state = kmState,
                modifier = Modifier.padding(top = 10.dp),
                keyboardType = KeyboardType.Number
            )

            if (uiState.currentKmError != null) {
                Text(
                    text = uiState.currentKmError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            ImagePicker(
                modifier = Modifier.padding(top = 20.dp),
                label = "Escolha a capa",
                onImageSelected = {
                    viewModel.onIntent(CreateContract.CreateIntent.OnCoverImageSelected(it))
                }
            )

            Button(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .fillMaxWidth()
                    .height(55.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(20.dp)
                    ),
                onClick = {
                    viewModel.onIntent(CreateContract.CreateIntent.OnStartTripClicked)
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ViaryPrimary,
                    contentColor = ViaryOnPrimary
                ),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = ViaryOnPrimary,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text(
                        text = "Iniciar Viagem",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CreateScreenPreview() {
    CreateScreen()
}