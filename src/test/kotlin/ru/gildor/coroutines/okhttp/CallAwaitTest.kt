package ru.gildor.coroutines.okhttp

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class CallAwaitTest {
    @Test
    fun await() = runBlocking {
        assertTrue(client.newCall(request("http://localhost/ok")).await().isSuccessful)
        assertFalse(client.newCall(request("http://localhost/error")).await().isSuccessful)
    }

    @Test()
    fun awaitExceptionRecorded() = testBlocking {
        val currentStack = Exception().stackTrace

        val exception = try {
            client.newCall(request("http://localhost/exception")).await()
            null
        } catch (e: Exception) {
            e
        }

        assertNotNull("Did not throw an Exception", exception)
        assertTrue("Received an Exception, but it is not an IOException", exception is IOException)

        exception as IOException

        // Verify that stack trace was recorded properly.
        // We drop 1 element from the currentStack since the line numbers of the
        // stack trace record in this method and the one recorded in [await] don't match.
        val matchedStackTraceEntries = currentStack.drop(1).zip(exception.stackTrace.drop(3))
        matchedStackTraceEntries.forEach { (a, b) -> assertEquals(a, b) }
    }

    @Test()
    fun awaitExceptionNonRecorded() = testBlocking {
        val currentStack = Exception().stackTrace

        val exception = try {
            client.newCall(request("http://localhost/exception")).await(recordStackTrace = false)
            null
        } catch (e: Exception) {
            e
        }

        assertNotNull("Did not throw an Exception", exception)
        assertTrue("Received an Exception, but it is not an IOException", exception is IOException)

        exception as IOException

        // See test [awaitExceptionRecorded] for explanation
        val matchedStackTraceEntries = currentStack.drop(1).zip(exception.stackTrace.drop(3))
        matchedStackTraceEntries.forEach { (a, b) -> assertNotEquals(a, b) }
    }

    @Test
    fun awaitCancel() = testBlocking {
        val call = client.newCall(request("http://localhost/wait"))
        val job = async(coroutineContext) {
            call.await()
        }
        assertFalse(call.isCanceled)
        job.cancel()
        assertTrue(call.isCanceled)
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val command = request.url().pathSegments()[0]
            val builder = Response.Builder()
                .body(ResponseBody.create(null, ByteArray(0)))
                .request(request)
                .protocol(Protocol.HTTP_1_1)
            when (command) {
                "error" -> builder.code(401).message("Error")
                "wait" -> {
                    Thread.sleep(100)
                    builder.code(200).message("Ok after wait")
                }
                "ok" -> builder.code(200).message("Ok")
                else -> throw IOException()
            }.build()
        }
        .build()

    private fun request(url: String): Request {
        return Request.Builder().url(url).build()
    }

}

private fun testBlocking(block: suspend CoroutineScope.() -> Unit) {
    runBlocking(Dispatchers.Unconfined, block)
}