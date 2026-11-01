plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.cercafunctions.appfunctions"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core-domain"))
    implementation(libs.androidx.appfunctions)
}
