package fr.o80.twitck.lib.api.exception

class ExtensionDependencyException(
    sourceExtension: String,
    dependencyExtension: String
) : Exception("$sourceExtension extension requires the $dependencyExtension extension")
