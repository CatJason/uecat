plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.jakewharton.scalpel"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    afterEvaluate {
        publishing {
            val versionName = "1.0.2"
            publications {
                create<MavenPublication>("release") {
                    from(components["release"])
                    groupId = "com.jakewharton.scalpel"
                    artifactId = "scalpellibrary"
                    version = versionName
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
}

dependencies {
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.5.0")
}