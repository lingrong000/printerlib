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

publishing {
    publications {
        // 为每个 variant 创建发布（这里以 release 为例）
        register<MavenPublication>("release") {
            // 手动指定 artifact
            afterEvaluate {
                // 使用 bundleReleaseAar 任务的输出作为 artifact
                val bundleTask = tasks.named("bundleReleaseAar").get()
                artifact(bundleTask)

                // 配置 Maven 坐标
                groupId = "com.github.lingrong000"
                artifactId = if (project.name == "printer") "printer" else "ipp-client-kotlin"
                version = "v1.0.7"

                // 手动创建 POM 依赖信息
                pom.withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")

                    // 为 printer 模块添加对 ipp-client-kotlin 的依赖声明
                    if (project.name == "printer") {
                        val dependencyNode = dependenciesNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", "com.github.lingrong000")
                        dependencyNode.appendNode("artifactId", "ipp-client-kotlin")
                        dependencyNode.appendNode("version", "v1.0.7")
                        dependencyNode.appendNode("scope", "compile")
                    }

                    // 你可以在这里添加其他传递依赖
                }
            }
        }
    }
}