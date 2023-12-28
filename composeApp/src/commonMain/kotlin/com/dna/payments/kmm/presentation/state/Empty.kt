package com.dna.payments.kmm.presentation.state

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dna.payments.kmm.MR
import com.dna.payments.kmm.presentation.theme.Dimens
import com.dna.payments.kmm.presentation.theme.DnaTextStyle
import com.dna.payments.kmm.presentation.theme.Paddings
import com.dna.payments.kmm.presentation.ui.common.DNAText
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun Empty(
    modifier: Modifier = Modifier,
    text: String = ""
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Image(
            painterResource(MR.images.ic_empty),
            contentDescription = null,
            modifier = Modifier.size(Dimens.emptyIconSize)
        )
        Spacer(Modifier.height(Paddings.normal))
        DNAText(
            text = text,
            modifier = Modifier,
            style = DnaTextStyle.Normal14Grey4
        )
        Spacer(Modifier.weight(2f))
    }
}