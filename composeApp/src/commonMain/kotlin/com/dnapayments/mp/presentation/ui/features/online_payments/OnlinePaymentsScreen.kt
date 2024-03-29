package com.dnapayments.mp.presentation.ui.features.online_payments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import com.dnapayments.mp.MR
import com.dnapayments.mp.domain.model.online_payments.OnlinePaymentMethod.CARD
import com.dnapayments.mp.domain.model.online_payments.OnlinePaymentMethod.CLICK_TO_PAY
import com.dnapayments.mp.domain.model.pos_payments.PosPaymentCard
import com.dnapayments.mp.domain.model.transactions.Transaction
import com.dnapayments.mp.presentation.state.ComponentCircle
import com.dnapayments.mp.presentation.state.ComponentRectangleLineShort
import com.dnapayments.mp.presentation.state.Empty
import com.dnapayments.mp.presentation.state.PaginationUiStateManager
import com.dnapayments.mp.presentation.theme.DnaTextStyle
import com.dnapayments.mp.presentation.theme.Paddings
import com.dnapayments.mp.presentation.ui.common.DNAText
import com.dnapayments.mp.presentation.ui.common.DNATextWithIcon
import com.dnapayments.mp.presentation.ui.common.DnaFilter
import com.dnapayments.mp.presentation.ui.features.date_range.DateRangeBottomSheet
import com.dnapayments.mp.presentation.ui.features.date_range.DateRangeWidget
import com.dnapayments.mp.presentation.ui.features.online_payments.detail.DetailOnlinePaymentScreen
import com.dnapayments.mp.presentation.ui.features.online_payments.status.StatusBottomSheet
import com.dnapayments.mp.presentation.ui.features.online_payments.status.StatusWidget
import com.dnapayments.mp.utils.extension.noRippleClickable
import com.dnapayments.mp.utils.extension.toCurrencySymbol
import com.dnapayments.mp.utils.navigation.LocalNavigator
import com.dnapayments.mp.utils.navigation.OnlinePaymentNavigatorResult
import com.dnapayments.mp.utils.navigation.OnlinePaymentNavigatorResultType
import com.dnapayments.mp.utils.navigation.currentOrThrow
import com.dnapayments.mp.utils.navigation.drawer_navigation.DrawerScreen
import com.dnapayments.mp.utils.navigation.pushWithResult
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.collectLatest

class OnlinePaymentsScreen : DrawerScreen {
    override val key: ScreenKey = "OnlinePaymentsScreen"
    override val isFilterEnabled: Boolean = true

    @Composable
    override fun Content() {

    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun DrawerContent(isToolbarCollapsed: Boolean) {
        val onlinePaymentsViewModel = getScreenModel<OnlinePaymentsViewModel>()
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
        val state by onlinePaymentsViewModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(key1 = Unit) {
            onlinePaymentsViewModel.effect.collectLatest { effect ->
                when (effect) {
                    is OnlinePaymentsContract.Effect.OnPageChanged -> {
                        pagerState.animateScrollToPage(effect.position)
                    }
                }
            }
        }
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                onlinePaymentsViewModel.setEvent(OnlinePaymentsContract.Event.OnPageChanged(page))
            }
        }

        OnlinePaymentsContent(
            modifier = Modifier.wrapContentHeight(),
            state = state,
            isToolbarCollapsed = isToolbarCollapsed,
            onRequestNextPage = {
                onlinePaymentsViewModel.setEvent(OnlinePaymentsContract.Event.OnLoadMore)
            },
            onClick = {
                navigator.pushWithResult(
                    OnlinePaymentNavigatorResult(
                        OnlinePaymentNavigatorResultType.DEFAULT,
                    ),
                    DetailOnlinePaymentScreen(it)
                )
            }
        ) {
            onlinePaymentsViewModel.setEvent(OnlinePaymentsContract.Event.OnRefresh)
        }
    }

    @Composable
    private fun OnlinePaymentsContent(
        modifier: Modifier = Modifier,
        state: OnlinePaymentsContract.State,
        isToolbarCollapsed: Boolean,
        onRequestNextPage: () -> Unit = {},
        onClick: (Transaction) -> Unit,
        onRefresh: () -> Unit = {}

    ) {
        Column(
            modifier = modifier.padding(horizontal = Paddings.medium),
            verticalArrangement = Arrangement.Top
        ) {
            PaginationUiStateManager(
                modifier = modifier.fillMaxSize(),
                resourceUiState = state.pagingUiState,
                pagingList = state.onlinePaymentList,
                onRequestNextPage = onRequestNextPage,
                onRefresh = onRefresh,
                isToolbarCollapsed = isToolbarCollapsed,
                successItemView = { transaction ->
                    OnlinePaymentItem(
                        transaction = transaction,
                        onClick = onClick
                    )

                },
                loadingView = { OnlinePaymentOnLoading() },
                emptyView = { Empty(text = stringResource(MR.strings.no_payments_yet)) }
            )
        }
    }

    @Composable
    private fun OnlinePaymentItem(
        transaction: Transaction,
        modifier: Modifier = Modifier,
        onClick: (Transaction) -> Unit
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current

        Box(
            modifier = modifier.padding(top = 2.dp, bottom = 6.dp)
                .shadow(2.dp, shape = RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .wrapContentHeight()
                .noRippleClickable {
                    onClick(transaction)
                }
        ) {
            Column(modifier = modifier.padding(Paddings.medium)) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (transaction.transactionType.backgroundColor != null && transaction.transactionType.imageResource != null) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        transaction.transactionType.backgroundColor,
                                        CircleShape
                                    )
                                    .size(40.dp)
                            ) {
                                Image(
                                    painter = painterResource(transaction.transactionType.imageResource),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(Paddings.small)
                                        .height(24.dp)
                                        .width(24.dp)
                                )
                            }
                        }
                        DNAText(
                            text = transaction.currency.toCurrencySymbol() + " " + transaction.amount.toString(),
                            style = DnaTextStyle.SemiBold20,
                            modifier = Modifier.padding(start = Paddings.small)
                        )
                    }
                    DNATextWithIcon(
                        text = stringResource(transaction.status.stringResource),
                        style = DnaTextStyle.WithAlphaNormal12,
                        icon = transaction.status.icon,
                        textColor = transaction.status.textColor,
                        backgroundColor = transaction.status.backgroundColor,
                        modifier = Modifier.padding(vertical = Paddings.extraSmall)
                    )
                }
                Spacer(modifier = Modifier.height(Paddings.medium))
                Divider()
                Spacer(modifier = Modifier.height(Paddings.medium))
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DNAText(
                        style = DnaTextStyle.WithAlpha14,
                        text = stringResource(MR.strings.payment_method)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier.noRippleClickable {
                            clipboardManager.setText(AnnotatedString(transaction.invoiceId))
                        }
                    ) {
                        when (transaction.paymentMethod) {
                            CARD -> {
                                if (transaction.cardType != null) {
                                    PosPaymentCard.fromCardType(transaction.cardType).imageResource?.let {
                                        painterResource(
                                            it
                                        )
                                    }
                                } else {
                                    null
                                }
                            }

                            else -> {
                                transaction.paymentMethod.imageResource?.let {
                                    painterResource(it)
                                }
                            }
                        }?.let {
                            Icon(
                                painter = it,
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.height(24.dp).width(24.dp)
                            )
                        }
                        DNAText(
                            modifier = modifier.padding(start = Paddings.small),
                            style = DnaTextStyle.Medium14,
                            text = when (transaction.paymentMethod) {
                                CARD -> {
                                    transaction.cardMask
                                }

                                CLICK_TO_PAY -> {
                                    transaction.cardMask
                                }

                                else -> {
                                    stringResource(transaction.paymentMethod.stringResource)
                                }
                            }
                        )
                    }

                }
                Spacer(modifier = Modifier.height(Paddings.medium))
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DNAText(
                        style = DnaTextStyle.WithAlpha14,
                        text = stringResource(MR.strings.order_number)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier.noRippleClickable {
                            clipboardManager.setText(AnnotatedString(transaction.invoiceId))
                        }
                    ) {
                        DNAText(
                            style = DnaTextStyle.Medium14,
                            text = transaction.invoiceId
                        )
                        Icon(
                            painter = painterResource(MR.images.ic_copy),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.height(24.dp).width(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Paddings.medium))
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DNAText(
                        style = DnaTextStyle.WithAlpha14,
                        text = stringResource(MR.strings.customer)
                    )
                    DNAText(
                        style = DnaTextStyle.Medium14,
                        text = transaction.payerName
                    )
                }
                Spacer(modifier = Modifier.height(Paddings.small))
            }
        }
    }

    @Composable
    private fun OnlinePaymentOnLoading(
        modifier: Modifier = Modifier,
    ) {
        Box(
            modifier = modifier.padding(top = 2.dp, bottom = 6.dp)
                .shadow(2.dp, shape = RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = modifier.padding(Paddings.medium)) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ComponentCircle(modifier = Modifier.size(40.dp))
                        ComponentRectangleLineShort(modifier = Modifier.padding(start = Paddings.small))
                    }
                    ComponentRectangleLineShort()
                }
                Spacer(modifier = Modifier.height(Paddings.medium))
                Divider()
                Spacer(modifier = Modifier.height(Paddings.medium))
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ComponentRectangleLineShort()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ComponentRectangleLineShort(modifier = Modifier.padding(start = Paddings.small))
                    }

                }
                Spacer(modifier = Modifier.height(Paddings.medium))
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ComponentRectangleLineShort()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ComponentRectangleLineShort(modifier = Modifier.padding(start = Paddings.small))
                    }
                }
                Spacer(modifier = Modifier.height(Paddings.medium))
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ComponentRectangleLineShort()
                    ComponentRectangleLineShort(modifier = Modifier.padding(start = Paddings.small))
                }
                Spacer(modifier = Modifier.height(Paddings.small))
            }
        }
    }

    @Composable
    override fun DrawerHeader() {  // Just for testing purposes
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            DNAText(
                text = stringResource(MR.strings.online_payments),
                style = DnaTextStyle.Bold20,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }

    @Composable
    override fun DrawerFilter() {  // Just for testing purposes
        val onlinePaymentsViewModel = getScreenModel<OnlinePaymentsViewModel>()
        val state by onlinePaymentsViewModel.uiState.collectAsState()
        val statusFilter = rememberSaveable { mutableStateOf(false) }
        val openDatePickerFilter = rememberSaveable { mutableStateOf(false) }

        LazyRow(modifier = Modifier.padding(start = Paddings.small)) {
            item {
                DnaFilter(
                    openBottomSheet = statusFilter,
                    dropDownContent = {
                        StatusWidget(state)
                    },
                    bottomSheetContent = {
                        StatusBottomSheet(
                            state = state,
                            onItemChange = {
                                statusFilter.value = false
                                onlinePaymentsViewModel.setEvent(
                                    OnlinePaymentsContract.Event.OnStatusChange(
                                        it
                                    )
                                )
                            }
                        )
                    }
                )
            }
            item {
                DnaFilter(
                    openBottomSheet = openDatePickerFilter,
                    dropDownContent = {
                        DateRangeWidget(
                            state.dateRange.first
                        )
                    },
                    bottomSheetContent = {
                        DateRangeBottomSheet(
                            dateSelection = state.dateRange.second,
                            onDatePeriodClick = {
                                openDatePickerFilter.value = false
                                onlinePaymentsViewModel.setEvent(
                                    OnlinePaymentsContract.Event.OnDateSelection(
                                        it
                                    )
                                )
                            }
                        )
                    }
                )
            }
        }
    }
}