dependencies {
    implementation(project(":common"))
    implementation(project(":queue"))
    implementation(project(":ticket"))
    implementation(libs.spring.boot.starter.webflux)
    
    testImplementation(testFixtures(project(":common")))
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}
