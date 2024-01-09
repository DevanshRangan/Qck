package com.dr.qck.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.dr.qck.utils.Constants.IS_ENABLED
import com.dr.qck.utils.Constants.LANGUAGE
import com.dr.qck.utils.Constants.NOTIFICATIONS_ENABLED
import com.dr.qck.utils.Constants.NOTIF_PERMISSION_COUNT
import com.dr.qck.utils.Constants.PERMISSION_REQ_COUNT
import com.dr.qck.utils.Constants.THEME
import com.dr.qck.utils.ThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DatastoreRepository @Inject constructor(
    private val datastore: DataStore<Preferences>
) {

    val userPreferencesFlow: Flow<UserPreferences> = datastore.data.catch {
        Log.d("DatastoreException", it.message.toString())
        if (it is IOException) {
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        UserPreferences(
            isEnabled = preferences[IS_ENABLED] ?: false,
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: false,
            theme = preferences[THEME] ?: ThemeType.LIGHT.name,
            language = preferences[LANGUAGE] ?: "",
            permissionRequestCount = preferences[PERMISSION_REQ_COUNT] ?: 0,
            notificationPermissionCount = preferences[NOTIF_PERMISSION_COUNT] ?: 0
        )
    }

    suspend fun updateKey(key: Preferences.Key<*>, value: Any) {
        datastore.edit { prefs ->
            when (value) {
                is Boolean -> {
                    prefs[key as Preferences.Key<Boolean>] = value
                }

                is String -> {
                    prefs[key as Preferences.Key<String>] = value
                }

                is Int -> {
                    prefs[key as Preferences.Key<Int>] = value
                }
            }
        }
    }
}