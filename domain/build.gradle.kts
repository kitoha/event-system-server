dependencies {
    implementation(project(":common"))
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly("org.postgresql:postgresql")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}
