package com.trian0.viary

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trian0.viary.data.models.Country
import com.trian0.viary.data.repositories.ViaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Currency

sealed class SplashNavState {
    data object Loading : SplashNavState()
    data object NavigateToHome : SplashNavState()
}

class MainViewModel(
    private val repository: ViaryRepository
) : ViewModel() {
    private val _navState = MutableStateFlow<SplashNavState>(SplashNavState.Loading)
    val navState: StateFlow<SplashNavState> = _navState.asStateFlow()
    var hasViaryInProgress by mutableStateOf(false)
        private set
    private val _currencyData = MutableStateFlow(Country())
    val country: StateFlow<Country> = _currencyData.asStateFlow()

    val keepSplashOn: StateFlow<Boolean> = _navState.map {
        it is SplashNavState.Loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    init {
        viewModelScope.launch {
            checkInitialSetup()
            fetchCountry()
        }

        viewModelScope.launch {
            repository.viaryInProgress.collect { viary ->
                hasViaryInProgress = viary != null
            }
        }
    }

    private suspend fun checkInitialSetup() {
        delay(50)
        _navState.value = SplashNavState.NavigateToHome
    }

    private fun fetchCountry() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val response = client.newCall(
                    Request.Builder()
                        .url("http://ip-api.com/json/?fields=countryCode,currency")
                        .build()
                ).execute()

                val body = response.body?.string() ?: return@launch
                val json = JSONObject(body)

                val currency = json.getString("currency")
                val countryCode = json.getString("countryCode")
                val symbol = Currency.getInstance(currency).symbol

                _currencyData.value = Country(
                    currency = currency,
                    countryCode = countryCode,
                    symbol = symbol,
                    loading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _currencyData.value = _currencyData.value.copy(loading = false)
            }
        }
    }
}