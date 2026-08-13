package com.donghyeon.mystock

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

private val stringLiteral = Regex("\"(?:\\\\.|[^\"\\\\])*\"")

abstract class SourceRuleCheckTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:Input
    abstract val rules: MapProperty<String, String>

    @get:Input
    abstract val title: Property<String>

    @get:Internal
    abstract val baseDirectory: DirectoryProperty

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun check() {
        val base = baseDirectory.get().asFile
        val compiled = rules.get().map { (pattern, message) -> Regex(pattern) to message }
        val violations = sources.files.filter { it.isFile }.sorted().flatMap { file ->
            val path = file.toRelativeString(base)
            file.readLines().mapIndexedNotNull { index, line ->
                val code = stringLiteral.replace(line, "\"\"")
                compiled.firstOrNull { (regex, _) -> regex.containsMatchIn(code) }
                    ?.let { (_, message) -> "$path:${index + 1} — $message" }
            }
        }
        report.get().asFile.apply {
            parentFile.mkdirs()
            writeText(violations.joinToString(separator = "\n", postfix = "\n"))
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                violations.joinToString(
                    separator = "\n",
                    prefix = "${title.get()} ${violations.size}건\n",
                ),
            )
        }
    }
}
