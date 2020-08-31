package fr.o80.twitck.lib.api.extension

interface Overlay {
    fun provideInformation(namespace: String, texts: List<String>)
}
