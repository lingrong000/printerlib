// how to build? run ./gradlew
// where is the aar? build/outputs/aar/

plugins {
    id("com.android.library") version "8.13.2"      // Android库插件
    id("org.jetbrains.kotlin.android") version "1.9.25" // Kotlin Android插件
    id("maven-publish") // Maven发布插件
}

group = "de.gmuth"
version = "3.4"

android {
    namespace = "de.gmuth.ipp"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }
    
    buildTypes {
        debug {
            isMinifyEnabled = false
            // 可选：为 debug 变体启用混淆规则（如果需要）
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.25")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

// gradlew clean -x test build
defaultTasks("assemble")

// 简化的 Maven 发布配置
//publishing {
//    publications {
//        register("release", MavenPublication::class) {
//            // 直接引用 Android 库的 AAR 产物
//            artifact("build/outputs/aar/ipp-client-release.aar")
//
//            groupId = group.toString()
//            artifactId = "ipp-client-kotlin"
//            version = version.toString()
//        }
//    }
//
//    repositories {
//        mavenLocal()
//    }
//}

publishing {
    publications {
        // release variant publication
        register<MavenPublication>("release") {
            afterEvaluate {
                // 从 Android library 的 components 中获取 release artifact（包含 AAR + pom）
                from(components["release"])
                groupId = "com.github.lingrong000"
                artifactId = "ipp-client-kotlin"
                version = "1.0.0"
            }
        }

        // debug variant publication（如果你确实需要在仓库中同时发布 debug 版本）
        register<MavenPublication>("debug") {
            afterEvaluate {
                from(components["debug"])
                groupId = "com.github.lingrong000"
                artifactId = "ipp-client-kotlin-debug"
                version = "1.0.0-debug"
            }
        }
    }

    repositories {
        mavenLocal()
    }
}