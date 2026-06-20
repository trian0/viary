package com.trian0.viary.ui.viarydetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.trian0.viary.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.ui.theme.Primary10
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Primary30
import com.trian0.viary.ui.theme.Primary50
import com.trian0.viary.ui.theme.Primary70
import com.trian0.viary.ui.theme.Secondary80
import com.trian0.viary.ui.theme.Secondary90
import org.koin.androidx.compose.koinViewModel
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@Composable
fun ViaryDetailsScreen(
    viaryId: String,
    viewModel: ViaryDetailsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viaryId) {
        viewModel.onIntent(ViaryDetailsContract.ViaryDetailsIntent.Load(viaryId))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.viary_details_screen_back_button),
                tint = Primary10,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onNavigateBack() },
            )
            Text(
                modifier = Modifier.padding(start = 9.dp),
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary50)
            }

            uiState.viary != null -> ViaryDetailsContent(state = uiState)

            else -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.viary_details_screen_not_found),
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary20,
                )
            }
        }
    }
}

@Composable
private fun ViaryDetailsContent(state: ViaryDetailsContract.ViaryDetailsUiState) {
    val viary = state.viary ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { HeaderSection(viary, state.checkpoints) }
        item { MapSection(viary, state.checkpoints) }
        item { StatsSection(viary, state.checkpoints, state.durationFormatted) }
        item { CheckpointLogsSection(viary, state.checkpoints) }
        if (state.allPhotos.isNotEmpty()) {
            item { GallerySection(state.allPhotos) }
        }
    }
}

@Composable
private fun HeaderSection(viary: Viary, checkpoints: List<Checkpoint>) {
    val dateFormat = SimpleDateFormat("MMM dd", LocalLocale.current.platformLocale)
    val departure = viary.departureTime?.let { dateFormat.format(it) } ?: ""
    val arrival = checkpoints.lastOrNull()?.time?.let { dateFormat.format(it) } ?: ""
    val dateRange = when {
        departure.isNotEmpty() && arrival.isNotEmpty() && departure != arrival -> "$departure - $arrival"
        departure.isNotEmpty() -> departure
        else -> ""
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(
            text = stringResource(R.string.viary_details_screen_completed_label),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Primary50,
            letterSpacing = 1.5.sp
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = viary.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Primary10
        )
        val originPrefix = stringResource(R.string.viary_details_screen_origin_prefix)
        val subtitle = buildString {
            if (viary.origin.isNotEmpty()) append(originPrefix.format(viary.origin))
            if (dateRange.isNotEmpty()) {
                if (isNotEmpty()) append(" • ")
                append(dateRange)
            }
        }
        if (subtitle.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Primary20
            )
        }
    }
}

private const val MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"

@Composable
private fun MapSection(viary: Viary, checkpoints: List<Checkpoint>) {
    val context = LocalContext.current

    val allPoints = remember(viary.id, checkpoints.size) {
        buildList {
            if (viary.latitudeOrigin != 0.0 || viary.longitudeOrigin != 0.0)
                add(LatLng(viary.latitudeOrigin, viary.longitudeOrigin))
            checkpoints.forEach { cp ->
                if (cp.latitude != 0.0 || cp.longitude != 0.0)
                    add(LatLng(cp.latitude, cp.longitude))
            }
            if (viary.latitudeArrival != 0.0 || viary.longitudeArrival != 0.0)
                add(LatLng(viary.latitudeArrival, viary.longitudeArrival))
        }
    }

    val markerTitles = remember(viary.id, checkpoints.size) {
        buildList {
            add(viary.origin.ifEmpty { context.getString(R.string.viary_details_screen_origin_default) })
            checkpoints.forEach { add(it.placeName) }
            add(context.getString(R.string.viary_details_screen_destination_default))
        }
    }

    val mapView = remember {
        MapView(context, MapLibreMapOptions.createFromAttributes(context).textureMode(true))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        mapView.onCreate(null)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(allPoints) {
        mapView.getMapAsync { map ->
            map.uiSettings.isScrollGesturesEnabled = false
            map.uiSettings.isZoomGesturesEnabled = false
            map.uiSettings.isRotateGesturesEnabled = false
            map.uiSettings.isTiltGesturesEnabled = false
            map.uiSettings.isDoubleTapGesturesEnabled = false
            map.uiSettings.isQuickZoomGesturesEnabled = false
            map.setStyle(Style.Builder().fromUri(MAP_STYLE)) {
                allPoints.forEachIndexed { i, point ->
                    map.addMarker(
                        MarkerOptions()
                            .position(point)
                            .title(markerTitles.getOrElse(i) { context.getString(R.string.viary_details_screen_point_fallback, i) })
                    )
                }
                when {
                    allPoints.size >= 2 -> {
                        val bounds = LatLngBounds.Builder()
                            .includes(allPoints)
                            .build()
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
                    }

                    allPoints.size == 1 ->
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(allPoints[0], 13.0))

                    else ->
                        map.animateCamera(CameraUpdateFactory.zoomTo(5.0))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun StatsSection(
    viary: Viary,
    checkpoints: List<Checkpoint>,
    durationFormatted: String,
) {
    val budgetSpent = checkpoints.sumOf { it.expense }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Outlined.LocationOn,
            label = stringResource(R.string.viary_details_screen_total_distance),
            value = "${String.format("%.1f", viary.kmEnd)} km"
        )
        StatCard(
            icon = Icons.Outlined.AttachMoney,
            label = stringResource(R.string.viary_details_screen_budget_spent),
            value = "R$ ${String.format("%.2f", budgetSpent)}"
        )
        StatCard(
            icon = Icons.Outlined.Schedule,
            label = stringResource(R.string.viary_details_screen_trip_duration),
            value = durationFormatted
        )
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Secondary90,
            contentColor = Primary10,
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = Primary50)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Primary20
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Primary10
            )
        }
    }
}

@Composable
private fun CheckpointLogsSection(viary: Viary, checkpoints: List<Checkpoint>) {
    val timeFormat = SimpleDateFormat("hh:mm a", LocalLocale.current.platformLocale)
    val hasArrival = viary.latitudeArrival != 0.0 || viary.longitudeArrival != 0.0
    val totalItems = 1 + checkpoints.size + (if (hasArrival) 1 else 0)

    val departurePoint = stringResource(R.string.viary_details_screen_departure_point)
    val finalDestination = stringResource(R.string.viary_details_screen_destination_default)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.viary_details_screen_checkpoints_log),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Primary10
        )
        Spacer(modifier = Modifier.height(16.dp))

        TimelineRow(isFirst = true, isLast = totalItems == 1) {
            Column {
                Text(
                    text = viary.origin.ifEmpty { departurePoint },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary10
                )
                val timeStr = viary.departureTime?.let { " • ${timeFormat.format(it)}" } ?: ""
                Text(
                    text = "$departurePoint$timeStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary30
                )
            }
        }

        checkpoints.forEachIndexed { index, cp ->
            val isLast = !hasArrival && index == checkpoints.lastIndex
            TimelineRow(isFirst = false, isLast = isLast) {
                Column {
                    Text(
                        text = cp.placeName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary10
                    )
                    val subtitle = buildString {
                        append(timeFormat.format(cp.time))
                        if (cp.expense > 0) append(" • R$ ${String.format("%.2f", cp.expense)}")
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary30
                    )
                }
            }
        }

        if (hasArrival) {
            TimelineRow(isFirst = false, isLast = true) {
                Text(
                    text = finalDestination,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary10
                )
            }
        }
    }
}

@Composable
private fun TimelineRow(
    isFirst: Boolean,
    isLast: Boolean,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(end = 16.dp, top = 2.dp)
                .width(16.dp)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(if (isFirst || isLast) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isFirst || isLast) Primary50 else Primary70)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .weight(1f)
                        .background(Secondary80)
                )
            }
        }
        Box(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp)) {
            content()
        }
    }
}

private fun resolveImageModel(path: String): Any =
    if (path.startsWith("http://") || path.startsWith("https://")) path
    else java.io.File(path)

@Composable
private fun GallerySection(photos: List<String>) {
    val preview = photos.take(5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.viary_details_screen_gallery_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Primary10
        )
        Spacer(modifier = Modifier.height(12.dp))

        val leftPhotos = preview.filterIndexed { i, _ -> i % 2 == 0 }
        val rightPhotos = preview.filterIndexed { i, _ -> i % 2 != 0 }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leftPhotos.forEach { photo ->
                    AsyncImage(
                        model = resolveImageModel(photo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rightPhotos.forEach { photo ->
                    AsyncImage(
                        model = resolveImageModel(photo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    }
}
