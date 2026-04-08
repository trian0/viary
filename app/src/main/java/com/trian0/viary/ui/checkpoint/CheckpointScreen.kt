package com.trian0.viary.ui.checkpoint

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trian0.viary.R
import com.trian0.viary.ui.components.ElevatedOutlinedTextField
import com.trian0.viary.ui.components.ImagePicker
import com.trian0.viary.ui.theme.Neutral10
import com.trian0.viary.ui.theme.Primary10
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Secondary20
import com.trian0.viary.ui.theme.Secondary30
import com.trian0.viary.ui.theme.Secondary90
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CheckpointScreen(
    viewModel: CheckpointViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    val checkpointName = rememberTextFieldState()

    LaunchedEffect(checkpointName.text) {

    }

    CheckpointScreenView(
        modifier = Modifier,
        onNavigateBack,
        viaryName = uiState.viaryName,
        onCoverImageSelected = {
            viewModel.onIntent(CheckpointContract.CheckpointIntent.OnCoverImageSelected(it))
        },
        checkpointName,
        uiState.checkpointNameError,
    )
}

@Composable
fun CheckpointScreenView(
    modifier: Modifier,
    onNavigateBack: () -> Unit = {},
    viaryName: String,
    onCoverImageSelected: (Uri) -> Unit = {},
    checkpointName: TextFieldState = rememberTextFieldState(),
    checkpointNameError: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
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

            Row(modifier = Modifier.padding(top = 32.dp).fillMaxWidth()) {
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

                        val dateFormat = SimpleDateFormat(stringResource(R.string.date_format_hh_mm),
                            Locale.getDefault())

                        Row(modifier = Modifier.padding(top = 36.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = Secondary20,
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
                            text = stringResource(R.string.checkpoint_screen_checkpoint_current_time_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Secondary30,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val dateFormat = SimpleDateFormat(stringResource(R.string.date_format_hh_mm),
                            Locale.getDefault())

                        Row(modifier = Modifier.padding(top = 36.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = Secondary20,
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
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckpointScreenPreview() {
    CheckpointScreenView(Modifier, viaryName = "Vumbora")
}