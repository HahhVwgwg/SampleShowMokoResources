package com.dnapayments.mp.data.model.authorization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthToken(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("is_admin")
    val isAdmin: Boolean? = false,
    val permissions: List<String>? = null
)