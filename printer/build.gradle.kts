plugins {
    alias(libs.plugins.android.library)
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
}

dependencies {
//    implementation(fileTree("libs") { include("*.aar") })
    implementation(libs.appcompat)
    implementation(libs.material)
    api(project(":ipp-client-kotlin"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("org.snmp4j:snmp4j:3.9.6")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
}

//afterEvaluate {
//    publishing {
//        publications {
//            create<MavenPublication>("release") {
//                from(components["all"])
//                 groupId = "com.cumtenn"
//                 artifactId = "printer"
//                 version = "1.0.0"
//            }
//        }
//    }
//}