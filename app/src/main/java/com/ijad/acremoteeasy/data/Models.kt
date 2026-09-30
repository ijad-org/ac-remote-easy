package com.ijad.acremoteeasy.data

data class IrCommand(
    val key: String,
    val label: String,
    val pattern: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IrCommand) return false
        return key == other.key && label == other.label && pattern.contentEquals(other.pattern)
    }

    override fun hashCode(): Int = 31 * key.hashCode() + pattern.contentHashCode()
}

data class BrandPack(
    val id: String,
    val name: String,
    val frequencyHz: Int,
    val protocolNote: String,
    val commands: Map<String, IrCommand>
)

data class AcDevice(
    val id: String,
    val name: String,
    val brandId: String,
    val brandName: String,
    val verified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class FavoriteAction(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val brandId: String,
    val commandKey: String,
    val label: String
)

data class TimerStub(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val label: String,
    val minutesFromNow: Int,
    val commandKey: String,
    val enabled: Boolean = true
)

enum class AcMode(val label: String) {
    Cool("Cool"),
    Heat("Heat"),
    Fan("Fan"),
    Dry("Dry"),
    Auto("Auto")
}

enum class FanSpeed(val label: String) {
    Auto("Auto"),
    Low("Low"),
    Medium("Med"),
    High("High")
}
