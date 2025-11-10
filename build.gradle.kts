plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.DAT250Project"
version = "0.0.1-SNAPSHOT"
description = "Software Technology Project for DAT250 - Group 11"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	//testImplementation("org.springframework.boot:spring-boot-starter-test")

    //added
    // --- TESTING DEPENDENCIES ---
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    //Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.6.0")
    implementation ("org.springframework.boot:spring-boot-starter-amqp")

    //DataBase
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")

    /*
    // anotations JPA
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    // implementation JPA
    implementation("org.hibernate.orm:hibernate-core:7.1.1.Final")
    // driver BD
    implementation("com.h2database:h2:2.3.232")
     */

    // Spring Security (authentication/authorization)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT library (JJWT) - for generating and validating tokens
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5") // for JSON (claims) serialization


}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    // Force tests to always run
    outputs.upToDateWhen { false }
}

