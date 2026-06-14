package com.trian0.viary.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import com.trian0.viary.ui.theme.Primary10
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Primary30
import com.trian0.viary.ui.theme.Secondary80
import com.trian0.viary.ui.theme.Secondary90
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CapturedMomentsRow(
    images: List<Uri>,
    onAddImage: (Uri) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onAddImage(it) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "CAPTURED MOMENT",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Primary10
        )

        LazyRow(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(106.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Secondary90)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = null,
                        tint = Primary30,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            items(images) { uri ->
                Box(modifier = Modifier.size(106.dp)) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(106.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onRemoveImage(uri) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CheckpointTimeline(
    checkpoints: List<Checkpoint>,
    symbol: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_screen_checkpoints),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary10
            )
            Text(
                text = stringResource(R.string.home_screen_stops_number, checkpoints.size),
                style = MaterialTheme.typography.labelMedium,
                color = Primary20
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        checkpoints.forEachIndexed { index, checkpoint ->
            Row(modifier = Modifier.fillMaxWidth()) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 6.dp, end = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Primary30)
                    )
                    if (index < checkpoints.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(80.dp)
                                .background(Secondary80)
                        )
                    }
                }

                CheckpointItem(
                    checkpoint = checkpoint,
                    symbol = symbol,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun CheckpointItem(
    checkpoint: Checkpoint,
    symbol: String,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardColors(
            containerColor = Secondary90,
            contentColor = Primary20,
            disabledContainerColor = Secondary90,
            disabledContentColor = Primary20
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (checkpoint.imageUri != null) {
                AsyncImage(
                    model = checkpoint.imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Secondary80)
                )
            }

            Column {
                Text(
                    text = timeFormat.format(checkpoint.time),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary30
                )

                Text(
                    text = checkpoint.placeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary10,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val photoCount = checkpoint.images.size
                    if (photoCount > 0) {
                        MetaTag(
                            icon = Icons.Outlined.CameraAlt,
                            label = stringResource(R.string.home_screen_photos_number, photoCount)
                        )
                    }

                    if (checkpoint.expense > 0.0) {
                        MetaTag(
                            icon = Icons.Outlined.AttachMoney,
                            label = "$symbol ${String.format("%.2f", checkpoint.expense)}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetaTag(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary20,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Primary20,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
