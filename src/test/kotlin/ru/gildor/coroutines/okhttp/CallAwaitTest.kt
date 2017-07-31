package ru.gildor.coroutines.okhttp

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CallAwaitTest {
    @Test
    fun await() = runBlocking {
        assertTrue(client.newCall(request("http://localhost/ok")).await().isSuccessful)
        assertFalse(client.newCall(request("http://localhost/error")).await().isSuccessful)
    }

    @Test(expected = IOException::class)
    fun awaitException() = testBlocking {
        client.newCall(request("http://localhost/exception")).await()
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
    runBlocking(Unconfined, block)
}