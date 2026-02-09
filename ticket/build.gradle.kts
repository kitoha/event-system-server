dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.springBootStarterDataRedis)
    implementation(libs.springKafka)
    testImplementation(testFixtures(project(":common")))
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}