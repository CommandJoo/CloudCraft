import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.named

plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.3")
}

group = "de.johannes"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.sf.jopt-simple:jopt-simple:4.7")
    implementation("com.google.code.gson:gson:2.11.0")

    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))));
    implementation("io.javalin:javalin:6.7.0");
}

tasks.withType<Jar> {
    archiveBaseName.set("cloud.jar")
    archiveFileName.set("cloud.jar")
    manifest {
        attributes["Main-Class"] = "de.johannes.Main"
    }
}

tasks.named<ShadowJar>("shadowJar") {
    destinationDirectory.set(file("cloud"))
}

tasks.test {
    useJUnitPlatform()
}