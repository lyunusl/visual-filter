# Visual Filter

Android ekranındaki medyayı cihazda analiz edip NSFW olarak sınıflandırdığı alanı maskeleyen bir **prototip**.

> Bu bir deneme sürümüdür. İçeriklerin önemli bir kısmını kaçırır, bazen zararsız görselleri de kapatır. Ayrıntılar için [Durum ve sınırlar](#durum-ve-sınırlar) bölümünü okuyun.

## APK'yi indir

[![APK indir: Android ARM64, sürüm 0.9.2](https://img.shields.io/badge/APK%20indir-Android%20ARM64%20%C2%B7%20s%C3%BCr%C3%BCm%200.9.2-2ea44f?style=for-the-badge&logo=android&logoColor=white)](https://github.com/lyunusl/visual-filter/releases/latest/download/VisualFilter-0.9.2-mobilenetv3small-ft-arm64.apk)

**[VisualFilter-0.9.2-mobilenetv3small-ft-arm64.apk dosyasını indir](https://github.com/lyunusl/visual-filter/releases/latest/download/VisualFilter-0.9.2-mobilenetv3small-ft-arm64.apk)**

Şimdilik yalnızca Android 14 ve üstünde, Instagram için çalışır.

<!-- GUNCELLENECEK: APK yeniden derlenince "Dosya boyutu" ve "SHA-256" satirlarini yeni degerlerle guncelleyin. -->
| Bilgi | Değer |
|---|---|
| Platform | Android, yalnızca ARM64 (arm64-v8a) işlemcili telefonlar |
| Sürüm | 0.9.2 (`0.9.2-mobilenetv3small-ft`, versionCode 25) |
| Dosya boyutu | yaklaşık 39 MB (40.773.457 bayt) <!-- GUNCELLENECEK: yeni APK boyutu --> |
| SHA-256 | `7d675440e35cc8b5ca3fe0ce2708d400790da61ee229dc2c0b283bf41c5265c9` <!-- GUNCELLENECEK: yeni APK SHA-256 --> |
| Uygulama adı (telefonda) | Visual Filter Scroll Research |
| Çalışması için | Android 14 veya üzeri ve telefonda yüklü Instagram uygulaması |

Uygulama Android 10 ve üzerine kurulabilir, ancak filtre yalnızca **Android 14 ve üzeri** sürümlerde başlatılabilir. Bu sürüm yalnızca **Instagram** uygulamasının penceresini inceler; başka uygulamalarda çalışmaz.

## Android'e kurulum

Bu uygulama Google Play Store'da değildir. Bu yüzden Android, kurulum sırasında birkaç uyarı gösterir. Aşağıdaki adımlar teknik bilgi gerektirmez. Menü adları telefon markasına ve Android sürümüne göre biraz farklı olabilir.

### 1. APK dosyasını indirin

1. Telefonunuzda bu sayfayı tarayıcıda (örneğin Chrome veya Samsung Internet) açın.
2. Yukarıdaki yeşil **APK indir** düğmesine dokunun.
3. Tarayıcı "Bu dosya türü cihazınıza zarar verebilir" gibi bir uyarı gösterirse **Yine de indir** seçeneğine dokunun.
4. İndirme bitince bildirimdeki **Aç** seçeneğine dokunun. Bildirimi kaçırdıysanız **Dosyalarım** veya **Dosyalar** uygulamasında **İndirilenler** klasörünü açıp `VisualFilter-0.9.2-mobilenetv3small-ft-arm64.apk` dosyasına dokunun.

### 2. "Bilinmeyen uygulamaları yükle" iznini verin

Android, Play Store dışından gelen dosyaları varsayılan olarak kurmaz. Dosyayı açtığınızda "Güvenliğiniz için telefonunuzun bu kaynaktan bilinmeyen uygulamaları yüklemesine izin verilmiyor" gibi bir mesaj görebilirsiniz.

1. Mesajdaki **Ayarlar** düğmesine dokunun.
2. Açılan ekranda **Bu kaynaktan izin ver** (veya **Bilinmeyen uygulamaları yükle**) anahtarını açın.
3. Geri tuşuyla kurulum ekranına dönün.

Mesaj çıkmazsa bu izni elle açabilirsiniz: **Ayarlar → Uygulamalar → (dosyayı açtığınız uygulama, örneğin Chrome veya Dosyalarım) → Bilinmeyen uygulamaları yükle → İzin ver**.

Samsung (One UI) telefonlarda bu ayar bazı sürümlerde **Ayarlar → Güvenlik ve gizlilik** altında da bulunur ve adı biraz farklı olabilir. Bazı Samsung modellerinde **Otomatik Engelleyici** açıksa Play Store dışından kurulum tamamen engellenir; bu durumda **Ayarlar → Güvenlik ve gizlilik → Otomatik Engelleyici** ayarını kurulum süresince kapatmanız gerekir. Kurulumdan sonra yeniden açmanızı öneririz.

### 3. Play Protect uyarısı

Kurulum sırasında Google Play Protect "Güvenli olmayan uygulama engellendi" veya "Bu uygulama tanınmıyor" gibi bir uyarı gösterebilir.

- Bu uyarı, uygulama Play Store'dan gelmediği ve Google tarafından incelenmediği için çıkar. Uygulama ayrıca erişilebilirlik hizmeti kullandığı için Play Protect daha dikkatli davranır.
- Devam etmek istiyorsanız **Ayrıntılar** (veya **Daha fazla ayrıntı**) seçeneğine, ardından **Yine de yükle** seçeneğine dokunun.
- Uygulamayı taramaya göndermek isteyip istemediğiniz sorulursa kendi tercihinize göre seçin; kurulumu etkilemez.

Bu uyarıyı ciddiye alın: APK'yi yalnızca bu sayfadaki bağlantıdan indirin. İsterseniz indirdiğiniz dosyanın SHA-256 değerini yukarıdaki tabloyla karşılaştırabilirsiniz.

### 4. Kurun ve açın

1. **Yükle** düğmesine dokunun.
2. Kurulum bitince **Aç** düğmesine dokunun. Uygulama, ana ekranda **Visual Filter Scroll Research** adıyla görünür.

### 5. Uygulamanın istediği izin: Erişilebilirlik hizmeti

Bu sürüm yalnızca **bir** izin ister: **Erişilebilirlik hizmeti**. Bu izni yalnızca siz, telefonun ayarlarından açabilirsiniz; uygulama bunu kendi kendine açamaz.

1. Uygulamada **Erişilebilirlik ayarlarını aç** düğmesine dokunun.
2. Açılan listede **Yüklü uygulamalar** veya **İndirilen uygulamalar** bölümüne girin (Samsung'da: **Erişilebilirlik → Yüklü uygulamalar**).
3. **Visual Filter Scroll Research** satırına dokunun ve anahtarı açın.
4. Android, uygulamanın "ekranı görüntüleyip kontrol edebileceğini" söyleyen bir uyarı gösterir. Okuyun ve kabul ediyorsanız **İzin ver** seçeneğine dokunun.

**"Kısıtlanmış ayar" uyarısı çıkarsa:** Android 13 ve üzerinde, tarayıcıdan indirilen uygulamaların erişilebilirlik iznini açmak ilk seferde engellenebilir ("Güvenliğiniz için bu ayar şu anda kullanılamıyor" gibi). Bu durumda:

1. **Ayarlar → Uygulamalar → Visual Filter Scroll Research** ekranını açın.
2. Sağ üstteki **⋮** (üç nokta) menüsüne dokunun ve **Kısıtlanmış ayarlara izin ver** seçeneğini seçin. Telefon kilit kodunuzu veya parmak izinizi isteyebilir.
3. Erişilebilirlik ayarlarına dönüp anahtarı yeniden açın.

**Bu izin neden gerekiyor?** Kaynak koddaki erişilebilirlik hizmeti tanımına (`app/src/main/res/xml/accessibility_filter.xml`) göre uygulama bu izinle şunları yapar:

- **Pencere içeriğini okuma:** Instagram ekranındaki görsel ve video alanlarının yerini ve boyutunu bulmak için. Maske yalnızca bu alanların üstüne konur.
- **Kaydırma ve pencere değişikliği olaylarını izleme:** Akış kaydırıldığında yeni görselin incelenmesi ve maskenin görselle birlikte taşınması için.
- **Pencerenin ekran görüntüsünü alma (Android 14+):** Instagram penceresinin anlık görüntüsü yalnızca bellekte alınır, ilgili görsel alanı kırpılıp telefondaki modele verilir. Görüntü kaydedilmez ve gönderilmez.
- **Maske çizme:** Engellenen alanın üstüne bir erişilebilirlik katmanı çizilir. Bu katman dokunmaları Instagram'a geçirir; maskenin üstünden kaydırabilirsiniz.

**Bu sürümün kullanmadığı izinler:** Kaynak kodda ve 0.9.2 APK'sinin manifestinde aşağıdakiler **yoktur**, dolayısıyla bunlar için izin istenmez:

- İnternet erişimi ve ağ durumu izni (manifestte açıkça kaldırılmıştır)
- Ekran kaydı / ekran yansıtma (MediaProjection)
- "Diğer uygulamaların üzerinde göster" izni (maske, erişilebilirlik hizmetinin kendi katmanıyla çizilir)
- Bildirim izni ve ön plan hizmeti
- Kamera, mikrofon, konum, rehber, depolama/dosya erişimi

### 6. Filtreyi başlatın

1. Uygulamaya geri dönün ve **Filtreyi başlat** düğmesine dokunun.
2. İlk seferde sizden **6 haneli bir durdurma PIN'i** belirlemeniz istenir. PIN'i iki kez girin ve **Kaydet ve başlat** seçeneğine dokunun. PIN'in kendisi değil, yalnızca geri çevrilemeyen bir özeti (hash) telefonunuzda saklanır.
3. Modelin hazırlanmasını birkaç saniye bekleyin, ardından **Instagram'ı aç** düğmesine dokunun.

Uygulamadan çıkmak filtreyi durdurmaz. Android hizmeti yeniden bağlarsa filtre kaldığı yerden devam eder.

### Filtreyi kapatma

- **Uygulama içinden:** **Filtreyi durdur** düğmesine dokunun ve PIN'inizi girin. Art arda 5 yanlış denemeden sonra 30 saniye beklemeniz gerekir.
- **PIN'i unuttuysanız veya hemen kapatmak istiyorsanız:** **Ayarlar → Erişilebilirlik → Yüklü uygulamalar → Visual Filter Scroll Research** ekranında anahtarı kapatın. PIN, Android'in kendi ayarlarını engellemez; bu yöntem her zaman çalışır.

### Uygulamayı kaldırma

1. Önce yukarıdaki gibi erişilebilirlik hizmetini kapatın (zorunlu değil ama önerilir).
2. **Ayarlar → Uygulamalar → Visual Filter Scroll Research → Kaldır** yolunu izleyin. Ya da ana ekrandaki simgeye basılı tutup **Kaldır** seçeneğine dokunun.

Kaldırınca PIN, eşik ayarı ve sayaç gibi tüm uygulama verileri silinir. Uygulama yedeklemeye kapalıdır; bu veriler buluta veya başka cihaza aktarılmaz.

### Güncelleme

1. Yeni sürüm yayınlandığında, aynı şekilde yeni APK'yi bu sayfadan indirin ve açın.
2. Android **Güncelle** seçeneğini gösterir; dokunun. Ayarlarınız (PIN, eşik, sayaç) korunur.
3. Güncellemeden sonra erişilebilirlik hizmeti kapalı görünürse 5. adımdaki gibi yeniden açın.

"Uygulama yüklenmedi" veya "paket mevcut paketle çakışıyor" hatası alırsanız yeni APK farklı bir anahtarla imzalanmıştır. Bu durumda eski sürümü kaldırıp yenisini kurmanız gerekir; eski ayarlar silinir.

### Kurulumdan sonra "bilinmeyen kaynak" iznini geri alma

Kurulum bittikten sonra bu izni kapatmanız önerilir:

- **Ayarlar → Uygulamalar → (Chrome, Samsung Internet veya Dosyalarım) → Bilinmeyen uygulamaları yükle →** anahtarı kapatın.
- Samsung'da Otomatik Engelleyici'yi kapattıysanız **Ayarlar → Güvenlik ve gizlilik → Otomatik Engelleyici** ayarını yeniden açın.

Bu izni kapatmak kurulu Visual Filter uygulamasını etkilemez. Bir sonraki güncellemede izni yeniden geçici olarak açmanız gerekir.

## Nasıl çalışır

1. Erişilebilirlik hizmeti Instagram ekranındaki görsel ve video alanlarının sınırlarını bulur.
2. Instagram penceresinin anlık görüntüsü bellekte alınır; her görsel alanı kırpılıp 224×224 boyuta getirilir.
3. Telefonda çalışan küçük bir model (MobileNetV3 Small, ONNX Runtime ile) iki sınıf için olasılık üretir: `nsfw_suggestive` (NSFW/kışkırtıcı) ve `normal`.
4. NSFW olasılığı varsayılan eşik olan **0,44775** veya üzerindeyse o görsel alanı opak bir maskeyle kapatılır. Maske, içerik ekrandan çıkana kadar kalır ve görsel kaydırılırken onunla birlikte hareket eder.
5. Eşik, filtre durdurulmuşken uygulama içinden 0,05 ile 0,95 arasında değiştirilebilir. Eşiği düşürmek daha çok görseli kapatır, yanlış engellemeleri de artırır.

Model kararı beklenirken görsel **açık kalır**. Yani uygunsuz bir içerik kısa bir süre görünebilir. Uygulama "kullanıcı görmeden gizleme" garantisi vermez.

### Cihaz içi çalışma: kaynak kodda ne bulduk

Aşağıdakiler bu depodaki kaynak koddan ve yayınlanacak 0.9.2 APK'sinin manifestinden kontrol edildi:

- **İnternet izni yok.** `app/src/main/AndroidManifest.xml`, `INTERNET` ve `ACCESS_NETWORK_STATE` izinlerini `tools:node="remove"` ile açıkça kaldırır. 0.9.2 APK'sinin derlenmiş manifestinde de bu izinler bulunmuyor. İnternet izni olmayan bir Android uygulaması ağ bağlantısı açamaz.
- **Uygulama kodunda ağ çağrısı yok.** `app` modülünde HTTP, soket veya yükleme kodu bulunmuyor.
- **ONNX Runtime telemetrisi kapalı.** Telemetri kodda kapatılmış (`setTelemetry(false)`) ve telemetri başlatıcısı manifestten kaldırılmıştır.
- **Görüntüler kaydedilmez.** Ekran görüntüsü yalnızca bellekte işlenir ve işlem bitince bırakılır. Uygulamanın diske yazdığı tek dosya, APK içindeki modelin doğrulama için uygulamanın özel klasörüne kopyalanmasıdır.
- **Saklanan veriler:** Yalnızca telefonda, uygulamanın özel ayar dosyasında eşik, PIN özeti, sayaç ve debug tercihi tutulur. Yedekleme ve cihazlar arası aktarım kapalıdır.
- **Sistem günlüğü:** Uygulama içindeki **Debug** seçeneği açıkken olasılık, süre ve konum gibi sayısal ölçümler Android'in sistem günlüğüne (logcat) yazılır. Görüntü yazılmaz.
- **Kapsam:** Yayın (ARM64) sürümü yalnızca Instagram'ın (`com.instagram.android`) penceresini işler.

Not: `tools/` klasöründeki bilgisayar betikleri uygulamanın parçası değildir ve APK'ye girmez.

## Durum ve sınırlar

Bu proje bir **prototiptir**. Aşağıdaki sonuçlar ürün doğruluğu, gerçek cihaz performansı veya kapsama garantisi **değildir**; erken tanı (diagnostic) sonuçlarıdır.

- **Emülatörde maskeleme kontrolü:** Maskeleme, Android API 34 x86_64 emülatöründe kendi lisanslı test ekranımızla kontrol edildi (6 güvenli ve 5 engellenen sonuç; engellenen alanların maskeyle kapandığı görüldü).
- **PC ile karar eşleşmesi:** Aynı emülatörde 21 örnekte (10 lisanslı görsel ve 11 yapay desen) Android'deki model çıktısı ile bilgisayardaki referans çıktı karşılaştırıldı; 21 örneğin hepsinde karar eşleşti. Bu yalnızca sayısal tutarlılık kontrolüdür, doğruluk ölçümü değildir.
- **Fiziksel cihazda test edilmedi:** Bu sürüm gerçek bir telefonda (örneğin Galaxy A55) test edilmedi. Gerçek telefondaki hız, pil kullanımı ve maske zamanlaması bilinmiyor.
- **Ayrı bir tanı örnekleminde:** Kullanıcı tarafından etiketlenmiş 189 görsellik ayrı bir tanı örnekleminde, engellenmesi gereken **37 hedefin yalnızca 6'sı** yakalandı ve **152 normal örneğin 24'ü** yanlışlıkla engellendi. Bu örneklem daha önce incelenmiş bir örneklemdir; bağımsız, kör bir test kümesi değildir.

Bu sonuçların pratik anlamı:

- Uygulama **içeriklerin çoğunu kaçırır**. Filtre açıkken de uygunsuz içerik görebilirsiniz.
- Uygulama **bazen zararsız görselleri de maskeler**.
- Model yalnızca iki sınıf ayırt eder. Saç, kol, bacak veya kıyafet türleri için ayrı kurallar yoktur.
- Görsel sınırları bulunamazsa maske uygulanmaz. Görsel, model kararı verilene kadar görünür kalır.
- Sayaç, yanlış engellemeleri ve aynı içeriğe yeniden gelmeyi de sayabilir.

Bu uygulamaya bir çocuğu veya başka birini korumak için güvenmeyin. Ebeveyn denetimi veya güvenlik ürünü olarak tasarlanmamıştır.

## Model dosyası hakkında

- Bu depo **model dosyasını içermez** ("model hariç" kaynak). Model ağırlıkları (`.onnx` vb.) `.gitignore` ile depodan dışlanmıştır.
- Yayınlanan 0.9.2 APK'si modeli içinde taşır: `review_mobilenetv3small224_224_rgb_fp32.onnx` (6.094.846 bayt, SHA-256 `e202e26cf35d3445d00ac7af74183d1d962121b3a5327fc41afd63605d5c4780`).
- Model, torchvision'ın ImageNet üzerinde önceden eğitilmiş MobileNetV3 Small ağırlıklarından yerelde ince ayar (fine-tune) yapılarak üretilmiştir. Kaynak ve lisans kayıtları `app/src/main/assets/` altındaki `MODEL-SOURCES.json`, `NOTICE.txt`, `license_provenance.json` ve `UPSTREAM_BACKBONE_PROVENANCE.json` dosyalarındadır. Bu kayıtlara göre torchvision kodu BSD-3-Clause lisanslıdır; ImageNet görsellerinin ve ince ayar verisinin hakları bağımsız olarak doğrulanmamıştır.
- Modeli derlemeye eklemek için `restore-pinned-models.ps1 -ModelPath <model dosyası>` kullanılır. Betik dosyanın SHA-256 değerini kontrol eder ve yalnızca doğru modeli `app/src/main/assets/` altına kopyalar. Model olmadan uygulama derlenebilir ama filtre çalışmaz.
- Kaynak notlarına göre (`docs/RELEASE-0.9.2.md`) 0.9.2 APK'si ARM64 için imzalı release yapılandırmasıyla derlendi; birim testleri ve lint bu derlemeyle birlikte çalıştırıldı (127 debug ve 127 release birim testi geçti, lint: 0 hata, 20 uyarı). Bu derlemede kullanılan yardımcı derleme betiği ve imzalama anahtarı bu depoda yer almaz.

Daha eski sürümlere ait İngilizce araştırma notları `docs/` klasöründedir. Bu notlardaki model ve sayı bilgileri 0.9.0 sürümüne aittir; 0.9.2 için geçerli bilgiler bu README ve `docs/RELEASE-0.9.2.md` dosyasındadır.

## Katkı

Katkılarınızı memnuniyetle karşılıyoruz: kod, değerlendirme araçları, etiketleme yönergeleri ve performans iyileştirmeleri.

**Lütfen issue veya pull request'lere görsel, video ya da kişisel veri eklemeyin.** Yasa dışı bir içerikle karşılaşırsanız bunu bu depoya değil, yetkili makamlara bildirin.

Ayrıntılar, kurallar ve derleme talimatları için [CONTRIBUTING.md](CONTRIBUTING.md) dosyasını okuyun.

## Lisans

- **Kod:** Bu depodaki projenin kendi kodu [MIT lisansı](LICENSE) ile paylaşılır.
- **Model ağırlıkları:** Model bu depoda yoktur; APK'nin içinde, ayrı bir yayın (release) dosyası olarak dağıtılır. MIT lisansı model ağırlıklarını kapsamaz.
- **Eğitim verisi hakları doğrulanmadı:** Modelin temel aldığı ön eğitimin (ImageNet) ve yerel ince ayarda kullanılan eğitim verisinin hakları **tam olarak doğrulanmamıştır**. Modeli veya APK'yi yeniden dağıtmadan ya da ticari amaçla kullanmadan önce bunu göz önünde bulundurun.
- **Üçüncü taraf lisansları:** Üçüncü taraf bileşenlerin lisansları geçerliliğini korur. Lisans metinleri `app/src/main/assets/` altındadır: ONNX Runtime (MIT, `ONNXRUNTIME-MIT-LICENSE.txt`) ve torchvision kodu (BSD-3-Clause, `FRAMEWORK_CODE_LICENSE.txt`).
