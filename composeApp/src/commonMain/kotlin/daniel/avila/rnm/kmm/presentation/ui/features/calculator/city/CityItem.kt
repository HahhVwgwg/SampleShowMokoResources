package daniel.avila.rnm.kmm.presentation.ui.features.calculator.city

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import daniel.avila.rnm.kmm.domain.model.city.City

@Composable
fun CityItem(city: City, selectedCity: MutableState<City>) {

    Column(modifier = Modifier.fillMaxWidth()
        .wrapContentHeight()
        .clickable(
            interactionSource = MutableInteractionSource(),
            indication = null,
            onClick = {
                selectedCity.value = city
            }
        )) {

        Divider(
            color = MaterialTheme.colors.secondary, thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        )
        {
            RadioButton(
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colors.surface,
                    unselectedColor = MaterialTheme.colors.primaryVariant.copy(alpha = 0.7f)
                ),
                interactionSource = MutableInteractionSource(),
                selected = city == selectedCity.value,
                onClick = { selectedCity.value = city },
            )
            Text(
                text = city.name,
                style = MaterialTheme.typography.button
            )
        }
    }
}
