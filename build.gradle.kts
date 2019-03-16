import com.jfrog.bintray.gradle.BintrayExtension.*
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.DokkaConfiguration
import java.net.URL

plugins {
    kotlin("jvm") version "1.3.21"
    jacoco
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
    id("org.jetbrains.dokka") version "0.9.17"
}

group = "ru.gildor.coroutines"
version = "1.0"
description = "Coroutine adapter for OkHttp Call"

repositories {
    jcenter()
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")
    api("com.squareup.okhttp3:okhttp:3.8.0")
    testImplementation("junit:junit:4.12")
}


tasks {
    jacocoTestReport {
        reports {
            xml.isEnabled = true
        }
    }
    test {
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

val sources = tasks.register<Jar>("sourcesJar") {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets.main.map { it.allSource })
}

val javadoc = tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.dokka)
    archiveClassifier.set("javadoc")
    from("$buildDir/javadoc")
}

tasks.dokka {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"

    externalDocumentationLink(delegateClosureOf<DokkaConfiguration.ExternalDocumentationLink.Builder> {
        url = URL("https://square.github.io/okhttp/3.x/okhttp/")
    })
}

publishing {
    publications {
        register<MavenPublication>("MavenJava") {
            from(components["java"])
            artifact(sources.get())
            artifact(javadoc.get())
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set(repoWeb)
                developers {
                    developer {
                        name.set("Andrey Mischenko")
                        email.set("git@gildor.ru")
                        organizationUrl.set("https://github.com/gildor")
                    }
                }
                issueManagement {
                    system.set("GitHub Issues")
                    url.set("$repoWeb/issues")
                }
                scm {
                    url.set(repoWeb)
                    connection.set("scm:git:$repoVcs")
                    developerConnection.set("scm:git:$repoVcs")
                    tag.set(releaseTag)
                }
                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
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
    pkg(delegateClosureOf<PackageConfig> {
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
