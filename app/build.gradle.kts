plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
android {
 namespace = "com.augelab.visualfilter"
 compileSdk = 35
 defaultConfig { applicationId = "com.augelab.visualfilter.research"; minSdk = 29; targetSdk = 35; versionCode = 25; versionName = "0.9.2-mobilenetv3small-ft"; ndk { abiFilters += providers.gradleProperty("visualFilterAbi").orElse("arm64-v8a").get().split(",") } }
 providers.gradleProperty("visualFilterKeyStore").orNull?.let { keyPath -> signingConfigs.getByName("debug").storeFile=file(keyPath) }
 buildTypes {
  getByName("release") {
   isMinifyEnabled = true
   isShrinkResources = true
   signingConfig = signingConfigs.getByName("debug")
   proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
  }
 }
 buildFeatures { compose = true; buildConfig = false }
 // Models are copied and verified before ORT loads them, so APK ZIP compression is safe.
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2025.02.00"))
 implementation("androidx.activity:activity-compose:1.10.1")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
 implementation("com.microsoft.onnxruntime:onnxruntime-android:1.30.0")
 testImplementation("junit:junit:4.13.2")
}
