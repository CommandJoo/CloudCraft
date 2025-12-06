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

    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")

    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.shadowJar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    archiveBaseName.set("heartbeat")
    archiveFileName.set("heartbeat.jar")

    doLast {
        val out = archiveFile.get().asFile
        val targets = listOf(
            File("../cloud/templates//template/plugins"),
            File("../cloud/templates//template2/plugins"),
        )

        targets.forEach { dir ->
            dir.mkdirs()
            out.copyTo(File(dir, out.name), overwrite = true)
        }
    }
}

tasks.test {
    useJUnitPlatform()
}