package com.trian0.viary.ui.historical

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trian0.viary.R
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.ui.components.CompletedViaryList
import com.trian0.viary.ui.home.HomeScreenSkeleton
import com.trian0.viary.ui.theme.Primary50
import org.koin.androidx.compose.koinViewModel
import java.util.Date

@Composable
fun HistoricalScreen(
    viewModel: HistoricalViewModel = koinViewModel(),
    onViaryClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    HistoricalScreenView(
        completedViary = uiState.completedViary,
        lastCheckpoints = uiState.lastCheckpoints,
        isLoading = uiState.isLoading,
        onViaryClick = onViaryClick,
    )
}

@Composable
fun HistoricalScreenView(
    completedViary: List<Viary> = emptyList(),
    lastCheckpoints: Map<String, Checkpoint?> = emptyMap(),
    isLoading: Boolean = true,
    onViaryClick: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.historical_screen_title),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Primary50
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.historical_screen_subtitle),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )

        when {
            isLoading -> HomeScreenSkeleton()

            completedViary.isEmpty() -> Column(
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = stringResource(R.string.historical_screen_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            else -> CompletedViaryList(
                viaryList = completedViary,
                modifier = Modifier.padding(top = 40.dp),
                lastCheckpoints = lastCheckpoints,
                onViaryClick = onViaryClick,
            )
        }
    }
}

@Preview(showBackground = true, locale = "pt")
@Preview(showBackground = true, locale = "es")
@Preview(showBackground = true, locale = "fr")
@Preview(showBackground = true)
@Composable
fun HistoricalScreenWithViaryPreview() {
    HistoricalScreenView(
        completedViary = listOf(
            Viary(
                name = "Rota do Sol",
                origin = "Salvador",
                departureTime = Date(),
                initialBudget = 0.0,
                kmEnd = 120f,
                status = Viary.ViaryStatus.COMPLETED,
                climate = "SUNNY",
                selectedImage = null
            )
        ),
        isLoading = false,
    )
}

@Preview(showBackground = true, locale = "pt")
@Preview(showBackground = true, locale = "es")
@Preview(showBackground = true, locale = "fr")
@Preview(showBackground = true)
@Composable
fun HistoricalScreenEmptyPreview() {
    HistoricalScreenView(
        completedViary = emptyList(),
        isLoading = false,
    )
}
