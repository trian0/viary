package com.trian0.viary.ui.checkpoint

import android.net.Uri
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trian0.viary.MainViewModel
import com.trian0.viary.R
import com.trian0.viary.data.utils.CurrencyOutputTransformation
import com.trian0.viary.ui.components.CapturedMomentsRow
import com.trian0.viary.ui.components.ElevatedOutlinedTextField
import com.trian0.viary.ui.components.ErrorDialog
import com.trian0.viary.ui.components.ImagePicker
import com.trian0.viary.ui.components.ShimmerEffect
import com.trian0.viary.ui.components.SuccessDialog
import com.trian0.viary.ui.components.ViaryButton
import com.trian0.viary.ui.create.CreateContract
import com.trian0.viary.ui.theme.BudgetExceeded
import com.trian0.viary.ui.theme.BudgetPositive
import com.trian0.viary.ui.theme.Neutral10
import com.trian0.viary.ui.theme.Primary10
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Primary30
import com.trian0.viary.ui.theme.Secondary30
import com.trian0.viary.ui.theme.Secondary80
import com.trian0.viary.ui.theme.Secondary90
import com.trian0.viary.ui.theme.Tertiary10
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CheckpointScreen(
    viewModel: CheckpointViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val country by mainViewModel.country.collectAsStateWithLifecycle()

    if (country.loading) {
        CheckpointScreenSkeleton()
        return
    }

    val uiState by viewModel.uiState.collectAsState()

    val initial = viewModel.currentState
    val checkpointName = rememberTextFieldState(initialText = initial.checkpointName)
    val checkpointBudget = rememberTextFieldState(initialText = initial.checkpointBudget)

    LaunchedEffect(checkpointName.text) {
        viewModel.onIntent(
            CheckpointContract.CheckpointIntent.OnCheckpointNameChanged(
                checkpointName.text.toString()
            )
        )
    }

    LaunchedEffect(checkpointBudget.text) {
        viewModel.onIntent(
            CheckpointContract.CheckpointIntent.OnCheckpointBudgetChanged(
                checkpointBudget.text.toString()
            )
        )
    }

    if (uiState.showSuccessDialog) {
        SuccessDialog(
            labelTitle = stringResource(R.string.checkpoint_screen_checkpoint_success_dialog_title),
            labelSubtitle = stringResource(R.string.checkpoint_screen_checkpoint_success_dialog_message),
            labelConfirm = stringResource(R.string.checkpoint_screen_checkpoint_success_dialog_button),
            onConfirm = onNavigateBack
        )
    }

    if (uiState.showErrorDialog) {
        ErrorDialog(
            labelSubtitle = R.string.checkpoint_screen_checkpoint_error_dialog_message,
            onDismiss = {
                viewModel.onIntent(CheckpointContract.CheckpointIntent.OnDismissErrorDialog)
            }
        )
    }

    CheckpointScreenView(
        modifier = Modifier,
        onNavigateBack,
        viaryName = uiState.viaryName,
        currentCoverImagePath = uiState.checkpointCoverPath,
        onCoverImageSelected = {
            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnCoverImageSelected(it))
        },
        checkpointName,
        uiState.checkpointNameError,
        country.symbol,
        country.currency,
        checkpointBudget,
        uiState.checkpointBudgetError,
        initialBudget = uiState.initialBudget,
        uiState.previewAccumulated,
        uiState.previewRemaining,
        uiState.capturedImages,
        onImageAdded = {
            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnImageAdded(it))
        },
        onImageRemoved = {
            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnImageRemoved(it))
        },
        onSaveCheckpointClicked = {
            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnSaveCheckpointClicked)
        },
        isLoading = uiState.isLoading
    )
}

@Composable
fun CheckpointScreenView(
    modifier: Modifier,
    onNavigateBack: () -> Unit = {},
    viaryName: String,
    currentCoverImagePath: String? = null,
    onCoverImageSelected: (Uri) -> Unit = {},
    checkpointName: TextFieldState = rememberTextFieldState(),
    checkpointNameError: Boolean = false,
    symbol: String = "",
    currency: String = "",
    checkpointBudget: TextFieldState = rememberTextFieldState(),
    checkpointBudgetError: Boolean = false,
    initialBudget: Double = 0.0,
    previewAccumulated: Double = 0.0,
    previewRemaining: Double = 0.0,
    capturedImages: List<Uri> = emptyList(),
    onImageAdded: (Uri) -> Unit = {},
    onImageRemoved: (Uri) -> Unit = {},
    onSaveCheckpointClicked: () -> Unit = {},
    isLoading: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
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

            Text(
                modifier = Modifier.padding(top = 30.dp),
                text = viaryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            ImagePicker(
                modifier = Modifier.padding(top = 40.dp),
                label = stringResource(R.string.checkpoint_screen_image_picker_title),
                imageSelectedTitle = stringResource(R.string.checkpoint_screen_image_picker_title),
                imageSelectedSubtitle = stringResource(R.string.create_screen_image_picker_selected_subtitle),
                currentImagePath = currentCoverImagePath,
                onImageSelected = {
                    onCoverImageSelected(it)
                }
            )

            Text(
                modifier = Modifier.padding(top = 40.dp),
                text = stringResource(R.string.checkpoint_screen_checkpoint_name_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Primary10
            )

            ElevatedOutlinedTextField(
                state = checkpointName,
                modifier = Modifier.padding(top = 10.dp),
                icon = Icons.Outlined.LocationOn,
                label = stringResource(R.string.checkpoint_screen_checkpoint_name_placeholder),
                isError = checkpointNameError
            )

            Row(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardColors(
                        containerColor = Secondary90,
                        contentColor = Primary20,
                        disabledContainerColor = Secondary90,
                        disabledContentColor = Primary20
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.checkpoint_screen_checkpoint_current_time_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Secondary30,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val dateFormat = SimpleDateFormat(
                            stringResource(R.string.date_format_hh_mm),
                            Locale.getDefault()
                        )

                        Row(
                            modifier = Modifier.padding(top = 36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = Primary30,
                            )

                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = dateFormat.format(Date()),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Neutral10,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f))

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardColors(
                        containerColor = Secondary90,
                        contentColor = Primary20,
                        disabledContainerColor = Secondary90,
                        disabledContentColor = Primary20
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.checkpoint_screen_checkpoint_expenses_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Secondary30,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            modifier = Modifier.padding(top = 36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = symbol.ifEmpty { "R$" },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Primary30,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = previewAccumulated.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Neutral10,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

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
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.checkpoint_screen_checkpoint_stop_expenses_label),
                        style = MaterialTheme.typography.labelMedium,
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
                            state = checkpointBudget,
                            modifier = Modifier.weight(0.6f),
                            keyboardType = KeyboardType.Number,
                            inputTransformation = InputTransformation.maxLength(15),
                            outputTransformation = CurrencyOutputTransformation(symbol),
                            isError = checkpointBudgetError
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
                                text = currency,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Tertiary10
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(top = 24.dp),
                        thickness = 1.dp,
                        color = Primary20.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.checkpoint_screen_checkpoint_initial_budget_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            color = Primary10
                        )

                        Text(
                            text = "$symbol $initialBudget",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.checkpoint_screen_checkpoint_remaining_balance_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            color = Primary10
                        )

                        Text(
                            text = "$symbol $previewRemaining",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (previewRemaining < 0) BudgetExceeded else BudgetPositive
                        )
                    }
                }
            }

            CapturedMomentsRow(
                modifier = Modifier.padding(top = 32.dp),
                images = capturedImages,
                onAddImage = { uri ->
                    onImageAdded(uri)
                },
                onRemoveImage = { uri ->
                    onImageRemoved(uri)
                }
            )

            ViaryButton(
                modifier = Modifier.padding(top = 32.dp),
                label = stringResource(R.string.checkpoint_screen_checkpoint_save_checkpoint_button),
                icon = Icons.Filled.AddCircle,
                onClicked = onSaveCheckpointClicked,
                isLoading = isLoading
            )
        }
    }
}

@Composable
fun CheckpointScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
    ) {
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

        Row(modifier = Modifier.fillMaxWidth()) {
            ShimmerEffect(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
            )

            Spacer(modifier = Modifier.weight(0.1f))

            ShimmerEffect(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckpointScreenPreview() {
    CheckpointScreenView(Modifier, viaryName = "Vumbora")
}