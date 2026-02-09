plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.springDependencyManagement)
}

allprojects {
    group = "com.eventticketserver"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

// 캡처를 위해 변수 선언
val libsCatalog = libs

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java-test-fixtures")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        "implementation"(libsCatalog.jackson.module.kotlin)
        "implementation"(libsCatalog.kotlin.reflect)
        
        "testImplementation"(libsCatalog.spring.boot.starter.test)
        "testImplementation"(libsCatalog.spring.boot.testcontainers)
        "testImplementation"(libsCatalog.kotlin.test.junit5)
        "testImplementation"(libsCatalog.testcontainers.junit.jupiter)
        "testImplementation"(libsCatalog.testcontainers.postgresql)
        "testImplementation"(libsCatalog.testcontainers.kafka)
        "testImplementation"(libsCatalog.testcontainers.redis)
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
