package org.home.net.socket.ex.timeout

import kotlinx.coroutines.launch
import org.home.net.ActionType
import org.home.net.TextMessage
import org.home.utils.RandomSelector
import org.home.utils.functions.threadsScope

fun main() {
    CheckTimeoutServer().start(1124)
    val clientsNumber = 25
    val messagesNumber = 20

    val clients = mutableListOf<CheckoutTimeoutClient>()

    for (i in 1..clientsNumber) {
        val client = CheckoutTimeoutClient()
        clients.add(client)
        client.connect("127.0.0.1", 1124)
    }

    val randomSelector = RandomSelector(
        clientsNumber.progression(),
        messagesNumber.progression()
    )

    val threadsScope = threadsScope(10)
    while (randomSelector.hasNext()) {
        val next = randomSelector.next()
        threadsScope.launch {
            sendMessage(clients[next[0] as Int], next[1] as Int)
        }
    }
}

private fun Int.progression() = (0 until this).toList()

private fun sendMessage(client: CheckoutTimeoutClient, i: Int) {
    Thread.currentThread().name = "client#${client.number}"
    client.send(hi(client, i))
}

private fun hi(client: CheckoutTimeoutClient, i: Int) =
    TextMessage(ActionType.CONNECT, "#${client.number}: hi#$i!")
