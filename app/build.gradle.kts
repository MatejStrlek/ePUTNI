import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "hr.algebra.eputni"
    compileSdk = 34

    defaultConfig {
        applicationId = "hr.algebra.eputni"
        minSdk = 26
        targetSdk = 34
        versionCode = 7
        versionName = "2.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String", "webClientId", properties.getProperty("webClientId"))
    }

    signingConfigs {
        create("release") {
            storeFile = file("D:\\Algebra\\Zavrsni_rad\\release-key.jks")
            storePassword = "edcbrothers"
            keyAlias = "mainkey"
            keyPassword = "edcbrothers"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation ("com.google.android.gms:play-services-auth:21.2.0")
    implementation ("com.google.android.gms:play-services-auth-api-phone:18.1.0")
    implementation ("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("androidx.navigation:navigation-runtime-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.mlkit:vision-common:17.3.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}