package com.dnapayments.mp.domain.model.online_payments

import com.dnapayments.mp.MR
import dev.icerock.moko.resources.StringResource

enum class OnlinePaymentOperationType(val stringResource: StringResource) {
    CHARGE(MR.strings.charge),
    REFUND(MR.strings.refund),
    PROCESS_NEW_PAYMENT(MR.strings.process_new_payment),
    CANCEL(MR.strings.cancel),
}