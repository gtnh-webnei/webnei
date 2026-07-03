plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "moe.takochan"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.7")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val uiDir = file("ui")
val uiGroup = "ui"
val uiPresent = uiDir.resolve("package.json").exists()

tasks.register<Exec>("uiInstall") {
    onlyIf { uiPresent }
    configureUiPnpmTask("install")
    description = "Install UI dependencies"
}

tasks.register<Exec>("uiBuild") {
    onlyIf { uiPresent }
    configureUiPnpmTask("build")
    description = "Build the UI for production"
}

tasks.register<Exec>("uiDev") {
    onlyIf { uiPresent }
    configureUiPnpmTask("dev")
    description = "Run the UI development server"
}

val uiStaticResources = layout.buildDirectory.dir("generated-resources/ui-static")

tasks.register<Copy>("copyUiDist") {
    onlyIf { uiPresent }
    dependsOn("uiBuild")
    from(uiDir.resolve("dist"))
    into(uiStaticResources)
    description = "Copy the UI production bundle into Spring Boot static resources"
    group = "build"
}

tasks.named<ProcessResources>("processResources") {
    if (uiPresent) {
        dependsOn("copyUiDist")
        from(uiStaticResources) {
            into("static")
        }
    }
}

fun Exec.configureUiPnpmTask(vararg pnpmArgs: String) {
    group = uiGroup
    workingDir = uiDir
    commandLine(pnpmCommand(*pnpmArgs))
}

fun pnpmCommand(vararg args: String): List<String> {
    val osName = System.getProperty("os.name").lowercase()
    val pnpmArgs = args.joinToString(" ")

    return when {
        osName.contains("windows") ->
            listOf("cmd", "/c", "pnpm", *args)

        osName.contains("mac") || osName.contains("darwin") ->
            listOf("/bin/zsh", "-lc", "pnpm $pnpmArgs")

        else ->
            listOf("/bin/bash", "-lc", "pnpm $pnpmArgs")
    }
}
