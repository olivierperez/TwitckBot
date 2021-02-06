package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.bean.CoolDown
import java.time.LocalDateTime

class CoolDownManager {

    private val coolDowns: MutableMap<String, LocalDateTime> = mutableMapOf()

    fun isCoolingDown(namespace: String, key: String): Boolean {
        val expiry = coolDowns["$namespace::$key"]
        return expiry != null && LocalDateTime.now().isBefore(expiry)
    }

    fun startCoolDown(namespace: String, key: String, coolDown: CoolDown?) {
        coolDown?.duration?.let { duration ->
            coolDowns["$namespace::$key"] = LocalDateTime.now() + duration
        }
    }

    fun executeIfCooledDown(
        namespace: String,
        key: String,
        coolDown: CoolDown?,
        block: () -> Unit
    ) {
        if (!isCoolingDown(namespace, key)) {
            startCoolDown(namespace, key, coolDown)
            block()
        }
    }

}
