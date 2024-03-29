package com.dnapayments.mp.domain.model.transactions

import com.dnapayments.mp.domain.model.online_payments.OnlinePaymentStatus
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class TransactionParam(
    var amount: Double,
    var status: OnlinePaymentStatus,
    val currency: String,
    val id: String
) : Parcelable {
    companion object {
        fun newInstance(from: Transaction): TransactionParam = TransactionParam(
            amount = from.amount,
            currency = from.currency,
            id = from.id,
            status = from.status
        )
    }
}