package com.dna.payments.kmm.presentation.ui.features.online_payments.refund

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.screenModelScope
import com.dna.payments.kmm.domain.interactors.use_cases.online_payments.RefundPaymentOperationUseCase
import com.dna.payments.kmm.domain.interactors.validation.ValidatePaymentAmount
import com.dna.payments.kmm.domain.model.online_payments.OperationType
import com.dna.payments.kmm.domain.network.Response.Companion.onSuccess
import com.dna.payments.kmm.domain.network.toResourceUiState
import com.dna.payments.kmm.presentation.model.ResourceUiState
import com.dna.payments.kmm.presentation.model.TextFieldUiState
import com.dna.payments.kmm.presentation.model.text_input.TextInput
import com.dna.payments.kmm.presentation.model.validation_result.ValidationResult
import com.dna.payments.kmm.presentation.mvi.BaseViewModel
import kotlinx.coroutines.launch

class OnlinePaymentRefundViewModel(
    private val validate: ValidatePaymentAmount,
    private val refundPaymentOperationUseCase: RefundPaymentOperationUseCase
) : BaseViewModel<OnlinePaymentRefundContract.Event, OnlinePaymentRefundContract.State, OnlinePaymentRefundContract.Effect>() {

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
            balance = mutableStateOf(0.0)
        )


    override fun handleEvent(event: OnlinePaymentRefundContract.Event) {
        when (event) {
            OnlinePaymentRefundContract.Event.OnAmountFieldChanged -> {
                with(currentState) {
                    val validateAmountResult = validate(
                        amount = currentState.amount.input.value,
                        balance = balance.value,
                        textInput = currentState.amount.textInput,
                        operationType = OperationType.REFUND
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
                    balance.value = event.balance
                }
            }
        }
    }

    private fun refund(transactionId: String) {
        setState { copy(refundState = ResourceUiState.Loading) }
        screenModelScope.launch {
            val result = refundPaymentOperationUseCase(
                transactionId = transactionId,
                amount = currentState.amount.input.value.toInt()
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
