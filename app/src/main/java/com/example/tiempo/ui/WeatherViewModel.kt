package com.example.tiempo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiempo.data.WeatherRepository
import com.example.tiempo.data.model.CurrentWeather
import com.example.tiempo.data.model.DayWeather
import com.example.tiempo.data.model.HourWeather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(
        val current: CurrentWeather,
        val days: List<DayWeather>,
        val hours: List<HourWeather>,
        val elevationM: Double
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun load(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            _uiState.value = try {
                val forecast = repository.getForecast(lat, lon)
                WeatherUiState.Success(forecast.current, forecast.days, forecast.hours, forecast.elevationM)
            } catch (e: Exception) {
                WeatherUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
