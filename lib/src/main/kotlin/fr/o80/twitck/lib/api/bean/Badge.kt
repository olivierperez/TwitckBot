package fr.o80.twitck.lib.api.bean

enum class Badge(val value: String) {
    ADMIN("admin"),
    BROADCASTER("broadcaster"),
    BITS("bits"),
    BITS_LEADER("bits-leader"),
    FOUNDER("founder"),
    GLOBAL_MOD("global_mod"),
    MODERATOR("moderator"),
    SUBSCRIBER("subscriber"),
    STAFF("staff"),
    TURBO("turbo"),
    UNKNOWN(""),
    VIP("vip");

    companion object {
        fun fromValue(value: String): Badge {
            return values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}