dependencies {
    implementation(project(":common"))
    implementation(project(":queue"))
    implementation(project(":ticket"))
    implementation(libs.springBootStarterWebflux)
    
    testImplementation(testFixtures(project(":common")))
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}