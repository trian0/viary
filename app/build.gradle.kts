import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.baselineprofile)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.trian0.viary"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.trian0.viary"
        minSdk = 24
        targetSdk = 37
        versionCode = 7
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "MAPBOX_ACCESS_TOKEN",
            "\"${localProperties.getProperty("MAPBOX_ACCESS_TOKEN", "")}\""
        )
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField(
                "String",
                "BANNER_AD_UNIT_ID",
                "\"ca-app-pub-3940256099942544/6300978111\""
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["admobAppId"] = "ca-app-pub-2346690986578970~6243381238"
            buildConfigField(
                "String",
                "BANNER_AD_UNIT_ID",
                "\"ca-app-pub-2346690986578970/2073306552\""
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Baseline profile and benchmark builds inherit release config; keep automated runs on AdMob test ids
// and add the benchmark-only source set (data seeding for the journeys), which release never sees
androidComponents {
    onVariants(selector().withBuildType("nonMinifiedRelease")) { it.configureForBenchmark() }
    onVariants(selector().withBuildType("benchmarkRelease")) { it.configureForBenchmark() }
}

fun com.android.build.api.variant.ApplicationVariant.configureForBenchmark() {
    sources.java?.addStaticSourceDirectory("src/benchmark/java")
    sources.manifests.addStaticManifestFile("src/benchmark/AndroidManifest.xml")
    useTestAdIds()
}

fun com.android.build.api.variant.ApplicationVariant.useTestAdIds() {
    manifestPlaceholders.put("admobAppId", "ca-app-pub-3940256099942544~3347511713")
    buildConfigFields?.put(
        "BANNER_AD_UNIT_ID",
        com.android.build.api.variant.BuildConfigField(
            "String",
            "\"ca-app-pub-3940256099942544/6300978111\"",
            null
        )
    )
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit5)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.coil.compose)
    implementation(libs.google.accompanist.permissions)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)
    testImplementation(libs.room.testing)

    // Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Test
    testImplementation(libs.bundles.unit.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Paging 3
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Maps
    implementation(libs.mapbox.maps)

    // Ads
    implementation(libs.play.services.ads)
    implementation(libs.ump)

    configurations.all {
        exclude(group = "com.intellij", module = "annotations")
    }
}