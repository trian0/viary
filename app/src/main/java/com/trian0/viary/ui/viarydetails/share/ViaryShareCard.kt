package com.trian0.viary.ui.viarydetails.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trian0.viary.R
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.utils.formatDistanceKm
import com.trian0.viary.data.utils.formatMoney
import com.trian0.viary.data.utils.formatTripDates
import com.trian0.viary.ui.theme.Neutral10
import com.trian0.viary.ui.theme.PlusJakartaSans
import com.trian0.viary.ui.theme.Primary10
import com.trian0.viary.ui.theme.Primary100
import com.trian0.viary.ui.theme.Primary20
import com.trian0.viary.ui.theme.Primary50
import com.trian0.viary.ui.theme.Primary60
import java.util.Date

// 4:5 a 3x = 1080×1350, o retrato nativo do Instagram. Card baixo também perde menos quando
// WhatsApp e Telegram reduzem o lado maior da imagem.
const val SHARE_CARD_WIDTH_DP = 360
const val SHARE_CARD_HEIGHT_DP = 450
const val SHARE_MAP_WIDTH_DP = 328
const val SHARE_MAP_HEIGHT_DP = 158
const val SHARE_EXPORT_SCALE = 3f

private const val MAX_NAMED_CHECKPOINTS = 3
private val HeaderHeight = 124.dp

// O bodyLarge do tema traz lineHeight de 24sp, que num card de altura fixa estoura até texto de 11sp
private val CardLineHeight = 1.2.em

data class ShareOptions(
    val showExpenses: Boolean = true,
    val showCover: Boolean = true,
    val showPlaceNames: Boolean = true,
)

/**
 * Card que vira imagem. Tem tamanho fixo e cores próprias (não segue o tema do sistema), para a imagem
 * sair igual em qualquer aparelho. [cover] é o que o Coil aceita (URL ou File).
 */
@Composable
fun ViaryShareCard(
    viary: Viary,
    checkpoints: List<Checkpoint>,
    durationFormatted: String,
    currencySymbol: String,
    options: ShareOptions,
    map: ImageBitmap?,
    cover: Any?,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale

    // A escala de fonte do sistema fica de fora para a prévia bater com a imagem, que sai sempre em fontScale 1
    CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density)) {
        Column(
            modifier = modifier
                .size(SHARE_CARD_WIDTH_DP.dp, SHARE_CARD_HEIGHT_DP.dp)
                .background(Color.White)
        ) {
            CardHeader(viary, checkpoints, options, cover.takeIf { options.showCover })

            Box(
                modifier = Modifier
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp)
                    .size(SHARE_MAP_WIDTH_DP.dp, SHARE_MAP_HEIGHT_DP.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary100)
            ) {
                if (map != null) {
                    Image(
                        bitmap = map,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Row(
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(
                    label = stringResource(R.string.share_card_distance),
                    value = formatDistanceKm(viary.kmEnd, locale),
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = stringResource(R.string.share_card_duration),
                    value = durationFormatted,
                    modifier = Modifier.weight(1f)
                )
                if (options.showExpenses) {
                    StatBox(
                        label = stringResource(R.string.share_card_spent),
                        value = formatMoney(checkpoints.sumOf { it.expense }, currencySymbol, locale),
                        highlighted = true,
                        modifier = Modifier.weight(1.25f)
                    )
                }
            }

            if (checkpoints.isNotEmpty()) {
                CheckpointSummary(
                    checkpoints = checkpoints,
                    showPlaceNames = options.showPlaceNames,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Neutral10)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardText(
                    text = stringResource(R.string.app_name).lowercase(locale),
                    size = 20.sp,
                    weight = FontWeight.ExtraBold,
                    color = Primary60
                )
                CardText(
                    text = stringResource(R.string.share_card_footer),
                    size = 11.sp,
                    weight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun CardHeader(
    viary: Viary,
    checkpoints: List<Checkpoint>,
    options: ShareOptions,
    cover: Any?,
) {
    val locale = LocalLocale.current.platformLocale
    val dates = formatTripDates(viary.departureTime, viary.arrivalTime ?: checkpoints.lastOrNull()?.time, locale)
    val climate = viary.climate
        ?.let { name -> Viary.ViaryClimate.entries.firstOrNull { it.name == name } }
        ?.let { stringResource(it.labelRes).lowercase(locale).replaceFirstChar { c -> c.titlecase(locale) } }
    val topLine = listOfNotNull(dates.ifEmpty { null }, climate).joinToString(" · ")
    val origin = viary.origin
        .takeIf { options.showPlaceNames && it.isNotEmpty() }
        ?.let { stringResource(R.string.viary_details_screen_origin_prefix, it) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .background(Primary50)
    ) {
        if (cover != null) {
            AsyncImage(
                model = cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Neutral10.copy(alpha = 0.8f))))
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            HeaderText(topLine, viary.name, origin)
        }
    }
}

@Composable
private fun HeaderText(topLine: String, name: String, origin: String?) {
    if (topLine.isNotEmpty()) {
        CardText(text = topLine, size = 12.sp, weight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.9f))
    }
    Text(
        text = name,
        maxLines = 2,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        lineHeight = 1.15.em,
        autoSize = TextAutoSize.StepBased(minFontSize = 18.sp, maxFontSize = 24.sp),
        modifier = Modifier.padding(top = 2.dp)
    )
    if (origin != null) {
        CardText(
            text = origin,
            size = 12.sp,
            weight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlighted) Primary50 else Primary100)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        CardText(
            text = label,
            size = 11.sp,
            weight = FontWeight.SemiBold,
            color = if (highlighted) Color.White.copy(alpha = 0.9f) else Primary20
        )
        Text(
            text = value,
            maxLines = 1,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.ExtraBold,
            color = if (highlighted) Color.White else Primary10,
            lineHeight = CardLineHeight,
            autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = 17.sp),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** "4 checkpoints · Mirante, Pousada, Café +1". Sem nomes, só a contagem. */
@Composable
private fun CheckpointSummary(
    checkpoints: List<Checkpoint>,
    showPlaceNames: Boolean,
    modifier: Modifier = Modifier,
) {
    val names = checkpoints.map { it.placeName }.filter { it.isNotBlank() }.take(MAX_NAMED_CHECKPOINTS)
    val remaining = checkpoints.size - names.size

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CardText(
            text = pluralStringResource(R.plurals.share_card_checkpoint_count, checkpoints.size, checkpoints.size),
            size = 13.sp,
            weight = FontWeight.ExtraBold,
            color = Primary20
        )
        if (showPlaceNames && names.isNotEmpty()) {
            CardText(text = "·", size = 13.sp, weight = FontWeight.Bold, color = Primary50)
            CardText(
                text = names.joinToString(", "),
                size = 13.sp,
                weight = FontWeight.Bold,
                color = Primary10,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (remaining > 0) {
                CardText(text = "+$remaining", size = 13.sp, weight = FontWeight.ExtraBold, color = Primary50)
            }
        }
    }
}

@Composable
private fun CardText(
    text: String,
    size: TextUnit,
    weight: FontWeight,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        fontFamily = PlusJakartaSans,
        lineHeight = CardLineHeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview(locale = "pt")
@Preview
@Composable
private fun ViaryShareCardPreview() {
    val start = Date(1_773_316_800_000L)
    val viary = Viary(
        name = "Serra da Mantiqueira e Campos do Jordão pela estrada velha",
        origin = "Campinas",
        departureTime = start,
        arrivalTime = Date(start.time + 74 * 3_600_000L),
        status = Viary.ViaryStatus.COMPLETED,
        kmEnd = 482.3f,
        selectedImage = null,
        climate = Viary.ViaryClimate.SUNNY.name,
    )
    val checkpoints = listOf(
        "Mirante do Cristo" to 86.0,
        "Pousada Vale Verde" to 420.0,
        "Café da Serra" to 58.5,
        "Cachoeira dos Véus" to 74.0,
        "Posto Serra Azul" to 150.0,
        "Mercado Municipal" to 11_496.0,
    ).mapIndexed { index, (place, expense) ->
        Checkpoint(placeName = place, expense = expense, time = Date(start.time + index * 9 * 3_600_000L))
    }
    ViaryShareCard(
        viary = viary,
        checkpoints = checkpoints,
        durationFormatted = "74h 20m",
        currencySymbol = "R$",
        options = ShareOptions(),
        map = null,
        cover = null,
    )
}
