package com.dna.payments.kmm.presentation.ui.features.overview_report.widgets.approval_average_metrics

import cafe.adriel.voyager.core.model.screenModelScope
import com.dna.payments.kmm.domain.interactors.use_cases.reports.approval_average_metrics.GetReportUseCase
import com.dna.payments.kmm.domain.model.average_metrics.Metric
import com.dna.payments.kmm.domain.model.currency.Currency
import com.dna.payments.kmm.domain.model.date_picker.DateSelection
import com.dna.payments.kmm.domain.model.overview_report.Summary
import com.dna.payments.kmm.domain.model.overview_report.OverviewReportType
import com.dna.payments.kmm.domain.model.overview_report.OverviewReportType.ONLINE_PAYMENTS
import com.dna.payments.kmm.domain.model.overview_report.OverviewReportType.POS_PAYMENTS
import com.dna.payments.kmm.domain.network.Response
import com.dna.payments.kmm.presentation.model.ResourceUiState
import com.dna.payments.kmm.presentation.mvi.BaseViewModel
import kotlinx.coroutines.launch

class ApprovalAverageMetricsViewModel(
    private val getReportUseCase: GetReportUseCase,
    private val overviewReportType: OverviewReportType
) : BaseViewModel<ApprovalAverageMetricsContract.Event, ApprovalAverageMetricsContract.State, ApprovalAverageMetricsContract.Effect>() {

    private lateinit var selectedCurrency: Currency
    private lateinit var dateSelection: DateSelection

    override fun createInitialState() =
        ApprovalAverageMetricsContract.State(
            paymentsSummaryList = ResourceUiState.Loading
        )

    override fun handleEvent(event: ApprovalAverageMetricsContract.Event) {
        when (event) {
            is ApprovalAverageMetricsContract.Event.OnFetchData -> {
                if (::selectedCurrency.isInitialized && ::dateSelection.isInitialized
                    && selectedCurrency == event.currency && dateSelection == event.dateSelection
                )
                    return

                selectedCurrency = event.currency
                dateSelection = event.dateSelection

                getPaymentsSummary()
            }
        }
    }

    private suspend fun fetchPaymentData(): Response<Pair<List<Summary>, List<Metric>>> {
        return when (overviewReportType) {
            POS_PAYMENTS -> {
                getReportUseCase.getPosPaymentsSummary(
                    startDate = dateSelection.startDate,
                    endDate = dateSelection.endDate,
                    currency = selectedCurrency.name
                )
            }
            ONLINE_PAYMENTS -> {
                getReportUseCase.getOnlinePaymentsSummary(
                    startDate = dateSelection.startDate,
                    endDate = dateSelection.endDate,
                    currency = selectedCurrency.name
                )
            }
        }
    }

    private fun getPaymentsSummary() {
        setState {
            copy(
                paymentsSummaryList = ResourceUiState.Loading
            )
        }
        screenModelScope.launch {
            val paymentSummaryList =
                when (val result = fetchPaymentData()) {
                    is Response.Success -> {
                        ResourceUiState.Success(result.data)
                    }
                    is Response.Error -> {
                        ResourceUiState.Error(result.error)
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
                    paymentsSummaryList = paymentSummaryList
                )
            }
        }
    }
}