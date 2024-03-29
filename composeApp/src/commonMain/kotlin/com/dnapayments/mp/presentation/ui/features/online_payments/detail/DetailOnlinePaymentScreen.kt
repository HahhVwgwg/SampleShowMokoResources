package com.dnapayments.mp.presentation.ui.features.online_payments.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.dnapayments.mp.MR
import com.dnapayments.mp.domain.model.online_payments.OnlinePaymentMethod
import com.dnapayments.mp.domain.model.online_payments.OnlinePaymentOperationType
import com.dnapayments.mp.domain.model.online_payments.OnlinePaymentOperationType.CANCEL
import com.dnapayments.mp.domain.model.online_payments.OnlinePaymentOperationType.CHARGE
import com.dnapayments.mp.domain.model.pos_payments.PosPaymentCard
import com.dnapayments.mp.domain.model.transactions.Transaction
import com.dnapayments.mp.presentation.state.ComponentRectangleLineLong
import com.dnapayments.mp.presentation.state.ComponentRectangleLineShort
import com.dnapayments.mp.presentation.state.ManagementResourceUiState
import com.dnapayments.mp.presentation.theme.DnaTextStyle
import com.dnapayments.mp.presentation.theme.Paddings
import com.dnapayments.mp.presentation.theme.greyFirst
import com.dnapayments.mp.presentation.theme.lightGrey
import com.dnapayments.mp.presentation.ui.common.ClipboardDotsContent
import com.dnapayments.mp.presentation.ui.common.DNADots
import com.dnapayments.mp.presentation.ui.common.DNAExpandBox
import com.dnapayments.mp.presentation.ui.common.DNAExpandBoxOnLoading
import com.dnapayments.mp.presentation.ui.common.DNAText
import com.dnapayments.mp.presentation.ui.common.DNATextWithIcon
import com.dnapayments.mp.presentation.ui.common.DNATopAppBar
import com.dnapayments.mp.presentation.ui.common.DNAYellowButton
import com.dnapayments.mp.presentation.ui.common.DefaultDotsContent
import com.dnapayments.mp.presentation.ui.common.DnaFilter
import com.dnapayments.mp.presentation.ui.common.LinkDotsContent
import com.dnapayments.mp.presentation.ui.common.MessageDotsContent
import com.dnapayments.mp.presentation.ui.common.PainterDotsContent
import com.dnapayments.mp.presentation.ui.common.PainterWithBackgroundDotsContent
import com.dnapayments.mp.presentation.ui.common.SuccessPopup
import com.dnapayments.mp.presentation.ui.features.online_payments.receipt.GetReceiptScreen
import com.dnapayments.mp.presentation.ui.features.online_payments.refund.OnlinePaymentRefundScreen
import com.dnapayments.mp.utils.UiText
import com.dnapayments.mp.utils.extension.changePlatformColor
import com.dnapayments.mp.utils.extension.toMoneyString
import com.dnapayments.mp.utils.navigation.LocalNavigator
import com.dnapayments.mp.utils.navigation.Navigator
import com.dnapayments.mp.utils.navigation.OnlinePaymentNavigatorResult
import com.dnapayments.mp.utils.navigation.OnlinePaymentNavigatorResultType.CHARGED
import com.dnapayments.mp.utils.navigation.OnlinePaymentNavigatorResultType.PROCESS_NEW_PAYMENT
import com.dnapayments.mp.utils.navigation.OnlinePaymentNavigatorResultType.REFUND
import com.dnapayments.mp.utils.navigation.clearResults
import com.dnapayments.mp.utils.navigation.currentOrThrow
import com.dnapayments.mp.utils.navigation.getResult
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

class DetailOnlinePaymentScreen(private val transactionParam: Transaction) : Screen {

    @Composable
    override fun Content() {

        changePlatformColor(true)

        val navigator = LocalNavigator.currentOrThrow
        val detailPaymentViewModel = getScreenModel<DetailOnlinePaymentViewModel>()
        val state by detailPaymentViewModel.uiState.collectAsState()

        val showSuccessMessage = remember { mutableStateOf(false) }
        val successMessage = remember { mutableStateOf(MR.strings.empty_text) }
        val showOperationBottomSheet = remember { mutableStateOf(false) }
        val result = getResult().value

        LaunchedEffect(transactionParam, result) {
            if (result == null) return@LaunchedEffect

            if (result is OnlinePaymentNavigatorResult) {
                when (result.value) {
                    REFUND, CHARGED, PROCESS_NEW_PAYMENT -> {
                        detailPaymentViewModel.setEvent(
                            DetailOnlinePaymentContract.Event.OnTransactionChanged(
                                result.id ?: transactionParam.id
                            )
                        )
                    }

                    else -> {
                        detailPaymentViewModel.setEvent(
                            DetailOnlinePaymentContract.Event.OnTransactionGet(
                                transactionParam
                            )
                        )
                    }
                }
                successMessage.value = result.value.successMessage ?: MR.strings.empty_text
                showSuccessMessage.value = result.value.successMessage != null
                navigator.clearResults()
            }
        }

        SuccessPopup(
            UiText.StringResource(successMessage.value),
            showSuccessMessage
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(greyFirst),
            verticalArrangement = Arrangement.Top
        ) {

            DNATopAppBar(
                title = stringResource(MR.strings.payment_detail),
                navigationIcon = painterResource(MR.images.ic_green_arrow_back),
                navigationText = stringResource(MR.strings.close),
                onNavigationClick = {
                    navigator.pop()
                },
                actionIcon = if (state.operationTypeList.isNotEmpty()) painterResource(MR.images.ic_actions) else null,
                onActionClick = {
                    showOperationBottomSheet.value = true
                }
            )

            OnlinePaymentDetailContent(
                modifier = Modifier.wrapContentHeight(),
                state = state,
                navigator = navigator
            )
            DnaFilter(
                openBottomSheet = showOperationBottomSheet,
                dropDownContent = {
                    OnlinePaymentOperationWidget(state)
                },
                bottomSheetContent = {
                    OnlinePaymentOperationBottomSheet(
                        state = state,
                        onItemChange = {
                            when (it) {
                                CHARGE -> {

                                }

                                OnlinePaymentOperationType.REFUND -> {
                                    navigator.push(
                                        OnlinePaymentRefundScreen(
                                            transactionParam
                                        )
                                    )
                                }

                                OnlinePaymentOperationType.PROCESS_NEW_PAYMENT -> {

                                }

                                CANCEL -> {

                                }
                            }
                        }
                    )
                }
            )
        }
    }

    @Composable
    private fun OnlinePaymentDetailContent(
        modifier: Modifier,
        state: DetailOnlinePaymentContract.State,
        navigator: Navigator,
    ) {
        Column(
            modifier.fillMaxSize()
        ) {
            ManagementResourceUiState(
                modifier = modifier,
                resourceUiState = state.detailPaymentState,
                successView = {
                    Box {
                        if (it != null) {
                            Box(
                                Modifier.zIndex(1f)
                            ) {

                                DetailOnlinePaymentContent(
                                    modifier = modifier,
                                    transaction = it
                                )
                            }
                            ZStackReceiptButton(
                                it,
                                navigator,
                                state
                            )
                        }
                    }
                },
                loadingView = {
                    DetailOnlinePaymentContentOnLoading()
                },
                onCheckAgain = {},
                onTryAgain = {},
            )
        }
    }


    @Composable
    private fun DetailOnlinePaymentContent(
        modifier: Modifier,
        transaction: Transaction
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            Spacer(modifier = Modifier.height(Paddings.medium))
            DNAText(
                text = transaction.amount.toMoneyString(transaction.currency),
                modifier = Modifier.align(CenterHorizontally),
                style = DnaTextStyle.Bold32
            )
            Spacer(modifier = Modifier.height(Paddings.medium))
            DNATextWithIcon(
                modifier = modifier.align(CenterHorizontally)
                    .padding(vertical = Paddings.extraSmall),
                text = stringResource(transaction.status.stringResource),
                style = DnaTextStyle.WithAlphaNormal12,
                icon = transaction.status.icon,
                textColor = transaction.status.textColor,
                backgroundColor = transaction.status.backgroundColor
            )
            Spacer(modifier = Modifier.height(Paddings.standard12dp))
            DNATextWithIcon(
                modifier = Modifier.padding(start = Paddings.medium, end = Paddings.medium)
                    .align(CenterHorizontally),
                text = transaction.description,
                style = DnaTextStyle.WithAlphaNormal14,
                icon = MR.images.ic_message
            )
            Spacer(modifier = Modifier.height(Paddings.large))
            PaymentDetails(modifier.padding(top = Paddings.small), transaction)
            Divider(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Paddings.medium),
                color = lightGrey
            )
            VerificationDetails(modifier, transaction)
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = Paddings.medium),
                color = lightGrey
            )
            SummaryDetails(modifier, transaction)
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = Paddings.medium),
                color = lightGrey
            )
            CustomerDetails(modifier, transaction)
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = Paddings.medium),
                color = lightGrey
            )
            LocationDetails(modifier, transaction)
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = Paddings.medium),
                color = lightGrey
            )
            PaymentPageDetails(modifier.padding(), transaction)
            Spacer(modifier = Modifier.height(Paddings.extraLarge))
        }
    }

    @Composable
    private fun PaymentDetails(
        modifier: Modifier,
        transaction: Transaction
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        DNAExpandBox(
            modifier,
            MR.strings.payment_detail,
            MR.images.ic_card,
            true,
        ) {
            DNADots {
                PainterDotsContent(
                    title = MR.strings.payment_method,
                    value = when (transaction.paymentMethod) {
                        OnlinePaymentMethod.CARD -> {
                            transaction.cardMask
                        }

                        OnlinePaymentMethod.CLICK_TO_PAY -> {
                            transaction.cardMask
                        }

                        else -> {
                            stringResource(transaction.paymentMethod.stringResource)
                        }
                    },
                    icon = when (transaction.paymentMethod) {
                        OnlinePaymentMethod.CARD -> {
                            if (transaction.cardType != null) {
                                PosPaymentCard.fromCardType(transaction.cardType).imageResource
                            } else {
                                null
                            }
                        }
                        else -> {
                            transaction.paymentMethod.imageResource
                        }
                    }
                )
            }
            DNADots {
                DefaultDotsContent(
                    title = MR.strings.charged_amount,
                    value = transaction.currency + " " + transaction.amount.toString()
                )
            }
            DNADots {
                DefaultDotsContent(
                    title = MR.strings.date,
                    value = transaction.createdDate
                )
            }
            DNADots {
                ClipboardDotsContent(
                    MR.strings.id,
                    transaction.id,
                    clipboardManager
                )
            }
            DNADots {
                ClipboardDotsContent(
                    MR.strings.order_number,
                    transaction.invoiceId,
                    clipboardManager
                )
            }
            DNADots {
                ClipboardDotsContent(
                    MR.strings.rnr,
                    transaction.reference,
                    clipboardManager
                )
            }
            DNADots(isContinued = false) {
                PainterWithBackgroundDotsContent(
                    MR.strings.transaction_type,
                    transaction.transactionType.value,
                    transaction.transactionType.imageResource,
                    transaction.transactionType.backgroundColor
                )
            }
        }
    }

    @Composable
    private fun VerificationDetails(
        modifier: Modifier,
        transaction: Transaction
    ) {
        DNAExpandBox(
            modifier,
            MR.strings.verification,
            MR.images.ic_verified_detail,
            isFirst = false
        ) {
            DNADots {
                DefaultDotsContent(
                    MR.strings.three_d_secure_short,
                    transaction.secure3D.toString()
                )
            }
            DNADots {
                DefaultDotsContent(
                    MR.strings.avs_house_number,
                    transaction.avsHouseNumberResult
                )
            }
            DNADots {
                DefaultDotsContent(
                    MR.strings.csc_result,
                    transaction.cscResult
                )
            }
            DNADots {
                DefaultDotsContent(
                    MR.strings.pa_enrollment,
                    transaction.paEnrollment
                )
            }
            DNADots(isContinued = false) {
                DefaultDotsContent(
                    MR.strings.pa_authentication,
                    transaction.paAuthentication
                )
            }
        }
    }

    @Composable
    private fun SummaryDetails(
        modifier: Modifier,
        transaction: Transaction
    ) {
        DNAExpandBox(
            modifier,
            MR.strings.summary,
            MR.images.ic_bill,
            isFirst = false
        ) {
            DNADots {
                DefaultDotsContent(
                    MR.strings.processing_type,
                    transaction.processingTypeName
                )
            }
            DNADots {
                DefaultDotsContent(
                    MR.strings.store,
                    transaction.shop ?: ""
                )
            }
            DNADots {
                MessageDotsContent(
                    MR.strings.description,
                    transaction.description
                )
            }
            DNADots {
                DefaultDotsContent(
                    MR.strings.authorised_on,
                    transaction.authDate,
                )
            }
            DNADots {
                DefaultDotsContent(
                    MR.strings.issuer,
                    transaction.issuer,
                )
            }
            DNADots {
                PainterDotsContent(
                    title = MR.strings.payment_method,
                    value = when (transaction.paymentMethod) {
                        OnlinePaymentMethod.CARD -> {
                            transaction.cardMask
                        }

                        OnlinePaymentMethod.CLICK_TO_PAY -> {
                            transaction.cardMask
                        }

                        else -> {
                            stringResource(transaction.paymentMethod.stringResource)
                        }
                    },
                    icon = when (transaction.paymentMethod) {
                        OnlinePaymentMethod.CARD -> {
                            if (transaction.cardType != null) {
                                PosPaymentCard.fromCardType(transaction.cardType).imageResource
                            } else {
                                null
                            }
                        }

                        else -> {
                            transaction.paymentMethod.imageResource
                        }
                    }
                )
            }
            DNADots {
                DefaultDotsContent(
                    MR.strings.acquirer_response_code,
                    transaction.acquirerResponseCode,
                )
            }
            DNADots(isContinued = false) {
                DefaultDotsContent(
                    MR.strings.auth_code,
                    transaction.authCode
                )
            }
        }
    }

    @Composable
    private fun CustomerDetails(
        modifier: Modifier,
        transaction: Transaction
    ) {
        DNAExpandBox(
            modifier,
            MR.strings.customer_details,
            MR.images.ic_customer,
            isFirst = false
        ) {
            DNADots {
                DefaultDotsContent(
                    MR.strings.name,
                    transaction.payerName
                )
            }
            DNADots {
                DefaultDotsContent(
                    MR.strings.account_id,
                    transaction.accountId
                )
            }
            DNADots {
                DefaultDotsContent(
                    MR.strings.email,
                    transaction.payerEmail
                )
            }
            DNADots(isContinued = false) {
                DefaultDotsContent(
                    MR.strings.phone,
                    transaction.payerPhone
                )
            }
        }
    }

    @Composable
    private fun LocationDetails(
        modifier: Modifier,
        transaction: Transaction
    ) {
        DNAExpandBox(
            modifier,
            MR.strings.location,
            MR.images.ic_location,
            isFirst = false
        ) {
            DNADots {
                DefaultDotsContent(
                    MR.strings.payer_ip,
                    transaction.payerIp
                )
            }
            DNADots(isContinued = false) {
                DefaultDotsContent(
                    MR.strings.description,
                    transaction.ipCity
                )
            }
        }
    }

    @Composable
    private fun PaymentPageDetails(
        modifier: Modifier,
        transaction: Transaction
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        DNAExpandBox(
            modifier,
            MR.strings.payment_page,
            MR.images.ic_link,
            isFirst = false
        ) {
            DNADots {
                DefaultDotsContent(
                    MR.strings.language,
                    transaction.language
                )
            }
            DNADots {
                LinkDotsContent(
                    MR.strings.post_link_address,
                    transaction.postLink,
                    clipboardManager
                )
            }
            DNADots(isContinued = false) {
                DefaultDotsContent(
                    MR.strings.post_link,
                    transaction.postLinkStatus.toString()
                )
            }
        }
    }

    @Composable
    private fun DetailOnlinePaymentContentOnLoading(
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(Paddings.medium))
            ComponentRectangleLineShort(modifier.align(CenterHorizontally))
            Spacer(modifier = Modifier.height(Paddings.medium))
            ComponentRectangleLineShort(modifier.align(CenterHorizontally))
            Spacer(modifier = Modifier.height(Paddings.standard12dp))
            ComponentRectangleLineLong(
                modifier.align(CenterHorizontally)
                    .padding(start = Paddings.medium, end = Paddings.medium)
            )
            Spacer(modifier = Modifier.height(Paddings.large))
            DNAExpandBoxOnLoading()
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = Paddings.medium),
                color = lightGrey
            )
            DNAExpandBoxOnLoading()
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = Paddings.medium),
                color = lightGrey
            )
            DNAExpandBoxOnLoading()
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = Paddings.medium),
                color = lightGrey
            )
            DNAExpandBoxOnLoading()
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = Paddings.medium),
                color = lightGrey
            )
            Spacer(modifier = Modifier.height(Paddings.large))
        }
    }

    @Composable
    private fun ZStackReceiptButton(
        transaction: Transaction,
        navigator: Navigator,
        state: DetailOnlinePaymentContract.State,
        modifier: Modifier = Modifier
    ) {
        if (state.isReceiptVisible) {
            Box(
                modifier.zIndex(2f)
            ) {
                Column {
                    Spacer(
                        modifier = modifier.fillMaxWidth().weight(1f)
                    )
                    DNAYellowButton(
                        content = {
                            DNAText(
                                text = stringResource(MR.strings.receipt),
                                style = DnaTextStyle.Medium16,
                                modifier = modifier.padding(vertical = Paddings.extraSmall)
                            )
                        },
                        onClick = { navigator.push(GetReceiptScreen(transaction)) },
                        modifier = modifier.fillMaxWidth().padding(
                            start = Paddings.medium,
                            end = Paddings.medium,
                            bottom = Paddings.small
                        )
                    )
                }
            }
        }
    }
}