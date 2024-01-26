package com.dna.payments.kmm.presentation.ui.features.payment_methods_detail

import cafe.adriel.voyager.core.model.screenModelScope
import com.dna.payments.kmm.data.model.payment_methods.UnregisterDomainRequest
import com.dna.payments.kmm.domain.interactors.use_cases.access_level.AccessLevelUseCase
import com.dna.payments.kmm.domain.interactors.use_cases.payment_method.UnregisterDomainUseCase
import com.dna.payments.kmm.domain.model.payment_methods.domain.Domain
import com.dna.payments.kmm.domain.model.payment_methods.setting.PaymentMethodType
import com.dna.payments.kmm.domain.model.permissions.AccessLevel
import com.dna.payments.kmm.domain.model.permissions.Section
import com.dna.payments.kmm.domain.network.Response
import com.dna.payments.kmm.domain.use_case.GetDomainsUseCase
import com.dna.payments.kmm.domain.use_case.GetTerminalSettingsUseCase
import com.dna.payments.kmm.presentation.model.ResourceUiState
import com.dna.payments.kmm.presentation.mvi.BaseViewModel
import com.dna.payments.kmm.utils.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailPaymentMethodsViewModel(
    private val getTerminalSettingsUseCase: GetTerminalSettingsUseCase,
    private val accessLevelUseCase: AccessLevelUseCase,
    private val getDomainsUseCase: GetDomainsUseCase,
    private val unregisterDomainUseCase: UnregisterDomainUseCase
) : BaseViewModel<DetailPaymentMethodsContract.Event, DetailPaymentMethodsContract.State, DetailPaymentMethodsContract.Effect>() {

    override fun createInitialState(): DetailPaymentMethodsContract.State =
        DetailPaymentMethodsContract.State(
            terminalSettings = ResourceUiState.Idle,
            domainList = ResourceUiState.Idle,
            domainUnregister = ResourceUiState.Idle
        )

    override fun handleEvent(event: DetailPaymentMethodsContract.Event) {
        when (event) {
            is DetailPaymentMethodsContract.Event.OnInit -> {
                fetchData(event.paymentMethodsType)
                getDomainList(event.paymentMethodsType)
            }

            is DetailPaymentMethodsContract.Event.OnUnregisterDomainItem -> {
                unregisterDomain(event.paymentMethodType, event.domain)

            }
        }
    }

    private fun unregisterDomain(paymentMethodType: PaymentMethodType, domain: Domain) {
        setState { copy(domainUnregister = ResourceUiState.Loading) }
        screenModelScope.launch {
            val result = unregisterDomainUseCase(paymentMethodType = paymentMethodType,
                unregisterDomainRequest = UnregisterDomainRequest(
                    domainNames = mutableListOf<String>().apply { add(domain.name) },
                    reason = "Unregister domain"
                ))
            setState {
                copy(
                    domainUnregister = when (result) {
                        is Response.Success -> {
                            setEffect {
                                DetailPaymentMethodsContract.Effect.OnUnregisterNewDomainSuccess
                            }
                            getDomainList(paymentMethodType)
                            ResourceUiState.Success(
                                result.data
                            )
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
                    },
                    domainList = ResourceUiState.Loading
                )
            }
        }
    }

    private fun fetchData(paymentMethodType: PaymentMethodType) {
        setState { copy(terminalSettings = ResourceUiState.Loading) }
            screenModelScope.launch {
                val result = getTerminalSettingsUseCase(paymentMethodType)
                setState {
                    copy(
                        terminalSettings = when (result) {
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

    private fun getDomainList(
        paymentMethodType: PaymentMethodType
    ) {
        setState { copy(domainList = ResourceUiState.Loading) }
        screenModelScope.launch {
            val result = getDomainsUseCase(paymentMethodType)
            setState {
                copy(
                    domainList = when (result) {
                        is Response.Success -> {
                            ResourceUiState.Success(result.data.map {
                                it.copy(
                                    isDeleteAvailable = accessLevelUseCase.hasPermission(
                                        Section.PAYMENT_METHODS,
                                        AccessLevel.FULL
                                    )
                                )
                            })
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
