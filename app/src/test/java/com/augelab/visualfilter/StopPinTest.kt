package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class StopPinTest {
 @Test fun onlySixAsciiDigits(){assertTrue(StopPin.valid("123456"));listOf("","12345","1234567","abcdef","１２３４５６").forEach{assertFalse(StopPin.valid(it))}}
 @Test fun correctPinOnly(){val record=StopPin.create("123456");assertTrue(StopPin.verify("123456",record));assertFalse(StopPin.verify("654321",record))}
 @Test fun saltedRecordsDiffer(){assertNotEquals(StopPin.create("123456"),StopPin.create("123456"))}
 @Test fun malformedRecordFailsClosed(){listOf("","v1:AA==:AA==","v2:a:b","v1:!:!").forEach{assertFalse(StopPin.verify("123456",it))}}
 @Test fun fifthFailureWaitsThirtySeconds(){var a=PinAttempts();repeat(4){a=a.failed(1000);assertEquals(0L,a.waitMillis(1000))};a=a.failed(1000);assertEquals(30000L,a.waitMillis(1000));assertEquals(1L,a.waitMillis(30999));assertEquals(0L,a.waitMillis(31000))}
 @Test fun rollbackDoesNotUnlock(){assertEquals(30000L,PinAttempts(0,31000).waitMillis(-100000))}
}
