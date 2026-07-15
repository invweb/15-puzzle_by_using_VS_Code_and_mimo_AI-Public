plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":core"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.game.puzzle.desktop.DesktopLauncherKt")
}

tasks.register<Exec>("runDesktop") {
    dependsOn("classes")
    val runtimeCP = sourceSets["main"].runtimeClasspath
    val cp = runtimeCP.files.joinToString(":")
    commandLine(
        org.gradle.internal.jvm.Jvm.current().javaHome.absolutePath + "/bin/java",
        "-cp", cp,
        "com.game.puzzle.desktop.DesktopLauncherKt"
    )
    workingDir = projectDir
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.game.puzzle.desktop.DesktopLauncherKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
