plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.dr.qck"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dr.qck"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "1.0.1"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // room db
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    //datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    //activity
    implementation("androidx.activity:activity-ktx:1.8.2")

    //dagger
    implementation("com.google.dagger:hilt-android:2.50")
    kapt ("com.google.dagger:dagger-compiler:2.50")
    kapt ("com.google.dagger:hilt-compiler:2.50")

    //multidex
    implementation("androidx.multidex:multidex:2.0.1")

    //splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

}