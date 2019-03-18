# Kotlin coroutines await extension for OkHttp3

This is small library that provides `await()` extension for [okhttp3.Call](https://square.github.io/okhttp/3.x/okhttp/okhttp3/Call.html) for integration with Kotlin coroutines

Based on [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) implementation.

Requires Kotlin 1.3+

Depends on [OkHttp3 3.8.0](https://github.com/square/okhttp/blob/master/CHANGELOG.md#version-380) so don't require updates to [newest version of OkHttp](https://github.com/square/okhttp/blob/master/CHANGELOG.md#version-3130) that require Java 8+ or Android 5+

## Usage

```kotlin
// Create OkHttp client
val client = OkHttpClient.Builder().build()
// Create request 
val request = Request.Builder().url("https://example.org/").build()

suspend fun main() {
    // Do call and await() for result from any suspend function
    val result = client.newCall(request).await()
    println("${result.code()}: ${result.message()}")
}
```

This library doesn't provide any facilities for non-blocking read of response body,
if you need a way to do that consider to use `withContext(Dispatchers.IO)` and wrap blocking calls.

See this issue about support of non-blocking API for Okio - https://github.com/square/okio/issues/501

## Download

Download the [JAR](https://bintray.com/gildor/maven/kotlin-coroutines-okhttp#files/ru/gildor/coroutines/kotlin-coroutines-okhttp):

Artifacts are available on [JCenter](https://bintray.com/gildor/maven/kotlin-coroutines-okhttp) and [Maven Central](https://search.maven.org/search?q=a:kotlin-coroutines-okhttp)

Gradle:

```kotlin
implementation("ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0.0")
```

Maven:

```xml
<dependency>
  <groupId>ru.gildor.coroutines</groupId>
  <artifactId>kotlin-coroutines-okhttp</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Debugging

Because this coroutines adapter uses async API of OkHttp by default 
stacktrace doesn't contain link on code that called it.

For example you have code from Usage section code that throws SocketTimeoutException

If you run it you will get stacktrace similar to this:

```
Exception in thread "main" java.net.SocketTimeoutException: Read timed out
	at java.base/java.net.SocketInputStream.socketRead0(Native Method)
	at java.base/java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
	...
```

As you can see, there is no way to understand from which line or class this request was started.
 
You should understand, this is not something unique for Kotlin coroutines or this adapter,
you would have the same stacktrace with `okhttp3.Callback` if would throw an exception on failure
To make this stacktrace more useful you can do a few things

### Wrap exception manually:
```kotlin
suspend fun main() {
   try {
       client.newCall(request).await()
   } catch (e: IOException) {
       // Catch original exception
       // Use some logger that will write line number to logs, so you can find the source of exception
       someLogger.error(e)
       // or just wrap exception and throw it to get stacktrace
       throw IOException("Some additional debug info: ${e.message}", e)
   }
}
```

This will give you stacktrace that shows line where exception was rethrown or logged:
```kotlin
Exception in thread "main" java.io.IOException: java.net.SocketTimeoutException: Read timed out
    at ru.gildor.coroutines.okhttp.MainKt.main(main.kt:20)
    at ru.gildor.coroutines.okhttp.MainKt$main$1.invokeSuspend(main.kt)
...
Caused by: java.net.SocketTimeoutException: Read timed out
    at java.base/java.net.SocketInputStream.socketRead0(Native Method)
    at java.base/java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
...
```
    
### Enable debug mode of kotlinx.coroutines:

```kotlin
// Set it before first usage of coroutines in your project
System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
```

This will allow you to see line where request is started:

```
Exception in thread "main" java.net.SocketTimeoutException
    (Coroutine boundary)
    at ru.gildor.coroutines.okhttp.MainKt.main(main.kt:15)
Caused by: java.net.SocketTimeoutException: timeout
    at okio.Okio$4.newTimeoutException(Okio.java:230)
    at okio.AsyncTimeout.exit(AsyncTimeout.java:285)
...
```

This will give you coroutines stack so you can debug it in most cases
    
### Enable stack recording

To help debug some complicated cases, 
when you want to know which code started coroutine, 
`await()` provides argument recordStack:

```kotin
suspend fun main() {
    client.newCall(request).await(recordStack = true)
}
```

Instead of recovering coroutine stacktrace this call will create exception on method call to save stack
and will throw it on error, so you will get detailed stack 

```
Exception in thread "main" java.io.IOException
	at ru.gildor.coroutines.okhttp.MainKt.main(main.kt:15)
	...
Caused by: java.net.SocketTimeoutException: timeout
	at okio.Okio$4.newTimeoutException(Okio.java:230)
	...
```

By default stack recording is disabled, but you can enable it using system properties
(should set it before first usage of the adapter):

```kotlin
System.setProperty(OKHTTP_STACK_RECORDER_PROPERTY, OKHTTP_STACK_RECORDER_ON)
```

But this method is relatively heavyweight because creates exception on each request 
(even successful, because we have to record stacktrace before invocation) 
and resulting stacktrace is full of coroutines internal calls, 

So I recommend to use it only if you have hard times to understand what is caused the problem and you need all information
