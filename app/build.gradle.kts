plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.tonydon.music_tangjian"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tonydon.music_tangjian"
        minSdk = 28
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("io.github.cymchad:BaseRecyclerViewAdapterHelper4:4.1.4")
    // Banner
    implementation("io.github.youth5201314:banner:2.2.2")
    // Glide
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // mmkv
    implementation("com.tencent:mmkv:2.2.2")
    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Gson
    implementation("com.google.code.gson:gson:2.13.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}