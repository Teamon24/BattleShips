package org.home.mvc.contoller

import home.extensions.AnysExtensions.className
import home.extensions.CollectionsExtensions.exclude
import javafx.beans.property.SimpleListProperty
import org.home.mvc.contoller.server.action.Action
import org.home.mvc.contoller.server.action.HasAShot
import org.home.mvc.contoller.server.action.HitAction
import org.home.mvc.contoller.server.action.MissAction
import org.home.mvc.contoller.server.action.SinkingAction
import kotlin.reflect.KClass

object ShotNotifierStrategies: GameComponent() {
    private var notifyAllStrategy: ShotNotifierStrategy? = null
    private var notifyOnlyShooterStrategy: ShotNotifierStrategy? = null

    fun create(enemies: SimpleListProperty<String>): ShotNotifierStrategy {
        return if (applicationProperties.isToNotifyAll) {
            notifyAllStrategy ?: ShotNotifierStrategy
                .Builder(enemies)
                .notifyAll(MissAction::class)
                .notifyAll(HitAction::class)
                .notifyAll(SinkingAction::class)
                .build()
                .also { notifyAllStrategy = it }
        } else {
            notifyOnlyShooterStrategy ?: ShotNotifierStrategy
                .Builder(enemies)
                .onlyShooterNotifier()
                .also { notifyOnlyShooterStrategy = it }
        }
    }
}

abstract class ShotNotifierStrategy(private val enemies: SimpleListProperty<String>) {

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

        val enemiesAndMessages = hashMapOf<String, MutableList<Action>>()
        if(toNotifyShooter) {
            enemiesAndMessages[hasAShot.player] = mutableListOf(hasAShot)
        } else {
            enemiesAndMessages.putAll(
                enemies
                    .exclude(hasAShot.target)
                    .associateWith { mutableListOf(hasAShot) }
            )
        }

        return enemiesAndMessages
    }

    internal class Builder(private val sockets: SimpleListProperty<String>) {
        private val strategy = object : ShotNotifierStrategy(sockets) {}
        fun notifyAll(hasAShot: KClass<out HasAShot>) = apply { strategy.shouldNotifyAll(hasAShot) }
        fun build() = strategy
        fun onlyShooterNotifier() = Builder(sockets).build()
    }
}
