package com.dnapayments.mp.presentation.ui.features.verification_code

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.screenModelScope
import com.dnapayments.mp.domain.interactors.validation.ValidateCode
import com.dnapayments.mp.domain.network.Response
import com.dnapayments.mp.domain.use_case.SendOtpInstructionsUseCase
import com.dnapayments.mp.domain.use_case.VerifyOtpCodeUseCase
import com.dnapayments.mp.presentation.model.ResourceUiState
import com.dnapayments.mp.presentation.model.TextFieldUiState
import com.dnapayments.mp.presentation.model.text_input.TextInput
import com.dnapayments.mp.presentation.model.validation_result.ValidationResult
import com.dnapayments.mp.presentation.mvi.BaseViewModel
import com.dnapayments.mp.utils.UiText
import kotlinx.coroutines.launch

class VerificationCodeViewModel(
    private val validateCode: ValidateCode,
    private val verifyOtpCodeUseCase: VerifyOtpCodeUseCase,
    private val sendOtpInstructionsUseCase: SendOtpInstructionsUseCase
) : BaseViewModel<VerificationCodeContract.Event, VerificationCodeContract.State, VerificationCodeContract.Effect>() {

    override fun createInitialState(): VerificationCodeContract.State =
        VerificationCodeContract.State(
            verificationCode = TextFieldUiState(
                input = mutableStateOf(""),
                textInput = TextInput.VERIFICATION_CODE,
                validationResult = mutableStateOf(ValidationResult(successful = true)),
                onFieldChanged = { this.setEvent(VerificationCodeContract.Event.OnEmailFieldChanged) }
            ),
            isButtonEnabled = mutableStateOf(false),
            sendCode = ResourceUiState.Idle
        )

    override fun handleEvent(event: VerificationCodeContract.Event) {
        when (event) {
            is VerificationCodeContract.Event.OnButtonSendClicked -> {
                with(currentState) {
                    verifyOtpCode(
                        event.email,
                        verificationCode.input.value
                    )
                }
            }

            VerificationCodeContract.Event.OnEmailFieldChanged -> {
                with(currentState) {
                    val verificationCodeResult =
                        validateCode(verificationCode.input.value, verificationCode.textInput)

                    verificationCode.validationResult.value = verificationCodeResult
                    isButtonEnabled.value =
                        verificationCodeResult.successful
                }
            }

            is VerificationCodeContract.Event.OnButtonResendCodeClicked -> {
                sendInstructions(event.email)
            }
        }
    }

    private fun verifyOtpCode(email: String, code: String) {
        setState { copy(sendCode = ResourceUiState.Loading) }
            screenModelScope.launch {
                val result = verifyOtpCodeUseCase(
                    code = code,
                    email = email,
                )
                setState {
                    copy(
                        sendCode = when (result) {
                            is Response.Success -> {
                                setEffect {
                                    VerificationCodeContract.Effect.OnVerificationSuccess(result.data.id)
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

    private fun sendInstructions(email: String) {
            screenModelScope.launch {
                sendOtpInstructionsUseCase(
                    email = email,
                )
        }
    }
}
