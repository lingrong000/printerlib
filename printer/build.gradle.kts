import java.time.LocalDate
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    kotlin("android")
    id("maven-publish") // Maven发布插件
}

// 生成版本号：yyyyMMddxx，支持自动递增
fun generateVersionName(): String {
    val date = LocalDate.now()
    val dateStr = String.format("%04d%02d%02d", date.year, date.monthValue, date.dayOfMonth)
    
    // 版本文件路径
    val versionFile = file("version.properties")
    
    // 读取现有版本
    var sequence = 1
    if (versionFile.exists()) {
        val props = Properties()
        versionFile.inputStream().use { props.load(it) }
        val savedDate = props.getProperty("date", "")
        val savedSequence = props.getProperty("sequence", "0").toIntOrNull() ?: 0
        
        if (savedDate == dateStr) {
            // 同一天，递增序号
            sequence = savedSequence + 1
        }
        // 不同天，序号重置为1
    }
    
    // 保存新版本
    val props = Properties()
    props.setProperty("date", dateStr)
    props.setProperty("sequence", sequence.toString())
    versionFile.outputStream().use { props.store(it, "Version info") }
    
    return "${dateStr}${String.format("%02d", sequence)}"
}

val versionName = generateVersionName()

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
    
    // 配置AAR输出文件名
    libraryVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.LibraryVariantOutputImpl
            output.outputFileName = "printer_${versionName}.aar"
        }
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
                version = versionName
            }
        }
    }
}