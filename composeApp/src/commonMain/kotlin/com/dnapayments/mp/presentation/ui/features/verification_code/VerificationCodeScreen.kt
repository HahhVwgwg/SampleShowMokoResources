package com.dnapayments.mp.presentation.ui.features.verification_code

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import com.dnapayments.mp.MR
import com.dnapayments.mp.domain.model.main_screens.ScreenName
import com.dnapayments.mp.presentation.theme.DnaTextStyle
import com.dnapayments.mp.presentation.ui.common.BasicCountdownTimer
import com.dnapayments.mp.presentation.ui.common.DNAGreenBackButton
import com.dnapayments.mp.presentation.ui.common.DNAText
import com.dnapayments.mp.presentation.ui.common.DNAVerificationCodeTextField
import com.dnapayments.mp.presentation.ui.common.DNAYellowButton
import com.dnapayments.mp.presentation.ui.common.UiStateController
import com.dnapayments.mp.presentation.ui.features.new_password.NewPasswordScreen
import com.dnapayments.mp.utils.constants.Constants
import com.dnapayments.mp.utils.extension.noRippleClickable
import com.dnapayments.mp.utils.firebase.logEvent
import com.dnapayments.mp.utils.navigation.LocalNavigator
import com.dnapayments.mp.utils.navigation.currentOrThrow
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.collectLatest

class VerificationCodeScreen(
    private val email: String
) : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val verificationCodeViewModel = getScreenModel<VerificationCodeViewModel>()

        val state by verificationCodeViewModel.uiState.collectAsState()

        val controller = LocalSoftwareKeyboardController.current
        val navigator = LocalNavigator.currentOrThrow


        LifecycleEffect(
            onStarted = {
                logEvent(
                    Constants.SCREEN_OPEN_EVENT,
                    mapOf(Constants.SCREEN_NAME to ScreenName.VERIFICATION_CODE)
                )
            }
        )

        UiStateController(state.sendCode)

        LaunchedEffect(key1 = Unit) {
            verificationCodeViewModel.effect.collectLatest { effect ->
                when (effect) {
                    is VerificationCodeContract.Effect.OnVerificationSuccess -> {
                        navigator.push(NewPasswordScreen(effect.id, email))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .noRippleClickable {
                    controller?.hide()
                },
            verticalArrangement = Arrangement.Top
        ) {
            RestorePasswordContent(
                modifier = Modifier.wrapContentHeight(),
                state = state,
                onBackToLoginClicked = { navigator.pop() },
                onSendClicked = {
                    verificationCodeViewModel.setEvent(
                        VerificationCodeContract.Event.OnButtonSendClicked(
                            email
                        )
                    )
                },
                onResendClicked = {
                    verificationCodeViewModel.setEvent(
                        VerificationCodeContract.Event.OnButtonResendCodeClicked(
                            email
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.weight(0.5f))
        }
    }

    @Composable
    private fun RestorePasswordContent(
        modifier: Modifier = Modifier,
        state: VerificationCodeContract.State,
        onBackToLoginClicked: () -> Unit,
        onSendClicked: () -> Unit,
        onResendClicked: () -> Unit,
    ) {
        Column(
            modifier = modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            DNAGreenBackButton(
                text = stringResource(MR.strings.back_to_email),
                onClick = onBackToLoginClicked,
                modifier = Modifier.padding(horizontal = 0.dp)
            )
            Spacer(modifier = modifier.height(16.dp))
            DNAText(
                text = stringResource(MR.strings.confirm_email),
                style = DnaTextStyle.Bold20
            )
            Spacer(modifier = modifier.height(8.dp))
            DNAText(
                text = stringResource(
                    MR.strings.confirm_email_description, email
                ),
                style = DnaTextStyle.Normal14
            )
            Spacer(modifier = modifier.height(32.dp))
            DNAText(
                text = stringResource(MR.strings.verification_code),
                style = DnaTextStyle.Medium16,
            )
            Spacer(modifier = Modifier.height(8.dp))
            DNAVerificationCodeTextField(state.verificationCode)
            Spacer(modifier = modifier.height(32.dp))
            DNAYellowButton(
                text = stringResource(MR.strings.confirm),
                onClick = onSendClicked,
                screenName = ScreenName.VERIFICATION_CODE,
                enabled = state.isButtonEnabled.value
            )
            Spacer(modifier = modifier.height(24.dp))
            DNAText(
                text = stringResource(MR.strings.verification_hint),
                style = DnaTextStyle.WithAlpha14,
                modifier = modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = modifier.height(24.dp))
            BasicCountdownTimer(onClick = onResendClicked)
        }
    }
}