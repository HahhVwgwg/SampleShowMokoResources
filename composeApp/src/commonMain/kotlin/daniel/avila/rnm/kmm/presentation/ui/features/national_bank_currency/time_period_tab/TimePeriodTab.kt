package daniel.avila.rnm.kmm.presentation.ui.features.national_bank_currency.time_period_tab

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import daniel.avila.rnm.kmm.domain.model.time_period_tab.TimePeriod
import daniel.avila.rnm.kmm.domain.model.time_period_tab.TimePeriodTab
import daniel.avila.rnm.kmm.presentation.ui.common.RoundedBackground

@Composable
fun TimePeriodsTab(modifier: Modifier, onTabSelected: (TimePeriodTab) -> Unit) {
    val list = listOf(
        TimePeriodTab("Неделя", true, TimePeriod.WEEK),
        TimePeriodTab("Месяц", false, TimePeriod.MONTH),
        TimePeriodTab("3 месяца", false, TimePeriod.THREE_MONTHS),
        TimePeriodTab("Год", false, TimePeriod.YEAR)
    )

    LaunchedEffect(list) {
        onTabSelected(list.first())
    }

    var selectedItem by remember { mutableStateOf(list.first()) }

    Row(
        modifier = modifier
            .wrapContentHeight()
            .padding(horizontal = 15.dp),
    ) {
        list.forEach { tabItem ->
            val isSelected = selectedItem == tabItem
            RoundedBackground(
                modifier = Modifier.wrapContentWidth(),
                backgroundColor = if (isSelected) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.secondary,
                height = 30.dp,
                paddingHorizontal = 14.dp,
                onClick = {
                    onTabSelected(tabItem)
                    selectedItem = tabItem
                },
            ) {
                Text(
                    text = tabItem.stringResId,
                    style = MaterialTheme.typography.button,
                    color = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSecondary,
                )
            }

            if (tabItem != list.last()) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}