package org.home.net.message

import java.io.Serializable


interface Message: Serializable

class MessagesInfo(val number: Int): Message {
    override fun toString(): String {
        return "messages [$number]"
    }
}

object MessagesDSL {

    @JvmInline
    value class Messages<T: Message>
    private constructor(val collection: Collection<T>) {
        fun forEach(function: (T) -> Unit) {
            collection.forEach {
                function(it)
            }
        }

        companion object {

            fun withInfo(addAll: MutableCollection<Message>.() -> Unit): Messages<Message> {
                val messages = mutableListOf<Message>().apply(addAll)
                return Messages(withMessagesInfo(messages))
            }

            fun withInfo(messages: Collection<Message>): Messages<Message> {
                return Messages(withMessagesInfo(messages))
            }

            fun withInfo(message: Message): Messages<Message> {
                val messages = mutableListOf<Message>().apply { add(message) }
                return Messages(withMessagesInfo(messages))
            }
        }
    }


    fun withMessagesInfo(messages: Collection<Message>): List<Message> {
        return MessagesInfo(messages.size) + messages
    }

    private operator fun MessagesInfo.plus(messages: Collection<Message>): List<Message> {
        return mutableListOf(this as Message).apply { addAll(messages) }
    }
}
