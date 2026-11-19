package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.novaDataStore: DataStore<Preferences> by preferencesDataStore(name = "novacore_prefs")

data class UserSettings(
    val autoSpeak: Boolean = true,
    val speechLocale: String = "ms-MY",
    val termuxWorkdir: String = "/data/data/com.termux/files/home",
    val lunoKeyId: String = "",
    val lunoKeySecret: String = "",
    val lunoCapitalMyr: String = "50"
)

class PreferencesStore(private val context: Context) {

    private object Keys {
        val AUTO_SPEAK = booleanPreferencesKey("auto_speak")
        val SPEECH_LOCALE = stringPreferencesKey("speech_locale")
        val TERMUX_WORKDIR = stringPreferencesKey("termux_workdir")
        val LUNO_KEY_ID = stringPreferencesKey("luno_key_id")
        val LUNO_KEY_SECRET = stringPreferencesKey("luno_key_secret")
        val LUNO_CAPITAL_MYR = stringPreferencesKey("luno_capital_myr")
    }

    val settingsFlow: Flow<UserSettings> = context.novaDataStore.data.map { prefs ->
        UserSettings(
            autoSpeak = prefs[Keys.AUTO_SPEAK] ?: true,
            speechLocale = prefs[Keys.SPEECH_LOCALE] ?: "ms-MY",
            termuxWorkdir = prefs[Keys.TERMUX_WORKDIR] ?: "/data/data/com.termux/files/home",
            lunoKeyId = prefs[Keys.LUNO_KEY_ID] ?: "",
            lunoKeySecret = prefs[Keys.LUNO_KEY_SECRET] ?: "",
            lunoCapitalMyr = prefs[Keys.LUNO_CAPITAL_MYR] ?: "50"
        )
    }

    suspend fun setAutoSpeak(enabled: Boolean) {
        context.novaDataStore.edit { it[Keys.AUTO_SPEAK] = enabled }
    }

    suspend fun setSpeechLocale(locale: String) {
        context.novaDataStore.edit { it[Keys.SPEECH_LOCALE] = locale }
    }

    suspend fun setTermuxWorkdir(workdir: String) {
        context.novaDataStore.edit { it[Keys.TERMUX_WORKDIR] = workdir }
    }

    suspend fun setLunoCredentials(keyId: String, keySecret: String) {
        context.novaDataStore.edit {
            it[Keys.LUNO_KEY_ID] = keyId.trim()
            it[Keys.LUNO_KEY_SECRET] = keySecret.trim()
        }
    }

    suspend fun setLunoCapitalMyr(capital: String) {
        context.novaDataStore.edit { it[Keys.LUNO_CAPITAL_MYR] = capital.trim() }
    }
}
