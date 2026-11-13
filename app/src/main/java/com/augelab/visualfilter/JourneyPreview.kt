package com.augelab.visualfilter

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog

@Composable fun JourneyPreview(onDismiss:()->Unit){
 var example by remember{mutableLongStateOf(1)}
 val moment=remember(example){JourneyMoment(example,SystemClock.uptimeMillis())}
 Dialog(onDismissRequest=onDismiss){
  Surface(shape=MaterialTheme.shapes.large){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   Text("Animasyon önizlemesi",style=MaterialTheme.typography.titleLarge)
   Text("Örnek ilerlemedir; gerçek sayacına eklenmez.",style=MaterialTheme.typography.bodySmall)
   Text(moment.theme.title,style=MaterialTheme.typography.titleMedium)
   AndroidView(factory={JourneyMaskView(it)},update={it.preview(moment)},modifier=Modifier.fillMaxWidth().height(330.dp))
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
    TextButton({example=nextBlockTotal(example)}){Text("Sonraki tema")}
    TextButton({example+=10}){Text("Sonraki seviye")}
   }
   TextButton(onDismiss,modifier=Modifier.fillMaxWidth()){Text("Önizlemeyi kapat")}
  }}
 }
}
