package fr.o80.twitck.extension.storage

class FileNameSanitizer {
    operator fun invoke(filename: String): String {
        return filename
            .replace("[^\\w\\d]".toRegex(), "_")
            .replace("_{2,}".toRegex(), "_")
    }
}