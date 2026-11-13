# Katkı rehberi

Visual Filter'a katkı vermek istediğiniz için teşekkürler. Bu proje bir prototiptir ve hassas bir konuyla ilgilidir. Bu yüzden aşağıdaki kurallar herkes için geçerlidir.

## Hangi katkılar aranıyor

- **Kod:** hata düzeltmeleri, maske yerleşimi ve takibi, Android uyumluluğu, kod kalitesi ve testler.
- **Değerlendirme araçları:** sonuçları ölçen, raporlayan veya tekrarlanabilir kılan betikler. Araçlar görsel yerine sayısal sonuçlar (olasılık, süre, karar, sayılar) üretmelidir.
- **Etiketleme yönergeleri:** neyin hangi sınıfa girmesi gerektiğini açıklayan yazılı kurallar ve sınır durumlarının metinle tarifi.
- **Performans:** çıkarım süresi, bellek kullanımı ve pil tüketimi iyileştirmeleri. Ölçüm yaptıysanız cihaz modelini, Android sürümünü ve ölçüm yöntemini yazın.

## Kesin kurallar

1. **Issue, pull request, yorum veya ek dosya olarak görsel, video ya da ekran görüntüsü göndermeyin.** Buna test görselleri, model çıktısı görselleri ve "örnek" amaçlı içerikler de dahildir. Bir durumu anlatmanız gerekiyorsa metinle tarif edin veya sayısal sonuçları paylaşın.
2. **Kişisel veri göndermeyin.** İsim, kullanıcı adı, profil bağlantısı, yüz içeren görsel, telefon numarası, konum, cihaz kimliği veya ham sistem günlüğü (logcat) paylaşmayın. Günlük paylaşmanız gerekiyorsa yalnızca uygulamanın sayısal ölçüm satırlarını ve kişisel bilgilerden arındırılmış hâlini ekleyin.
3. **Eğitim ve değerlendirme verisi yalnızca lisansı açık veri kümelerinden gelmelidir.** Kullandığınız veri kümesinin adını, kaynağını ve lisansını yazın. Lisansı belirsiz, izinsiz toplanmış veya sosyal medyadan kazınmış veri kabul edilmez.
4. **Reşit olmayan kişileri içeren hiçbir içerik kullanılamaz, paylaşılamaz ve tarif edilemez.** Bu kuralın istisnası yoktur.
5. **Yasa dışı içerikle karşılaşırsanız bu depoya değil, yetkili makamlara bildirin.** Türkiye'de bunun için örneğin İhbar Web (ihbarweb.org.tr) kullanılabilir. Böyle bir içeriği hiçbir biçimde depoya, issue'lara veya pull request'lere eklemeyin.
6. Model ağırlıkları (`.onnx`, `.tflite`, `.pt` vb.), APK dosyaları ve imzalama anahtarları depoya eklenmez. `.gitignore` bunları dışlar; lütfen zorla eklemeyin.

Bu kurallara uymayan katkılar açıklama yapılmadan kapatılabilir ve içerik silinebilir.

## Derleme

### Gerekenler

Kaynak koddaki sürümler:

| Bileşen | Sürüm |
|---|---|
| JDK | 17 |
| Android SDK | Platform 35 (compileSdk ve targetSdk 35), build-tools 35.0.0 |
| Gradle | 8.11.1 (depodaki Gradle wrapper ile, dağıtım SHA-256 değeri sabitlenmiş) |
| Android Gradle Plugin | 8.9.1 |
| Kotlin | 2.1.10 (Compose derleyici eklentisi dahil) |
| Jetpack Compose BOM | 2025.02.00 |
| ONNX Runtime Android | 1.30.0 |
| minSdk | 29 (filtre çalışma zamanında Android 14 / API 34 ister) |

Android SDK yolunu `ANDROID_HOME` ortam değişkeniyle veya depoya eklenmeyen bir `local.properties` dosyasında (`sdk.dir=...`) belirtin.

### Modüller

- `app`: Visual Filter uygulaması (paket adı `com.augelab.visualfilter.research`).
- `synthetic-target`: Emülatörde maskelemeyi denemek için kontrollü test ekranı. Test görselleri olmadan derlenir ama görsel gösteremez.
- `model-benchmark`: Android ile bilgisayar arasındaki sayısal tutarlılığı ve model süresini ölçen ayrı test uygulaması. Model dosyası ve referans dosyası (`binary-parity.json`) olmadan derlenir ama çalışmaz.

### Test verileri dağıtılmaz

Bu depo hiçbir test görselini ve bu görsellere ait etiket, kaynak veya referans kaydını içermez. `app/src/androidTest/assets/` ve `synthetic-target/src/main/assets/` klasörleri `.gitignore` ile depodan dışlanmıştır. Birim testleri (`app/src/test`) bu verilere ihtiyaç duymaz.

Emülatör veya karşılaştırma testlerini çalıştırmak isterseniz kendi test verinizi yalnızca yerelde hazırlayın. Bu veri:

- lisansı açık ve yeniden kullanıma izin veren kaynaklardan gelmeli,
- yalnızca yetişkinleri içermeli,
- kimliği belirlenebilir kişileri içermemeli (ünlüler dahil; yüz, isim veya dosya adı üzerinden tanınabilir olmamalı),
- yalnızca sizin bilgisayarınızda kalmalı.

Bu verileri ve bunlardan üretilen dosyaları (görsel, etiket listesi, referans çıktısı) hiçbir zaman issue'lara veya pull request'lere eklemeyin.

### Komutlar

Birim testleri (model dosyası gerekmez):

```bash
./gradlew :app:testDebugUnitTest
```

Windows'ta `./gradlew` yerine `gradlew.bat` kullanın.

Modeli derlemeye eklemek (model dosyası sizde varsa):

```powershell
./restore-pinned-models.ps1 -ModelPath <model-dosyasinin-yolu>
```

Betik yalnızca SHA-256 değeri `e202e26cf35d3445d00ac7af74183d1d962121b3a5327fc41afd63605d5c4780` olan dosyayı kabul eder. Model olmadan uygulama derlenir, ancak filtre başlatıldığında model yüklenemez.

ARM64 telefon için APK:

```bash
./gradlew :app:assembleRelease
```

Emülatör (x86_64) için:

```bash
./gradlew :app:assembleDebug -PvisualFilterAbi=x86_64
```

İsteğe bağlı olarak `-PvisualFilterKeyStore=<anahtar-deposu-yolu>` ile imzalama için kendi anahtar deponuzu verebilirsiniz. Kendi anahtarınızla imzaladığınız APK, yayınlanan APK'nin üzerine güncelleme olarak kurulamaz; önce yayınlanan sürümün kaldırılması gerekir.

Lint:

```bash
./gradlew :app:lintRelease
```

## Pull request önerileri

- Değişikliği küçük ve tek bir konuya odaklı tutun.
- Davranış değiştiren kod için birim testi ekleyin veya güncelleyin.
- Doğruluk, hız veya kapsama hakkında bir iddiada bulunuyorsanız ölçüm yöntemini, örneklem büyüklüğünü ve cihazı yazın. Emülatör sonuçlarını gerçek telefon sonucu gibi sunmayın.
- Açıklamaları ve kullanıcıya görünen metinleri Türkçe yazmayı tercih edin.
