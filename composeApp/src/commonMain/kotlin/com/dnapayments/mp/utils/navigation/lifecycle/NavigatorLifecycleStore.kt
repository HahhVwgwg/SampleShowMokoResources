package com.dnapayments.mp.utils.navigation.lifecycle

import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.concurrent.ThreadSafeMap
import com.dnapayments.mp.utils.navigation.Navigator
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public typealias NavigatorKey = String

public object NavigatorLifecycleStore {

    private val owners = ThreadSafeMap<NavigatorKey, ThreadSafeMap<KType, NavigatorDisposable>>()

    /**
     * Register a NavigatorDisposable that will be called `onDispose` on the
     * [navigator] leaves the Composition.
     */
    @ExperimentalVoyagerApi
    public inline fun <reified T : NavigatorDisposable> register(
        navigator: Navigator,
        noinline factory: (NavigatorKey) -> T
    ): T {
        return register(navigator, typeOf<T>(), factory) as T
    }

    @OptIn(InternalVoyagerApi::class)
    @PublishedApi
    internal fun <T : NavigatorDisposable> register(
        navigator: Navigator,
        screenDisposeListenerType: KType,
        factory: (NavigatorKey) -> T
    ): NavigatorDisposable {
        return owners.getOrPut(navigator.key) {
            ThreadSafeMap<KType, NavigatorDisposable>().apply {
                put(screenDisposeListenerType, factory(navigator.key))
            }
        }.getOrPut(screenDisposeListenerType) {
            factory(navigator.key)
        }
    }

    @OptIn(InternalVoyagerApi::class)
    @ExperimentalVoyagerApi
    public fun remove(navigator: Navigator) {
        owners.remove(navigator.key)?.forEach { it.value.onDispose(navigator) }
    }
}