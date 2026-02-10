plugins {
    alias(libs.plugins.avro)
}

dependencies {
    implementation(libs.avro.core)
    implementation(libs.kafka.avro.serializer)
    
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

avro {
    // Generate private fields with getters/setters (Bean style)
    fieldVisibility.set("PRIVATE")
}