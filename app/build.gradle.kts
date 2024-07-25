plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.apollo)
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "hansffu.ontime"
        minSdk = 26
        targetSdk = 33
        versionCode = 31
        versionName = "3.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    namespace = "hansffu.ontime"
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
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
    implementation(libs.support.wearable)
    implementation(libs.material)
    implementation(libs.lifecycle.runtime.ktx)
    compileOnly(libs.wearable)
    implementation(libs.play.services.wearable)
    implementation(libs.play.services.location)
    implementation(libs.androidx.wear)
    implementation(libs.wear.input)
    implementation(libs.activity.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.viewpager2)
    implementation(libs.apollo.runtime)
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.jetbrains.annotations)

    //compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.runtime.livedata)

    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.placeholder.material)
    implementation(libs.accompanist.permissions)

    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.material)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}

apollo {
    service("entur") {
        packageName.set("hansffu.ontime.graphql")
        customScalarsMapping.set(mapOf("DateTime" to "java.time.OffsetDateTime"))
    }
}

