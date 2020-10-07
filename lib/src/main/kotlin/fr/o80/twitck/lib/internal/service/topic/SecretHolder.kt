package fr.o80.twitck.lib.internal.service.topic

object SecretHolder {

    private const val alphabet = "azertyuiopqsdfghjklmwxcvbn0123456789/*-+&é'(-è_çà)=^\$ù*,;:!?./§%µ¨£"

    val secret: String = (0..100).map { alphabet.random() }.joinToString("")

}
