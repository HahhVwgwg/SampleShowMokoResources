package com.dna.payments.kmm.presentation.ui.features.team_management

import com.dna.payments.kmm.domain.model.team_management.Teammate
import com.dna.payments.kmm.presentation.model.ResourceUiState
import com.dna.payments.kmm.presentation.mvi.UiEffect
import com.dna.payments.kmm.presentation.mvi.UiEvent
import com.dna.payments.kmm.presentation.mvi.UiState

interface TeamManagementContract {
    sealed interface Event : UiEvent {
        data object OnInit : Event
    }

    data class State(
        val teammateListAll: ResourceUiState<List<Teammate>>,
        val teammateListInvited: ResourceUiState<List<Teammate>>,
        val hasPermission: Boolean
    ) : UiState

    sealed interface Effect : UiEffect {}
}


