package modeep.modev.global.util

object LanguageDetector {
    fun detect(path: String): String {
        val fileName = path.substringAfterLast("/")
        val lowerFileName = fileName.lowercase()

        return when {
            lowerFileName == "dockerfile" -> "dockerfile"
            lowerFileName == "makefile" -> "makefile"
            lowerFileName == "gradlew" -> "shell"
            lowerFileName == "pom.xml" -> "xml"
            lowerFileName == "build.gradle" || lowerFileName == "settings.gradle" -> "groovy"
            lowerFileName == "build.gradle.kts" || lowerFileName == "settings.gradle.kts" -> "kotlin"
            else ->
                when (lowerFileName.substringAfterLast(".", missingDelimiterValue = "")) {
                    "kt", "kts" -> "kotlin"
                    "java" -> "java"
                    "js", "mjs", "cjs" -> "javascript"
                    "jsx" -> "jsx"
                    "ts" -> "typescript"
                    "tsx" -> "tsx"
                    "json" -> "json"
                    "yaml", "yml" -> "yaml"
                    "xml" -> "xml"
                    "html", "htm" -> "html"
                    "css" -> "css"
                    "scss" -> "scss"
                    "md", "markdown" -> "markdown"
                    "sh", "bash" -> "shell"
                    "properties" -> "properties"
                    "sql" -> "sql"
                    "py" -> "python"
                    else -> "plaintext"
                }
        }
    }
}
