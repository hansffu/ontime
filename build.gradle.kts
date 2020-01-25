// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  val kotlin_version = "1.3.61"
  val apollo_version = "1.2.2"

  extra.apply {
    set("kotlin_version", "1.3.61")
    set("apollo_version", "1.2.2")
  }
  repositories {
    jcenter()
    google()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:3.5.3")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    classpath("com.apollographql.apollo:apollo-gradle-plugin:$apollo_version")
    classpath(kotlin("gradle-plugin", version = kotlin_version))

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
