package com.example.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "equip_settings")

object EquipManager {
    private val MASCOT = stringPreferencesKey("equipped_mascot")
    private val THEME = stringPreferencesKey("equipped_theme")
    private lateinit var ctx: Context

    fun init(context: Context) {
        ctx = context.applicationContext
    }

    val equippedMascot: Flow<String?>
        get() = ctx.dataStore.data.map { it[MASCOT]?.takeIf { s -> s.isNotEmpty() } }

    val equippedTheme: Flow<String?>
        get() = ctx.dataStore.data.map { it[THEME]?.takeIf { s -> s.isNotEmpty() } }

    suspend fun toggle(category: String, itemId: String) {
        val key = if (category == "Mascot") MASCOT else THEME
        ctx.dataStore.edit { prefs ->
            prefs[key] = if (prefs[key] == itemId) "" else itemId
        }
    }
}
