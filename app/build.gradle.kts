import org.gradle.kotlin.dsl.invoke

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.cumtenn.printerlib"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.cumtenn.printerlib"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(project(":printer"))
//    implementation(fileTree("libs") { include("*.aar") })
//    implementation("org.snmp4j:snmp4j:3.9.6")
//    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
//    implementation("com.github.lingrong000:printerlib:8048530")
//    implementation("com.github.lingrong000.printerlib:printer:v1.0.7")
//    implementation("com.github.lingrong000.printerlib:ipp-client-kotlin:v1.0.7")
//    implementation("com.github.lingrong000:printerlib:2206127")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}