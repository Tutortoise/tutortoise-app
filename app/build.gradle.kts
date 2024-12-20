import java.io.FileInputStream
import java.util.Properties

val localProperties = Properties()

localProperties.load(FileInputStream(rootProject.file("local.properties")))

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
}

val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(rootProject.file("keystore.properties")))

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties.getProperty("store.file"))
            storePassword = keystoreProperties.getProperty("store.password")
            keyAlias = keystoreProperties.getProperty("key.alias")
            keyPassword = keystoreProperties.getProperty("key.password")
        }

        getByName("debug") {
            storeFile = file(keystoreProperties.getProperty("store.file"))
            storePassword = keystoreProperties.getProperty("store.password")
            keyAlias = keystoreProperties.getProperty("key.alias")
            keyPassword = keystoreProperties.getProperty("key.password")
        }
    }
    namespace = "com.tutortoise.tutortoise"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tutortoise.tutortoise"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val baseUrl = localProperties.getProperty(
            "base.url",
            System.getenv("BASE_URL") ?: "https://default-url.com/"
        )
        val googleOauthClientId = localProperties.getProperty(
            "google.oauth.client.id",
            System.getenv("GOOGLE_OAUTH_CLIENT_ID") ?: ""
        )

        buildConfigField(
            "String",
            "BASE_URL",
            "\"$baseUrl\""
        )

        buildConfigField(
            "String",
            "GOOGLE_OAUTH_CLIENT_ID",
            "\"$googleOauthClientId\""
        )

        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug { signingConfig = signingConfigs.getByName("debug") }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
    buildFeatures { viewBinding = true }
    viewBinding { enable = true }
}

dependencies {
    implementation(libs.androidx.core)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.flexbox)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
    implementation(libs.androidx.viewpager2)
    implementation(libs.circleimageview)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database)
    implementation(libs.google.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.material.icons.extended)
    implementation(libs.play.services.location)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.security.crypto)
    implementation(libs.glide)
    implementation(libs.shimmer.android)
    implementation(libs.ucrop)
}