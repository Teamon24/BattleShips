package org.home.mvc.view.component

import home.extensions.AnysExtensions.invoke
import org.home.app.di.FxScopes
import org.home.app.di.ViewInjector
import org.home.mvc.GameBean
import org.home.mvc.view.component.TransitType.BACKWARD
import org.home.mvc.view.component.TransitType.FORWARD
import org.home.utils.logTransit
import tornadofx.View
import kotlin.reflect.KClass

typealias Next = View
typealias NextClass<Next> = KClass<Next>
typealias Prev = View
typealias TransitLogic = Prev.(Next, TransitType) -> Unit

abstract class ViewSwitch: GameBean() {

    enum class Type {
        REPLACEMENT, OPEN
    }

    protected abstract val transition: TransitLogic

    fun <Next : View> View.backTransitTo(to: KClass<Next>) = transitLogic(to, BACKWARD, transition)
    fun <Next : View> View.backTransferTo(to: KClass<Next>) = transferLogic(to, BACKWARD, transition)

    fun <Next : View> View.transitTo(to: KClass<Next>) = transitLogic(to, FORWARD, transition)

    fun <Next : View, Prev: View> Prev.transferTo(to: KClass<Next>, before: Next.() -> Unit = {}) {
        transferLogic(to, FORWARD, transition, before)
    }

    private inline fun <Next : View> View.transitLogic(to: KClass<Next>, type: TransitType, transition: TransitLogic) {
        val prevView = this
        val nextView = find(to)
        ViewInjector {
            getView(to).also {
                prevView.logTransit(nextView)
                prevView.transition(nextView, type)
            }
        }

    }

    private inline fun <Next : View> View.transferLogic(
        to: KClass<Next>,
        type: TransitType,
        transition: TransitLogic,
        before: Next.() -> Unit = {}
    ) {
        val prevView = this
        ViewInjector {
            getView(to, FxScopes.getGameScope()).also {
                prevView.logTransit(it)
                it.before()
                prevView.transition(it, type)
            }
        }

    }
}

object ViewReplacement: ViewSwitch() {
    override val transition: TransitLogic = { nextView, type ->  replaceWith(nextView, metro(type)) }
}

object ViewOpenWindow : ViewSwitch() {
    override val transition: TransitLogic = { nextView, _ ->
        close();
        nextView.openWindow()
    }
}


