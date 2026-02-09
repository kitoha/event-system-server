dependencies {
    testFixturesImplementation(libs.springBootStarterTest)
    testFixturesImplementation(libs.springBootTestcontainers)
    testFixturesImplementation(libs.testcontainers.postgresql)
    testFixturesImplementation(libs.testcontainers.kafka)
    testFixturesImplementation(libs.testcontainers.redis)
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}
