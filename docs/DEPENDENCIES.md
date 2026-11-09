# Dependency policy
Pinned build plugins: AGP 8.10.1, Kotlin/Compose compiler 2.2.21; Gradle 8.11.1; Java 17; Android API 35, minimum 26. UI BOM 2024.12.01. See app/build.gradle.kts for exact library versions.

`:app:dependencyInventory` resolves the actual runtime artifacts, writes coordinates and SHA-256, and rejects unreviewed dependency families. The current families use Apache-2.0 (AndroidX, Kotlin, JetBrains, Google Android/Guava annotations). Material icons are code-native Apache-2.0 vector assets tinted per module; no paid Canva elements were copied. Original Erthi launcher vector is part of this repository.

This check does not replace reviewing each dependency NOTICE or independently validating initially downloaded artifact digests. Gradle strict verification metadata is added only after it can be generated and inspected; absence is a recorded release blocker, not silently described as verified.
