# Kotlin coroutines await extension for OkHttp

This is small library that provides `await()` extension for [okhttp3.Call](https://square.github.io/okhttp/3.x/okhttp/okhttp3/Call.html) for integration with Kotlin coroutines

Based on [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) implementation.

## Download

Download the [JAR](https://bintray.com/gildor/maven/kotlin-coroutines-okhttp#files/ru/gildor/coroutines/kotlin-coroutines-okhttp):

Gradle:

```groovy
compile 'ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0.0'
```

Maven:

```xml
<dependency>
  <groupId>ru.gildor.coroutines</groupId>
  <artifactId>kotlin-coroutines-okhttp</artifactId>
  <version>1.0.0</version>
</dependency>
```