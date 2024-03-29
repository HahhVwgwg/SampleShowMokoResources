package com.dnapayments.mp.presentation.ui.features.payment_links

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.rememberPagerState
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
import cafe.adriel.voyager.koin.getScreenModel
import com.dnapayments.mp.MR
import com.dnapayments.mp.domain.model.payment_links.PaymentLinkHeader
import com.dnapayments.mp.domain.model.payment_links.PaymentLinkItem
import com.dnapayments.mp.presentation.state.ComponentRectangleLineShort
import com.dnapayments.mp.presentation.state.Empty
import com.dnapayments.mp.presentation.state.PaginationUiStateManager
import com.dnapayments.mp.presentation.theme.Dimens
import com.dnapayments.mp.presentation.theme.DnaTextStyle
import com.dnapayments.mp.presentation.theme.Paddings
import com.dnapayments.mp.presentation.ui.common.DNAText
import com.dnapayments.mp.presentation.ui.common.DNATextWithIcon
import com.dnapayments.mp.presentation.ui.common.DnaFilter
import com.dnapayments.mp.presentation.ui.features.date_range.DateRangeBottomSheet
import com.dnapayments.mp.presentation.ui.features.date_range.DateRangeWidget
import com.dnapayments.mp.presentation.ui.features.payment_links.status.StatusBottomSheet
import com.dnapayments.mp.presentation.ui.features.payment_links.status.StatusWidget
import com.dnapayments.mp.utils.extension.noRippleClickable
import com.dnapayments.mp.utils.extension.toCurrencySymbol
import com.dnapayments.mp.utils.navigation.drawer_navigation.DrawerScreen
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.collectLatest

class PaymentLinksScreen : DrawerScreen {

    override val isFilterEnabled: Boolean = true

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun DrawerContent(isToolbarCollapsed: Boolean) {
        val paymentLinksViewModel = getScreenModel<PaymentLinksViewModel>()
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
        val state by paymentLinksViewModel.uiState.collectAsState()

        LaunchedEffect(key1 = Unit) {
            paymentLinksViewModel.effect.collectLatest { effect ->
                when (effect) {
                    is PaymentLinksContract.Effect.OnPageChanged -> {
                        pagerState.animateScrollToPage(effect.position)
                    }
                }
            }
        }

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                paymentLinksViewModel.setEvent(PaymentLinksContract.Event.OnPageChanged(page))
            }
        }

        PaymentLinksContent(
            modifier = Modifier.wrapContentHeight(),
            state = state,
            isToolbarCollapsed = isToolbarCollapsed,
            onRequestNextPage = {
                paymentLinksViewModel.setEvent(PaymentLinksContract.Event.OnLoadMore)
            }
        ) {
            paymentLinksViewModel.setEvent(PaymentLinksContract.Event.OnRefresh)
        }
    }

    @Composable
    override fun Content() {

    }

    @Composable
    private fun PaymentLinksContent(
        modifier: Modifier = Modifier,
        state: PaymentLinksContract.State,
        isToolbarCollapsed: Boolean,
        onRequestNextPage: () -> Unit = {},
        onRefresh: () -> Unit = {}
    ) {
        PaginationUiStateManager(
            modifier = modifier.fillMaxSize().padding(horizontal = Paddings.medium),
            resourceUiState = state.pagingUiState,
            pagingList = state.paymentLinkList,
            onRequestNextPage = onRequestNextPage,
            onRefresh = onRefresh,
            isToolbarCollapsed = isToolbarCollapsed,
            successItemView = { paymentLinkByPeriod ->
                when (paymentLinkByPeriod) {
                    is PaymentLinkItem -> {
                        PaymentLinkItem(paymentLinkItem = paymentLinkByPeriod)
                    }

                    is PaymentLinkHeader -> {
                        PaymentLinkItemHeader(paymentLinkHeader = paymentLinkByPeriod)
                    }
                }
            },
            loadingView = { PaymentLinkItemOnLoading() },
            emptyView = { Empty(text = stringResource(MR.strings.no_payment_links)) }
        )
    }

    @Composable
    override fun DrawerHeader() {
        Column {
            Spacer(modifier = Modifier.height(Paddings.large))
            DNAText(
                text = stringResource(MR.strings.payment_links),
                style = DnaTextStyle.Bold20,
                modifier = Modifier.padding(
                    horizontal = Paddings.medium,
                    vertical = Paddings.standard
                )
            )
        }
    }

    @Composable
    override fun DrawerFilter() {
        val paymentLinksViewModel = getScreenModel<PaymentLinksViewModel>()
        val state by paymentLinksViewModel.uiState.collectAsState()
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
                                paymentLinksViewModel.setEvent(
                                    PaymentLinksContract.Event.OnStatusChange(
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
                                paymentLinksViewModel.setEvent(
                                    PaymentLinksContract.Event.OnDateSelection(
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

    @Composable
    private fun PaymentLinkItemHeader(
        modifier: Modifier = Modifier,
        paymentLinkHeader: PaymentLinkHeader
    ) {
        Box(
            modifier = modifier.padding(top = Paddings.small)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DNAText(
                    style = DnaTextStyle.WithAlpha14,
                    text = paymentLinkHeader.title.getText()
                )
            }
        }
    }

    @Composable
    private fun PaymentLinkItem(
        modifier: Modifier = Modifier,
        paymentLinkItem: PaymentLinkItem
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current

        Box(
            modifier = modifier.padding(vertical = Paddings.small)
                .shadow(2.dp, shape = RoundedCornerShape(Paddings.small))
                .background(Color.White, RoundedCornerShape(Paddings.small))
                .fillMaxWidth()
                .wrapContentHeight()
                .noRippleClickable {

                }
        ) {
            Column(modifier = modifier.padding(Paddings.medium)) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DNAText(
                        text = paymentLinkItem.currency.toCurrencySymbol() + " " + paymentLinkItem.amount.toString(),
                        style = DnaTextStyle.SemiBold20
                    )
                    DNATextWithIcon(
                        text = stringResource(paymentLinkItem.status.stringResource),
                        style = DnaTextStyle.WithAlphaNormal12,
                        icon = paymentLinkItem.status.icon,
                        secondIcon = paymentLinkItem.status.iconEnd,
                        textColor = paymentLinkItem.status.textColor,
                        backgroundColor = paymentLinkItem.status.backgroundColor
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
                        text = stringResource(MR.strings.customer)
                    )
                    DNAText(
                        style = DnaTextStyle.Medium14,
                        text = paymentLinkItem.customerName
                    )
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
                            clipboardManager.setText(AnnotatedString(paymentLinkItem.invoiceId))
                        }
                    ) {
                        DNAText(
                            style = DnaTextStyle.Medium14,
                            text = paymentLinkItem.invoiceId
                        )
                        Icon(
                            painter = painterResource(MR.images.ic_copy),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(Dimens.iconSize)
                        )
                    }

                }

                Spacer(modifier = Modifier.height(Paddings.small))
            }
        }
    }

    @Composable
    private fun PaymentLinkItemOnLoading(
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
                    ComponentRectangleLineShort()
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
            }
        }
    }
}