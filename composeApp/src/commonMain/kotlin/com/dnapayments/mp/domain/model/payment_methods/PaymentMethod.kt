package com.dnapayments.mp.domain.model.payment_methods

import com.dnapayments.mp.domain.model.payment_methods.setting.PaymentMethodType
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource

@Parcelize
data class PaymentMethod(
    val title: StringResource,
    val icon: ImageResource,
    val description: StringResource,
    val type: PaymentMethodType
) : Parcelable

