dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.kafka)
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}
