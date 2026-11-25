package com.trian0.viary.ui.viarydetails.share

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trian0.viary.R
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.GeoPoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.utils.launchShareChooser
import com.trian0.viary.data.utils.writeShareImage
import com.trian0.viary.ui.theme.Primary50
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.roundToInt

internal fun buildShareMapData(
    viary: Viary,
    checkpoints: List<Checkpoint>,
    route: List<GeoPoint>,
) = ShareMapData(
    origin = GeoPoint(viary.latitudeOrigin, viary.longitudeOrigin).takeIf { it.isSet },
    checkpoints = checkpoints.map { GeoPoint(it.latitude, it.longitude) }.filter { it.isSet },
    destination = GeoPoint(viary.latitudeArrival, viary.longitudeArrival).takeIf { it.isSet },
    route = route,
)

@Composable
fun ShareViaryDialog(
    viary: Viary,
    checkpoints: List<Checkpoint>,
    durationFormatted: String,
    route: List<GeoPoint>,
    currencySymbol: String,
    cover: Any?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportLayer = rememberGraphicsLayer()

    var showExpenses by rememberSaveable { mutableStateOf(true) }
    var showCover by rememberSaveable { mutableStateOf(true) }
    var showPlaceNames by rememberSaveable { mutableStateOf(true) }
    var sharing by remember { mutableStateOf(false) }
    var map by remember { mutableStateOf<ImageBitmap?>(null) }
    var mapLoading by remember { mutableStateOf(true) }

    val mapData = remember(viary, checkpoints, route) { buildShareMapData(viary, checkpoints, route) }
    LaunchedEffect(mapData) {
        mapLoading = true
        map = renderShareMap(context, mapData, SHARE_MAP_WIDTH_DP, SHARE_MAP_HEIGHT_DP)?.asImageBitmap()
        mapLoading = false
    }

    val chooserTitle = stringResource(R.string.share_dialog_title)
    val errorMessage = stringResource(R.string.share_dialog_error)

    fun share() {
        scope.launch {
            sharing = true
            val bitmap = exportLayer.toImageBitmap().asAndroidBitmap()
            val uri = withContext(Dispatchers.IO) { writeShareImage(context, bitmap, viary.name) }
            val launched = uri != null && launchShareChooser(context, uri, chooserTitle)
            sharing = false
            if (!launched) Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                ExportCard(
                    layer = exportLayer,
                    viary = viary,
                    checkpoints = checkpoints,
                    durationFormatted = durationFormatted,
                    currencySymbol = currencySymbol,
                    options = ShareOptions(showExpenses, showCover, showPlaceNames),
                    map = map,
                    cover = cover,
                )
                Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.viary_details_screen_back_button)
                            )
                        }
                        Text(
                            text = chooserTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        val previewWidth = maxWidth - 32.dp
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(shape = RoundedCornerShape(12.dp), shadowElevation = 6.dp) {
                                ViaryShareCard(
                                    viary = viary,
                                    checkpoints = checkpoints,
                                    durationFormatted = durationFormatted,
                                    currencySymbol = currencySymbol,
                                    options = ShareOptions(showExpenses, showCover, showPlaceNames),
                                    map = map,
                                    cover = cover,
                                    modifier = Modifier.fitWidth(previewWidth)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            Text(
                                text = stringResource(R.string.share_dialog_include),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary50
                            )
                            OptionRow(stringResource(R.string.share_option_expenses), showExpenses) { showExpenses = it }
                            OptionRow(stringResource(R.string.share_option_cover), showCover) { showCover = it }
                            OptionRow(stringResource(R.string.share_option_place_names), showPlaceNames) {
                                showPlaceNames = it
                            }
                            Button(
                                onClick = ::share,
                                enabled = !mapLoading && !sharing,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary50,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                            ) {
                                if (mapLoading || sharing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                                }
                                Text(
                                    modifier = Modifier.padding(start = 8.dp),
                                    text = stringResource(R.string.share_dialog_share_button),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedTrackColor = Primary50, checkedThumbColor = Color.White)
        )
    }
}

/**
 * Card na escala de exportação. Só grava no [layer], sem aparecer na tela nem ocupar espaço, e como a
 * densidade é fixa a imagem sai com o mesmo tamanho e nitidez em qualquer aparelho.
 */
@Composable
internal fun ExportCard(
    layer: GraphicsLayer,
    viary: Viary,
    checkpoints: List<Checkpoint>,
    durationFormatted: String,
    currencySymbol: String,
    options: ShareOptions,
    map: ImageBitmap?,
    cover: Any?,
) {
    CompositionLocalProvider(LocalDensity provides Density(SHARE_EXPORT_SCALE)) {
        ViaryShareCard(
            viary = viary,
            checkpoints = checkpoints,
            durationFormatted = durationFormatted,
            currencySymbol = currencySymbol,
            options = options,
            map = map,
            cover = cover,
            modifier = Modifier
                .layout { measurable, _ ->
                    val placeable = measurable.measure(Constraints())
                    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                }
                .drawWithContent { layer.record { this@drawWithContent.drawContent() } }
        )
    }
}

/** Mede o conteúdo no tamanho real e só o encolhe na tela, para a captura sair em escala 1:1. */
private fun Modifier.fitWidth(target: Dp): Modifier = layout { measurable, _ ->
    val placeable = measurable.measure(Constraints())
    val scale = min(1f, target.toPx() / placeable.width)
    layout((placeable.width * scale).roundToInt(), (placeable.height * scale).roundToInt()) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}
