package com.trian0.viary.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trian0.viary.R
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.utils.elapsedTime
import com.trian0.viary.ui.components.ShimmerEffect
import com.trian0.viary.ui.components.ViaryButton
import com.trian0.viary.ui.components.ViarySecondaryButton
import com.trian0.viary.ui.theme.Primary10
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Primary50
import com.trian0.viary.ui.theme.Secondary90
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.util.Date

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.init()
    }

    HomeScreenView(
        uiState.viaryInProgress,
        uiState.totalViary,
        uiState.isLoading
    )
}

@Composable
fun HomeScreenView(
    viaryInProgress: Viary?,
    totalViary: Int = 0,
    isLoading: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 30.dp)
        ) {
            if (isLoading) {
                HomeScreenSkeleton()
            } else {
                viaryInProgress?.let { viary ->
                    InProgressViary(
                        viaryName = viary.name,
                        viaryDepartureTime = viary.departureTime ?: Date()
                    )
                } ?: HomeTitle(totalViary)
            }
        }
    }
}

@Composable
fun HomeTitle(
    totalViary: Int
) {
    Text(
        text = stringResource(R.string.home_screen_title),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Primary50
    )

    Text(
        modifier = Modifier.padding(top = 8.dp),
        text = stringResource(R.string.home_screen_subtitle),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.ExtraBold
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalArrangement = Arrangement.SpaceAround
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
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.home_screen_total_viary_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary10,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = totalViary.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.home_screen_greater_distance_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary10,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "421",
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun InProgressViary(
    viaryName: String,
    viaryDepartureTime: Date = Date(),
) {
    var elapsed by remember { mutableStateOf(viaryDepartureTime.elapsedTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            elapsed = viaryDepartureTime.elapsedTime()
            delay(1000L)
        }
    }

    Text(
        text = stringResource(R.string.home_screen_viary_in_progress_title),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Primary50
    )

    Text(
        modifier = Modifier.padding(top = 8.dp),
        text = viaryName,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.ExtraBold
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
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
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.home_screen_elapsed_time_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary10,
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp, end = 25.dp),
                    text = elapsed,
                    style = MaterialTheme.typography.headlineMedium,
                )
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
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.home_screen_distance_traveled_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary10,
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "128.4",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
    }

    ViaryButton(
        label = stringResource(R.string.home_screen_add_checkpoint_button),
        icon = Icons.Filled.AddCircle
    )

    ViarySecondaryButton(
        label = stringResource(R.string.home_screen_end_viary_button),
        icon = Icons.Filled.OutlinedFlag,
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
fun HomeScreenSkeleton() {
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
    }
}

@Preview(showBackground = true, locale = "pt")
@Preview(showBackground = true, locale = "es")
@Preview(showBackground = true, locale = "fr")
@Preview(showBackground = true)
@Composable
fun HomeScreenWithViaryPreview() {
    HomeScreenView(
        Viary(
            name = "Rota do Sol",
            origin = "Salvador",
            departureTime = Date(),
            kmStart = 0.0,
            kmEnd = 0.0,
            status = Viary.ViaryStatus.IN_PROGRESS,
            climate = "CLOUDY",
            selectedImage = null
        )
    )
}

@Preview(showBackground = true, locale = "pt")
@Preview(showBackground = true, locale = "es")
@Preview(showBackground = true, locale = "fr")
@Preview(showBackground = true)
@Composable
fun HomeScreenWithoutViaryPreview() {
    HomeScreenView(
        null
    )
}