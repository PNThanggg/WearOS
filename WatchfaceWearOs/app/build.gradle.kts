@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.vietapps.watchface"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.vietapps.watchface"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

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

    implementation(libs.core.ktx)

    implementation(libs.play.services.wearable)
    implementation(libs.play.services.base)

    implementation(libs.palette.ktx)

    implementation(libs.material)

    implementation(libs.wear.watchface)
    implementation(libs.wear.watchface.client)
    implementation(libs.wear.watchface.complications.rendering)
    implementation(libs.wear.watchface.data)
    implementation(libs.wear.watchface.editor)
    implementation(libs.wear.watchface.style)

    implementation(libs.percentlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.recyclerview)


}