package com.augelab.visualfilter

data class ThresholdChoice(val value:Float,val initializeCurrentModel:Boolean,val migratedFromLegacy:Boolean)
object ModelThresholdPolicy {
 fun choose(currentModelValue:Float?,legacyModelValue:Float?):ThresholdChoice {
  if(currentModelValue!=null && BinaryModelContract.validThreshold(currentModelValue))return ThresholdChoice(currentModelValue,false,false)
  return ThresholdChoice(BinaryModelContract.THRESHOLD,true,legacyModelValue!=null)
 }
}
