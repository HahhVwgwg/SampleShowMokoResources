package com.dnapayments.mp.presentation.ui.features.drawer_navigation

import cafe.adriel.voyager.core.model.screenModelScope
import com.dnapayments.mp.domain.interactors.use_cases.authorization.AuthorizationUseCase
import com.dnapayments.mp.domain.interactors.use_cases.drawer.DrawerUseCase
import com.dnapayments.mp.domain.interactors.use_cases.profile.MerchantUseCase
import com.dnapayments.mp.domain.network.Response
import com.dnapayments.mp.presentation.model.ResourceUiState
import com.dnapayments.mp.presentation.mvi.BaseViewModel
import com.dnapayments.mp.utils.UiText
import kotlinx.coroutines.launch

class DrawerViewModel(
    private val merchantUseCase: MerchantUseCase,
    private val authorizationUseCase: AuthorizationUseCase,
    private val drawerUseCase: DrawerUseCase
) :
    BaseViewModel<DrawerScreenContract.Event, DrawerScreenContract.State, DrawerScreenContract.Effect>() {

    init {
        setState {
            copy(
                navItems = drawerUseCase.getNavItemList(),
                settingsItems = drawerUseCase.getSettingsItems()
            )
        }
    }

    override fun createInitialState(): DrawerScreenContract.State =
        DrawerScreenContract.State(
            navItems = emptyList(),
            settingsItems = emptyList(),
            merchants = ResourceUiState.Loading,
            merchantChange = ResourceUiState.Idle
        )

    override fun handleEvent(event: DrawerScreenContract.Event) {
        when (event) {
            is DrawerScreenContract.Event.OnMerchantChange -> {
                changeMerchant(event.data.merchantId)
            }
            DrawerScreenContract.Event.OnStart -> {
                getMerchants()
            }
        }
    }

    private fun getMerchants() {
        screenModelScope.launch {
            val result = merchantUseCase()
            setState {
                copy(
                    merchants = when (result) {
                        is Response.Success -> {
                            if (result.data.isNotEmpty()) {
                                setEffect {
                                    DrawerScreenContract.Effect.OnMerchantSelected(result.data.first().name)
                                }
                            }
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

    private fun changeMerchant(merchantId: String) {
        screenModelScope.launch {
            setState { copy(merchantChange = ResourceUiState.Loading) }
            val result = authorizationUseCase.changeMerchant(merchantId)
            setState {
                copy(
                    merchantChange = when (result) {
                        is Response.Success -> {
                            setEffect { DrawerScreenContract.Effect.OnMerchantChange }
                            getMerchants()
                            ResourceUiState.Success(Unit)
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