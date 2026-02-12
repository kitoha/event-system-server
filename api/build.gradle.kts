dependencies {
    implementation(project(":common"))
    implementation(project(":queue"))
    implementation(project(":ticket"))
    implementation(libs.springBootStarterWebflux)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

    testImplementation(testFixtures(project(":common")))
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}