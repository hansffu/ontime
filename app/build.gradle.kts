val apolloVersion = "3.1.0"
val room_version = "2.4.2"
val accompanistVersion = "0.20.3"

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.apollographql.apollo3") version "3.8.5"
}

android {
    compileSdk = 31
    buildToolsVersion = "30.0.3"
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

    kotlin{
        jvmToolchain(17)
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    namespace = "hansffu.ontime"
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
   compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kapt {
        javacOptions {
            option("--target", 17)
        }
    }
}

dependencies {
    implementation(fileTree("include" to "*.jar", "dir" to "libs"))
    implementation("com.google.android.support:wearable:2.9.0")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    compileOnly("com.google.android.wearable:wearable:2.9.0")
    implementation("com.google.android.gms:play-services-wearable:17.1.0")
    implementation("com.google.android.gms:play-services-location:19.0.1")
    implementation("androidx.wear:wear:1.2.0")
    implementation("androidx.wear:wear-input:1.1.0")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.5")
    compileOnly("org.jetbrains:annotations:17.0.0")

    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    implementation("org.jetbrains:annotations:17.0.0")

    //compose
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["compose_version"]}")

    implementation("androidx.wear.compose:compose-material:1.0.0-alpha18")
    implementation("androidx.wear.compose:compose-foundation:1.0.0-alpha18")
    implementation("androidx.wear.compose:compose-navigation:1.0.0-alpha18")
    implementation("androidx.compose.material:material-icons-extended:1.1.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.1.1")

    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-pager:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-placeholder-material:$accompanistVersion")

    debugImplementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${rootProject.extra["compose_version"]}")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${rootProject.extra["compose_version"]}")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
apollo {
    service("entur"){
        packageName.set("hansffu.ontime.graphql")
        customScalarsMapping.set(mapOf("DateTime" to "java.time.OffsetDateTime"))
    }
}

