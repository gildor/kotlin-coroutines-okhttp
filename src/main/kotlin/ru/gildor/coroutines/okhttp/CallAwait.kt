package ru.gildor.coroutines.okhttp

import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * Suspend extension that allows suspend [Call] inside coroutine.
 *
 * @param recordStackTrace If set to true, the current stack trace is recorded before awaiting the request.
 *                         OkHttp performs requests in a thread pool and thus the stack trace is otherwise lost.
 *                         Recording the current stack trace comes at a minor performance cost.
 * @return Result of request or throw exception
 */
suspend fun Call.await(recordStackTrace: Boolean = true): Response {
    val recordedStackTrace = if (recordStackTrace) IOException("Exception occurred while awaiting Call.") else null
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return

                if (recordedStackTrace != null) {
                    recordedStackTrace.initCause(e)
                    continuation.resumeWithException(recordedStackTrace)
                } else {
                    continuation.resumeWithException(e)
                }
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
        }
    }
}
