package com.dr.qck.utils

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    const val DATASTORE_NAME = "user_prefs"
    const val DATABASE_NAME = "exception_db"

    // datastore keys
    val IS_ENABLED = booleanPreferencesKey("is_enabled")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val THEME = stringPreferencesKey("theme")
    val LANGUAGE = stringPreferencesKey("language")
    val PERMISSION_REQ_COUNT = intPreferencesKey("permission_count")
    val NOTIF_PERMISSION_COUNT = intPreferencesKey("notif_permission_count")
}