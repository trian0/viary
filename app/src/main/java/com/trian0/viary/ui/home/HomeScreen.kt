package com.trian0.viary.ui.home

import android.R.attr.text
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trian0.viary.ui.theme.ViaryPrimary
import com.trian0.viary.ui.theme.ViarySecondary
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = "Viary em Progresso",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp)),
            colors = CardColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.LightGray,
                disabledContentColor = Color.DarkGray
            )
        ) {
            uiState.viary?.let { viary ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(10.dp)
                ) {
                    if (viary.selectedImage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 5.dp,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .height(150.dp)
                        ) {
                            AsyncImage(
                                model = Uri.fromFile(File(viary.selectedImage!!)),
                                contentDescription = "Imagem selecionada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Text(
                        modifier = Modifier.padding(10.dp).align(Alignment.CenterHorizontally),
                        text = viary.name,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "",
                            modifier = Modifier.size(20.dp).align(Alignment.CenterVertically),
                            tint = ViaryPrimary
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                modifier = Modifier.padding(bottom = 2.dp),
                                text = "Origem",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = viary.origin,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Row(modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = "",
                            modifier = Modifier.size(20.dp).align(Alignment.CenterVertically),
                            tint = ViarySecondary
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                modifier = Modifier.padding(bottom = 2.dp),
                                text = "Data de Partida",
                                style = MaterialTheme.typography.labelLarge
                            )
                            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            Text(
                                text = format.format(viary.departureTime!!),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Row() {
                        Column() { }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}