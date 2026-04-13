package com.trian0.viary.ui.create

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trian0.viary.MainViewModel
import com.trian0.viary.R
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.utils.CurrencyOutputTransformation
import com.trian0.viary.ui.components.ClimateViary
import com.trian0.viary.ui.components.ElevatedOutlinedTextField
import com.trian0.viary.ui.components.ErrorDialog
import com.trian0.viary.ui.components.ImagePicker
import com.trian0.viary.ui.components.RequestLocationPermission
import com.trian0.viary.ui.components.ShimmerEffect
import com.trian0.viary.ui.components.SuccessDialog
import com.trian0.viary.ui.components.ViaryButton
import com.trian0.viary.ui.theme.Primary10
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Secondary80
import com.trian0.viary.ui.theme.Secondary90
import com.trian0.viary.ui.theme.Tertiary10
import com.trian0.viary.ui.theme.Tertiary90
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateScreen(
    viewModel: CreateViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val country by mainViewModel.country.collectAsStateWithLifecycle()

    if (country.loading) {
        CreateScreenSkeleton()
        return
    }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val viaryNameState = rememberTextFieldState()
    val locateState = rememberTextFieldState()
    val budgetState = rememberTextFieldState()
    val climateSate = remember { mutableStateOf("") }

    var showPermissionDialog by remember { mutableStateOf(false) }

    if (showPermissionDialog) {
        RequestLocationPermission(
            onDismiss = { showPermissionDialog = false }
        )
    }

    LaunchedEffect(viaryNameState.text) {
        viewModel.onIntent(CreateContract.CreateIntent.OnViaryNameChanged(viaryNameState.text.toString()))
    }

    LaunchedEffect(locateState.text) {
        viewModel.onIntent(CreateContract.CreateIntent.OnDepartureLocationChanged(locateState.text.toString()))
    }

    LaunchedEffect(budgetState.text) {
        viewModel.onIntent(
            CreateContract.CreateIntent.OnCurrentBudgetChanged(budgetState.text.toString())
        )
    }

    LaunchedEffect(climateSate.value) {
        viewModel.onIntent(CreateContract.CreateIntent.OnClimateChanged(climateSate.value))
    }

    val tripSuccessMessage = stringResource(R.string.create_screen_trip_started_message)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreateContract.CreateEffect.TripCreatedSuccessfully -> {
                    Toast.makeText(context, tripSuccessMessage, Toast.LENGTH_SHORT)
                        .show()
                }

                is CreateContract.CreateEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (uiState.showSuccessDialog) {
        SuccessDialog(
            labelTitle = stringResource(R.string.dialog_success_create_screen_title),
            labelSubtitle = stringResource(R.string.dialog_success_create_screen_subtitle),
            labelConfirm = stringResource(R.string.dialog_success_create_screen_button_message),
            onConfirm = onNavigateBack
        )
    }

    if (uiState.showErrorDialog) {
        ErrorDialog(
            labelSubtitle = R.string.dialog_error_create_screen_subtitle,
            onDismiss = {
                viewModel.onIntent(CreateContract.CreateIntent.OnDismissErrorDialog)
            }
        )
    }

    CreateScreenView(
        modifier = Modifier,
        viaryNameState,
        uiState.viaryNameError,
        locateState,
        uiState.departureLocationError,
        budgetState,
        uiState.currentBudgetError,
        onCoverImageSelected = { uri ->
            viewModel.onIntent(CreateContract.CreateIntent.OnCoverImageSelected(uri))
        },
        onStartTripClicked = {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                viewModel.onIntent(CreateContract.CreateIntent.OnStartTripClicked)
            } else {
                showPermissionDialog = true
            }
        },
        onNavigateBack,
        uiState.isLoading,
        climateSate,
        countryCurrency = country.currency,
        symbol = country.symbol
    )
}

@Composable
fun CreateScreenView(
    modifier: Modifier,
    viaryName: TextFieldState = rememberTextFieldState(),
    viaryNameError: Boolean = false,
    locate: TextFieldState = rememberTextFieldState(),
    locateError: Boolean = false,
    budget: TextFieldState = rememberTextFieldState(),
    budgetError: Boolean = false,
    onCoverImageSelected: (Uri) -> Unit = {},
    onStartTripClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    isLoading: Boolean = false,
    climateState: MutableState<String> = mutableStateOf(""),
    countryCurrency: String = "",
    symbol: String = "",
) {
    var selectedWeather by remember { mutableStateOf(Viary.ViaryClimate.SUNNY) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = null,
                tint = Primary10,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically)
                    .clickable { onNavigateBack() },
            )

            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 9.dp),
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp, top = 64.dp)
                .background(Color.Transparent),
        ) {
            Text(
                text = stringResource(R.string.create_screen_title),
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = stringResource(R.string.create_screen_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Primary20
            )

            ImagePicker(
                modifier = Modifier.padding(top = 40.dp),
                label = stringResource(R.string.create_screen_image_picker_title),
                imageSelectedTitle = stringResource(R.string.create_screen_image_picker_selected_title),
                imageSelectedSubtitle = stringResource(R.string.create_screen_image_picker_selected_subtitle),
                onImageSelected = {
                    onCoverImageSelected(it)
                }
            )

            Text(
                modifier = Modifier.padding(top = 40.dp),
                text = stringResource(R.string.create_screen_name_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Primary10
            )

            ElevatedOutlinedTextField(
                state = viaryName,
                modifier = Modifier.padding(top = 10.dp),
                icon = Icons.Outlined.TravelExplore,
                label = stringResource(R.string.create_screen_name_placeholder),
                isError = viaryNameError
            )

            Text(
                modifier = Modifier.padding(top = 20.dp),
                text = stringResource(R.string.create_screen_origin_name_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Primary10
            )

            ElevatedOutlinedTextField(
                state = locate,
                modifier = Modifier.padding(top = 10.dp),
                icon = Icons.Outlined.LocationOn,
                label = stringResource(R.string.create_screen_origin_name_placeholder),
                isError = locateError
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                colors = CardColors(
                    containerColor = Secondary90,
                    contentColor = Primary20,
                    disabledContainerColor = Secondary90,
                    disabledContentColor = Primary20
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.create_screen_climate_title),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = stringResource(R.string.create_screen_climate_subtitle),
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary20
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val weatherOptions = listOf(
                            Viary.ViaryClimate.SUNNY to Icons.Outlined.WbSunny,
                            Viary.ViaryClimate.CLOUDY to Icons.Outlined.Cloud,
                            Viary.ViaryClimate.RAINY to Icons.Outlined.WaterDrop,
                            Viary.ViaryClimate.CHILLY to Icons.Outlined.SentimentVerySatisfied
                        )

                        weatherOptions.forEach { (climate, icon) ->
                            ClimateViary(
                                icon = icon,
                                label = stringResource(climate.labelRes),
                                isSelected = selectedWeather == climate,
                                onClick = {
                                    selectedWeather = climate
                                    climateState.value = climate.name
                                }
                            )
                        }
                    }

                    Text(
                        modifier = Modifier.padding(top = 26.dp),
                        text = stringResource(R.string.create_screen_budget_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary10
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ElevatedOutlinedTextField(
                            state = budget,
                            modifier = Modifier.weight(0.6f),
                            keyboardType = KeyboardType.Number,
                            inputTransformation = InputTransformation.maxLength(15),
                            outputTransformation = CurrencyOutputTransformation(symbol),
                            isError = budgetError
                        )

                        Card(
                            modifier = Modifier
                                .height(56.dp)
                                .padding(start = 28.dp),
                            colors = CardColors(
                                containerColor = Secondary80,
                                contentColor = Tertiary10,
                                disabledContainerColor = Secondary80,
                                disabledContentColor = Tertiary10
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.CenterHorizontally),
                                text = countryCurrency,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Tertiary10
                            )
                        }
                    }
                }
            }

            ViaryButton(
                label = stringResource(R.string.create_screen_start_viary_button),
                icon = Icons.Outlined.RocketLaunch,
                onClicked = {
                    onStartTripClicked()
                },
                isLoading = isLoading
            )
        }
    }
}

@Composable
fun CreateScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        ShimmerEffect(
            modifier = Modifier
                .width(150.dp)
                .height(28.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        )
    }
}

@Preview(showBackground = true, name = "Português", locale = "pt")
@Preview(showBackground = true, name = "Español", locale = "es")
@Preview(showBackground = true, name = "Français", locale = "fr")
@Preview(showBackground = true)
@Composable
fun CreateScreenPreview() {
    CreateScreenView(modifier = Modifier.background(Tertiary90.copy(0.3f)), isLoading = false)
}