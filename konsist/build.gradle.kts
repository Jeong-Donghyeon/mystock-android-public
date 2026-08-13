plugins {
    alias(libs.plugins.mystock.jvm.library)
}

dependencies {
    testImplementation(libs.konsist)
}

tasks.test {
    inputs.files(
        fileTree(rootDir) {
            include("**/src/main/**/*.kt")
            exclude("**/build/**", "konsist/**")
        },
    )
        .withPropertyName("productionSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(rootDir.resolve("docs/DESIGN.md"))
        .withPropertyName("designDocument")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
