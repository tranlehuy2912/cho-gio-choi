import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Giong ben homework-gate: mat khau con dau nam ngoai ma nguon. Chua tao file thi
// van build duoc ban go loi, chi ban release la chua ky duoc.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}
val hasKeystore = keystorePropsFile.exists()

// Plugin google-services chi bat khi da co google-services.json, y het ben
// homework-gate.
//
// Bat vo dieu kien thi may nao chua tai file ve la build do ngay tu dau. Thieu file
// thi app nay van mo len duoc, chi la man nao cung bao "chua noi Firebase" thay vi
// tat ngang.
//
// HAI DU AN FIREBASE, MOI BAN BUILD MOT CAI - dung ba file cua ben homework-gate,
// vi ba app phai o cung mot du an moi nhin thay chung mot cai nha:
//
//   app/src/debug/google-services.json   du an THU  - may ao dung
//   app/google-services.json             du an THAT - may ba noi dung
//
// Hai file nay khong phai ban sao cua ben homework-gate: moi applicationId la mot
// "app" rieng trong cung du an Firebase. Vao console, them app Android
// vn.huytl.chogiochoi vao dung du an do, roi tai file moi ve.
val fileFirebaseThat = file("google-services.json")
val fileFirebaseThu = file("src/debug/google-services.json")
val coFirebase = fileFirebaseThat.exists() || fileFirebaseThu.exists()
if (coFirebase) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.warn("Chua co app/google-services.json - ban build nay khong noi duoc sang tablet.")
}

if (coFirebase && !fileFirebaseThu.exists()) {
    logger.warn(
        "CHU Y: chua co app/src/debug/google-services.json, nen BAN GO LOI DANG NOI " +
            "VAO DU AN FIREBASE THAT."
    )
}

android {
    namespace = "vn.huytl.chogiochoi"
    compileSdk = 37

    defaultConfig {
        applicationId = "vn.huytl.chogiochoi"
        // Thap hon ben tablet (26). May cua ba noi khong biet doi nao, ha xuong
        // Android 7 cho chac; app nay khong dung API nao moi hon the.
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

    }

    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (hasKeystore) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        // Cho [Defaults] biet dang la ban go loi hay ban that de chon bot.
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(libs.coroutines.android)
}
