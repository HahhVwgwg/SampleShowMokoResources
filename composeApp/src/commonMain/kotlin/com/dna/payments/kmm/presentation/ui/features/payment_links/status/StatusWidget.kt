package com.dna.payments.kmm.presentation.ui.features.payment_links.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dna.payments.kmm.MR
import com.dna.payments.kmm.domain.model.payment_links.PaymentLinkStatus
import com.dna.payments.kmm.presentation.theme.DnaTextStyle
import com.dna.payments.kmm.presentation.theme.Paddings
import com.dna.payments.kmm.presentation.theme.white
import com.dna.payments.kmm.presentation.ui.common.DNAText
import com.dna.payments.kmm.presentation.ui.features.payment_links.PaymentLinksContract
import com.dna.payments.kmm.utils.extension.noRippleClickable
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun StatusWidget(
    state: PaymentLinksContract.State
) {
//    ManagementResourceUiState(
//        resourceUiState = state.statusList,
//        successView = { status ->
    if (state.statusList.isEmpty())
        return
    DNAText(
        modifier = Modifier.wrapContentWidth(),
        text = stringResource(state.statusList[state.indexOfSelectedStatus].stringResourceStatus),
        style = DnaTextStyle.Medium14
    )

//        loadingView = {
//            ComponentRectangleLineShort(
//                modifier = Modifier
//                    .width(30.dp)
//            )
//        },
//        onCheckAgain = {},
//        onTryAgain = {},
//)
}


@Composable
fun StatusBottomSheet(
    state: PaymentLinksContract.State,
    onItemChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = white)
            .padding(Paddings.medium),
        verticalArrangement = Arrangement.Top
    ) {
        DNAText(
            text = stringResource(MR.strings.status),
            style = DnaTextStyle.SemiBold20
        )

        Spacer(modifier = Modifier.height(Paddings.medium))

        state.statusList.forEach { item ->
            StatusItem(
                status = item,
                isSelected = state.statusList[state.indexOfSelectedStatus] == item,
                onItemClick = {
                    onItemChange(state.statusList.indexOf(item))
                })
        }

        Spacer(modifier = Modifier.height(Paddings.medium))
    }
//    loadingView = {
//        LazyColumn {
//            item {
//                DNAText(text = stringResource(MR.strings.all_statuses))
//            }
//            items(10) {
//                ComponentRectangleLineShort(
//                    modifier = Modifier
//                        .width(30.dp)
//                )
//            }
//        }
//    },
//    onCheckAgain = {},
//    onTryAgain = {},
//    )
}

@Composable
fun StatusItem(status: PaymentLinkStatus, isSelected: Boolean, onItemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = Paddings.standard
            )
            .noRippleClickable {
                if (!isSelected) {
                    onItemClick()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DNAText(
            modifier = Modifier.weight(1f),
            text = stringResource(status.stringResourceStatus),
            style = if (isSelected) DnaTextStyle.SemiBold16 else DnaTextStyle.Medium16Grey5,
        )
        if (isSelected)
            Icon(
                modifier = Modifier.padding(start = Paddings.medium),
                painter = painterResource(
                    MR.images.ic_success
                ),
                tint = Color.Unspecified,
                contentDescription = null,
            )
    }
}