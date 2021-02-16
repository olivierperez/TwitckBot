import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("kapt") version "1.4.30"
}

repositories {
    mavenCentral()
    jcenter()
}

allprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")

    group = "fr.o80.twitck"
    version = "0.4.0"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        implementation(kotlin("stdlib"))

        implementation("com.squareup.moshi:moshi:1.9.3")
        implementation("com.squareup.moshi:moshi-adapters:1.9.3")
        implementation("com.squareup.moshi:moshi-kotlin:1.9.3")
        kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.3")

        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
        testImplementation("io.mockk:mockk:1.10.0")
    }
}