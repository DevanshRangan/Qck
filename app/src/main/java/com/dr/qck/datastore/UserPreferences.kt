package com.dr.qck.datastore

data class UserPreferences(
    val isEnabled: Boolean,
    val notificationsEnabled: Boolean,
    val theme: String,
    val language: String,
    val permissionRequestCount: Int,
    val notificationPermissionCount: Int
)