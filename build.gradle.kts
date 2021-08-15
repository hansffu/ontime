// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  val kotlin_version = "1.5.21"
  val apollo_version = "2.5.9"

  extra.apply {
    set("kotlin_version", "1.5.21")
    set("apollo_version", "2.5.9")
  }
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.0.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    classpath("com.apollographql.apollo:apollo-gradle-plugin:$apollo_version")
    classpath(kotlin("gradle-plugin", version = kotlin_version))

    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
  }
}

