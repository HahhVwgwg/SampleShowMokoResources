package com.dnapayments.mp.presentation.ui.features.login

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.screenModelScope
import com.dnapayments.mp.domain.interactors.use_cases.authorization.AuthorizationUseCase
import com.dnapayments.mp.domain.interactors.validation.ValidateEmail
import com.dnapayments.mp.domain.interactors.validation.ValidatePassword
import com.dnapayments.mp.domain.network.Response.Companion.onSuccess
import com.dnapayments.mp.domain.network.toResourceUiState
import com.dnapayments.mp.presentation.model.ResourceUiState
import com.dnapayments.mp.presentation.model.TextFieldUiState
import com.dnapayments.mp.presentation.model.text_input.TextInput
import com.dnapayments.mp.presentation.model.validation_result.ValidationResult
import com.dnapayments.mp.presentation.mvi.BaseViewModel
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authorizationUseCase: AuthorizationUseCase,
    private val validateEmail: ValidateEmail,
    private val validatePassword: ValidatePassword,
) : BaseViewModel<LoginContract.Event, LoginContract.State, LoginContract.Effect>() {

    override fun createInitialState(): LoginContract.State =
        LoginContract.State(
            email = TextFieldUiState(
                input = mutableStateOf(""),
                textInput = TextInput.EMAIL_ADDRESS,
                validationResult = mutableStateOf(ValidationResult(successful = true)),
                onFieldChanged = { this.setEvent(LoginContract.Event.OnEmailFieldChanged) }
            ),
            password = TextFieldUiState(
                input = mutableStateOf(""),
                textInput = TextInput.PASSWORD,
                validationResult = mutableStateOf(ValidationResult(successful = true)),
                onFieldChanged = { this.setEvent(LoginContract.Event.OnPasswordFieldChanged) }
            ),
            authorization = ResourceUiState.Idle,
            isLoginEnabled = mutableStateOf(false)
        )

    override fun handleEvent(event: LoginContract.Event) {
        when (event) {
            LoginContract.Event.OnLoginClicked -> {
                with(currentState) {
                    authorize(email.input.value, password.input.value)
                }
            }

            LoginContract.Event.OnEmailFieldChanged -> {
                with(currentState) {
                    val validateEmailResult = validateEmail(email.input.value, email.textInput)
                    val validatePasswordResult = validatePassword(
                        password.input.value,
                        password.textInput
                    )
                    email.validationResult.value = validateEmailResult
                    isLoginEnabled.value =
                        validateEmailResult.successful && validatePasswordResult.successful
                }
            }

            LoginContract.Event.OnPasswordFieldChanged -> {
                with(currentState) {
                    val validateEmailResult = validateEmail(email.input.value, email.textInput)
                    val validatePasswordResult = validatePassword(
                        password.input.value,
                        password.textInput
                    )
                    password.validationResult.value = validatePasswordResult
                    isLoginEnabled.value =
                        validateEmailResult.successful && validatePasswordResult.successful
                }

            }
        }
    }

    private fun authorize(email: String, password: String) {
        screenModelScope.launch {
            setState { copy(authorization = ResourceUiState.Loading) }
            val result = authorizationUseCase(
                userName = email,
                password = password
            )
            setState {
                copy(
                    authorization = result.onSuccess {
                        setEffect {
                            LoginContract.Effect.OnLoginSuccess
                        }
                    }.toResourceUiState()
                )
            }
        }
    }
}
