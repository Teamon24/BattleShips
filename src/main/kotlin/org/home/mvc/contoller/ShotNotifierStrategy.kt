package org.home.mvc.contoller

import home.extensions.AnysExtensions.className
import home.extensions.CollectionsExtensions.exclude
import org.home.mvc.ApplicationProperties
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.contoller.server.action.HasAShot
import org.home.mvc.contoller.server.action.HitAction
import org.home.mvc.contoller.server.action.MissAction
import org.home.mvc.contoller.server.action.ShotAction
import org.home.mvc.contoller.server.action.SinkingAction
import org.home.utils.PlayersSockets
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
                .notifyAll(SinkingAction::class)
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
        put(MissAction::class, notifyOnlyWhoMadeAShot)
        put(HitAction::class, notifyOnlyWhoMadeAShot)
        put(SinkingAction::class, notifyOnlyWhoMadeAShot)
    }

    fun shouldNotifyAll(action: KClass<out HasAShot>) { map[action] = false }

    fun messages(hasAShot: HasAShot): HashMap<String, MutableList<Action>> {
        val toNotifyShooter = map[hasAShot::class]

        toNotifyShooter ?: throw RuntimeException(
            "There is no boolean flag for ${HasAShot::class.className}'s descendant: ${hasAShot.className}"
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
