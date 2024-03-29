package com.dnapayments.mp.domain.model.payment_methods.domain

import com.dnapayments.mp.domain.model.payment_methods.setting.PaymentMethodType
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class Domain(
    val name: String,
    val paymentMethodType: PaymentMethodType,
    val isDeleteAvailable: Boolean = true
) : Parcelable
