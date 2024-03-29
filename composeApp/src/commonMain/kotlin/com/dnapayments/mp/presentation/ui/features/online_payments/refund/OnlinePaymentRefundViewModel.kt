package com.dnapayments.mp.presentation.ui.features.online_payments.refund

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.screenModelScope
import com.dnapayments.mp.domain.interactors.use_cases.online_payments.RefundPaymentOperationUseCase
import com.dnapayments.mp.domain.interactors.validation.ValidatePaymentAmount
import com.dnapayments.mp.domain.model.online_payments.OnlinePaymentOperationType
import com.dnapayments.mp.domain.network.Response.Companion.onSuccess
import com.dnapayments.mp.domain.network.toResourceUiState
import com.dnapayments.mp.presentation.model.ResourceUiState
import com.dnapayments.mp.presentation.model.TextFieldUiState
import com.dnapayments.mp.presentation.model.text_input.TextInput
import com.dnapayments.mp.presentation.model.validation_result.ValidationResult
import com.dnapayments.mp.presentation.mvi.BaseViewModel
import kotlinx.coroutines.launch

class OnlinePaymentRefundViewModel(
    private val validate: ValidatePaymentAmount,
    private val refundPaymentOperationUseCase: RefundPaymentOperationUseCase,
    balance: Double
) : BaseViewModel<OnlinePaymentRefundContract.Event, OnlinePaymentRefundContract.State, OnlinePaymentRefundContract.Effect>() {

    init {
        setState {
            copy(
                balance = balance
            )
        }
    }

    override fun createInitialState(): OnlinePaymentRefundContract.State =
        OnlinePaymentRefundContract.State(
            amount = TextFieldUiState(
                input = mutableStateOf(""),
                textInput = TextInput.AMOUNT,
                validationResult = mutableStateOf(ValidationResult(successful = true)),
                onFieldChanged = { this.setEvent(OnlinePaymentRefundContract.Event.OnAmountFieldChanged) }
            ),
            isButtonEnabled = mutableStateOf(true),
            refundState = ResourceUiState.Idle,
            balance = 0.0
        )


    override fun handleEvent(event: OnlinePaymentRefundContract.Event) {
        when (event) {
            OnlinePaymentRefundContract.Event.OnAmountFieldChanged -> {
                with(currentState) {
                    val validateAmountResult = validate(
                        amount = currentState.amount.input.value,
                        balance = balance,
                        textInput = currentState.amount.textInput,
                        onlinePaymentOperationType = OnlinePaymentOperationType.REFUND
                    )
                    amount.validationResult.value = validateAmountResult
                    isButtonEnabled.value = validateAmountResult.successful
                }
            }

            is OnlinePaymentRefundContract.Event.OnRefundClicked -> {
                refund(event.transactionId)
            }

            is OnlinePaymentRefundContract.Event.OnInit -> {
                with(currentState) {
                    amount.input.value = event.amount.toInt().toString()
                    copy(balance = event.balance)
                }
            }
        }
    }

    private fun refund(transactionId: String) {
        setState { copy(refundState = ResourceUiState.Loading) }
        screenModelScope.launch {
            val result = refundPaymentOperationUseCase(
                transactionId = transactionId,
                amount = currentState.amount.input.value.toDouble()
            )
            setState {
                copy(
                    refundState = result.onSuccess {
                        setEffect {
                            OnlinePaymentRefundContract.Effect.OnSuccessfullyRefunded(it.id)
                        }
                    }.toResourceUiState()
                )
            }
        }
    }
}
