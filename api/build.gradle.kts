dependencies {
    implementation(project(":common"))
    implementation(project(":queue"))
    implementation(project(":ticket"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}