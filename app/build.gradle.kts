import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.io.FileInputStream
import java.util.Properties

val apollo_version = "2.5.9"
plugins {
    id("com.android.application")
    id("com.apollographql.apollo")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.2"
    defaultConfig {
        applicationId = "hansffu.ontime"
        minSdk = 26
        targetSdk = 30
        versionCode = 18
        versionName = "2.0"

    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    lint {
        isCheckDependencies = true
    }

    apollo {
        customTypeMapping.set(mapOf("DateTime" to "java.lang.String"))
    }

}

dependencies {
    implementation(fileTree("include" to "*.jar", "dir" to "libs"))
    implementation("com.google.android.support:wearable:2.8.1")
    compileOnly("com.google.android.wearable:wearable:2.8.1")
    implementation("com.google.android.gms:play-services-wearable:17.1.0")
    implementation("com.google.android.gms:play-services-location:18.0.0")
    implementation("com.android.volley:volley:1.2.0")
    implementation("org.apache.commons:commons-collections4:4.1")
    implementation("androidx.wear:wear:1.1.0")
    implementation("androidx.activity:activity-ktx:1.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("com.squareup.retrofit2:retrofit:2.6.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.0")
    implementation("com.squareup.retrofit2:converter-gson:2.6.0")
    implementation("com.apollographql.apollo:apollo-runtime:$apollo_version")
    implementation("com.apollographql.apollo:apollo-coroutines-support:$apollo_version")
    compileOnly("org.jetbrains:annotations:13.0")
    testCompileOnly("org.jetbrains:annotations:13.0")
}
