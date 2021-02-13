package fr.o80.twitck.extension.storage

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fr.o80.twitck.lib.api.Pipeline
import fr.o80.twitck.lib.api.extension.StorageExtension
import fr.o80.twitck.lib.api.service.ServiceLocator
import fr.o80.twitck.lib.api.service.log.Logger
import fr.o80.twitck.lib.api.service.ConfigService
import java.io.File

class InFileStorageExtension(
    private val outputDirectory: File,
    private val logger: Logger,
    private val sanitizer: FileNameSanitizer = FileNameSanitizer()
) : StorageExtension {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val usersDirectory = File(outputDirectory, "users")

    private val lock = Any()

    init {
        if (!outputDirectory.exists())
            outputDirectory.mkdirs()
        if (!usersDirectory.exists())
            usersDirectory.mkdirs()
        if (!outputDirectory.isDirectory) {
            throw IllegalStateException("The path ${outputDirectory.absolutePath} is not a directory!")
        }
    }

    override fun hasUserInfo(login: String): Boolean {
        logger.trace("Check if user info exists $login")
        val userFile = getUserFile(login)
        return userFile.isFile
    }

    override fun putUserInfo(login: String, namespace: String, key: String, value: String) {
        logger.trace("Putting user info into $login [$namespace//$key] => $value")
        with(getOrCreateUser(login)) {
            putExtra("$namespace//$key", value)
            save(this)
        }
    }

    override fun getUserInfo(login: String, namespace: String, key: String): String? {
        logger.trace("Getting user info of $login [$namespace//$key]")
        return getOrCreateUser(login).getExtra("$namespace//$key")
    }

    override fun putGlobalInfo(namespace: String, key: String, value: String) {
        logger.trace("Putting info into [$namespace//$key] => $value")
        with(getOrCreateGlobal()) {
            putExtra(namespace, key, value)
            save(this)
        }
    }

    override fun getGlobalInfo(namespace: String): List<Pair<String, String>> {
        logger.trace("Getting all info [$namespace]")
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
        logger.trace("Saving global...")
        synchronized(lock) {
            try {
                val globalFile = getGlobalFile()
                val globalJson = moshi.adapter(Global::class.java).indent("  ").toJson(global)
                globalFile.writer().use {
                    it.write(globalJson)
                }
                logger.trace("Global saved")
            } catch (e: Exception) {
                logger.error("Failed to save", e)
            }
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
            try {
                val userFile = getUserFile(user.login)
                val userJson = moshi.adapter(User::class.java).indent("  ").toJson(user)
                userFile.writer().use {
                    it.write(userJson)
                }
                logger.trace("User ${user.login} saved")
            } catch (e: Exception) {
                logger.error("Failed to save", e)
            }
        }
    }

    private fun getGlobalFile() =
        File(outputDirectory, "global.json")

    private fun getUserFile(login: String) =
        File(usersDirectory, "${sanitizer(login)}.json")

    companion object {
        fun installer(
            pipeline: Pipeline,
            serviceLocator: ServiceLocator,
            configService: ConfigService
        ): StorageExtension? {
            val config = configService.getConfig(
                "storage.json",
                InFileStorageConfiguration::class
            )
                ?.takeIf { it.enabled }
                ?: return null

            val logger = serviceLocator.loggerFactory.getLogger(InFileStorageExtension::class)
            logger.info("Installing Storage extension...")

            return InFileStorageExtension(
                outputDirectory = File(config.data.storageDirectory),
                logger = logger
            )
        }
    }

}