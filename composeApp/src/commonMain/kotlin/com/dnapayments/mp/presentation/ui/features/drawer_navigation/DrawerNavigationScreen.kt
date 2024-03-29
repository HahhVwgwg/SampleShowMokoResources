package com.dnapayments.mp.presentation.ui.features.drawer_navigation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.dnapayments.mp.MR
import com.dnapayments.mp.data.model.profile.Merchant
import com.dnapayments.mp.domain.model.nav_item.NavItem
import com.dnapayments.mp.domain.model.nav_item.NavItemPosition
import com.dnapayments.mp.domain.model.nav_item.SettingsItem
import com.dnapayments.mp.domain.model.nav_item.SettingsPosition
import com.dnapayments.mp.presentation.state.ManagementResourceUiState
import com.dnapayments.mp.presentation.theme.DnaTextStyle
import com.dnapayments.mp.presentation.theme.Paddings
import com.dnapayments.mp.presentation.theme.Shapes
import com.dnapayments.mp.presentation.theme.blueDrawer
import com.dnapayments.mp.presentation.ui.common.DNAOutlinedGreenButton
import com.dnapayments.mp.presentation.ui.common.DNAText
import com.dnapayments.mp.presentation.ui.common.DNAYellowButton
import com.dnapayments.mp.presentation.ui.common.MerchantName
import com.dnapayments.mp.presentation.ui.common.UiStateController
import com.dnapayments.mp.utils.extension.changePlatformColor
import com.dnapayments.mp.utils.extension.noRippleClickable
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.collectLatest

class DrawerNavigationScreen(
    private val onNavItemClick: (NavItemPosition) -> Unit = {},
    private val onSettingsClick: (SettingsPosition) -> Unit = {},
    private val onMerchantSelected: (MerchantName) -> Unit = {},
    private val onMerchantChange: () -> Unit = {}
) : Screen {

    override val key: String = "DrawerScreen"

    @Composable
    override fun Content() {
        changePlatformColor()
        val drawerViewModel = getScreenModel<DrawerViewModel>()

        val state by drawerViewModel.uiState.collectAsState()

        LifecycleEffect(
            onStarted = {
                drawerViewModel.setEvent(DrawerScreenContract.Event.OnStart)
            }
        )

        LaunchedEffect(key1 = Unit) {
            drawerViewModel.effect.collectLatest { effect ->
                when (effect) {
                    DrawerScreenContract.Effect.OnMerchantChange -> {
                        onMerchantChange()
                    }
                    is DrawerScreenContract.Effect.OnMerchantSelected -> {
                        onMerchantSelected(effect.merchantName)
                    }
                }
            }
        }

        UiStateController(state.merchantChange)

        Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
            ManagementResourceUiState(
                resourceUiState = state.merchants,
                modifier = Modifier.fillMaxWidth(),
                successView = {
                    MerchantViewHeader(
                        merchants = it,
                        onMerchantClick = { merchantId ->
                            drawerViewModel.setEvent(
                                DrawerScreenContract.Event.OnMerchantChange(
                                    merchantId
                                )
                            )
                        }
                    )
                },
                onCheckAgain = {},
                onTryAgain = {},
                loadingView = {
                    MerchantViewHeader(
                        merchants = listOf(
                            Merchant(
                                name = stringResource(MR.strings.loading),
                                companyName = stringResource(MR.strings.loading),
                                isActive = false,
                                isDefault = false, merchantId = "", portalGuideViewedDate = ""
                            )
                        )
                    )
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    AddNewLinkButton(
                        onCreateNewLinkClick = {
                            onNavItemClick(NavItemPosition.ADD_NEW_PAYMENT_LINK)
                        }
                    )
                    Spacer(modifier = Modifier.height(Paddings.small))
                }
                item {
                    VirtualTerminalButton(
                        onVirtualTerminalClick = {
                            onNavItemClick(NavItemPosition.VIRTUAL_TERMINAL)
                        }
                    )
                    Spacer(modifier = Modifier.height(Paddings.medium))
                }
                items(
                    state.navItems,
                    key = { navItem -> navItem.position }
                ) { navItem ->
                    DrawerItem(navItem, onNavItemClick)
                }
                item {
                    Spacer(modifier = Modifier.height(Paddings.small))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(Paddings.medium))
                }
                items(
                    state.settingsItems,
                    key = { settingsItem ->
                        settingsItem.position
                    }
                ) { settingsItem ->
                    SettingsItem(settingsItem, onSettingsClick)
                }
                item {
                    Spacer(modifier = Modifier.height(Paddings.medium))
                    HorizontalDivider()
                    ProfileItem()
                }
            }
        }
    }
}

@Composable
fun VirtualTerminalButton(
    onVirtualTerminalClick: () -> Unit
) {
    DNAOutlinedGreenButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Paddings.medium,
            ),
        onClick = onVirtualTerminalClick,
        content = {
            Row(modifier = Modifier.wrapContentWidth().wrapContentHeight()) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    painter = painterResource(MR.images.ic_virtual_terminal),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(Paddings.standard))
                DNAText(
                    text = stringResource(MR.strings.virtual_terminal),
                    style = DnaTextStyle.GreenMedium16
                )
            }
        }
    )
}

@Composable
fun AddNewLinkButton(
    onCreateNewLinkClick: () -> Unit
) {
    DNAYellowButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Paddings.medium,
            ),
        onClick = onCreateNewLinkClick,
        content = {
            Row(modifier = Modifier.wrapContentWidth().wrapContentHeight()) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    painter = painterResource(MR.images.ic_add),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(Paddings.standard))
                DNAText(
                    text = stringResource(MR.strings.add_link),
                    style = DnaTextStyle.Medium16
                )
            }
        }
    )
}

@Composable
private fun MerchantViewHeader(
    merchants: List<Merchant>,
    modifier: Modifier = Modifier,
    onMerchantClick: (Merchant) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.wrapContentHeight().fillMaxWidth().padding(
            Paddings.medium
        ),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp).background(blueDrawer, shape = Shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(MR.images.ic_store),
                contentDescription = null
            )
        }
        Column(modifier = Modifier.weight(1f).wrapContentHeight().animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().noRippleClickable {
                    isExpanded = !isExpanded
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(Paddings.medium))
                Column(modifier = Modifier.weight(1f)) {
                    DNAText(
                        text = merchants.first().name,
                        style = DnaTextStyle.SemiBold20
                    )
                    DNAText(
                        text = merchants.first().companyName,
                        style = DnaTextStyle.WithAlpha12
                    )
                }
                Icon(
                    modifier = Modifier.padding(horizontal = Paddings.medium),
                    painter = painterResource(
                        if (isExpanded) MR.images.ic_dropdown_arrow_expanded
                        else MR.images.ic_dropdown_arrow
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(Paddings.medium))
                Column {
                    merchants.forEachIndexed { index, merchant ->
                        MerchantExpandableItem(
                            modifier = Modifier.fillMaxWidth(),
                            merchant = merchant,
                            isFirst = index == 0,
                            onMerchantClick = {
                                isExpanded = false
                                onMerchantClick(it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MerchantExpandableItem(
    modifier: Modifier = Modifier,
    merchant: Merchant,
    isFirst: Boolean,
    onMerchantClick: (Merchant) -> Unit
) {
    Row(
        modifier = modifier
            .wrapContentHeight()
            .padding(
                start = Paddings.medium,
                top = Paddings.small,
                bottom = Paddings.small
            ).noRippleClickable {
                if (!isFirst) {
                    onMerchantClick(merchant)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DNAText(
            modifier = Modifier.weight(1f),
            text = merchant.name,
            style = if (isFirst) DnaTextStyle.Medium16 else DnaTextStyle.WithAlpha16,
        )
        if (isFirst)
            Icon(
                modifier = Modifier.padding(horizontal = Paddings.medium),
                painter = painterResource(
                    MR.images.ic_success
                ),
                tint = Color.Unspecified,
                contentDescription = null,
            )
    }
}

@Composable
private fun DrawerItem(
    navItem: NavItem,
    onNavItemClick: (NavItemPosition) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Paddings.extraLarge,
                vertical = Paddings.standard
            )
            .noRippleClickable { onNavItemClick(navItem.position) }) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(navItem.imageDrawableId),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(Paddings.medium))
        DNAText(text = stringResource(navItem.title), style = DnaTextStyle.Medium16)
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SettingsItem(
    settingsItem: SettingsItem,
    onSettingsClick: (SettingsPosition) -> Unit = {}
) {
    Row(
        modifier = Modifier.padding(
            horizontal = Paddings.extraLarge,
            vertical = Paddings.standard
        )
            .noRippleClickable { onSettingsClick(settingsItem.position) }) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(settingsItem.imageDrawableId),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(Paddings.medium))
        DNAText(
            modifier = Modifier.weight(1f),
            text = stringResource(settingsItem.title),
            style = DnaTextStyle.Medium16
        )
        Icon(
            painter = painterResource(MR.images.ic_arrow_right),
            contentDescription = null,
            tint = Color.Unspecified
        )
    }
}

@Composable
private fun ProfileItem(onProfileClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Paddings.extraLarge,
                end = Paddings.extraLarge,
                top = Paddings.large,
                bottom = Paddings.xxLarge
            )
            .noRippleClickable(onProfileClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(MR.images.ic_profile),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(Paddings.medium))
        DNAText(text = stringResource(MR.strings.profile), style = DnaTextStyle.Medium16)
    }
}