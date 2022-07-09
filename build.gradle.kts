import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    id("org.openjfx.javafxplugin") version "0.0.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/com.vk.api/sdk
    implementation("com.vk.api:sdk:1.0.14")
//    implementation("com.github.yvasyliev:java-vk-bots-longpoll-api:3.2.10")
    implementation("org.slf4j:slf4j-jdk14:1.7.26")
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:2.0.3")
    implementation("io.ktor:ktor-client-cio:2.0.3")
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("net.bramp.ffmpeg:ffmpeg:0.7.0")
}

javafx {
    version = "17.0.1"
    modules = listOf("javafx.controls", "javafx.media", "javafx.web")
//    modules = [ 'javafx.controls', 'javafx.media' ]
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}