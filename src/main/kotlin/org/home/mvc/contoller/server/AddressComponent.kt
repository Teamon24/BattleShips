package org.home.mvc.contoller.server

import home.LazyMutable
import home.extensions.BooleansExtensions.or
import home.extensions.BooleansExtensions.then
import org.home.mvc.GameComponent
import org.home.utils.IpUtils

abstract class AddressComponent: GameComponent() {
    abstract val freePort: String
    abstract val publicIp: String

    abstract fun getAddress(): String

    fun localIp()  : String = TODO("not implemented")
}

class AddressComponentImpl: AddressComponent() {
    override val freePort: String by LazyMutable {
        applicationProperties.isServer.then(IpUtils.freePort().toString()).or("")
    }

    override var publicIp: String = ""
        get() {
            applicationProperties.isServer {
                field.ifEmpty { field = IpUtils.publicIp() }
            }
            return field
        }

    override fun getAddress() = "$publicIp:$freePort"
}

class AddressComponentImplDebug: AddressComponent() {
    override val publicIp get() = applicationProperties.ip!!
    override val freePort: String = applicationProperties.port!!
    override fun getAddress() = "$publicIp:$freePort"
}