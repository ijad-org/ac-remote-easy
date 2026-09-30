package com.ijad.acremoteeasy.data

import android.content.Context
import org.json.JSONObject

object BrandPackLoader {
    private const val ASSET_DIR = "brands"

    fun listBrandIds(context: Context): List<String> {
        return context.assets.list(ASSET_DIR)?.sorted().orEmpty()
            .filter { it.endsWith(".json") }
            .map { it.removeSuffix(".json") }
    }

    fun loadAll(context: Context): List<BrandPack> =
        listBrandIds(context).mapNotNull { load(context, it) }

    fun load(context: Context, brandId: String): BrandPack? {
        return try {
            val json = context.assets.open("$ASSET_DIR/$brandId.json")
                .bufferedReader()
                .use { it.readText() }
            parse(json)
        } catch (_: Throwable) {
            null
        }
    }

    fun parse(json: String): BrandPack {
        val root = JSONObject(json)
        val commandsObj = root.getJSONObject("commands")
        val commands = mutableMapOf<String, IrCommand>()
        val keys = commandsObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val cmd = commandsObj.getJSONObject(key)
            val arr = cmd.getJSONArray("pattern")
            val pattern = IntArray(arr.length()) { i -> arr.getInt(i) }
            commands[key] = IrCommand(
                key = key,
                label = cmd.optString("label", key),
                pattern = pattern
            )
        }
        return BrandPack(
            id = root.getString("id"),
            name = root.getString("name"),
            frequencyHz = root.optInt("frequencyHz", 38000),
            protocolNote = root.optString("protocolNote", ""),
            commands = commands
        )
    }
}
