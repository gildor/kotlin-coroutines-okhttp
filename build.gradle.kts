import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayExtension.MavenCentralSyncConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import groovy.util.Node
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import ru.gildor.opublish.GithubRepo
import ru.gildor.opublish.LicenseType
import kotlin.reflect.KClass
import java.net.URL

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.2.41"
    jacoco
    `maven-publish`
    id("com.jfrog.bintray") version "1.7.3"
    id("ru.gildor.opublish") version "0.1.0"
}

group = "ru.gildor.coroutines"
version = "0.1"
description = ""

repositories {
    jcenter()
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.17")
    compile("com.squareup.okhttp3:okhttp:3.8.0")
    testCompile("junit:junit:4.12")
}

kotlin.experimental.coroutines = Coroutines.ENABLE

tasks {
    val jacocoTestReport by getting(JacocoReport::class) {
        reports {
            xml.isEnabled = true
        }
    }
    "test" {
        finalizedBy(jacocoTestReport)
    }
}

/* Publishing */

opublish {
    repo = GithubRepo("gildor", "kotlin-coroutines-okhttp")
    labels = listOf("okhttp", "kotlin", "coroutines")
    license = LicenseType.Apache
    releaseTag = "v${project.version}"
    repositories {

    }
}
