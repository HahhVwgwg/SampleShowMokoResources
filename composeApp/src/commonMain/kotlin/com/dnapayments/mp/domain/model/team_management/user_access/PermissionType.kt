package com.dnapayments.mp.domain.model.team_management.user_access

data class PermissionType(
    val name: String,
    val type: PermissionTypeOption,
    var isChecked: Boolean,
    val permissionList: List<Permission>
)
