import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayExtension.MavenCentralSyncConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import groovy.util.Node
//import org.jetbrains.dokka.DokkaConfiguration
//import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.reflect.KClass
import java.net.URL

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.2.41"
    jacoco
    `maven-publish`
    id("com.jfrog.bintray") version "1.7.3"
//    id("org.jetbrains.dokka") version "0.9.16"
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

val githubId = "gildor/kotlin-coroutines-okhttp"
val repoWeb = "https://github.com/$githubId"
val repoVcs = "$repoWeb.git"
val tags = listOf("okhttp", "kotlin", "coroutines")
val licenseId = "Apache-2.0"
val licenseName = "The Apache Software License, Version 2.0"
val licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
val releaseTag = "v${project.version}"

/* KDoc */

/*val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"

    externalDocumentationLink(delegateClosureOf<DokkaConfiguration.ExternalDocumentationLink.Builder> {
        url = URL("https://square.github.io/okhttp/3.x/okhttp/")
    })
}*/

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn("classes")
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    //    dependsOn(dokka)
    classifier = "javadoc"
    from("$buildDir/javadoc")
}

publishing {
    publications {
        create("MavenJava", MavenPublication::class.java) {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            pom.withXml {
                NodeScope(asNode()) {
                    "name" to project.name
                    "description" to project.description.toString()
                    "url" to repoWeb
                    "developers" {
                        "developer" {
                            "name" to "Andrey Mischenko"
                            "email" to "git@gildor.ru"
                            "organizationUrl" to "https://github.com/gildor"
                        }
                    }
                    "issueManagement" {
                        "system" to "GitHub Issues"
                        "url" to "$repoWeb/issues"
                    }
                    "scm" {
                        "url" to repoWeb
                        "connection" to "scm:git:$repoVcs"
                        "developerConnection" to "scm:git:$repoVcs"
                        "tag" to releaseTag
                    }
                    "licenses" {
                        "license" {
                            "name" to licenseName
                            "url" to licenseUrl
                        }
                    }
                }
            }
        }
    }
}

bintray {
    user = project.properties["bintray.user"]?.toString()
    key = project.properties["bintray.key"]?.toString()
    setPublications("MavenJava")
    publish = true
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = project.properties["bintray.repo"]?.toString() ?: "maven"
        name = project.name
        desc = description
        githubRepo = githubId
        githubReleaseNotesFile = "CHANGELOG.md"
        websiteUrl = repoWeb
        issueTrackerUrl = "$repoWeb/issues"
        vcsUrl = repoVcs
        setLicenses(licenseId)
        setLabels(*tags.toTypedArray())
        version(delegateClosureOf<VersionConfig> {
            name = project.version.toString()
            vcsTag = releaseTag
            mavenCentralSync(delegateClosureOf<MavenCentralSyncConfig> {
                sync = project.properties["sonatype.user"] != null
                user = project.properties["sonatype.user"]?.toString()
                password = project.properties["sonatype.password"]?.toString()
                close = "true"
            })
        })
    })
}

/**
 * Helper DSL to define Pom
 */
class NodeScope(private val node: Node, block: NodeScope.() -> Unit) {
    init {
        block()
    }

    infix fun String.to(value: String) {
        node.appendNode(this, value)
    }

    operator fun String.invoke(block: NodeScope.() -> Unit) {
        node.appendNode(this).apply { NodeScope(this, block) }
    }
}