plugins {
    alias(libs.plugins.avro)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.springKafka)

    implementation(libs.avro.core)
    implementation(libs.kafka.avro.serializer)

    runtimeOnly("org.postgresql:postgresql")

    testImplementation(testFixtures(project(":common")))
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}