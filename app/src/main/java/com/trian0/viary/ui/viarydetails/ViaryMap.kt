package com.trian0.viary.ui.viarydetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.OnCircleAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.trian0.viary.R
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.GeoPoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.ui.theme.MapDestinationColor
import com.trian0.viary.ui.theme.MapOriginColor
import com.trian0.viary.ui.theme.Primary50

private const val MAP_STYLE = Style.MAPBOX_STREETS
private const val SINGLE_POINT_ZOOM = 13.0
private const val EMPTY_MAP_ZOOM = 5.0
private const val MAP_PADDING = 80.0
private const val MARKER_RADIUS = 10.0
private const val ROUTE_WIDTH = 4.0

private enum class MarkerType(val color: Color) {
    ORIGIN(MapOriginColor),
    CHECKPOINT(Primary50),
    DESTINATION(MapDestinationColor),
}

private data class MapMarker(val point: Point, val name: String, val type: MarkerType)

private data class MarkerSelection(val marker: MapMarker, val anchorPx: Offset)

private fun GeoPoint.toPoint(): Point = Point.fromLngLat(longitude, latitude)

@OptIn(MapboxDelicateApi::class)
@Composable
fun ViaryMap(viary: Viary, checkpoints: List<Checkpoint>, route: List<GeoPoint>) {
    val context = LocalContext.current
    val originName = viary.origin.ifEmpty {
        stringResource(R.string.viary_details_screen_departure_point)
    }
    val destinationName = stringResource(R.string.viary_details_screen_destination_default)

    val mapMarkers = remember(viary.id, checkpoints.size, originName, destinationName) {
        buildList {
            GeoPoint(viary.latitudeOrigin, viary.longitudeOrigin)
                .takeIf { it.isSet }
                ?.let { add(MapMarker(it.toPoint(), originName, MarkerType.ORIGIN)) }
            checkpoints.forEach { cp ->
                GeoPoint(cp.latitude, cp.longitude)
                    .takeIf { it.isSet }
                    ?.let { add(MapMarker(it.toPoint(), cp.placeName, MarkerType.CHECKPOINT)) }
            }
            GeoPoint(viary.latitudeArrival, viary.longitudeArrival)
                .takeIf { it.isSet }
                ?.let { add(MapMarker(it.toPoint(), destinationName, MarkerType.DESTINATION)) }
        }
    }
    val currentMarkers by rememberUpdatedState(mapMarkers)

    val mapView = remember {
        MapView(context, MapInitOptions(context, styleUri = MAP_STYLE, textureView = true)).apply {
            gestures.updateSettings {
                scrollEnabled = false
                pinchToZoomEnabled = false
                rotateEnabled = false
                pitchEnabled = false
                doubleTapToZoomInEnabled = false
                doubleTouchToZoomOutEnabled = false
                quickZoomEnabled = false
            }
        }
    }

    val routeLine = remember(mapMarkers, route) {
        if (route.isNotEmpty()) route.map { it.toPoint() } else mapMarkers.map { it.point }
    }

    val routeManager = remember { mutableStateOf<PolylineAnnotationManager?>(null) }
    val markersManager = remember { mutableStateOf<CircleAnnotationManager?>(null) }
    val selection = remember { mutableStateOf<MarkerSelection?>(null) }

    DisposableEffect(mapView) {
        val dismissOnMapClick = OnMapClickListener {
            selection.value = null
            false
        }
        mapView.gestures.addOnMapClickListener(dismissOnMapClick)
        onDispose { mapView.gestures.removeOnMapClickListener(dismissOnMapClick) }
    }

    LaunchedEffect(mapMarkers) { selection.value = null }

    LaunchedEffect(mapMarkers, routeLine) {
        mapView.mapboxMap.getStyle {
            val line = routeManager.value ?: mapView.annotations
                .createPolylineAnnotationManager()
                .also { created ->
                    created.lineCap = LineCap.ROUND
                    created.lineJoin = LineJoin.ROUND
                    routeManager.value = created
                }
            line.deleteAll()
            if (routeLine.size >= 2) {
                line.create(
                    PolylineAnnotationOptions()
                        .withPoints(routeLine)
                        .withLineColor(Primary50.toArgb())
                        .withLineWidth(ROUTE_WIDTH)
                )
            }
            val manager = markersManager.value ?: mapView.annotations
                .createCircleAnnotationManager()
                .also { created ->
                    created.addClickListener(OnCircleAnnotationClickListener { tapped ->
                        val marker = currentMarkers.firstOrNull { it.point == tapped.point }
                            ?: return@OnCircleAnnotationClickListener false
                        val pixel = mapView.mapboxMap.pixelForCoordinate(marker.point)
                        selection.value = MarkerSelection(
                            marker,
                            Offset(pixel.x.toFloat(), pixel.y.toFloat())
                        )
                        true
                    })
                    markersManager.value = created
                }
            manager.deleteAll()
            mapMarkers.forEach { marker ->
                manager.create(
                    CircleAnnotationOptions()
                        .withPoint(marker.point)
                        .withCircleRadius(MARKER_RADIUS)
                        .withCircleColor(marker.type.color.toArgb())
                        .withCircleStrokeWidth(2.0)
                        .withCircleStrokeColor(Color.White.toArgb())
                )
            }
            val points = routeLine.ifEmpty { mapMarkers.map { it.point } }
            val camera = when {
                points.size >= 2 -> mapView.mapboxMap.cameraForCoordinates(
                    points,
                    CameraOptions.Builder().build(),
                    EdgeInsets(MAP_PADDING, MAP_PADDING, MAP_PADDING, MAP_PADDING),
                    null,
                    null
                )

                points.size == 1 -> CameraOptions.Builder()
                    .center(points[0])
                    .zoom(SINGLE_POINT_ZOOM)
                    .build()

                else -> CameraOptions.Builder().zoom(EMPTY_MAP_ZOOM).build()
            }
            mapView.camera.easeTo(camera)
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
        selection.value?.let { MarkerBalloon(it) }
    }
}

@Composable
private fun MarkerBalloon(selection: MarkerSelection) {
    val gapPx = with(LocalDensity.current) {
        (MARKER_RADIUS.dp + 6.dp).roundToPx()
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            val x = (selection.anchorPx.x - placeable.width / 2f)
                .coerceIn(0f, (constraints.maxWidth - placeable.width).coerceAtLeast(0).toFloat())
            val y = (selection.anchorPx.y - placeable.height - gapPx)
                .coerceAtLeast(0f)
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(x.toInt(), y.toInt())
            }
        }
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 200.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(selection.marker.type.color, CircleShape)
            )
            Text(
                text = selection.marker.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
