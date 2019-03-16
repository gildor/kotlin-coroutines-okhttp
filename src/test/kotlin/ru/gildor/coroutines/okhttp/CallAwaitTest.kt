package ru.gildor.coroutines.okhttp

import kotlinx.coroutines.*
import okhttp3.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class CallAwaitTest {
    @Test
    fun await() = runBlocking {
        assertTrue(call("http://localhost/ok").await().isSuccessful)
        assertFalse(call("http://localhost/error").await().isSuccessful)
    }

    @Test(expected = IOException::class)
    fun `throw exception on coroutine`() = testBlocking {
        call("http://localhost/exception").await()
    }

    @Test
    fun `cancel call on coroutine cancellation`() = testBlocking {
        val call = call("http://localhost/wait")
        val job = async(Dispatchers.Unconfined) {
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
            val body = ResponseBody.create(MediaType.parse("plain/text"), "Body")
            when (command) {
                "error" -> builder.code(401).message("Error")
                "wait" -> {
                    Thread.sleep(100)
                    builder.code(200).message("Ok after wait")
                }
                "ok" -> builder.code(200).message("Ok")
                else -> throw IOException()
            }.body(body).build()
        }
        .build()

    private fun call(url: String): Call {
        return client.newCall(Request.Builder().url(url).build())
    }

}

private fun testBlocking(block: suspend CoroutineScope.() -> Unit) {
    runBlocking(block = block)
}