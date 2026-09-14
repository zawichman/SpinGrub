package com.spingrub.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spingrub")

/**
 * Persists all app data using DataStore Preferences. Lists and favorites are
 * serialized to JSON strings to keep the dependency footprint tiny.
 */
class SpinGrubRepository(private val context: Context) {

    private object Keys {
        val MEAT = stringPreferencesKey("meat")
        val METHOD = stringPreferencesKey("method")
        val SAUCE = stringPreferencesKey("sauce")
        val FAVORITES = stringPreferencesKey("favorites")
        val SOUND = booleanPreferencesKey("sound")
        val HAPTICS = booleanPreferencesKey("haptics")
        val CONFETTI = booleanPreferencesKey("confetti")
    }

    val data: Flow<SpinGrubData> = context.dataStore.data.map { prefs ->
        SpinGrubData(
            meat = prefs[Keys.MEAT]?.let(::decodeList) ?: DefaultData.MEAT,
            method = prefs[Keys.METHOD]?.let(::decodeList) ?: DefaultData.METHOD,
            sauce = prefs[Keys.SAUCE]?.let(::decodeList) ?: DefaultData.SAUCE,
            favorites = prefs[Keys.FAVORITES]?.let(::decodeFavorites) ?: emptyList(),
            soundEnabled = prefs[Keys.SOUND] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            confettiEnabled = prefs[Keys.CONFETTI] ?: true,
        )
    }

    suspend fun setItems(category: Category, items: List<String>) {
        val key = when (category) {
            Category.MEAT -> Keys.MEAT
            Category.METHOD -> Keys.METHOD
            Category.SAUCE -> Keys.SAUCE
        }
        context.dataStore.edit { it[key] = encodeList(items) }
    }

    suspend fun addItem(category: Category, item: String) {
        val trimmed = item.trim()
        if (trimmed.isEmpty()) return
        context.dataStore.edit { prefs ->
            val key = when (category) {
                Category.MEAT -> Keys.MEAT
                Category.METHOD -> Keys.METHOD
                Category.SAUCE -> Keys.SAUCE
            }
            val current = prefs[key]?.let(::decodeList) ?: when (category) {
                Category.MEAT -> DefaultData.MEAT
                Category.METHOD -> DefaultData.METHOD
                Category.SAUCE -> DefaultData.SAUCE
            }
            if (current.none { it.equals(trimmed, ignoreCase = true) }) {
                prefs[key] = encodeList(current + trimmed)
            }
        }
    }

    suspend fun removeItem(category: Category, item: String) {
        context.dataStore.edit { prefs ->
            val key = when (category) {
                Category.MEAT -> Keys.MEAT
                Category.METHOD -> Keys.METHOD
                Category.SAUCE -> Keys.SAUCE
            }
            val current = prefs[key]?.let(::decodeList) ?: when (category) {
                Category.MEAT -> DefaultData.MEAT
                Category.METHOD -> DefaultData.METHOD
                Category.SAUCE -> DefaultData.SAUCE
            }
            prefs[key] = encodeList(current.filterNot { it == item })
        }
    }

    suspend fun addFavorite(name: String, meat: String, method: String, sauce: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.let(::decodeFavorites) ?: emptyList()
            val fav = Favorite(
                id = UUID.randomUUID().toString(),
                name = name.trim().ifEmpty { "$meat • $method • $sauce" },
                meat = meat, method = method, sauce = sauce
            )
            prefs[Keys.FAVORITES] = encodeFavorites(current + fav)
        }
    }

    suspend fun removeFavorite(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.let(::decodeFavorites) ?: emptyList()
            prefs[Keys.FAVORITES] = encodeFavorites(current.filterNot { it.id == id })
        }
    }

    suspend fun setSound(enabled: Boolean) =
        context.dataStore.edit { it[Keys.SOUND] = enabled }.let { }

    suspend fun setHaptics(enabled: Boolean) =
        context.dataStore.edit { it[Keys.HAPTICS] = enabled }.let { }

    suspend fun setConfetti(enabled: Boolean) =
        context.dataStore.edit { it[Keys.CONFETTI] = enabled }.let { }

    suspend fun resetToDefaults() {
        context.dataStore.edit { prefs ->
            prefs[Keys.MEAT] = encodeList(DefaultData.MEAT)
            prefs[Keys.METHOD] = encodeList(DefaultData.METHOD)
            prefs[Keys.SAUCE] = encodeList(DefaultData.SAUCE)
        }
    }

    // ---- JSON helpers ----
    private fun encodeList(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun decodeList(json: String): List<String> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (e: Exception) {
        emptyList()
    }

    private fun encodeFavorites(list: List<Favorite>): String {
        val arr = JSONArray()
        list.forEach { fav ->
            arr.put(
                JSONObject()
                    .put("id", fav.id)
                    .put("name", fav.name)
                    .put("meat", fav.meat)
                    .put("method", fav.method)
                    .put("sauce", fav.sauce)
            )
        }
        return arr.toString()
    }

    private fun decodeFavorites(json: String): List<Favorite> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Favorite(
                id = o.optString("id", UUID.randomUUID().toString()),
                name = o.optString("name"),
                meat = o.optString("meat"),
                method = o.optString("method"),
                sauce = o.optString("sauce"),
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}
