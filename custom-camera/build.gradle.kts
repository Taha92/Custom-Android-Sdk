plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    `maven-publish`
}

android {
    namespace = "com.example.custom_camera"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.custom_camera"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // CameraX core library using camera2 implementation
    implementation("androidx.camera:camera-camera2:1.3.3")

    // CameraX Lifecycle Library
    implementation("androidx.camera:camera-lifecycle:1.3.3")

    // CameraX View class
    implementation("androidx.camera:camera-view:1.0.0-alpha14")
    implementation("androidx.camera:camera-video:1.0.0-alpha14")

    // TFLite
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.3.1")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.Taha92"
            artifactId = "custom-camera"
            version = "1.0"

            //from(components["release"])
        }
    }
}