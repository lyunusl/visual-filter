package com.augelab.visualfilter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);val repo=FilterRepository(this)
  setContent{
   val status by SessionState.state.collectAsState()
   var blockedTotal by remember{mutableLongStateOf(repo.blockCount())}
   var showJourneyPreview by remember{mutableStateOf(false)}
   DisposableEffect(repo){val subscription=repo.observeProgress{blockedTotal=repo.blockCount()};onDispose{subscription.close()}}
   var developer by remember{mutableStateOf(repo.developer())}
   var threshold by remember{mutableFloatStateOf(repo.threshold())}
   val uiScope=rememberCoroutineScope()
   var pinDialog by remember{mutableStateOf(false)}
   var pin by remember{mutableStateOf("")};var confirmPin by remember{mutableStateOf("")}
   var pinBusy by remember{mutableStateOf(false)};var pinError by remember{mutableStateOf("")}
   val protected=status.active || repo.filterRequested()
   val setupPin=!repo.hasStopPin()
   MaterialTheme{Surface(Modifier.fillMaxSize()){
    Column(Modifier.padding(20.dp).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
     Text("Visual Filter",style=MaterialTheme.typography.headlineMedium)
     Text("Görsel alanı filtresi · 0.9.2 · Deneysel",style=MaterialTheme.typography.titleMedium)
     Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
      val journey=JourneyMoment(blockedTotal,0)
      Text("Yolculuğun",style=MaterialTheme.typography.titleMedium)
      Text("$blockedTotal engelleme · Seviye ${journey.level}",style=MaterialTheme.typography.titleLarge)
      Text("${journey.released}/10 halka açıldı · ${journey.levelName}")
      LinearProgressIndicator(progress={journey.released/10f},modifier=Modifier.fillMaxWidth())
      Text("Her yeni engellemede bir halka açılır. 10 halkada bir seviye tamamlanır; günlük seri veya puan kaybı yoktur.",style=MaterialTheme.typography.bodySmall)
      Text("Model engellemelerini sayar. Aynı izlenen içerik tekrar sayılmaz; geri dönülen veya kimliği değişen içerik yeniden sayılabilir. Yanlış engeller de sayıya dahildir.",style=MaterialTheme.typography.bodySmall)
      Button({showJourneyPreview=true}){Text("Animasyonları önizle")}
     }}
     Text("Instagram’da inceleme sırasında görsel açık kalır. Model engel kararı verirse tespit edilen görsel kapanır ve o içerik ekrandan ayrılana kadar kapalı kalır. Aynı görsel kayarken maske onunla taşınır; yeni görsel yeniden incelenir; maskenin üstünden kaydırabilirsiniz.")
     Text("Erişilebilirlik iznini kendiniz açın, sonra buradan filtreyi başlatın. Başlangıçta modellerin hazırlanmasını bekleyin. Android14 veya üzeri gerekir.")
     if(Build.VERSION.SDK_INT<34)Text("Bu cihaz bu yöntemi desteklemiyor.",color=MaterialTheme.colorScheme.error)
     Button({startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))},enabled=!status.active){Text("Erişilebilirlik ayarlarını aç")}
     Button({if(protected || !repo.hasStopPin()){pin="";confirmPin="";pinError="";pinDialog=true}else AccessibilityFilterControl.start(this@MainActivity)},enabled=Build.VERSION.SDK_INT>=34){Text(if(protected)"Filtreyi durdur"else "Filtreyi başlat")}
     Button({packageManager.getLaunchIntentForPackage(InstagramAccessibilityService.INSTAGRAM)?.let{startActivity(it)}},enabled=status.active){Text("Instagram’ı aç")}
     if(isEmulatorDebugBuild(this@MainActivity))Button({startActivity(Intent().setClassName("com.augelab.visualfilter.synthetic.scroll","com.augelab.visualfilter.synthetic.SyntheticActivity"))},enabled=status.active){Text("Kontrollü test ekranını aç")}
     Text(status.message)
     if(status.active)Text(status.metrics,style=MaterialTheme.typography.bodySmall)
     Text("Durdurmak için 6 haneli PIN gerekir. Uygulamadan çıkmak filtreyi durdurmaz. Android hizmeti yeniden bağlarsa filtre devam eder. Cihazın sistem ayarları ve zorla durdurma işlevi bundan bağımsızdır.")
     Text("Model: MobileNetV3 Small224 · yerel fine-tune",style=MaterialTheme.typography.titleMedium)
     if(repo.modelThresholdMigrated())Text("Yeni V3 Small224 modeline geçişte önceki model eşiği aktarılmadı. Bu modelin başlangıç eşiği 0,44775; eski ayarınız saklandı.",style=MaterialTheme.typography.bodySmall)
     Text("Engelleme eşiği: %.5f".format(threshold),style=MaterialTheme.typography.titleMedium)
     Slider(value=threshold,onValueChange={threshold=it},onValueChangeFinished={repo.setThreshold(threshold)},valueRange=.05f.. .95f,enabled=!protected)
     Text("Eşiği düşürmek daha fazla görseli engeller; yanlış engellemeler de artabilir. Ayarlamak için filtreyi durdurun.")
     TextButton({threshold=BinaryModelContract.THRESHOLD;repo.setThreshold(threshold)},enabled=!protected){Text("Modelin varsayılan eşiğine dön (0,44775)")}
     Text("Engellenen videonun sonraki kareleri SAFE olsa da maske kalkmaz. İçerik değişince yeni inceleme başlar. Altı farklı animasyon, açılan zincirler ve sabır/istikrar mesajları gösterilir; üzerinden kaydırabilirsiniz.")
     Text("Yerel model iki sınıf tahmin eder: NSFW/kışkırtıcı ve Normal. Validation’dan seçilmiş deneysel eşik 0,44775. Saç, kol, bacak ve dekolte için ayrı kurallar yoktur. Normal sonucu güvenlik garantisi değildir.")
     Text("Görsel sınırları",style=MaterialTheme.typography.titleMedium)
     Text("Yalnız Instagram’ın bildirdiği görsel/video sınırları kullanılır. Sınır bulunamazsa maske uygulanmaz. Model kararı beklenirken görsel görünür; kısa süreli uygunsuz içerik görülebilir.")
     Text("Hız: otomatik maksimum. Her inceleme bittiğinde sıradaki görüntü işlenir; gerçek hız cihaza ve Android’e bağlıdır.")
     Row{Checkbox(developer,{developer=it;repo.setDeveloper(it)});Text("Debug: tahmin yüzdeleri ve sayısal ölçümler",Modifier.padding(top=12.dp))}
     Text("Debug açıkken maskenin üstünde son kare puanı, ilk engeli başlatan puan ve eşik ayrı görünür. Bu puan doğruluk veya dinî uygunluk yüzdesi değildir.")
     Text("Deneysel yerel fine-tune. Kullanıcı etiketli 189 test görselinde NSFW adaylarının 6/37’si yakalandı; Normal adaylarının 24/152’si yanlış engellendi. Ayrı saç, kol, bacak veya kıyafet sınıfları doğrulanmadı. Genel model etiketli sonuçlar bu insan değerlendirmesinden farklıdır. İnternet izni yok; görüntüler yalnızca bellekte incelenir, kaydedilmez veya gönderilmez.")
     Text("Kaydırma bildirimi görüntü ekrana geldikten sonra ulaşabilir. Açılmış videoya yeni içerik gelebilir. Kullanıcı görmeden gizleme veya sürekli video güvenliği garanti edilmez.")
    }
   }}
   if(showJourneyPreview)JourneyPreview{showJourneyPreview=false}
   if(pinDialog)AlertDialog(
    onDismissRequest={if(!pinBusy){pinDialog=false;pin="";confirmPin=""}},
    properties=DialogProperties(securePolicy=SecureFlagPolicy.SecureOn),
    title={Text(if(setupPin)"Durdurma PIN'i belirleyin"else "Filtreyi durdur")},
    text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
     Text(if(setupPin)"6 haneli PIN'i yalnızca bu cihazda belirleyin ve hatırlayın. Uygulama içinden durdurma bu PIN'i gerektirir."else "Filtre, doğru PIN girilene kadar çalışmaya devam eder.")
     OutlinedTextField(pin,{pin=it.filter{c->c in '0'..'9'}.take(6)},label={Text("PIN")},singleLine=true,visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),enabled=!pinBusy)
     if(setupPin)OutlinedTextField(confirmPin,{confirmPin=it.filter{c->c in '0'..'9'}.take(6)},label={Text("PIN tekrar")},singleLine=true,visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),enabled=!pinBusy)
     if(pinError.isNotEmpty())Text(pinError,color=MaterialTheme.colorScheme.error)
    }},
    confirmButton={TextButton(onClick={
     if(!StopPin.valid(pin) || (setupPin && pin!=confirmPin)){pinError="6 haneli PIN girin; tekrar alanı aynı olmalı."}
     else{pinBusy=true;uiScope.launch{
      try{
       val ok=if(setupPin)withContext(Dispatchers.Default){repo.createStopPin(pin)}else AccessibilityFilterControl.stop(this@MainActivity,pin)
       if(ok){pinDialog=false;pin="";confirmPin="";if(setupPin)AccessibilityFilterControl.start(this@MainActivity)}
       else{pin="";pinError=if(repo.pinWaitSeconds()>0)"Çok fazla deneme. ${repo.pinWaitSeconds()} saniye bekleyin."else "PIN doğrulanamadı veya kaydedilemedi."}
      }finally{pinBusy=false}
     }}
    },enabled=!pinBusy){Text(if(setupPin)"Kaydet ve başlat"else "PIN ile durdur")}},
    dismissButton={TextButton({pinDialog=false;pin="";confirmPin=""},enabled=!pinBusy){Text("Vazgeç")}}
   )
  }
 }
}
