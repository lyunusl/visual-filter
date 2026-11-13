package com.augelab.visualfilter

enum class JourneyTheme(val title:String){DAWN("Yeni bir gün"),WAVES("Sakin dalgalar"),GROVE("Büyüyen filizler"),STARS("Gece yolculuğu"),BREATH("Nefes arası"),PATH("Adım adım")}
data class JourneyMessage(val title:String,val body:String)
data class JourneyMoment(val total:Long,val startedAtMs:Long,val themeOverride:JourneyTheme?=null){
 val theme:JourneyTheme get()=themeOverride?:JourneyTheme.entries[((total.coerceAtLeast(1)-1)%JourneyTheme.entries.size).toInt()]
 val level:Long get()=if(total<=0)1 else (total-1)/10+1
 val released:Int get()=if(total<=0)0 else ((total-1)%10+1).toInt()
 val remaining:Int get()=10-released
 val message:JourneyMessage get()=JourneyMessages.all[((total.coerceAtLeast(1)-1)%JourneyMessages.all.size).toInt()]
 val levelName:String get()=when(level){1L->"İlk adımlar";2L->"Ritim";3L->"Kararlılık";4L->"Denge";else->"İstikrar"}
}
fun nextBlockTotal(value:Long)=if(value<0)1L else if(value==Long.MAX_VALUE)value else value+1

/** Original supportive copy; no quotations, diagnosis, moral score or punishment streak. */
object JourneyMessages {
 val all=listOf(
  JourneyMessage("Bir anlık sabır","Bu an geçecek. Dikkatini vermek istediğin şeye dönebilirsin."),
  JourneyMessage("Küçük adım, gerçek emek","Her şeyi bugün değiştirmek zorunda değilsin. Bu adım yeter."),
  JourneyMessage("Dürtü bir emir değildir","Bir isteği fark edebilir ve peşinden gitmemeyi seçebilirsin."),
  JourneyMessage("Yoluna devam et","Dünkü zorluk bugünkü adımının değerini azaltmaz."),
  JourneyMessage("Kendine alan aç","Yavaş bir nefes al. Bir sonraki seçimin için zamanın var."),
  JourneyMessage("İstikrar sessiz büyür","Küçük ve düzenli seçimler, zamanla yeni bir yol oluşturur."),
  JourneyMessage("Sabır da çalışılır","Beklediğin bu kısa an, sabrına ayırdığın bir alandır."),
  JourneyMessage("Dalgayı izle","Dürtüler dalgalar gibi değişir. Her dalgayı takip etmen gerekmez."),
  JourneyMessage("Bir filiz gibi","Gelişim her gün görünmeyebilir. Emeğin yine de değerlidir."),
  JourneyMessage("Yeniden başlayabilirsin","Zorlandığın bir gün, devam edemeyeceğin anlamına gelmez."),
  JourneyMessage("Dikkatin kıymetli","Onu bugün sana iyi gelen bir işe ayırabilirsin."),
  JourneyMessage("Sıradaki küçük adım","Biraz su, kısa bir yürüyüş ya da ertelediğin küçük bir iş."),
  JourneyMessage("Acele etmek gerekmiyor","Kendine değişmek için zaman ve tekrar etme fırsatı ver."),
  JourneyMessage("Bir alışkanlığa ara ver","Otomatik yaptığın şeye kısa bir durak eklemek de bir seçimdir."),
  JourneyMessage("Kendine nazik davran","Sert sözler yerine sakin bir hatırlatma: Devam edebilirsin."),
  JourneyMessage("Bugün buradasın","Geçmişi çözmek zorunda değilsin. Şu andaki seçimine odaklan."),
  JourneyMessage("Nefes kadar bir ara","Omuzlarını gevşet. Dikkatini yavaşça yeniden topla."),
  JourneyMessage("Yol adımlarla açılır","Kusursuz bir başlangıç beklemeden küçük bir adım atabilirsin."),
  JourneyMessage("Sabır, yönünü korumaktır","Zorlansan da değer verdiğin yöne tekrar dönebilirsin."),
  JourneyMessage("Bir şeyleri eksilt","Sana iyi gelmeyene daha az, önem verdiğine daha çok alan."),
  JourneyMessage("Kökler görünmeden büyür","Her çabanın sonucu hemen görünmeyebilir. Acele etme."),
  JourneyMessage("Bir günle sınırlı değil","Değişim tek bir karar değil; yeniden seçebilme sürecidir."),
  JourneyMessage("Kısa bir mola mümkün","Ekranı bırakıp çevrene bakabilirsin. Devam etmek için acelen yok."),
  JourneyMessage("Kendi hızında ilerle","Başkasının yoluyla kıyaslamadan, kendi adımını fark et."),
  JourneyMessage("Bu anı aşabilirsin","Bütün günü düşünmeden, yalnızca bu ana biraz sabır ayır."),
  JourneyMessage("İsteği fark et, bekle","Bir dürtünün varlığı, onu uygulamak zorunda olduğun anlamına gelmez."),
  JourneyMessage("Yeniye yer aç","Eski bir alışkanlığın yerine küçük, yararlı bir uğraş koyabilirsin."),
  JourneyMessage("Emek birikmeye devam eder","Bir aksama, daha önce gösterdiğin çabayı silmez."),
  JourneyMessage("Şimdi burada ol","Nefesini, oturduğun yeri ve çevrendeki sesleri fark et."),
  JourneyMessage("Yönün, hızından önemli","Hızlı ilerlemek yerine sürdürebileceğin bir adımı seç."),
  JourneyMessage("Sabır kendine de gerekir","Öğrenirken zorlanmak mümkündür. Kendine süre tanı."),
  JourneyMessage("Alışkanlığı sorgula","Buna gerçekten zaman ayırmak istiyor musun? Bir an durup seç."),
  JourneyMessage("İyi olanı besle","Dikkatini bir kitaba, bir işe ya da sevdiğin birine çevirebilirsin."),
  JourneyMessage("Devam etmek de başarıdır","Bugün küçük görünen çaban, sürecinin bir parçasıdır."),
  JourneyMessage("Bir nefes, yeni bir seçim","Dikkatin dağıldığında nazikçe geri getirebilirsin."),
  JourneyMessage("Kendi yoluna dön","Şimdi, senin için anlamlı olan bir sonraki adımı seç.")
 )
}
