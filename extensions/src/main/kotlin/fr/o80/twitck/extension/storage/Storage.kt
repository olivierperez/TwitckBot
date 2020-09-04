package fr.o80.twitck.extension.storage

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.extension.TwitckExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import java.io.File

class Storage(
    private val outputDirectory: File,
    private val logger: Logger,
    private val sanitizer: FileNameSanitizer = FileNameSanitizer()
) : StorageExtension {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val lock = Any()

    init {
        if (!outputDirectory.exists())
            outputDirectory.mkdirs()
        if (!outputDirectory.isDirectory) {
            throw IllegalStateException("The path ${outputDirectory.absolutePath} is not a directory!")
        }
    }

    override fun putUserInfo(login: String, namespace: String, key: String, value: String) {
        logger.debug("Putting user info into $login [$namespace//$key] => $value")
        with(getOrCreateUser(login)) {
            putExtra("$namespace//$key", value)
            save(this)
        }
    }

    override fun getUserInfo(login: String, namespace: String, key: String): String? {
        logger.debug("Getting user info of $login [$namespace//$key]")
        return getOrCreateUser(login).getExtra("$namespace//$key")
    }

    override fun putGlobalInfo(namespace: String, key: String, value: String) {
        logger.debug("Putting info into [$namespace//$key] => $value")
        with(getOrCreateGlobal()) {
            putExtra(namespace, key, value)
            save(this)
        }
    }

    override fun getGlobalInfo(namespace: String): List<Pair<String, String>> {
        logger.debug("Getting all info [$namespace]")
        return getOrCreateGlobal().getExtras(namespace)
    }

    private fun getOrCreateGlobal(): Global {
        val globalFile = getGlobalFile()
        return if (globalFile.isFile) {
            globalFile.reader().use { reader ->
                moshi.adapter(Global::class.java).fromJson(reader.readText())!!
            }
        } else {
            logger.trace("There's no global file")
            Global()
        }
    }

    private fun save(global: Global) {
        logger.debug("Saving global...")
        synchronized(lock) {
            val globalFile = getGlobalFile()
            val globalJson = moshi.adapter(Global::class.java).indent("  ").toJson(global)
            globalFile.writer().use {
                it.write(globalJson)
            }
            logger.trace("Global saved")
        }
    }

    private fun getOrCreateUser(login: String): User {
        val userFile = getUserFile(login)
        return if (userFile.isFile) {
            logger.trace("User $login already has a file !")
            userFile.reader().use { reader ->
                moshi.adapter(User::class.java).fromJson(reader.readText())!!
            }
        } else {
            logger.trace("User $login has no files")
            User(login)
        }
    }

    private fun save(user: User) {
        logger.debug("Saving user ${user.login}...")
        synchronized(lock) {
            val userFile = getUserFile(user.login)
            val userJson = moshi.adapter(User::class.java).indent("  ").toJson(user)
            userFile.writer().use {
                it.write(userJson)
            }
            logger.trace("User ${user.login} saved")
        }
    }

    private fun getGlobalFile() =
        File(outputDirectory, "global.json")

    private fun getUserFile(login: String) =
        File(outputDirectory, "users/${sanitizer(login)}.json")

    class Configuration {

        @DslMarker
        private annotation class Dsl

        private var output: File? = null

        @Dsl
        fun output(output: File) {
            this.output = output
        }

        fun build(serviceLocator: ServiceLocator): Storage {
            val theOutput = output
                ?: throw IllegalStateException("Output must be set for the extension ${Storage::class.simpleName}")

            val logger = serviceLocator.loggerFactory.getLogger(Storage::class)

            return Storage(theOutput, logger)
        }
    }

    companion object Extension : TwitckExtension<Configuration, Storage> {
        override fun install(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configure: Configuration.() -> Unit
        ): Storage {
            return Configuration()
                .apply(configure)
                .build(serviceLocator)
        }

    }
}