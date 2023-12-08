package com.dna.payments.kmm.presentation.ui.features.team_management

import cafe.adriel.voyager.core.model.screenModelScope
import com.dna.payments.domain.presentation.team_management.TeamManagementSearchParameters
import com.dna.payments.domain.presentation.team_management.UserType
import com.dna.payments.kmm.domain.interactors.use_cases.access_level.AccessLevelUseCase
import com.dna.payments.kmm.domain.model.permissions.AccessLevel
import com.dna.payments.kmm.domain.model.permissions.Section
import com.dna.payments.kmm.domain.network.Response
import com.dna.payments.kmm.presentation.model.ResourceUiState
import com.dna.payments.kmm.presentation.mvi.BaseViewModel
import com.dna.payments.kmm.utils.UiText
import kotlinx.coroutines.launch

class TeamManagementViewModel(
    private val teamManagementByUserPageSource: TeamManagementByUserPageSource,
    private val accessLevelUseCase: AccessLevelUseCase
) : BaseViewModel<TeamManagementContract.Event, TeamManagementContract.State, TeamManagementContract.Effect>() {

    private var role: UserType = UserType.ALL

    init {
        setState {
            copy(
                hasPermission =
                accessLevelUseCase.hasPermission(
                    Section.TEAM_MANAGEMENT,
                    AccessLevel.FULL
                )
            )
        }
    }

    override fun createInitialState(): TeamManagementContract.State =
        TeamManagementContract.State(
            teammateListAll = ResourceUiState.Idle,
            teammateListInvited = ResourceUiState.Idle,
            hasPermission = false
        )

    override fun handleEvent(event: TeamManagementContract.Event) {
        when (event) {
            is TeamManagementContract.Event.OnInit -> {
                getTeammateList()
            }
        }
    }

    private fun getTeammateList() {
        setState { copy(teammateListAll = ResourceUiState.Loading) }
        screenModelScope.launch {
            teamManagementByUserPageSource.updateParameters(
                TeamManagementSearchParameters(
                    role = if (role == UserType.ALL) "" else role.displayName.lowercase(),
                    isActive = true
                )
            )
            val result = teamManagementByUserPageSource.onLoadMore()
            setState {
                copy(
                    teammateListAll = when (result) {
                        is Response.Success -> {
                            ResourceUiState.Success(result.data)
                        }

                        is Response.Error -> {
                            ResourceUiState.Error(result.error)
                        }

                        is Response.NetworkError -> {
                            ResourceUiState.Error(UiText.DynamicString("Network error"))
                        }

                        is Response.TokenExpire -> {
                            ResourceUiState.Error(UiText.DynamicString("Token expired"))
                        }
                    }
                )
            }
        }
    }
}