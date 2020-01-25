import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.io.FileInputStream
import java.util.Properties

val apollo_version = "1.3.0"
plugins {
  id("com.android.application")
  id("com.apollographql.android")
  kotlin("android")
  kotlin("android.extensions")
}

android {

  compileSdkVersion(29)
  buildToolsVersion("29.0.2")
  defaultConfig {
    applicationId = "hansffu.ontime"
    minSdkVersion(26)
    targetSdkVersion(29)
    versionCode = 15
    versionName = "2.0"

  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }


  apollo {
    customTypeMapping.set(mapOf("DateTime" to "java.lang.String"))
  }

  signingConfigs {
    register("release")
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs["release"]
    }
  }
  val localProps = Properties()
  val isRunningOnTravis = System.getenv("CI") == "true"

  if (isRunningOnTravis) {
    signingConfigs["release"].storeFile = file("../hansffu_upload_key.jks")
    signingConfigs["release"].storePassword = System.getenv("keystore_password")
    signingConfigs["release"].keyAlias = System.getenv("keystore_alias")
    signingConfigs["release"].keyPassword = System.getenv("keystore_alias_password")

  } else if (file("../local.properties").exists()) {
    localProps.load(FileInputStream(file("../local.properties")))
    val keyProps = Properties()
    if (localProps["keystore.props.file"] != null) {
      keyProps.load(FileInputStream(file(localProps.getProperty("keystore.props.file"))))
//      keyProps.load(file(uri(localProps["keystore.props.file"])))
    }
    signingConfigs["release"].storeFile = if (keyProps.getProperty("store") != null) File(keyProps.getProperty("store")) else null
    signingConfigs["release"].keyAlias = keyProps.getProperty("alias")
    signingConfigs["release"].storePassword = keyProps.getProperty("storePass")
    signingConfigs["release"].keyPassword = keyProps.getProperty("pass")
  }
}

dependencies {
  implementation(fileTree("include" to "*.jar", "dir" to "libs"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${KotlinCompilerVersion.VERSION}")
  implementation("com.google.android.support:wearable:2.5.0")
  compileOnly("com.google.android.wearable:wearable:2.5.0")
  implementation("com.google.android.gms:play-services-wearable:17.0.0")
  implementation("com.google.android.gms:play-services-location:17.0.0")
  implementation("com.android.volley:volley:1.1.0")
  implementation("org.apache.commons:commons-collections4:4.1")
  implementation("androidx.recyclerview:recyclerview:1.1.0")
  implementation("androidx.percentlayout:percentlayout:1.0.0")
  implementation("androidx.cardview:cardview:1.0.0")
  // implementation("com.android.support:support-v4:28.0.0")
  implementation("androidx.wear:wear:1.0.0")
  implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
  implementation("com.patloew.rxlocation:rxlocation:1.0.5")
  implementation("com.github.tbruyelle:rxpermissions:0.10.2")
  implementation("com.squareup.retrofit2:retrofit:2.6.0")
  implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.0")
  implementation("com.squareup.retrofit2:converter-gson:2.6.0")
  implementation("com.apollographql.apollo:apollo-runtime:$apollo_version")
  implementation("com.apollographql.apollo:apollo-rx2-support:$apollo_version")
  implementation("io.reactivex.rxjava2:rxkotlin:2.3.0")
  compileOnly("org.jetbrains:annotations:13.0")
  testCompileOnly("org.jetbrains:annotations:13.0")
}
repositories {
  mavenCentral()
}
