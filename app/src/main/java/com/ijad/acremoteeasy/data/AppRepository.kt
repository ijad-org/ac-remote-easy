package com.ijad.acremoteeasy.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "ac_remote_easy")

class AppRepository(private val context: Context) {
    private val devicesKey = stringPreferencesKey("devices_json")
    private val favoritesKey = stringPreferencesKey("favorites_json")
    private val timersKey = stringPreferencesKey("timers_json")

    val devices: Flow<List<AcDevice>> = context.dataStore.data.map { prefs ->
        decodeDevices(prefs[devicesKey].orEmpty())
    }

    val favorites: Flow<List<FavoriteAction>> = context.dataStore.data.map { prefs ->
        decodeFavorites(prefs[favoritesKey].orEmpty())
    }

    val timers: Flow<List<TimerStub>> = context.dataStore.data.map { prefs ->
        decodeTimers(prefs[timersKey].orEmpty())
    }

    suspend fun addDevice(name: String, brand: BrandPack, verified: Boolean): AcDevice {
        val device = AcDevice(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { brand.name },
            brandId = brand.id,
            brandName = brand.name,
            verified = verified
        )
        context.dataStore.edit { prefs ->
            val list = decodeDevices(prefs[devicesKey].orEmpty()).toMutableList()
            list.add(0, device)
            prefs[devicesKey] = encodeDevices(list)
        }
        return device
    }

    suspend fun removeDevice(deviceId: String) {
        context.dataStore.edit { prefs ->
            val devices = decodeDevices(prefs[devicesKey].orEmpty()).filterNot { it.id == deviceId }
            prefs[devicesKey] = encodeDevices(devices)
            val favorites = decodeFavorites(prefs[favoritesKey].orEmpty())
                .filterNot { it.deviceId == deviceId }
            prefs[favoritesKey] = encodeFavorites(favorites)
            val timers = decodeTimers(prefs[timersKey].orEmpty())
                .filterNot { it.deviceId == deviceId }
            prefs[timersKey] = encodeTimers(timers)
        }
    }

    suspend fun addFavorite(
        device: AcDevice,
        commandKey: String,
        label: String
    ) {
        val fav = FavoriteAction(
            id = UUID.randomUUID().toString(),
            deviceId = device.id,
            deviceName = device.name,
            brandId = device.brandId,
            commandKey = commandKey,
            label = label
        )
        context.dataStore.edit { prefs ->
            val list = decodeFavorites(prefs[favoritesKey].orEmpty()).toMutableList()
            if (list.none { it.deviceId == fav.deviceId && it.commandKey == fav.commandKey }) {
                list.add(0, fav)
                prefs[favoritesKey] = encodeFavorites(list)
            }
        }
    }

    suspend fun removeFavorite(id: String) {
        context.dataStore.edit { prefs ->
            val list = decodeFavorites(prefs[favoritesKey].orEmpty()).filterNot { it.id == id }
            prefs[favoritesKey] = encodeFavorites(list)
        }
    }

    suspend fun addTimer(
        device: AcDevice,
        label: String,
        minutesFromNow: Int,
        commandKey: String
    ) {
        val timer = TimerStub(
            id = UUID.randomUUID().toString(),
            deviceId = device.id,
            deviceName = device.name,
            label = label,
            minutesFromNow = minutesFromNow,
            commandKey = commandKey,
            enabled = true
        )
        context.dataStore.edit { prefs ->
            val list = decodeTimers(prefs[timersKey].orEmpty()).toMutableList()
            list.add(0, timer)
            prefs[timersKey] = encodeTimers(list)
        }
    }

    suspend fun toggleTimer(id: String) {
        context.dataStore.edit { prefs ->
            val list = decodeTimers(prefs[timersKey].orEmpty()).map {
                if (it.id == id) it.copy(enabled = !it.enabled) else it
            }
            prefs[timersKey] = encodeTimers(list)
        }
    }

    suspend fun removeTimer(id: String) {
        context.dataStore.edit { prefs ->
            val list = decodeTimers(prefs[timersKey].orEmpty()).filterNot { it.id == id }
            prefs[timersKey] = encodeTimers(list)
        }
    }

    fun deviceById(devices: List<AcDevice>, id: String): AcDevice? =
        devices.firstOrNull { it.id == id }

    private fun encodeDevices(list: List<AcDevice>): String {
        val arr = JSONArray()
        list.forEach { d ->
            arr.put(
                JSONObject()
                    .put("id", d.id)
                    .put("name", d.name)
                    .put("brandId", d.brandId)
                    .put("brandName", d.brandName)
                    .put("verified", d.verified)
                    .put("createdAt", d.createdAt)
            )
        }
        return arr.toString()
    }

    private fun decodeDevices(raw: String): List<AcDevice> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        AcDevice(
                            id = o.getString("id"),
                            name = o.getString("name"),
                            brandId = o.getString("brandId"),
                            brandName = o.getString("brandName"),
                            verified = o.optBoolean("verified", false),
                            createdAt = o.optLong("createdAt", 0L)
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun encodeFavorites(list: List<FavoriteAction>): String {
        val arr = JSONArray()
        list.forEach { f ->
            arr.put(
                JSONObject()
                    .put("id", f.id)
                    .put("deviceId", f.deviceId)
                    .put("deviceName", f.deviceName)
                    .put("brandId", f.brandId)
                    .put("commandKey", f.commandKey)
                    .put("label", f.label)
            )
        }
        return arr.toString()
    }

    private fun decodeFavorites(raw: String): List<FavoriteAction> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        FavoriteAction(
                            id = o.getString("id"),
                            deviceId = o.getString("deviceId"),
                            deviceName = o.getString("deviceName"),
                            brandId = o.getString("brandId"),
                            commandKey = o.getString("commandKey"),
                            label = o.getString("label")
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun encodeTimers(list: List<TimerStub>): String {
        val arr = JSONArray()
        list.forEach { t ->
            arr.put(
                JSONObject()
                    .put("id", t.id)
                    .put("deviceId", t.deviceId)
                    .put("deviceName", t.deviceName)
                    .put("label", t.label)
                    .put("minutesFromNow", t.minutesFromNow)
                    .put("commandKey", t.commandKey)
                    .put("enabled", t.enabled)
            )
        }
        return arr.toString()
    }

    private fun decodeTimers(raw: String): List<TimerStub> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        TimerStub(
                            id = o.getString("id"),
                            deviceId = o.getString("deviceId"),
                            deviceName = o.getString("deviceName"),
                            label = o.getString("label"),
                            minutesFromNow = o.getInt("minutesFromNow"),
                            commandKey = o.getString("commandKey"),
                            enabled = o.optBoolean("enabled", true)
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
