plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
val filterPackage="com.augelab.visualfilter.research"
val benchmarkPackage="com.augelab.visualfilter.benchmark"
check(benchmarkPackage!=filterPackage){"Instrumentation must never target the accessibility filter package"}
val stageClassifier by tasks.registering(Sync::class) {
 from("../app/src/main/java") { include("**/OnnxResearchClassifier.kt","**/BinaryModelContract.kt") }
 into(layout.buildDirectory.dir("generated/model-sources"))
}
android {
 namespace="com.augelab.visualfilter.benchmark"
 compileSdk=35
 defaultConfig { applicationId=benchmarkPackage; minSdk=29; targetSdk=35; versionCode=1; versionName="1.0-isolated-probe"; ndk { abiFilters += providers.gradleProperty("visualFilterAbi").orElse("x86_64").get().split(",") } }
 providers.gradleProperty("visualFilterKeyStore").orNull?.let{signingConfigs.getByName("debug").storeFile=file(it)}
 sourceSets.getByName("main").assets.srcDirs("../app/src/main/assets","../app/src/androidTest/assets")
 sourceSets.getByName("main").java.srcDir(layout.buildDirectory.dir("generated/model-sources"))
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget="17" }
}
tasks.named("preBuild").configure { dependsOn(stageClassifier) }
dependencies {
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
 implementation("com.microsoft.onnxruntime:onnxruntime-android:1.30.0")
}
