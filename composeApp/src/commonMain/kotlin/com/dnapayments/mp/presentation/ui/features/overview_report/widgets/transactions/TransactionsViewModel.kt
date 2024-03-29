package com.dnapayments.mp.presentation.ui.features.overview_report.widgets.transactions

import cafe.adriel.voyager.core.model.screenModelScope
import com.dnapayments.mp.domain.interactors.use_cases.date_picker.DateHelper
import com.dnapayments.mp.domain.interactors.use_cases.reports.transactions.OnlineSummaryGraphUseCase
import com.dnapayments.mp.domain.interactors.use_cases.reports.transactions.PosSummaryGraphUseCase
import com.dnapayments.mp.domain.model.currency.Currency
import com.dnapayments.mp.domain.model.date_picker.DateSelection
import com.dnapayments.mp.domain.model.online_payments.OnlinePaymentStatus
import com.dnapayments.mp.domain.model.overview_report.OverviewReportType
import com.dnapayments.mp.domain.model.payment_status.PaymentStatus
import com.dnapayments.mp.domain.model.pos_payments.PosPaymentStatus
import com.dnapayments.mp.domain.network.Response
import com.dnapayments.mp.presentation.model.ResourceUiState
import com.dnapayments.mp.presentation.mvi.BaseViewModel
import com.dnapayments.mp.utils.chart.histogram.HistogramEntry
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val posSummaryGraphUseCase: PosSummaryGraphUseCase,
    private val onlineSummaryGraphUseCase: OnlineSummaryGraphUseCase,
    private val dateHelper: DateHelper,
    overviewReportType: OverviewReportType
) :
    BaseViewModel<TransactionsContract.Event, TransactionsContract.State, TransactionsContract.Effect>() {

    private lateinit var selectedCurrency: Currency
    private lateinit var dateSelection: DateSelection

    init {
        val paymentPair = when (overviewReportType) {
            OverviewReportType.POS_PAYMENTS -> Pair(PosPaymentStatus.entries, PosPaymentStatus.ALL)
            OverviewReportType.ONLINE_PAYMENTS -> Pair(
                OnlinePaymentStatus.getMainStatuses(),
                OnlinePaymentStatus.CHARGE
            )
        }
        setState {
            copy(
                paymentStatusList = paymentPair.first,
                selectedPaymentStatus = paymentPair.second
            )
        }
    }

    override fun createInitialState() = TransactionsContract.State(
        paymentsGraphSummary = ResourceUiState.Idle,
        paymentStatusList = emptyList(),
        selectedPaymentStatus = OnlinePaymentStatus.CHARGE,
    )

    override fun handleEvent(event: TransactionsContract.Event) {
        when (event) {
            is TransactionsContract.Event.OnFetchData -> {
                if (::selectedCurrency.isInitialized && ::dateSelection.isInitialized
                    && selectedCurrency == event.currency && dateSelection == event.dateSelection
                )
                    return

                selectedCurrency = event.currency
                dateSelection = event.dateSelection

                getPaymentsGraphSummary(currentState.selectedPaymentStatus)
            }
            is TransactionsContract.Event.OnPaymentStatusChange -> {

                if (!::selectedCurrency.isInitialized || !::dateSelection.isInitialized) return

                setState {
                    copy(selectedPaymentStatus = event.paymentStatus)
                }

                getPaymentsGraphSummary(event.paymentStatus)
            }
        }
    }

    private suspend fun getRequestByPaymentStatus(paymentStatus: PaymentStatus): Response<Pair<HistogramEntry, HistogramEntry>> {
        val intervalType = dateHelper.findIntervalType(dateSelection)
        return when (paymentStatus) {
            is OnlinePaymentStatus -> {
                onlineSummaryGraphUseCase.getOnlineSummaryGraph(
                    startDate = dateSelection.startDate,
                    endDate = dateSelection.endDate,
                    currency = selectedCurrency.name,
                    status = paymentStatus.name,
                    intervalType = intervalType
                )
            }
            is PosPaymentStatus -> {
                posSummaryGraphUseCase.getPosSummaryGraph(
                    startDate = dateSelection.startDate,
                    endDate = dateSelection.endDate,
                    currency = selectedCurrency.name,
                    intervalType = intervalType,
                    status = paymentStatus
                )
            }
            else -> throw IllegalArgumentException("Unknown payment status")
        }
    }

    private fun getPaymentsGraphSummary(paymentStatus: PaymentStatus) {
        setState {
            copy(
                paymentsGraphSummary = ResourceUiState.Loading
            )
        }
        screenModelScope.launch {
            val summary = when (val result = getRequestByPaymentStatus(paymentStatus)) {
                is Response.Success -> {
                    ResourceUiState.Success(
                        result.data
                    )
                }
                is Response.Error -> {
                    ResourceUiState.Error(
                        result.error
                    )
                }
                is Response.NetworkError -> {
                    ResourceUiState.NetworkError
                }
                is Response.TokenExpire -> {
                    ResourceUiState.TokenExpire
                }
            }
            setState {
                copy(
                    paymentsGraphSummary = summary
                )
            }
        }
    }
}