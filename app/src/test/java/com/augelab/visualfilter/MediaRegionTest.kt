package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class MediaRegionTest{
 @Test fun maskAndCaptureUseSamePixelBounds(){assertArrayEquals(intArrayOf(0,102,648,998),MediaRegion().pixels(720,1280))}
 @Test fun feedRegionExcludesSurroundingUi(){assertArrayEquals(intArrayOf(36,320,684,768),MediaRegion(.05f,.25f,.95f,.6f).pixels(720,1280))}
 @Test(expected=IllegalArgumentException::class) fun emptyRegionRejected(){MediaRegion(0f,.7f,1f,.6f)}
 @Test fun feedbackOnlyRecognizesAlmostBlack(){assertTrue(looksLikeOwnMask(IntArray(100){0xff000000.toInt()}));assertFalse(looksLikeOwnMask(IntArray(100){0xffabcdef.toInt()}));assertFalse(looksLikeOwnMask(intArrayOf()))}
 @Test fun sliderBoundaryDoesNotFailOnFloatRounding(){MediaRegion(top=.78f-.1f)}
}
