package com.dnapayments.mp.domain.interactors.use_cases.online_payments

import com.dnapayments.mp.data.model.search.OrderParameter
import com.dnapayments.mp.data.model.search.Paging
import com.dnapayments.mp.data.model.search.Search
import com.dnapayments.mp.data.model.search.SearchParameter
import com.dnapayments.mp.domain.model.search.field.Field
import com.dnapayments.mp.domain.model.search.type_order.TypeOrder
import com.dnapayments.mp.domain.repository.TransactionRepository
import com.dnapayments.mp.utils.extension.convertToServerFormat
import korlibs.time.DateTime
import korlibs.time.DateTimeSpan

class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: String) =
        transactionRepository.getTransactionBySearchParameter(
            search = Search(
                orderParameters = listOf(
                    OrderParameter(
                        field = Field.CREATED_DATE.value,
                        typeOrder = TypeOrder.DESC.name
                    ),
                    OrderParameter(
                        field = Field.ID.value,
                        typeOrder = TypeOrder.DESC.name
                    )
                ),
                paging = Paging(
                    0,
                    20
                ),
                searchParameters = listOf(
                    SearchParameter(
                        name = CREATED_DATE_NAME,
                        method = BETWEEN_METHOD,
                        searchParameter = listOf(
                            DateTime.now().minus(DateTimeSpan(years = 1)).convertToServerFormat(),
                            DateTime.now().convertToServerFormat(),
                        )
                    ),
                    SearchParameter(
                        name = TRANSACTION_NAME,
                        method = TRANSACTION_METHOD,
                        searchParameter = listOf(transactionId)
                    )
                )
            )
        )

    companion object {
        const val TRANSACTION_NAME = "id"
        const val TRANSACTION_METHOD = "in"
        const val BETWEEN_METHOD = "between"
        const val CREATED_DATE_NAME = "created_date"
    }
}
