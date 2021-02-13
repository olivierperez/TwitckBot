package fr.o80.twitck.extension.ngrok

import fr.o80.twitck.lib.api.extension.StorageExtension


private const val KEY_URL = "URL"

class NgrokStorage(
    private val storage: StorageExtension
) {

    private val namespace: String = NgrokTunnelExtension::class.java.name

    fun getUrl(): String? {
        return storage.getGlobalInfo(namespace).firstOrNull { (key, _) -> key == KEY_URL }?.second
    }

    fun update(url: String) {
        storage.putGlobalInfo(namespace, KEY_URL, url)
    }

}
