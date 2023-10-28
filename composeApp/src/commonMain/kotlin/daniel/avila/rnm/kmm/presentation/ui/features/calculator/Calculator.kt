package daniel.avila.rnm.kmm.presentation.ui.features.calculator

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import daniel.avila.rnm.kmm.MR
import daniel.avila.rnm.kmm.domain.model.city.City
import daniel.avila.rnm.kmm.domain.model.currency.Currency
import daniel.avila.rnm.kmm.domain.model.exchange_rate.BuyOrSell
import daniel.avila.rnm.kmm.presentation.model.ResourceUiState
import daniel.avila.rnm.kmm.presentation.ui.common.BottomSheet
import daniel.avila.rnm.kmm.presentation.ui.common.RoundedBackground
import daniel.avila.rnm.kmm.presentation.ui.features.calculator.currency.CurrencyContract
import daniel.avila.rnm.kmm.presentation.ui.features.calculator.currency.CurrencyViewModel
import daniel.avila.rnm.kmm.presentation.ui.features.home.custom_main_tab.CustomTabBar
import daniel.avila.rnm.kmm.presentation.ui.features.home.custom_main_tab.TabItem
import daniel.avila.rnm.kmm.presentation.ui.features.calculator.exchange_list_main.ExchangeRateListMain
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calculator(modifier: Modifier = Modifier, city: City?) {

    var selectedTab by remember { mutableStateOf(TabItem("", false)) }

    var openBottomSheet by rememberSaveable { mutableStateOf(false) }

    var currencyList by remember { mutableStateOf<List<Currency>>(emptyList()) }

    var inputText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                "100",
                TextRange(3)
            )
        )
    }

    var currencies by remember { mutableStateOf<Pair<Currency, Currency>?>(null) }

    val currencyViewModel = koinInject<CurrencyViewModel>()

    val state by currencyViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        currencyViewModel.effect.collectLatest { effect ->
            when (effect) {
                is CurrencyContract.Effect.NavigateToCurrencyDialog -> {
                    currencyList = effect.currencies
                    openBottomSheet = true
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Spacer(modifier = Modifier.height(6.dp))

        CustomTabBar(modifier = Modifier.fillMaxWidth()) {
            selectedTab = it
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                value = inputText,
                onValueChange = { input ->
                    when {
                        input.text.length > 10 -> {

                        }
                        input.text.isEmpty() || input.text == "00" -> {
                            inputText = input.copy(text = "0", TextRange(1))
                        }
                        else -> inputText =
                            input.copy(input.text.filter { it.isDigit() }.trimStart('0'))
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    autoCorrect = false
                ),
                cursorBrush = Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.15f to Color.Transparent,
                    0.15f to Color.Black,
                    0.85f to Color.Black,
                    0.85f to Color.Transparent,
                    1.00f to Color.Transparent
                ),
                maxLines = 1,
                textStyle = MaterialTheme.typography.body2.copy(textAlign = TextAlign.End),
            )

            Spacer(modifier = Modifier.width(10.dp))

            RoundedBackground(
                modifier = Modifier.wrapContentWidth(),
                height = 36.dp,
                paddingHorizontal = 8.dp,
                onClick = {
                    currencyViewModel.setEvent(CurrencyContract.Event.OnCurrencyClick)
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(MR.images.ic_usa_flag),
                        contentDescription = null,
                        modifier = Modifier
                            .width(20.dp)
                            .height(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    when (val currencyList = state.currencies) {
                        is ResourceUiState.Success -> {
                            LaunchedEffect(currencyList.data) {
                                println("hey we are updating currency list")
                                currencies = Pair(currencyList.data[0], currencyList.data[1])
                            }

                            Text(
                                text = currencies?.second?.code.orEmpty(),
                                style = MaterialTheme.typography.h6
                            )
                        }
                        else -> {

                        }
                    }
                    Image(
                        painter = painterResource(MR.images.expand_more),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .width(14.dp)
                            .height(8.dp)
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f).height(1.dp)
                    .background(MaterialTheme.colors.secondary)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = when (selectedTab.buyOrSell) {
                    BuyOrSell.BUY -> "Вы потратите".uppercase()
                    BuyOrSell.SELL -> "Вы получите".uppercase()
                },
                style = MaterialTheme.typography.h1
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        ExchangeRateListMain(
            modifier = Modifier.weight(1f), selectedTab.buyOrSell, inputText.text, currencies, city
        )
    }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val scope = rememberCoroutineScope()

    if (openBottomSheet) {
        BottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { openBottomSheet = false }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(10.dp)
            ) {
                items(currencyList) {
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp).padding(10.dp).clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null,
                    ) {
                        currencies = Pair(currencies?.first ?: it, it)
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) openBottomSheet = false
                        }
                    }) {
                        Text(text = it.name, style = MaterialTheme.typography.button)
                    }
                }
            }
        }
    }
}