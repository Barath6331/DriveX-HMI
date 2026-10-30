package com.barath.drivexhmi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.barath.drivexhmi.data.SimulatedVehicleDataSource
import com.barath.drivexhmi.data.VehicleRepository
import com.barath.drivexhmi.data.VehicleRepositoryImpl
import com.barath.drivexhmi.model.VehicleScenario
import com.barath.drivexhmi.ui.model.DashboardUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel responsible for managing and exposing the reactive HMI dashboard state.
 * Connects the UI to the domain vehicle repository.
 */
class DashboardViewModel(
    private val repository: VehicleRepository = VehicleRepositoryImpl(
        dataSource = SimulatedVehicleDataSource()
    )
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.vehicleState,
        repository.isAutoCycleActive
    ) { state, isAutoCycling ->
        DashboardUiState.fromProcessedState(state, isAutoCycling)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DashboardUiState()
    )

    fun toggleAutoCycle() {
        val current = uiState.value.isAutoCycleActive
        repository.setAutoCycling(!current)
    }

    fun selectScenario(scenario: VehicleScenario) {
        repository.selectScenario(scenario)
    }

    fun nextScenario() {
        repository.nextScenario()
    }

    fun injectNegativeSpeedEdgeCase() {
        repository.selectScenario(VehicleScenario.INVALID_SPEED)
    }

    fun injectStaleDataEdgeCase() {
        repository.selectScenario(VehicleScenario.STALE_DATA)
    }

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }

    companion object {
        fun provideFactory(
            repository: VehicleRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(repository) as T
            }
        }
    }
}
