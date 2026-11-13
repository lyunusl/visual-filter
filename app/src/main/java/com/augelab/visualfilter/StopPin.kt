package com.augelab.visualfilter

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Local stop authorization only; never prevents Android's own app/service controls. */
object StopPin {
 private const val ITERATIONS=210_000
 fun valid(pin:String)=pin.length==6 && pin.all{it in '0'..'9'}
 private fun derive(pin:String,salt:ByteArray):ByteArray {
  val spec=PBEKeySpec(pin.toCharArray(),salt,ITERATIONS,256)
  return try{SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded}finally{spec.clearPassword()}
 }
 fun create(pin:String):String {
  require(valid(pin));val salt=ByteArray(16).also{SecureRandom().nextBytes(it)}
  return "v1:"+Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(derive(pin,salt))
 }
 fun verify(pin:String,record:String):Boolean {
  if(!valid(pin))return false
  return runCatching{
   val parts=record.split(':');require(parts.size==3 && parts[0]=="v1")
   val salt=Base64.getDecoder().decode(parts[1]);val expected=Base64.getDecoder().decode(parts[2])
   require(salt.size==16 && expected.size==32)
   MessageDigest.isEqual(expected,derive(pin,salt))
  }.getOrDefault(false)
 }
}

/** Persisted lockout; clock rollback cannot bypass the current wait. */
data class PinAttempts(val failures:Int=0,val blockedUntil:Long=0) {
 fun waitMillis(now:Long)=(blockedUntil-now).coerceIn(0,30_000)
 fun failed(now:Long):PinAttempts=if(failures>=4)PinAttempts(0,now+30_000)else PinAttempts(failures+1)
}
