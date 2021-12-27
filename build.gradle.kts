import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.0"
    `maven-publish`
}

group = "me.emilesteenkamp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.6.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.0")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.emilesteen"
            artifactId = "flow-tree"
            version = "0.0.0"

            from(components["java"])
        }
    }
}