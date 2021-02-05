dependencies {
    compileOnly(project(":lib"))

    implementation("com.github.olivierperez.KotlinSlobs:lib:1.2")

    implementation("io.ktor:ktor-server-core:1.3.0")
    implementation("io.ktor:ktor-server-netty:1.3.0")
    implementation("io.ktor:ktor-websockets:1.3.0")
}
