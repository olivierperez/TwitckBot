package fr.o80.twitck.lib.api.extension

interface StorageExtension {
    fun putUserInfo(login: String, namespace: String, key: String, value: String)
    fun getUserInfo(login: String, namespace: String, key: String): String?
//    fun put(namespace: String, key: String, value: String)
//    fun get(namespace: String, key: String): String?
}