package org.home.mvc.contoller

import org.home.mvc.ApplicationProperties
import org.home.net.message.Action
import org.home.net.message.HasAShot
import org.home.net.message.HitAction
import org.home.net.message.MissAction
import org.home.net.message.ShotAction
import org.home.utils.PlayersSockets
import org.home.utils.extensions.CollectionsExtensions.exclude
import org.home.utils.extensions.className
import tornadofx.Controller
import kotlin.reflect.KClass

object ShotNotifierStrategies: Controller() {
    private val applicationProperties: ApplicationProperties by di()

    private var notifyAll: ShotNotifierStrategy? = null
    private var notifyOnlyShooter: ShotNotifierStrategy? = null

    fun create(sockets: PlayersSockets): ShotNotifierStrategy {
        return if (applicationProperties.isToNotifyAll) {
            notifyAll ?: ShotNotifierStrategy
                .Builder(sockets)
                .notifyAll(MissAction::class)
                .notifyAll(HitAction::class)
                .build()
                .also { notifyAll = it }
        } else {
            notifyOnlyShooter ?: ShotNotifierStrategy
                .Builder(sockets)
                .onlyShooterNotifier()
                .also { notifyOnlyShooter = it }
        }
    }
}

abstract class ShotNotifierStrategy(private val sockets: PlayersSockets) {

    protected val map = HashMap<KClass<out HasAShot>, Boolean>().apply {
        val notifyOnlyWhoMadeAShot = true
        put(ShotAction::class, notifyOnlyWhoMadeAShot)
        put(MissAction::class, notifyOnlyWhoMadeAShot)
        put(HitAction::class, notifyOnlyWhoMadeAShot)
    }

    fun shouldNotifyAll(action: KClass<out HasAShot>) { map[action] = false }

    fun notifications(hasAShot: HasAShot): HashMap<String, MutableList<Action>> {
        val toNotifyShooter = map[hasAShot::class]

        toNotifyShooter ?: throw RuntimeException(
            "There is no boolean flag for ${HasAShot::className}'s descendant: ${hasAShot.className}"
        )

        val toSend = hashMapOf<String, MutableList<Action>>()
        if(toNotifyShooter) {
            toSend[hasAShot.player] = mutableListOf(hasAShot)
        } else {
            toSend.putAll(
                sockets
                    .map { it.player!! }
                    .exclude(hasAShot.target)
                    .associateWith { mutableListOf(hasAShot) }
            )
        }

        return toSend
    }

    internal class Builder(private val sockets: PlayersSockets) {
        private val strategy = object : ShotNotifierStrategy(sockets) {}
        fun notifyAll(hasAShot: KClass<out HasAShot>) = apply { strategy.shouldNotifyAll(hasAShot) }
        fun build() = strategy
        fun onlyShooterNotifier() = Builder(sockets).build()
    }
}
