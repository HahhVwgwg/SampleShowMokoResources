package com.dna.payments.kmm.utils.pull_to_refresh

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dna.payments.kmm.MR
import com.dna.payments.kmm.presentation.theme.Paddings
import dev.icerock.moko.resources.compose.painterResource


@Composable
fun DNAPullToRefreshIndicator(
    refreshTriggerDistance: Dp,
    state: PullToRefreshState
) {
    val refreshTriggerPx = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
    val indicatorSize = 24.dp
    val indicatorHeightPx = with(LocalDensity.current) { indicatorSize.toPx() }
    val rotation: Float
    val scaleFraction: Float
    val alphaFraction: Float
    if (!state.isRefreshing) {
        val progress = (state.contentOffset / refreshTriggerPx.coerceAtLeast(1f))
            .coerceIn(0f, 1f)
        rotation = progress * 360
        scaleFraction = LinearOutSlowInEasing.transform(progress)
        alphaFraction = progress
    } else {
        val transition = rememberInfiniteTransition()
        rotation = transition.animateValue(
            0f,
            1f,
            Float.VectorConverter,
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 2000, // 1 and 1/3 second
                    easing = LinearEasing
                )
            )
        ).value * 360
        scaleFraction = 1f
        alphaFraction = 1f
    }
    Image(
        painter = painterResource(MR.images.refresh_custom),
        contentDescription = null,
        modifier = Modifier
            .graphicsLayer {
                translationY = (state.contentOffset - indicatorHeightPx) / 2f
                scaleX = scaleFraction
                scaleY = scaleFraction
                alpha = alphaFraction
            }
            .size(indicatorSize)
            .rotate(rotation)
    )
}

@Composable
fun DNAPullToRefreshIndicatorBottom() {
    val indicatorSize = 24.dp

    val transition = rememberInfiniteTransition()

    val rotation = transition.animateValue(
        0f,
        1f,
        Float.VectorConverter,
        infiniteRepeatable(
            animation = tween(
                durationMillis = 2000, // 2 second
                easing = LinearEasing
            )
        )
    ).value * 360

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Paddings.small)
    ) {
        Image(
            painter = painterResource(MR.images.refresh_custom),
            contentDescription = null,
            modifier = Modifier
                .size(indicatorSize)
                .rotate(rotation)
        )
    }
}