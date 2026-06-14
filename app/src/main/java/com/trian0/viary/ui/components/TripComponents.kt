package com.trian0.viary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trian0.viary.R
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.ui.theme.Neutral90
import com.trian0.viary.ui.theme.Primary10
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Primary30
import com.trian0.viary.ui.theme.Secondary80
import com.trian0.viary.ui.theme.Secondary90
import java.text.SimpleDateFormat
import java.util.Locale

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
fun CompletedViaryList(
    modifier: Modifier = Modifier,
    viaryList: List<Viary>,
    lastCheckpoints: Map<String, Checkpoint?> = emptyMap(),
    onViaryClick: (String) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        viaryList.forEachIndexed { index, viary ->
            CompletedViaryItem(
                viary = viary,
                lastCheckpointName = lastCheckpoints[viary.id]?.placeName ?: "",
                isLast = index == viaryList.lastIndex,
                onClick = { onViaryClick(viary.id) }
            )
        }
    }
}

@Composable
fun CompletedViaryItem(
    viary: Viary,
    lastCheckpointName: String,
    isLast: Boolean = false,
    onClick: () -> Unit = {},
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Row(modifier = Modifier.fillMaxWidth()) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 16.dp, top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Primary30)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(300.dp)
                        .background(Secondary80)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            onClick = onClick,
            colors = CardColors(
                containerColor = Secondary90,
                contentColor = Primary20,
                disabledContainerColor = Secondary90,
                disabledContentColor = Primary20
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                viary.departureTime?.let {
                    Text(
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                        text = dateFormat.format(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary30,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (viary.selectedImage != null) {
                    AsyncImage(
                        model = viary.selectedImage,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Secondary80)
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = viary.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary10
                    )

                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "${viary.origin} → $lastCheckpointName",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary20
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = Primary20.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.home_screen_distance_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary20
                            )
                            Text(
                                text = "${String.format("%.1f", viary.kmEnd)} km",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary10
                            )
                        }

                        Card(
                            colors = CardColors(
                                containerColor = Secondary80,
                                contentColor = Primary10,
                                disabledContainerColor = Secondary80,
                                disabledContentColor = Primary10
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                text = stringResource(R.string.home_screen_see_viary_button),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
