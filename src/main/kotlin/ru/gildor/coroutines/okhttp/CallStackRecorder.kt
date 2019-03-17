@file:Suppress("RedundantVisibilityModifier")

package ru.gildor.coroutines.okhttp


public const val OKHTTP_STACK_RECORDER_PROPERTY = "ru.gildor.coroutines.okhttp.stackrecorder"

/**
 * Debug turned on value for [DEBUG_PROPERTY_NAME]. See [newCoroutineContext][CoroutineScope.newCoroutineContext].
 */
public const val OKHTTP_STACK_RECORDER_ON = "on"

/**
 * Debug turned on value for [DEBUG_PROPERTY_NAME]. See [newCoroutineContext][CoroutineScope.newCoroutineContext].
 */
public const val OKHTTP_STACK_RECORDER_OFF = "off"

@JvmField
val isRecordStack = when (System.getProperty(OKHTTP_STACK_RECORDER_PROPERTY)) {
    OKHTTP_STACK_RECORDER_ON -> true
    OKHTTP_STACK_RECORDER_OFF, null, "" -> false
    else -> error("System property '$OKHTTP_STACK_RECORDER_PROPERTY' has unrecognized value '${System.getProperty(OKHTTP_STACK_RECORDER_PROPERTY)}'")
}
