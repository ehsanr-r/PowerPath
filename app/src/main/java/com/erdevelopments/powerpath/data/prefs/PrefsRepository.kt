package com.erdevelopments.powerpath.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "powerpath_prefs")

@Singleton
class PrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_USER_ID = longPreferencesKey("selected_user_id")
    private val KEY_DAY_ID = longPreferencesKey("selected_day_id")

    val selectedUserId: Flow<Long?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val selectedDayId: Flow<Long?> = context.dataStore.data.map { it[KEY_DAY_ID] }

    suspend fun setSelectedUser(id: Long) {
        context.dataStore.edit { it[KEY_USER_ID] = id }
    }

    suspend fun setSelectedDay(id: Long) {
        context.dataStore.edit { it[KEY_DAY_ID] = id }
    }
}