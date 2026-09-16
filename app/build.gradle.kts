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

// Token bot nam trong local.properties, file do khong bao gio vao git. Chua dien
// thi van build duoc: BuildConfig nhan chuoi rong, app chay len se hoi ba dien tay.
// Xem local.properties.mau.
val biMatFile = rootProject.file("local.properties")
val biMat = Properties().apply {
    if (biMatFile.exists()) {
        biMatFile.inputStream().use { load(it) }
    }
}

// Doc mot dong trong local.properties ra dang chuoi Kotlin da boc san nhay kep,
// vi buildConfigField nhan nguyen van doan ma chu khong nhan gia tri.
fun chuoiBiMat(ten: String): String {
    val v = (biMat.getProperty(ten) ?: "").trim()
    return "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
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

        buildConfigField("String", "BOT_THAT", chuoiBiMat("BOT_THAT"))
        buildConfigField("String", "BOT_MAY_AO", chuoiBiMat("BOT_MAY_AO"))
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.coroutines.android)
}
