import com.android.build.gradle.internal.packaging.defaultExcludes

plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.powersaver"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.powersaver"
        minSdk = 26 // Updated to API 26 for compatibility with JavaMail and Base64 encoder
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
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
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources {
            excludes.add("/META-INF/DEPENDENCIES")
            excludes.add("/META-INF/LICENSE.md")
            excludes.add("/META-INF/*")
        }
    }
}

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.8.3")
    implementation("androidx.navigation:navigation-ui:2.8.3")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Networking
    implementation("com.android.volley:volley:1.2.1")

    // Google Play Services Auth
    implementation("com.google.android.gms:play-services-auth:20.2.0")

    // Google API Client libraries for Gmail API
    implementation("com.google.api-client:google-api-client:1.33.0")
    implementation("com.google.api-client:google-api-client-android:1.33.0")
    implementation("com.google.api-client:google-api-client-gson:1.33.0")

    // Gmail API library
    implementation("com.google.apis:google-api-services-gmail:v1-rev20230123-2.0.0")

    // Google OAuth2
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")

    // iText for PDF generation
    implementation("com.itextpdf:itext7-core:7.2.5")

    // JavaMail and Activation libraries for Android compatibility
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    implementation ("com.google.http-client:google-http-client-gson:1.40.0")

}
