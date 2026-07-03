import java.math.BigDecimal
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val versionPropertiesFile = rootProject.file("version.properties")

fun loadVersionProperties(): Properties {
    return Properties().apply {
        if (versionPropertiesFile.isFile) {
            versionPropertiesFile.inputStream().use(::load)
        }
        putIfAbsent("versionName", "1.0")
        putIfAbsent("versionCode", "1")
    }
}

fun nextVersionName(current: String): String {
    val semverParts = current.split(".").map { it.toIntOrNull() }
    if (semverParts.size >= 3 && semverParts.all { it != null }) {
        return semverParts
            .mapIndexed { index, value -> if (index == semverParts.lastIndex) value!! + 1 else value!! }
            .joinToString(".")
    }

    return BigDecimal(current)
        .add(BigDecimal("0.1"))
        .setScale(1)
        .toPlainString()
}

fun shouldAutoIncrementAppVersion(): Boolean {
    if (gradle.startParameter.isDryRun) return false
    return gradle.startParameter.taskNames
        .map { it.substringAfterLast(":").lowercase() }
        .any { taskName ->
            taskName == "build" ||
                taskName.startsWith("assemble") ||
                taskName.startsWith("bundle") ||
                taskName.startsWith("install")
        }
}

fun incrementPersistedAppVersion() {
    val properties = loadVersionProperties()
    val currentVersionName = properties.getProperty("versionName", "1.0")
    val currentVersionCode = properties.getProperty("versionCode", "1").toIntOrNull() ?: 1
    properties["versionName"] = nextVersionName(currentVersionName)
    properties["versionCode"] = (currentVersionCode + 1).toString()
    versionPropertiesFile.outputStream().use { output ->
        properties.store(output, "Anime Manager app version")
    }
}

val appVersionProperties = loadVersionProperties()
val appVersionName = appVersionProperties.getProperty("versionName", "1.0")
val appVersionCode = appVersionProperties.getProperty("versionCode", "1").toIntOrNull() ?: 1
val autoIncrementAppVersion = shouldAutoIncrementAppVersion()
val appVersionIncremented = AtomicBoolean(false)

tasks.configureEach {
    val taskName = name.lowercase()
    if (
        taskName == "build" ||
        taskName.startsWith("assemble") ||
        taskName.startsWith("bundle") ||
        taskName.startsWith("install")
    ) {
        doLast {
            if (autoIncrementAppVersion && appVersionIncremented.compareAndSet(false, true)) {
                incrementPersistedAppVersion()
            }
        }
    }
}

android {
    namespace = "com.example.animemanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.animemanager"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            isMinifyEnabled = false
        }
        create("releaseLike") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":feature:home"))
    implementation(project(":feature:library"))
    implementation(project(":feature:calendar"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:detail"))
    implementation(project(":feature:edit"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    coreLibraryDesugaring(libs.androidx.core.desugar)

    kapt(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.appcash.turbine)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)
}
