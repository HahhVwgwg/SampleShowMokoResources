package com.dna.payments.kmm.utils.navigation

import androidx.compose.runtime.Composable

@Composable
internal expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)

expect val shouldUseSwipeBack: Boolean