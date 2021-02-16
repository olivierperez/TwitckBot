dependencies {
    implementation(project(":lib"))
    implementation(project(":extensions-experimental"))
    implementation(project(":extension-channel"))
    implementation(project(":extension-help"))
    implementation(project(":extension-market"))
    implementation(project(":extension-ngrok"))
    implementation(project(":extension-overlay"))
    implementation(project(":extension-points"))
    implementation(project(":extension-poll"))
    implementation(project(":extension-repeat"))
    implementation(project(":extension-rewards"))
    implementation(project(":extension-runtime-command"))
    implementation(project(":extension-sound"))
    implementation(project(":extension-storage"))
    implementation(project(":extension-viewer-promotion"))
    implementation(project(":extension-welcome"))
    implementation(project(":extension-whisper"))
    // implementation("'"com.github.olivierperez.TwitckBot:lib:0.0.4")
    // implementation("'"com.github.olivierperez.TwitckBot:extensions-experimental:0.0.4")
    // implementation("'"com.github.olivierperez.TwitckBot:extension-overlay:0.0.4")
    // implementation("'"com.github.olivierperez.TwitckBot:extension-poll:0.0.4")
    // etc.

    implementation("com.github.ajalt:clikt:2.6.0")
    implementation("ch.qos.logback:logback-classic:1.0.13")
}

tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to "fr.o80.twitck.application.MainKt")
    }
    from(
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) },
        configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    )
    archiveFileName.set("TwitckBot.jar")
}

tasks.create("prepareConfig", Copy::class) {
    group = "build"
    from(file("$rootDir/sample/config"))
    into(file("$buildDir/libs/.config"))
}

tasks.create("prepareAssets", Copy::class) {
    group = "build"
    from("$rootDir/sample/") {
        include("fonts/")
        include("TwitckBot.bat")
        include("TwitckBot.sh")
        include("ngrok.bat")
        include("ngrok.sh")
    }
    into("$buildDir/libs/")
}

tasks.create("release", Zip::class) {
    group = "build"
    dependsOn("build", "prepareConfig", "prepareAssets")

    from("$buildDir/libs")

    archiveBaseName.set("TwitckBot")
    destinationDirectory.set(File(buildDir, "dist"))
}