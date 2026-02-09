dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(libs.springBootStarterDataRedis)
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}