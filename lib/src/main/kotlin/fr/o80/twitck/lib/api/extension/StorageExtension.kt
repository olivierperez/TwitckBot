package fr.o80.twitck.lib.api.extension

interface StorageExtension {
    fun putUserInfo(login: String, namespace: String, key: String, value: String)
    fun getUserInfo(login: String, namespace: String, key: String): String?

    fun putGlobalInfo(namespace: String, key: String, value: String)
    fun getGlobalInfo(namespace: String): List<Pair<String, String>>
    //fun getGlobalInfo(namespace: String, key: String): String?
}