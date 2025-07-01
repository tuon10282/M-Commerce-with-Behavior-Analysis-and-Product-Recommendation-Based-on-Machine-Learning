plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
// Bắt buộc để Firebase nhận google-services.json
}

android {
    namespace = "com.calmpuchia.userapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.calmpuchia.userapp"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // ✅ Firebase BoM: quản lý version đồng bộ
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))

    // ✅ Firebase SDK (không cần version nếu dùng BoM)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-messaging")

    // Google Play Services cho GPS
    implementation ("com.google.android.gms:play-services-location:21.0.1")


    // ✅ AndroidX & UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)

    // ✅ Google Sign-In & Credentials API
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // ✅ Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Up ảnh product
    implementation("com.squareup.picasso:picasso:2.8")
    implementation ("com.google.firebase:firebase-messaging:23.2.1")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    // Converter JSON => Java object
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    // Nếu cần log request (debug)
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    // UCrop for image cropping
    implementation ("com.vanniktech:android-image-cropper:4.5.0")




}
