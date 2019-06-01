plugins {
	`kotlin-project-config`
}

dependencies {
	implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
	api(kotlin("reflect"))
	api(project(":livingdoc-api"))

	testCompile("ch.qos.logback:logback-classic:${Versions.logback}")
	testCompile("org.assertj:assertj-core:${Versions.assertJ}")
	testCompile("org.mockito:mockito-core:${Versions.mockitoVersion}")
	testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlinVersion}")
}
