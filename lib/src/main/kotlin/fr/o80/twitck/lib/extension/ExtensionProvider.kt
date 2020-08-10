package fr.o80.twitck.lib.extension

// TODO Provider plusieurs extensions à partir d'une interface passée en params (+ créer un module qui liste les interfaces disponibles ? pour le cas où quelqu'un veuille développer sa propre extension Help)
interface ExtensionProvider {
    fun <T> provide(extensionClass: Class<T>): T?
}