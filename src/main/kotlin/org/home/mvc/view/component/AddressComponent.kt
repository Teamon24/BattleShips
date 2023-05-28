package org.home.mvc.view.component

import org.home.mvc.GameComponent
import org.home.utils.IpUtils

abstract class AddressComponent: GameComponent() {
    abstract fun publicIp(): String
    abstract fun freePort(): Int
    fun address(): String = "${publicIp()}:${freePort()}"
}

class AddressComponentImpl : AddressComponent() {
    override fun publicIp(): String = IpUtils.publicIp()
    override fun freePort() = IpUtils.freePort()
}

class AddressComponentImplDebug : AddressComponent() {
    override fun publicIp() = applicationProperties.ip!!
    override fun freePort() = applicationProperties.port
}

