package com.augelab.visualfilter

/** Permission and live binding are different states; never changes Android settings. */
fun accessibilityConnectionMessage(permissionEnabled:Boolean):String =
 if(permissionEnabled)
  "Erişilebilirlik izni açık, fakat hizmet bağlı değil. Visual Filter Scroll Research hizmetini erişilebilirlik ayarlarından kapatıp yeniden açın, ardından filtreyi başlatın."
 else "Önce Visual Filter erişilebilirlik hizmetini kendiniz açın."
