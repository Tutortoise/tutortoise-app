package com.tutortoise.tutortoise.domain

object EventBus {
    private val listeners = mutableMapOf<Class<*>, MutableList<(Any) -> Unit>>()

    fun subscribe(eventType: Class<*>, listener: (Any) -> Unit) {
        listeners.getOrPut(eventType) { mutableListOf() }.add(listener)
    }

    fun unsubscribe(eventType: Class<*>, listener: (Any) -> Unit) {
        listeners[eventType]?.remove(listener)
    }

    fun publish(event: Any) {
        listeners[event::class.java]?.forEach { listener ->
            listener(event)
        }
    }
}

// Event class for profile updates
data class ProfileUpdateEvent(val name: String? = null, val email: String? = null)
