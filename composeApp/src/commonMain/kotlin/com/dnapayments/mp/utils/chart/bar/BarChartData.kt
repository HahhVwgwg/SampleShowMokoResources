package com.dnapayments.mp.utils.chart.bar

import androidx.compose.runtime.Immutable
import com.dnapayments.mp.utils.chart.grid.GridChartData
import com.dnapayments.mp.utils.chart.line.LegendItemData
import com.dnapayments.mp.utils.chart.line.SymbolShape

@Immutable
data class BarChartData(
    val categories: List<BarChartCategory>,
) : GridChartData {
    // TODO hide those values from the user
    override val minX: Long = 0
    override val maxX: Long
        get() = if (categories.isEmpty()) {
            1
        } else {
            categories.size * 2.toLong()
        }

    override val minY: Float
        get() = if (categories.isEmpty()) {
            0f
        } else {
            categories.minOf { it.minY }.coerceAtMost(0f)
        }

    override val maxY: Float
        get() = if (categories.isEmpty()) {
            1f
        } else {
            categories.maxOf { it.maxY }.coerceAtLeast(0f)
        }

    override val legendData: List<LegendItemData>
        get() {
            return categories
                .flatMap { it.entries }
                .distinctBy { it.x }
                .map {
                    LegendItemData(
                        name = it.x,
                        symbolShape = SymbolShape.RECTANGLE,
                        color = it.color,
                        dashed = false,
                    )
                }
        }
}
