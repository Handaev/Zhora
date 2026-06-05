plugins {
	java
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	implementation("io.minio:minio:9.0.0")

	implementation("org.apache.pdfbox:pdfbox:3.0.2")
	implementation("com.twelvemonkeys.imageio:imageio-webp:3.12.0")

	implementation("org.springframework.kafka:spring-kafka:3.2.4")
	implementation("org.apache.kafka:kafka-clients:3.7.0")

	implementation("org.postgresql:postgresql:42.3.1")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	implementation("org.mapstruct:mapstruct:1.6.2")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.2")
	implementation("org.projectlombok:lombok-mapstruct-binding:0.2.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")

	testImplementation("org.testcontainers:testcontainers:1.21.4")
	testImplementation("org.testcontainers:junit-jupiter")

	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:kafka:1.21.4")
	testImplementation("org.testcontainers:minio:1.19.7")

	testImplementation("org.springframework.kafka:spring-kafka-test")

	testImplementation("org.awaitility:awaitility")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
