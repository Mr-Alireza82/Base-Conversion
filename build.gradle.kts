// Standalone build: pure Java 17 library, checked against the Android API (minSdk 26, core library desugaring).
plugins {
    `java-library`
    id("ru.vyarus.animalsniffer") version "2.0.1"
}

group = "com.psp.pos"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    signature("com.toasttab.android:gummy-bears-api-26:0.16.0:coreLib2@signature")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

animalsniffer {
    sourceSets = listOf(project.sourceSets.main.get())
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 17
}

tasks.test {
    useJUnitPlatform()
}
