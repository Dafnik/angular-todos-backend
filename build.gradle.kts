import org.apache.tools.ant.filters.ReplaceTokens
import kotlin.jvm.java

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "me.dafnik"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

springBoot {
	buildInfo()
}

dependencies {
	implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.17.2")

	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// needed to skip generation of plain jar
tasks.getByName<Jar>("jar") {
	enabled = false
}

tasks.bootJar {
	enabled = true
	archiveFileName.set("monolith.jar")
}

/**
 * Needed to use properties from external sources and forward them to application*.yaml files.
 * For more details: https://www.baeldung.com/spring-boot-auto-property-expansion
 */
tasks.processResources {
	copySpec {
		from("src/main/resources")
		include("**/application*.yml")
		include("**/application*.yaml")
		include("**/application*.properties")
		project.properties.forEach { prop ->
			filter(mapOf(prop.key to prop.value), ReplaceTokens::class.java)
			filter(mapOf(("project." + prop.key) to prop.value), ReplaceTokens::class.java)
		}
	}
}
