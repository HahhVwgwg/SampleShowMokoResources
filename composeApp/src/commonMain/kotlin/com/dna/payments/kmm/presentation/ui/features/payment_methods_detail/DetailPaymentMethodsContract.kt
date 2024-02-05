package com.dna.payments.kmm.presentation.ui.features.payment_methods_detail

import com.dna.payments.kmm.domain.model.payment_methods.domain.Domain
import com.dna.payments.kmm.domain.model.payment_methods.setting.DetailTerminalSetting
import com.dna.payments.kmm.domain.model.payment_methods.setting.PaymentMethodType
import com.dna.payments.kmm.domain.model.payment_methods.setting.TerminalSetting
import com.dna.payments.kmm.presentation.model.ResourceUiState
import com.dna.payments.kmm.presentation.mvi.UiEffect
import com.dna.payments.kmm.presentation.mvi.UiEvent
import com.dna.payments.kmm.presentation.mvi.UiState

interface DetailPaymentMethodsContract {
    sealed interface Event : UiEvent {
        data class OnInit(val paymentMethodsType: PaymentMethodType) : Event

        data class OnUnregisterDomainItem(
            val paymentMethodType: PaymentMethodType,
            val domain: Domain
        ) : Event

        data class OnChangeTerminalSetting(
            val paymentMethodType: PaymentMethodType,
            val detailTerminalSetting: DetailTerminalSetting
        ) : Event

    }

    data class State(
        val terminalSettings: ResourceUiState<List<TerminalSetting>>,
        val domainList: ResourceUiState<List<Domain>>,
        val domainUnregister: ResourceUiState<Unit>
    ) : UiState

    sealed interface Effect : UiEffect {
        data object OnUnregisterNewDomainSuccess : Effect
    }
}


