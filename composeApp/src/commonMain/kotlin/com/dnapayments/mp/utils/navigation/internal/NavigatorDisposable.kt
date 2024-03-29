package com.dnapayments.mp.utils.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.DisposableEffectIgnoringConfiguration
import cafe.adriel.voyager.core.stack.StackEvent
import com.dnapayments.mp.utils.navigation.Navigator
import com.dnapayments.mp.utils.navigation.lifecycle.NavigatorLifecycleStore

private val disposableEvents: Set<StackEvent> =
    setOf(StackEvent.Pop, StackEvent.Replace)

@Composable
internal fun NavigatorDisposableEffect(
    navigator: Navigator
) {
    DisposableEffectIgnoringConfiguration(navigator) {
        onDispose {
            disposeNavigator(navigator)
        }
    }
}

@OptIn(InternalVoyagerApi::class)
@Composable
internal fun StepDisposableEffect(
    navigator: Navigator
) {
    val currentScreens = navigator.items

    DisposableEffect(currentScreens) {
        onDispose {
            val newScreenKeys = navigator.items.map { it.key }
            if (navigator.lastEvent in disposableEvents) {
                currentScreens.filter { it.key !in newScreenKeys }.forEach {
                    navigator.dispose(it)
                }
                navigator.clearEvent()
            }
        }
    }
}

@OptIn(InternalVoyagerApi::class)
@Composable
internal fun ChildrenNavigationDisposableEffect(
    navigator: Navigator
) {
    // disposing children navigators
    DisposableEffectIgnoringConfiguration(navigator) {
        onDispose {
            fun disposeChildren(navigator: Navigator) {
                disposeNavigator(navigator)
                navigator.children.values.forEach { childNavigator ->
                    disposeChildren(childNavigator)
                }
                navigator.children.clear()
            }
            if (navigator.parent == null || navigator.disposeBehavior.disposeNestedNavigators) {
                navigator.children.values.forEach { childNavigator ->
                    disposeChildren(childNavigator)
                }
            }
        }
    }

    // referencing nested navigators in parent navigator
    DisposableEffectIgnoringConfiguration(navigator) {
        navigator.parent?.children?.put(navigator.key, navigator)
        onDispose {
            if (navigator.parent?.disposeBehavior?.disposeNestedNavigators != false) {
                navigator.parent?.children?.remove(navigator.key)
            }
        }
    }
}

@OptIn(InternalVoyagerApi::class, ExperimentalVoyagerApi::class)
internal fun disposeNavigator(navigator: Navigator) {
    for (screen in navigator.items) {
        navigator.dispose(screen)
    }
    NavigatorLifecycleStore.remove(navigator)
    navigator.clearEvent()
}