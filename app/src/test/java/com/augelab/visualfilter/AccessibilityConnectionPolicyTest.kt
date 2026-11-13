package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class AccessibilityConnectionPolicyTest {
 @Test fun enabledButDisconnectedDoesNotAskForNewPermission(){
  val message=accessibilityConnectionMessage(true)
  assertTrue(message.contains("izni açık"))
  assertTrue(message.contains("hizmet bağlı değil"))
  assertFalse(message.contains("Önce Visual Filter"))
 }
 @Test fun disabledServiceRequiresOwnerAction(){
  assertEquals("Önce Visual Filter erişilebilirlik hizmetini kendiniz açın.",accessibilityConnectionMessage(false))
 }
}
