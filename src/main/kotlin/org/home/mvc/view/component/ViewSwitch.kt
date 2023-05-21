package org.home.mvc.view.component

import home.extensions.AnysExtensions.invoke
import org.home.app.di.FxScopes
import org.home.app.di.GameScope
import org.home.app.di.ViewInjector
import org.home.mvc.GameBean
import org.home.mvc.view.component.TransitType.BACKWARD
import org.home.mvc.view.component.TransitType.FORWARD
import org.home.utils.logTransit
import tornadofx.View
import tornadofx.find
import kotlin.reflect.KClass

typealias Next = View
typealias NextClass<T> = KClass<T>
typealias Prev = View
typealias TransitLogic = Prev.(Next, TransitType) -> Unit

abstract class ViewSwitch: GameBean() {

    enum class ViewSwitchType {
        REPLACEMENT, OPEN
    }

    protected abstract val transition: TransitLogic

    fun <T : View> View.backTransitTo(to: KClass<T>) = transitLogic(to, BACKWARD, transition)
    fun <T : View> View.backTransferTo(to: KClass<T>) = transferLogic(to, BACKWARD, transition)

    fun <T : View> View.transitTo(to: KClass<T>) = transitLogic(to, FORWARD, transition)

    fun <T : View> View.transferTo(to: KClass<T>, before: () -> Unit = {}) {
        before()
        transferLogic(to, FORWARD, transition)
    }

    private inline fun <T : View> View.transitLogic(to: KClass<T>, type: TransitType, transition: TransitLogic) {
        val prevView = this
        val nextView = find(to)
        ViewInjector {
            getView(to).also {
                prevView.logTransit(nextView)
                prevView.transition(nextView, type)
            }
        }

    }

    private inline fun <T : View> View.transferLogic(to: KClass<T>, type: TransitType, transition: TransitLogic) {
        val prevView = this
        ViewInjector {
            getView(to, FxScopes.getGameScope()).also {
                prevView.logTransit(it)
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


