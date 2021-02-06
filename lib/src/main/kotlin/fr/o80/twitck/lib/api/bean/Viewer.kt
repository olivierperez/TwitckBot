package fr.o80.twitck.lib.api.bean

data class Viewer(
    val login: String,
    var displayName: String,
    var badges: List<Badge>,
    var userId: String,
    var color: String
) {
    infix fun hasNoPrivilegesOf(privilegedBadges: Collection<Badge>): Boolean {
        return badges.none { badge -> badge in privilegedBadges }
    }
}
