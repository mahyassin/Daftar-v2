plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.example.daftar"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.daftar"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    testImplementation("org.xerial:sqlite-jdbc:3.50.3.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)


    val navigationVersion = "1.2.0"

    implementation("androidx.navigation3:navigation3-runtime:$navigationVersion")
    implementation("androidx.navigation3:navigation3-ui:$navigationVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")


    val roomVersion = "2.8.5"

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    val lifecycleVersion = "2.11.0"
    // viewModel(factory = ...), ViewModel, and factory APIs


    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")


}
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
