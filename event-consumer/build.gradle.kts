dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.kafka:spring-kafka")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}