package com.trian0.viary.ui.historical

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.trian0.viary.R
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.ui.components.CompletedViaryItem
import com.trian0.viary.ui.home.HomeScreenSkeleton
import com.trian0.viary.ui.theme.Primary50
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import java.util.Date

@Composable
fun HistoricalScreen(
    viewModel: HistoricalViewModel = koinViewModel(),
    onViaryClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyViary = viewModel.completedPaged.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    HistoricalScreenView(
        lazyViary = lazyViary,
        lastCheckpoints = uiState.lastCheckpoints,
        onViaryClick = onViaryClick,
    )
}

@Composable
fun HistoricalScreenView(
    lazyViary: LazyPagingItems<Viary>,
    lastCheckpoints: Map<String, Checkpoint?> = emptyMap(),
    onViaryClick: (String) -> Unit = {},
) {
    val isRefreshing = lazyViary.loadState.refresh is LoadState.Loading
    val isEmpty = lazyViary.itemCount == 0 && !isRefreshing

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                modifier = Modifier.padding(top = 30.dp),
                text = stringResource(R.string.historical_screen_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Primary50
            )
            Text(
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp),
                text = stringResource(R.string.historical_screen_subtitle),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }

        when {
            isRefreshing -> item { HomeScreenSkeleton() }

            isEmpty -> item {
                Text(
                    text = stringResource(R.string.historical_screen_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            else -> {
                items(count = lazyViary.itemCount) { index ->
                    val viary = lazyViary[index] ?: return@items
                    val isLast = index == lazyViary.itemCount - 1 &&
                            lazyViary.loadState.append.endOfPaginationReached
                    CompletedViaryItem(
                        viary = viary,
                        lastCheckpointName = lastCheckpoints[viary.id]?.placeName ?: "",
                        isLast = isLast,
                        onClick = { onViaryClick(viary.id) }
                    )
                }

                if (lazyViary.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .wrapContentSize(Alignment.Center)
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "pt")
@Preview(showBackground = true, locale = "es")
@Preview(showBackground = true, locale = "fr")
@Preview(showBackground = true)
@Composable
fun HistoricalScreenWithViaryPreview() {
    val pagingData = flowOf(
        PagingData.from(
            listOf(
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
            )
        )
    ).collectAsLazyPagingItems()

    HistoricalScreenView(lazyViary = pagingData)
}

@Preview(showBackground = true, locale = "pt")
@Preview(showBackground = true, locale = "es")
@Preview(showBackground = true, locale = "fr")
@Preview(showBackground = true)
@Composable
fun HistoricalScreenEmptyPreview() {
    val pagingData = flowOf(PagingData.empty<Viary>()).collectAsLazyPagingItems()
    HistoricalScreenView(lazyViary = pagingData)
}
