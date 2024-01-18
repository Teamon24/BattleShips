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
tasks.test { useJUnitPlatform() }

val koinVersion = "3.3.3"

dependencies {
    implementation("home:utils:1.1") {
        version {
            isChanging = true
            branch = "main"
        }
        isChanging = true
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
        jvmTarget = "17"
    }
}

javafx {
    version = "19"
    modules = listOf("javafx.base", "javafx.controls", "javafx.fxml", "javafx.graphics")
    configuration = "implementation"
}

fun path(vararg paths: String): String {
    return paths.joinToString(separator = File.separator) { it }
}

val resourcesFolder                  by extra(path(projectDir.path, "src", "main", "resources"))
val playerPropPrefix                 by extra("player")
val currentPlayerPropertiesName      by extra("current-player")
val currentPlayerPropertiesNameDev   by extra("current-player-dev")
val currentPlayerPropertiesNameDebug by extra("current-player-debug")
val currentPlayerPropertyName        by extra("currentPlayer")
val playerPropertyName               by extra("player")
val playersPropertyName              by extra("players")
val playerPropertyRegex              by extra(Regex("$playerPropPrefix-[\\d]+.properties"))
val playersNumber                    by extra(4)
val propsList                        by extra(createPlayersProperties(playersNumber))
val pidsFile                         by extra(path(projectDir.path, "pids.txt").createFile())
val java                             by extra(path(System.getProperty("java.home"), "bin", "java"))
val javaCp                           by extra(listOf(java, "-classpath", sourceSets["main"].runtimeClasspath.asPath))

`tasks defenitions` {
    val main = application.mainClass.get()

    val player           = { i: Int -> "player-$i"}
    val battleOnRun      = { i: Int -> "battle-on-run-$i"}
    val createProperties = "createProperties"
    val multiscreenCheck = "multiscreenCheck"
    val runPlayers       = "runPlayers"
    val killPlayers      = "killPlayers"
    val animationCheck   = "animationCheck"


    createProperties.task {
        deletePlayersProperties()
        createPlayersProperties(playersNumber)
    }

    0.until(playersNumber) { i ->
        player(i).apply {
            javaCp(main, "app", playerPropertiesName(i))
            when (i) {
                0 -> dependsOn("build" and createProperties)
            }
        }

        battleOnRun(i).apply {
            javaCp(main, "app:battle-on-run", playerPropertiesName(i))
            when (i) {
                0 -> dependsOn("build" and createProperties)
            }
        }
    }

    multiscreenCheck {
        javaCp(main, "check:multiscreen")
        dependsOn("build")
    }

    animationCheck {
        javaCp(main, "check:animation")
        dependsOn("build")
    }

    runPlayers {
        last {
            pidsFile.printWriter().use { writer ->
                propsList.indices.forEach { player ->
                    val command = javaCp + main + "app" + propsList[player]
                    writer.println(command.pid())
                }
            }
        }
        dependsOn("build")
    }

    killPlayers last {
        pidsFile.lines {
            ProcessHandle.of(it.toLong()).ifPresent(ProcessHandle::destroy)
        }
    }
    "fatJar".type<Jar> {
        doLast {
            // We need this for Gradle optimization to work
            dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
            // Naming the jar
            archiveClassifier.set("standalone")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            // Provided we set it up in the application plugin configuration
            manifest { attributes(mapOf("Main-Class" to application.mainClass)) }

            val contents =
                configurations
                    .runtimeClasspath
                    .get()
                    .map { if (it.isDirectory) it else zipTree(it) } + sourceSets.main.get().output

            from(contents)
        }
    }
}

inline fun `tasks defenitions`(block: () -> Unit) { block() }

typealias TaskName = String
typealias ProcessCommand = String
typealias Filepath = String
typealias PropertiesName = String

fun List<ProcessCommand>.pid() = ProcessBuilder(this).start().pid()
fun Filepath.createFile() = File(this).apply { createNewFile() }

// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// TaskName extensions
inline infix fun <reified T: Task> TaskName.type(noinline action: T.() -> Unit) = tasks { register(this@type, action) }
             fun TaskName                  .task(action: Action<in Task>) = tasks { register(this@task, action) }
operator     fun TaskName                  .invoke(block: TaskName.() -> Unit): Unit = this.block()
inline infix fun TaskName                  .last(action: Action<in Task>) = tasks { register(this@last) { doLast(action) } }
             fun TaskName                  .javaCp(vararg args: String) { last { exec { commandLine(javaCp + args) } } }
infix        fun TaskName                  .dependsOn(task: TaskName) { tasks.named(this) { dependsOn(task) } }
infix        fun TaskName                  .dependsOn(tasksList: List<TaskName>) { tasksList.forEach { dependsOn(it) } }
infix        fun List<TaskName>            .dependOn(tasks: List<TaskName>) { forEach { it dependsOn tasks } }

// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Players properties
fun deletePlayersProperties() = resourcesFolder.subfiles().doIf(File::delete) { it.name.matches(playerPropertyRegex) }
fun createPlayersProperties(playersNumber: Int) =
    ArrayList<PropertiesName>(playersNumber).apply {
        val lines = currentPlayerPropertiesName.propertiesFile().lines()
        val debugLines = currentPlayerPropertiesNameDebug.propertiesFile().lines()

        0.until(playersNumber) {
            add(createPlayerProperties(it, playersNumber, lines, debugLines))
        }
}

fun createPlayerProperties(
    playerNumber: Int,
    playersNumber: Int,
    appLines: List<String>,
    debugLines: List<String>
): PropertiesName {
    val devLines = mutableListOf<String>()
    val playerPropName = playerPropertiesName(playerNumber)

    val currentPlayerLine = when (playerNumber) {
        0 -> appLines.first { it.contains(currentPlayerPropertyName) }
        else -> "$currentPlayerPropertyName=$playerPropName"
    }

    devLines.apply {
        add(currentPlayerLine)
        add("$playerPropertyName=$playerNumber")
        add("$playersPropertyName=$playersNumber")
        addAll(debugLines)
        addIfAbsent(appLines)

        playerPropName
            .propertiesFilepath()
            .createFile()
            .write(this)
    }

    return playerPropName
}

inline fun notIn(strings: List<String>, predicate: (String) -> Boolean) = !strings.any { s -> predicate(s) }

fun MutableList<String>.addIfAbsent(source: List<String>) {
    val strings = source.filter { s -> notIn(this) { t -> propertiesNamesEqual(t, s) } }
    addAll(strings)
}

fun propertiesNamesEqual(line: String, another: String) = extractPropertyName(line) == extractPropertyName(another)
fun extractPropertyName(line: String) = line.substring(0, line.indexOf("="))

fun playerPropertiesName(i: Int): PropertiesName = when (i) {
    0 -> currentPlayerPropertiesNameDev
    else -> "${playerPropPrefix}-${i}"
}

fun PropertiesName.propertiesFile() = File(propertiesFilepath())
fun PropertiesName.propertiesFilepath() = path(resourcesFolder, "${this}.properties")

fun        File.lines()                          = Files.readAllLines(toPath())
inline fun File.lines(onLine: (String) -> Unit)  = lines().forEach(onLine)
fun        File.write(lines: Collection<String>) = Files.write(this.toPath(), lines)
fun        Filepath.subfiles()                   = File(this).listFiles().toList()

inline fun <T> Collection<T>.doIf(action: (T) -> Unit, predicate: (T) -> Boolean) =
    forEach { predicate(it).ifTrue { action(it) } }

inline fun Int.until(number: Int, onEach: (Int) -> Unit) {
    for (i in this until number) {
        onEach(i)
    }
}

infix fun <T> T.and(that: T) = listOf(this, that)

inline fun <reified T> projectProperty(it: String) = extra[it] as T