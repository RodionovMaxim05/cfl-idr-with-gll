plugins {
	kotlin("jvm") version "1.9.20"
	application
	jacoco
}

version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation(files("libs/solver.jar"))
	implementation("org.antlr:antlr4:4.13.1") // for solver
	implementation("org.jgrapht:jgrapht-core:1.5.2")
	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}
kotlin {
	jvmToolchain(11)
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
