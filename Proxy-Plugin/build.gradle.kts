import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.3")
}

group = "de.johannes"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("net.sf.jopt-simple:jopt-simple:4.7")
    implementation("com.google.code.gson:gson:2.11.0")

    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "de.johannes.Main"
    }
}
tasks.named<ShadowJar>("shadowJar") {
    destinationDirectory.set(file("../cloud/proxy/plugins"))
}

tasks.test {
    useJUnitPlatform()
}