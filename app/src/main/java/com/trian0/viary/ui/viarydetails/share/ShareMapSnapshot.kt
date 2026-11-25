package com.trian0.viary.ui.viarydetails.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapSnapshotOptions
import com.mapbox.maps.SnapshotOverlay
import com.mapbox.maps.SnapshotOverlayOptions
import com.mapbox.maps.Size
import com.mapbox.maps.Snapshotter
import com.mapbox.maps.Style
import com.trian0.viary.data.models.GeoPoint
import com.trian0.viary.ui.theme.MapDestinationColor
import com.trian0.viary.ui.theme.MapOriginColor
import com.trian0.viary.ui.theme.Primary50
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "ShareMap"
private const val SINGLE_POINT_ZOOM = 13.0
private const val MAP_PADDING_DP = 36.0
private const val ROUTE_WIDTH_DP = 4.5f
private const val ROUTE_CASING_DP = 8f
private const val ENDPOINT_RADIUS_DP = 8f
private const val CHECKPOINT_RADIUS_DP = 10f
private const val MARKER_STROKE_DP = 2.5f
private const val NUMBER_TEXT_DP = 10f

// Atribuição desenhada por nós para controlar tamanho e posição. O crédito é exigido pelas licenças.
private const val ATTRIBUTION = "© Mapbox © OpenStreetMap"
private const val ATTRIBUTION_TEXT_DP = 8f
private const val ATTRIBUTION_MARGIN_DP = 8f
private const val ATTRIBUTION_PADDING_H_DP = 5f
private const val ATTRIBUTION_PADDING_V_DP = 2f

/** Pontos a desenhar no mapa do card. Quem monta já filtra os que não têm coordenada. */
data class ShareMapData(
    val origin: GeoPoint?,
    val checkpoints: List<GeoPoint>,
    val destination: GeoPoint?,
    val route: List<GeoPoint>,
) {
    /** Mesmo critério do ViaryMap: sem rota do Directions, liga os marcadores em linha reta. */
    val line: List<GeoPoint>
        get() = route.ifEmpty { listOfNotNull(origin) + checkpoints + listOfNotNull(destination) }
}

private fun GeoPoint.toPoint(): Point = Point.fromLngLat(longitude, latitude)

/**
 * Renderiza o mapa estático do card com a rota e os marcadores desenhados por cima.
 * Precisa rodar na main thread. Retorna null se não houver o que desenhar ou se o snapshot falhar.
 */
suspend fun renderShareMap(context: Context, data: ShareMapData, widthDp: Int, heightDp: Int): Bitmap? {
    val points = data.line.map { it.toPoint() }
    if (points.isEmpty()) return null

    return suspendCancellableCoroutine { continuation ->
        // No Android o size do snapshot é em pixels de tela, não em dp; o pixelRatio só escala o estilo
        val options = MapSnapshotOptions.Builder()
            .size(Size(widthDp * SHARE_EXPORT_SCALE, heightDp * SHARE_EXPORT_SCALE))
            .pixelRatio(SHARE_EXPORT_SCALE)
            .build()
        val overlayOptions = SnapshotOverlayOptions(showLogo = false, showAttributes = false)
        val snapshotter = Snapshotter(context, options, overlayOptions)
        var destroyed = false
        fun release() {
            if (!destroyed) {
                destroyed = true
                snapshotter.destroy()
            }
        }
        continuation.invokeOnCancellation { release() }

        try {
            snapshotter.setStyleUri(Style.MAPBOX_STREETS)
            val camera = snapshotter.cameraFor(points)
            snapshotter.setCamera(camera)
            snapshotter.start(
                overlayCallback = { overlay -> drawOverlay(overlay, data) },
                resultCallback = { bitmap, error ->
                    if (bitmap == null) Log.e(TAG, "Snapshot falhou: $error")
                    release()
                    if (continuation.isActive) continuation.resume(bitmap)
                },
            )
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e(TAG, "Erro ao gerar o mapa do card", e)
            release()
            if (continuation.isActive) continuation.resume(null)
        }
    }
}

private fun Snapshotter.cameraFor(points: List<Point>): CameraOptions {
    if (points.distinct().size < 2) {
        return CameraOptions.Builder().center(points.first()).zoom(SINGLE_POINT_ZOOM).build()
    }
    val padding = MAP_PADDING_DP * SHARE_EXPORT_SCALE
    return cameraForCoordinates(points, EdgeInsets(padding, padding, padding, padding), null, null)
}

private fun drawOverlay(overlay: SnapshotOverlay, data: ShareMapData) {
    val canvas = overlay.canvas
    val dp = SHARE_EXPORT_SCALE

    // screenCoordinate devolve pixels de tela, os mesmos do bitmap
    fun screenOf(point: GeoPoint): Pair<Float, Float> {
        val screen = overlay.screenCoordinate(point.toPoint())
        return screen.x.toFloat() to screen.y.toFloat()
    }

    val line = data.line
    if (line.size >= 2) {
        val path = Path()
        line.forEachIndexed { index, point ->
            val (x, y) = screenOf(point)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint(android.graphics.Color.WHITE, ROUTE_CASING_DP * dp))
        canvas.drawPath(path, linePaint(Primary50.toArgb(), ROUTE_WIDTH_DP * dp))
    }

    data.origin?.let { canvas.drawMarker(screenOf(it), ENDPOINT_RADIUS_DP * dp, MapOriginColor.toArgb(), dp) }
    data.destination?.let { canvas.drawMarker(screenOf(it), ENDPOINT_RADIUS_DP * dp, MapDestinationColor.toArgb(), dp) }
    data.checkpoints.forEachIndexed { index, checkpoint ->
        val position = screenOf(checkpoint)
        canvas.drawMarker(position, CHECKPOINT_RADIUS_DP * dp, Primary50.toArgb(), dp)
        canvas.drawNumber(position, (index + 1).toString(), dp)
    }
    canvas.drawAttribution(dp)
}

private fun linePaint(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
    this.color = color
    strokeWidth = width
}

private fun Canvas.drawMarker(position: Pair<Float, Float>, radius: Float, color: Int, dp: Float) {
    val (x, y) = position
    drawCircle(x, y, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.WHITE })
    drawCircle(
        x, y, radius - MARKER_STROKE_DP * dp,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color },
    )
}

private fun Canvas.drawNumber(position: Pair<Float, Float>, text: String, dp: Float) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        textSize = NUMBER_TEXT_DP * dp
    }
    val (x, y) = position
    val baseline = y - (paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f
    drawText(text, x, baseline, paint)
}

private fun Canvas.drawAttribution(dp: Float) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(200, 40, 40, 40)
        textSize = ATTRIBUTION_TEXT_DP * dp
    }
    val metrics = paint.fontMetrics
    val paddingH = ATTRIBUTION_PADDING_H_DP * dp
    val paddingV = ATTRIBUTION_PADDING_V_DP * dp
    val right = width - ATTRIBUTION_MARGIN_DP * dp
    val bottom = height - ATTRIBUTION_MARGIN_DP * dp
    val pill = RectF(
        right - paint.measureText(ATTRIBUTION) - 2 * paddingH,
        bottom - (metrics.descent - metrics.ascent) - 2 * paddingV,
        right,
        bottom,
    )
    val radius = pill.height() / 2f
    drawRoundRect(
        pill, radius, radius,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(190, 255, 255, 255) },
    )
    drawText(ATTRIBUTION, pill.left + paddingH, pill.bottom - paddingV - metrics.descent, paint)
}
