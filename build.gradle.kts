import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.nio.file.Files

buildscript {
    dependencies {
        classpath("org.apache.commons:commons-lang3:3.12.0")
    }
}

plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.10"
    kotlin("jvm") version "1.8.10"
}

group = "home"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application { mainClass.set("org.home.app.run.ApplicationKt") }
val sl: String by extra(File.separator)
apply(from = "$projectDir${sl}build-kts-extensions.gradle.kts")
tasks.test { useJUnitPlatform() }

val koinVersion = "3.3.3"

dependencies {
    implementation("home:utils") {
        version {
            branch = "main"
        }
    }

    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    implementation("com.eclipsesource.minimal-json:minimal-json:0.9.5")

    testImplementation("org.testfx:testfx-core:4.0.13-alpha")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    testImplementation("com.google.guava:guava-collections:r03")
    testImplementation("com.google.guava:guava:13.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "16"
    }
}

javafx {
    version = "19"
    modules = listOf("javafx.base", "javafx.controls", "javafx.fxml", "javafx.graphics")
    configuration = "implementation"
}


val resourcesFolder by extra("${projectDir.path}${sl}src${sl}main${sl}resources${sl}")
val playerPropPrefix by extra("player")
val run by extra("run")

val appPropName by extra("application")
val appPropNameDev by extra("application-dev")
val appPropNameDebug by extra("application-debug")
val regex = Regex("$playerPropPrefix-[\\d]+.properties")

val java = "${System.getProperty("java.home")}${sl}bin${sl}java"
val javaCp = listOf(java, "-classpath", sourceSets["main"].runtimeClasspath.asPath)

val playersNumber = 4
val propsList = createProperties(playersNumber)

"createProperties".task {
    deleteProperties()
    createProperties(playersNumber)
}

val main = application.mainClass.get()

0.until(playersNumber) { i ->
    "player-$i" {
        javaCp(main, "app", playerPropName(i))
        when (i) {
            0 -> dependsOn("build" and "createProperties")
        }
    }

    "battle-on-run-$i" {
        javaCp(main, "app:battle-on-run", playerPropName(i))
        when (i) {
            0 -> dependsOn("build" and "createProperties")
        }
    }
}

"multiscreenCheck".javaCp(main, "check:multiscreen")
"multiscreenCheck" dependsOn "build"


infix fun String.dependsOn(task: String) {
    tasks.named(this) { dependsOn(task) }
}

infix fun String.dependsOn(tasksList: List<String>) {
    tasksList.forEach { dependsOn(it) }
}

infix fun List<String>.dependOn(tasks: List<String>) {
    forEach { it dependsOn tasks }
}

typealias TaskName = String
infix operator fun TaskName.invoke(block: String.() -> Unit): Unit = this.block()
infix fun TaskName.task(action: Action<in Task>): TaskContainer = tasks { register(this@task, action) }
infix fun TaskName.doLast(action: Action<in Task>): TaskContainer = tasks { register(this@doLast) { doLast(action) } }

fun TaskName.javaCp(vararg args: String) { task { exec(javaCp, *args) } }
fun Task.exec(command: List<String>, vararg args: String) = doLast { exec { commandLine(command + args) } }

val pidsFile = "$projectDir${sl}pids.txt".createFile()

"runPlayers".doLast {
    pidsFile.printWriter().use { writer ->
        propsList.indices.forEach { player ->
            writer.println(process(javaCp + main + "app", propsList[player]).pid())
        }
    }
}

"runPlayers" dependsOn "build"

"killPlayers".doLast {
    pidsFile.lines {
        ProcessHandle.of(it.toLong()).ifPresent(ProcessHandle::destroy)
    }
}

fun process(command: List<String>, vararg args: String) = ProcessBuilder(command + args).start()

fun String.createFile() = File(this).apply { createNewFile() }

fun deleteProperties() = resourcesFolder.subfiles().doIfMatches(regex, File::delete) { it.name }

fun createProperties(playersNumber: Int) = ArrayList<String>(playersNumber).apply {
    val lines = "appPropName".propFile().lines()
    val debugLines = "appPropNameDebug".propFile().lines()

    0.until(playersNumber) {
        add(createProp(it, playersNumber, lines, debugLines))
    }
}

fun createProp(playerNumber: Int, playersNumber: Int, appLines: List<String>, debugLines: List<String>): String {
    val devLines = mutableListOf<String>()
    val playerPropName = playerPropName(playerNumber)

    val current = "currentPlayer"

    val currentPlayerLine = when (playerNumber) {
        0 -> appLines.first { it.contains(current) }
        else -> "$current=$playerPropName"
    }

    devLines.apply {
        add(currentPlayerLine)
        add("player=$playerNumber")
        add("players=$playersNumber")
        addAll(debugLines)
        addIfAbsent(appLines)

        playerPropName
            .propFilepath()
            .createFile()
            .write(this)
    }

    return playerPropName
}

fun MutableList<String>.addIfAbsent(source: List<String>) {
    val strings = source.filter { s -> notIn(this) { t -> propNameEqual(t, s) } }
    addAll(strings)
}

inline fun notIn(strings: List<String>, predicate: (String) -> Boolean) = !strings.any { s -> predicate(s) }

fun propNameEqual(line: String, another: String) = extractPropName(line) == extractPropName(another)

fun extractPropName(line: String) = line.substring(0, line.indexOf("="))

fun playerPropName(i: Int) = when (i) {
    0 -> appPropNameDev
    else -> "${playerPropPrefix}-${i}"
}

fun String.propFilepath() = "$resourcesFolder${this}.properties"

fun String.propFile() = File((extra[this] as String).propFilepath())
fun File.lines() = Files.readAllLines(toPath())
inline fun File.lines(onLine: (String) -> Unit) = lines().forEach(onLine)
fun File.write(lines: Collection<String>) = Files.write(this.toPath(), lines)
fun String.subfiles() = File(this).listFiles().toList()
fun <T> Collection<T>.doIf(action: (T) -> Unit, predicate: (T) -> Boolean) =
    forEach { predicate(it).ifTrue { action(it) } }


fun <T> Collection<T>.doIfMatches(regex: Regex, action: (T) -> Unit, get: (T) -> String) =
    doIf(action) { get(it).matches(regex) }

inline fun Int.until(number: Int, onEach: (Int) -> Unit) {
    for (i in this until number) {
        onEach(i)
    }
}

infix fun <T> T.and(that: T) = listOf(this, that)