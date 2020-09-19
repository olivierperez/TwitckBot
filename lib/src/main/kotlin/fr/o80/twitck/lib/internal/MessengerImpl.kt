package fr.o80.twitck.lib.internal

import fr.o80.twitck.lib.api.Messenger
import fr.o80.twitck.lib.api.TwitckBot
import fr.o80.twitck.lib.api.bean.SendMessage

// Besoin 1 : éviter les moments où le bot spam d'informations non-essentielles (ex : dire bonjour à tout le monde quand on se fait raid)
// Besoin 2 : répondre immédiatement à une interraction du tchat (ex : quelqu'un achète sur le Market, il faut lui donner un feedback)
// Besoin 3 : éviter les redites successives (ex : 2 utilisateurs utilisent la commande !market à quelques secondes d'interval, une seule réponse suffit)
// Besoin 4 : référencer auprès du bot, une liste de messages récurrents (ex : "Retrouvez-nous aussi sur Discord -> http://...")

// Idée a (3) : pourvoir tagger un message, ou lui donner un cooldown
// Écriture a => send(channel, "Voici ce qu'on a sur l'étagère: ...", CoolDown(1 minute))

// Idée b1 (1) : le bot gère une liste de messages à diffuser quand il a le temps
// Idée b2 (1) : un message peut être catégorisé comme "postpone"
// Idée b2 (2) : un message peut être catégorisé comme "immediate"
// Idée b3 (4) : un message peut être catégorisé comme "récurrent", et sera notifié si le bot n'a rien fait pendant 5 minutes
// Écriture b : send(channel, "Salut GiftSub, installe-toi", Postpone)
// Écriture b : send(channel, "Ton achat de commande perso s'est bien passée !idontwantgiftsub", Immediate)
// Écriture b : send(channel, "Vous connaissez la commande !skarab42 ?", Repeating)

class MessengerImpl(
    private val bot: TwitckBot
): Messenger {

    override fun send(message: SendMessage) {
        bot.send(message.channel, message.content)
    }

}
