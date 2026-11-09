import java.security.MessageDigest

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
android {
 namespace = "org.erthi.guide"
 compileSdk = 35
 defaultConfig {
  applicationId = "org.erthi.guide"
  minSdk = 26
  targetSdk = 35
  versionCode = 1
  versionName = "0.1.0-alpha"
  testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 buildFeatures { compose = true }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.9.3")
 implementation("androidx.appcompat:appcompat:1.7.0")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("androidx.navigation:navigation-compose:2.8.5")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.foundation:foundation")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-extended")
 testImplementation("junit:junit:4.13.2")
 androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
 androidTestImplementation("androidx.compose.ui:ui-test-junit4")
 androidTestImplementation("androidx.test.ext:junit:1.2.1")
 androidTestImplementation("androidx.test:runner:1.6.2")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
}
// Public production packaging requires a separately reviewed release policy.
val requireReleaseReview = tasks.register("requireReleaseReview") { doLast { error("Production release requires content, rights, privacy and device review. Build Debug for testing.") } }
tasks.configureEach { if (name in setOf("assembleRelease", "bundleRelease", "packageRelease")) dependsOn(requireReleaseReview) }

tasks.register("dependencyInventory") {
    group = "verification"
    doLast {
        val approved = mapOf("androidx." to "Apache-2.0", "org.jetbrains.kotlin" to "Apache-2.0", "org.jetbrains.kotlinx" to "Apache-2.0",
            "org.jetbrains" to "Apache-2.0", "com.google.guava" to "Apache-2.0", "com.google.errorprone" to "Apache-2.0",
            "com.google.code.findbugs" to "Apache-2.0", "com.google.j2objc" to "Apache-2.0", "com.google.android" to "Apache-2.0")
        val rows = configurations.getByName("debugRuntimeClasspath").resolvedConfiguration.resolvedArtifacts.sortedBy { it.moduleVersion.id.toString() }.map { artifact ->
            val id = artifact.moduleVersion.id
            val license = approved.entries.firstOrNull { id.group.startsWith(it.key) }?.value
                ?: error("Dependency license review required: $id")
            val digest = MessageDigest.getInstance("SHA-256").digest(artifact.file.readBytes()).joinToString("") { "%02x".format(it.toInt() and 255) }
            "$id\t$license\t$digest"
        }
        val output = layout.buildDirectory.file("dependency-inventory.tsv").get().asFile
        output.parentFile.mkdirs(); output.writeText("coordinate\tlicense-policy\tsha256\n" + rows.joinToString("\n") + "\n")
    }
}
