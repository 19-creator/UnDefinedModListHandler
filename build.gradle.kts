plugins {
    id("java")
}

group = "com.undefined"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Adding the toml4j library dependency
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
}
