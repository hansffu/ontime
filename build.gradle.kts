// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  val compose_version by extra("1.6.8")
  val kotlin_version = "2.0.0"

  extra.apply {
    set("kotlin_version", "2.0.0")
  }
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:8.3.2")
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

