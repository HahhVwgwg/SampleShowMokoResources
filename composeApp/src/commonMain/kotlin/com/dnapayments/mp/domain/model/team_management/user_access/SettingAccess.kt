package com.dnapayments.mp.domain.model.team_management.user_access

data class SettingAccess(
    val name: String,
    val type: SettingAccessType,
    var isChecked: Boolean = false,
    val isEnabled: Boolean = false
)