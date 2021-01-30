package fr.o80.twitck.lib.api

//TODO OPZ Supprimer ce fichier
/*import fr.o80.twitck.lib.api.extension.ExtensionInstaller
import fr.o80.twitck.lib.internal.TwitckBotImpl*/

/*@DslMarker
private annotation class TwitckConfigDsl*/

/*@TwitckConfigDsl
fun twitckBot(
    oauthToken: String,
    hostName: String,
    configurator: TwitckConfigurator.() -> Unit
): TwitckBot {
    val configuration = TwitckConfigurator(oauthToken, hostName).apply(configurator).build()
    return TwitckBotImpl(configuration)
}*/

/*class TwitckConfigurator(
    private val oauthToken: String,
    private val hostName: String
) {
    private val extensions: MutableList<Any> = mutableListOf()
    private val pipeline = PipelineImpl()
    private val commandTriggering = CommandTriggeringImpl()
    private val loggerFactory = Slf4jLoggerFactory()

    private val serviceLocator: ServiceLocator = ServiceLocatorImpl(
        extensionProvider = object : ExtensionProvider {
            override fun <T : Any> first(extensionInterface: KClass<T>): T =
                extensions.first { extensionInterface.isInstance(it) }
                    .let { extension -> extensionInterface.cast(extension) }

            override fun <T : Any> provide(extensionInterface: KClass<T>): List<T> =
                extensions
                    .filter { extension -> extensionInterface.isInstance(extension) }
                    .map { extension -> extensionInterface.cast(extension) }

            override fun <T : Any> forEach(extensionInterface: KClass<T>, block: (extension: T) -> Unit) {
                provide(extensionInterface)
                    .forEach(block)
            }
        },
        loggerFactory = loggerFactory,
        twitchApi = TwitchApiImpl(oauthToken, loggerFactory),
        commandTriggering = commandTriggering
    )*/

/*@TwitckConfigDsl
fun <Configuration : Any, A : Any> install(
    extension: ExtensionInstaller<Configuration, A>,
    configure: Configuration.() -> Unit
) {
    val installed = extension.install(pipeline, serviceLocator, configure)
    extensions += installed
}*/

/* internal fun build(): TwitckConfiguration {
     return TwitckConfiguration(
         oauthToken = oauthToken,
         hostName = hostName,
         pipeline = pipeline,
         serviceLocator = serviceLocator,
         commandsFromExtension = commandTriggering
     )
 }

}*/
