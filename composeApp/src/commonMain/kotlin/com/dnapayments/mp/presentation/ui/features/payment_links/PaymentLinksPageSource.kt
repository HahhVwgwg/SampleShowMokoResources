package com.dnapayments.mp.presentation.ui.features.payment_links

import com.dnapayments.mp.domain.interactors.use_cases.payment_link.PaymentLinkUseCase
import com.dnapayments.mp.domain.model.payment_links.PaymentLinkByPeriod
import com.dnapayments.mp.domain.model.payment_links.PaymentLinksSearchParameters
import com.dnapayments.mp.domain.network.Response
import com.dnapayments.mp.domain.interactors.page_source.PageSource

class PaymentLinksPageSource(
    private val paymentLinkUseCase: PaymentLinkUseCase
) : PageSource<PaymentLinkByPeriod>() {

    private lateinit var search: PaymentLinksSearchParameters

    override suspend fun onLoadMore(): Response<List<PaymentLinkByPeriod>> {
        if (noMoreItems) return Response.Success(remoteData)
        currentPage++
        search.page = currentPage
        search.size = TAKE_ITEM_SIZE_IN_ONE_REQUEST

        return when (val response = paymentLinkUseCase(
            startDate = search.startDate,
            endDate = search.endDate,
            status = search.status,
            page = search.page,
            size = search.size
        )) {
            is Response.Success -> handleSuccessResponse(response.data)
            is Response.Error -> handleError(response.error)
            is Response.NetworkError -> handleNetworkError()
            is Response.TokenExpire -> handleNetworkError()
        }
    }

    private fun handleSuccessResponse(list: List<PaymentLinkByPeriod>): Response<List<PaymentLinkByPeriod>> {
        remoteData.addAll(list)
        noMoreItems = list.count() <= TAKE_ITEM_SIZE_IN_ONE_REQUEST
        return Response.Success(remoteData)
    }

    override fun updateParameters(any: Any) {
        search = any as PaymentLinksSearchParameters
    }

    override fun onReset() {
        remoteData.clear()
        currentPage = FIRST_PAGE
        noMoreItems = false
    }
}