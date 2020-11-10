package fr.o80.twitck.lib.internal.service

import fr.o80.twitck.lib.api.bean.CoolDown
import java.time.LocalDateTime

class CoolDownManager {

    private val coolDowns: MutableMap<String, LocalDateTime> = mutableMapOf()

    fun isCoolingDown(key: String) : Boolean {
        val expiry = coolDowns[key]
        return expiry != null && LocalDateTime.now().isBefore(expiry)
    }

    fun startCoolDown(content: String, coolDown: CoolDown?) {
        coolDown?.duration?.let { duration ->
            coolDowns[content] = LocalDateTime.now() + duration
        }
    }

}
