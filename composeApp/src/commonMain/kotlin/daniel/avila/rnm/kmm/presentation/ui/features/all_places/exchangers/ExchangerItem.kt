package daniel.avila.rnm.kmm.presentation.ui.features.all_places.exchangers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.seiko.imageloader.rememberImagePainter
import daniel.avila.rnm.kmm.domain.model.exchange_rate.ExchangeRate
import daniel.avila.rnm.kmm.presentation.ui.common.RoundedBackground
import daniel.avila.rnm.kmm.utils.extension.formatDistance

@Composable
fun ExchangerItem(
    item: ExchangeRate,
    isFirst: Boolean = false,
    onClick: () -> Unit,
) {
    val addBorder = item.location.tags.isNotEmpty()

    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        Row(
            modifier = Modifier.wrapContentHeight()
                .fillMaxWidth()
                .padding(top = if (addBorder) 14.dp else 0.dp)
                .border(
                    BorderStroke(
                        1.dp,
                        when {
                            addBorder && isFirst -> MaterialTheme.colors.surface
                            addBorder -> MaterialTheme.colors.secondary
                            else -> Color.Transparent
                        }
                    ),
                    shape = RoundedCornerShape(5.dp)
                ).clickable(
                    indication = null,
                    interactionSource = MutableInteractionSource()
                ) {
                    onClick()
                }
                .padding(start = 5.dp, end = 5.dp, top = 15.dp, bottom = 15.dp)
                .zIndex(0f)
        ) {
            Image(
                painter = rememberImagePainter(item.logo),
                contentDescription = null,
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f).wrapContentHeight()) {

                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.wrapContentHeight().weight(1f),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = item.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.button
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "~" + item.location.distance.formatDistance() + " • " + item.locationCount + " филиалов",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.h6
                        )
                    }
                    Text(
                        modifier = Modifier.wrapContentWidth().wrapContentHeight()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        textAlign = TextAlign.Center,
                        text = "Все пункты",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.surface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                item.currencyRateList.forEach {
                    CurrencyRateItem(
                        it
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 15.dp, end = 15.dp, top = if (addBorder) 5.dp else 0.dp),
        ) {
            item.location.tags.forEach { tagItem ->
                val isFillBlue = tagItem == item.location.tags.first() && isFirst
                RoundedBackground(
                    modifier = Modifier.wrapContentWidth(),
                    backgroundColor = if (isFillBlue) MaterialTheme.colors.surface else MaterialTheme.colors.secondary,
                    border = 10.dp,
                    height = 18.dp,
                    paddingHorizontal = 8.dp,
                ) {
                    Text(
                        text = tagItem.displayName,
                        style = MaterialTheme.typography.h5,
                        color = if (isFillBlue) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSecondary,
                    )
                }
            }
        }
    }
}