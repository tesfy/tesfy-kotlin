import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    id("maven-publish")
    id("signing")
}

group = "io.tesfy"
version = "1.0.0"

extra["isReleaseVersion"] = !version.toString().contains("SNAPSHOT")

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.11.3")
    implementation("io.github.jamsesso:json-logic-java:1.0.5")
    implementation("commons-codec:commons-codec:1.14")
    testImplementation("org.junit.platform:junit-platform-runner:1.7.0")
    testImplementation("io.kotest:kotest-runner-junit5:4.3.0") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core:4.3.0") // for kotest core jvm assertions
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnitPlatform()
    filter {
        includeTestsMatching("io.tesfy.unit.*")
    }
}

tasks.create<Test>("itest") {
    useJUnitPlatform()
    filter {
        includeTestsMatching("io.tesfy.itest.*")
    }
}

tasks.create<Test>("testAll") {
    useJUnitPlatform()
}

tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from("javadoc")
}

tasks.create<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            name = "deploy"
            url = if (project.extra["isReleaseVersion"] as Boolean) releasesRepoUrl else snapshotsRepoUrl
            credentials {
                username = project.properties["nexusUsername"]?.toString() ?: ""
                password = project.properties["nexusPassword"]?.toString() ?: ""
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            pom {
                name.set("Tesfy Kotlin")
                description.set("A kotlin and java version of a library to do A/B testing")
                url.set("https://tesfy.io/")

                scm {
                    connection.set("git@github.com:tesfy/tesfy-kotlin.git")
                    url.set("https://github.com/tesfy/tesfy-kotlin")
                }

                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("sebastian-ziegler")
                        name.set("Sebastian Ziegler")
                        email.set("sebastian.ziegler.m@gmail.com")
                    }
                }
            }

            artifactId = "tesfy-kotlin"
            version = "1.0.0"

            from(components["java"])
        }
    }
}

signing {
    setRequired({
        (project.extra["isReleaseVersion"] as Boolean) && gradle.taskGraph.hasTask("publish")
    })
    useGpgCmd()
    sign(publishing.publications["maven"])
}