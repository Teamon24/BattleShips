package org.home.mvc.view.component

import org.home.mvc.GameComponent
import org.home.mvc.view.battle.subscription.NewServerInfo
import org.home.utils.IpUtils

abstract class AddressComponent: GameComponent() {
    abstract fun publicIp(): String
    abstract fun freePort(): Int
    abstract fun NewServerInfo.freePort(): Int
    fun address(): String = "${publicIp()}:${freePort()}"
}

class AddressComponentImpl : AddressComponent() {
    override fun publicIp() = IpUtils.publicIp()
    override fun freePort() = IpUtils.freePort()
    override fun NewServerInfo.freePort() = this@AddressComponentImpl.freePort()
}

class AddressComponentImplDebug : AddressComponent() {
    override fun publicIp() = applicationProperties.ip!!
    override fun freePort() = applicationProperties.port
    override fun NewServerInfo.freePort() = IpUtils.freePort()
}

