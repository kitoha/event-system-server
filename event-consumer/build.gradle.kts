dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(libs.spring.boot.starter.batch)
    implementation(libs.spring.kafka)
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}
