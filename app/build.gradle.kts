plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.quickconnect"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.quickconnect"
        minSdk = 22
        targetSdk = 33
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    implementation("androidx.palette:palette-ktx:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.google.zxing:core:3.4.1")

    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation ("me.dm7.barcodescanner:zxing:1.9.13")
    
    implementation ("com.google.android.gms:play-services-vision:20.1.3")

    implementation ("com.github.fabiosassu:StackExpandableView:1.0.3")

    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.4")

    // retrofit

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")

// GSON

    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

// coroutine

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")


    implementation("io.coil-kt:coil-svg:2.2.0")
    implementation ("io.coil-kt:coil:1.4.0") // Use the latest version


    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.6.2") // For Kotlin use lifecycle-viewmodel-ktx


    implementation ("androidx.lifecycle:lifecycle-runtime: 2.1.0")

    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
// Lifecycles only (without ViewModel or LiveData)
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Coroutine Lifecycle Scopes
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")


// ViewModel and LiveData
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
// alternatively - just ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.6.2") // For Kotlin use lifecycle-viewmodel-ktx
// alternatively - just LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata:2.1.0")
// alternatively - Lifecycles only (no ViewModel or LiveData). Some UI
//     AndroidX libraries use this lightweight import for Lifecycle
    implementation ("androidx.lifecycle:lifecycle-runtime:2.1.0")

    annotationProcessor ("androidx.lifecycle:lifecycle-compiler:2.1.0") // For Kotlin use kapt instead of annotationProcessor
// alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation ("androidx.lifecycle:lifecycle-common-java8:2.1.0")

// optional - ReactiveStreams support for LiveData
    implementation ("androidx.lifecycle:lifecycle-reactivestreams:2.1.0") // For Kotlin use lifecycle-reactivestreams-ktx

// optional - Test helpers for LiveData
    testImplementation ("androidx.arch.core:core-testing:2.1.0")

    // Replace Coil with Picasso in your build.gradle
    implementation ("com.squareup.picasso:picasso:2.8")

    implementation ("com.sdsmdg.tastytoast:tastytoast:0.1.1")

    // Material Dialog Library
    implementation ("dev.shreyaspatil.MaterialDialog:MaterialDialog:2.2.3")

    // Material Design Library
    implementation ("com.google.android.material:material:1.0.0")

    // Lottie Animation Library
    implementation ("com.airbnb.android:lottie:5.2.0")


    implementation ("com.github.bumptech.glide:glide:4.16.0")

//    implementation ("com.github.pavlospt:BlurBehind:1.0.0")












}