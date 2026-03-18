plugins {
	kotlin("jvm") version "1.9.20"
	id("com.gradleup.shadow") version "9.2.2"
	application
	jacoco
	id("io.gitlab.arturbosch.detekt") version "1.23.8"
	id("org.jetbrains.dokka") version "2.1.0"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(files("libs/solver-new.jar"))
	implementation("org.antlr:antlr4:4.13.1") // for solver
	implementation("org.jgrapht:jgrapht-core:1.5.2")
	testImplementation(kotlin("test"))
	detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
}

tasks.test {
	useJUnitPlatform()
}
kotlin {
	jvmToolchain(21)
}

application {
	mainClass.set("org.cfl_idr_with_gll.Main")
}

jacoco {
	toolVersion = "0.8.13"
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

detekt {
	toolVersion = "1.23.8"
	parallel = true
	config.setFrom("detekt/config.yml")
}
