package fr.o80.twitck.lib.api.bean.subscription

sealed class SubscriptionEventType

object NewSubscription : SubscriptionEventType()
object Notification : SubscriptionEventType()
class UnknownType(val value: String) : SubscriptionEventType()
