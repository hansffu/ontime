// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  val kotlinVersion = "1.4.10"
  val apolloVersion = "2.4.1"

  extra.apply {
    set("kotlin_version", kotlinVersion)
    set("apollo_version", apolloVersion)
  }
  repositories {
    jcenter()
    google()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:4.1.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    classpath("com.apollographql.apollo:apollo-gradle-plugin:$apolloVersion")
    classpath(kotlin("gradle-plugin", version = kotlinVersion))

    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
  }
}

allprojects {
  repositories {
    jcenter()
    google()
    maven { url = uri("https://jitpack.io") }
  }
}

tasks.register("clean",Delete::class) {
  delete(rootProject.buildDir)
}
