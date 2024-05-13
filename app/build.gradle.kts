plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.customcamerasdkapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.customcamerasdkapp"
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
    /*buildFeatures {
        mlModelBinding = true
    }*/
}

dependencies {

    /*implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)*/

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.72")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.0")
    implementation("androidx.exifinterface:exifinterface:1.3.2")
/*


    // CameraX core library using camera2 implementation
    implementation("androidx.camera:camera-camera2:1.3.3")

    // CameraX Lifecycle Library
    implementation("androidx.camera:camera-lifecycle:1.3.3")

    // CameraX View class
    implementation("androidx.camera:camera-view:1.0.0-alpha14")
    implementation("androidx.camera:camera-video:1.0.0-alpha14")

    // TFLite
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.3.1")
*/

    implementation(project(":custom-camera"))
    //implementation("com.github.Taha92:Custom-Android-Sdk:1.0")

    /*implementation("org.tensorflow:tensorflow-lite-task-vision:0.3.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")*/



}