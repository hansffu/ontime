plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.apollo)
    alias(libs.plugins.hilt)
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "dev.hansffu.ontime"
        minSdk = 26
        targetSdk = 33
        versionCode = 31
        versionName = "3.0"
        vectorDrawables {
            useSupportLibrary = true
        }
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["dagger.hilt.disableModulesHaveInstallInCheck"] = "true"
            }
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }

    kotlin {
        jvmToolchain(17)
    }

    namespace = "dev.hansffu.ontime"
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(fileTree("include" to "*.jar", "dir" to "libs"))
    implementation(libs.material)
    compileOnly(libs.wearable)
    implementation(libs.play.services.location)
    implementation(libs.androidx.wear)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.apollo.runtime)
    implementation(libs.apollo.adapters)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)


    //compose
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.input)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.runtime.livedata)

    implementation(libs.accompanist.placeholder.material)
    implementation(libs.accompanist.permissions)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.material)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}

apollo {
    service("entur") {
        packageName.set("dev.hansffu.ontime.graphql")
        mapScalar(
            "DateTime",
            "java.time.OffsetDateTime",
            "com.apollographql.apollo3.adapter.JavaOffsetDateTimeAdapter"
        )
//        customScalarsMapping.set(mapOf("DateTime" to "java.time.OffsetDateTime"))
    }
}

