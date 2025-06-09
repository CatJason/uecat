plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "me.ele.uetool"
    compileSdk = 33

    defaultConfig {
        minSdk = 23

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

    // 添加组件发布配置（方案二）
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

afterEvaluate {
    publishing {
        val versionName = "1.0.9"
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "me.ele.uetool"
                artifactId = "uetool"
                version = versionName

                // 可选的 POM 配置
                pom {
                    name.set("UETool")
                    description.set("A tool for Android UI development")
                    url.set("https://github.com/your-repo/uetool")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("0001")
                            name.set("catJason")
                            email.set("jason20121221.email@example.com")
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                val baseUrl = buildDir.parent
                val releasesRepoUrl = "$baseUrl/repos/releases"
                val snapshotsRepoUrl = "$baseUrl/repos/snapshots"
                url = uri(if (versionName.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            }
        }
    }
}

dependencies {
    implementation("com.github.tiann:FreeReflection:3.1.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
}