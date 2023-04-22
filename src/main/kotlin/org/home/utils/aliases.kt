package org.home.utils

import org.home.net.PlayerSocket
import tornadofx.Dimension
import tornadofx.UIComponent
import java.util.concurrent.LinkedBlockingQueue
import kotlin.reflect.KClass

typealias UIKClass = KClass<out UIComponent>

typealias PlayersSockets = LinkedBlockingQueue<PlayerSocket>
typealias PlayerSocketMessages<T> = Pair<PlayerSocket, Collection<T>>
typealias PlayersSocketsMessages<T> = LinkedBlockingQueue<PlayerSocketMessages<T>>

typealias LinearUnits = Dimension<Dimension.LinearUnits>