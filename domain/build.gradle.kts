dependencies {
    implementation(project(":common"))
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.flywayCore)
    implementation(libs.flywayDatabasePostgresql)
    runtimeOnly("org.postgresql:postgresql")
    testImplementation(testFixtures(project(":common")))
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}