plugins {
    alias(libs.plugins.android.library)
    kotlin("android")
    id("maven-publish") // Maven发布插件
}

android {
    namespace = "com.cumtenn.printer"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
//    implementation(fileTree("libs") { include("*.aar") })
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("org.snmp4j:snmp4j:3.9.6")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.25")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                // 从 Android library 的 components 中获取 release artifact（包含 AAR + pom）
                from(components["release"])
                groupId = "com.github.lingrong000"
                artifactId = "printer"
                version = "v1.1.1"
            }
        }
    }
}