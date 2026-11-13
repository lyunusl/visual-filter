package com.augelab.visualfilter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreTest {
    private fun frame(luminance: Int) = IntArray(ContentChangeDetector.SAMPLE_COUNT) { luminance }

    @Test fun firstFrameIsAcceptedAndIdenticalFrameIsSkipped() {
        val detector = ContentChangeDetector()
        assertTrue(detector.update(frame(80), 0L))
        assertFalse(detector.update(frame(80), 1_000_000_000L))
    }

    @Test fun gradualChangesAccumulateAgainstAcceptedBaseline() {
        val detector = ContentChangeDetector(threshold = 10.0 / 255.0)
        assertTrue(detector.update(frame(80), 0L))
        assertFalse(detector.update(frame(84), 400_000_000L))
        assertFalse(detector.update(frame(88), 800_000_000L))
        assertTrue(detector.update(frame(92), 1_200_000_000L))
        assertFalse(detector.update(frame(96), 1_600_000_000L))
    }

    @Test fun cooldownDoesNotAdvanceBaselineOrPostponeNextAllowedChange() {
        val detector = ContentChangeDetector()
        assertTrue(detector.update(frame(0), 0L))
        assertFalse(detector.update(frame(100), 100_000_000L))
        assertFalse(detector.update(frame(100), 349_999_999L))
        assertTrue(detector.update(frame(100), 350_000_000L))
        assertFalse(detector.update(frame(100), 700_000_000L))
    }

    @Test fun acceptedInputIsCopiedBeforeCallerReusesItsBuffer() {
        val detector = ContentChangeDetector(cooldownNs = 0L)
        val reusable = frame(0)
        assertTrue(detector.update(reusable, 0L))
        reusable.fill(255)
        assertTrue(detector.update(reusable, 1L))
        reusable.fill(0)
        assertTrue(detector.update(reusable, 2L))
    }

    @Test fun thresholdIsInclusiveAndNormalized() {
        val detector = ContentChangeDetector(threshold = 1.0, cooldownNs = 0L)
        assertTrue(detector.update(frame(0), 0L))
        assertFalse(detector.update(frame(254), 1L))
        assertTrue(detector.update(frame(255), 2L))
    }

    @Test fun thresholdUsesEverySample() {
        val detector = ContentChangeDetector(threshold = 0.5, cooldownNs = 0L)
        assertTrue(detector.update(frame(0), 0L))
        val halfChanged = frame(0)
        halfChanged.fill(255, 0, halfChanged.size / 2)
        assertTrue(detector.update(halfChanged, 1L))
    }

    @Test fun resetAcceptsTheNextFrameRegardlessOfPriorStateOrClock() {
        val detector = ContentChangeDetector()
        assertTrue(detector.update(frame(20), 4_000L))
        detector.reset()
        assertTrue(detector.update(frame(20), 0L))
    }

    @Test fun detectorValidatesConfiguration() {
        for (threshold in listOf(0.0, -0.1, 1.1, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) {
                ContentChangeDetector(threshold = threshold)
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            ContentChangeDetector(cooldownNs = -1L)
        }
    }

    @Test fun invalidSamplesDoNotBecomeABaselineOrAdvanceTheClock() {
        val detector = ContentChangeDetector(cooldownNs = 0L)
        assertThrows(IllegalArgumentException::class.java) { detector.update(IntArray(5), 100L) }
        assertThrows(IllegalArgumentException::class.java) { detector.update(frame(256), 100L) }
        assertThrows(IllegalArgumentException::class.java) { detector.update(frame(-1), 100L) }
        assertTrue(detector.update(frame(0), 0L))
        assertThrows(IllegalArgumentException::class.java) { detector.update(frame(256), 100L) }
        assertTrue(detector.update(frame(255), 1L))
    }

    @Test fun detectorRejectsClockRegressionIncludingAfterSkippedFrames() {
        val detector = ContentChangeDetector()
        assertTrue(detector.update(frame(0), 10L))
        assertFalse(detector.update(frame(0), 20L))
        assertThrows(IllegalArgumentException::class.java) { detector.update(frame(255), 15L) }
        assertTrue(detector.update(frame(255), 350_000_010L))
    }

    @Test fun monotonicClockMayHaveANegativeOrigin() {
        val detector = ContentChangeDetector(cooldownNs = 10L)
        assertTrue(detector.update(frame(0), -20L))
        assertFalse(detector.update(frame(255), -11L))
        assertTrue(detector.update(frame(255), -10L))
    }

    @Test fun elapsedClockSpanDoesNotOverflowIntoPermanentCooldown() {
        val detector = ContentChangeDetector()
        assertTrue(detector.update(frame(0), Long.MIN_VALUE))
        assertTrue(detector.update(frame(255), Long.MAX_VALUE))
    }

    @Test fun emptyStatisticsAreUnavailableRatherThanZeroLatency() {
        val snapshot = PerformanceMonitor().snapshot()
        assertEquals(0, snapshot.sampleCount)
        assertNull(snapshot.inference)
        assertNull(snapshot.decision)
        assertNull(snapshot.overlay)
    }

    @Test fun metricsUseTheSpecifiedTimestampBoundaries() {
        val monitor = PerformanceMonitor()
        monitor.record(10_000_000L, 15_000_000L, 35_000_000L, 42_000_000L)
        val snapshot = monitor.snapshot()
        assertEquals(1, snapshot.sampleCount)
        assertPercentiles(snapshot.inference!!, 20.0, 20.0, 20.0)
        assertPercentiles(snapshot.decision!!, 32.0, 32.0, 32.0)
        assertPercentiles(snapshot.overlay!!, 7.0, 7.0, 7.0)
    }

    @Test fun percentilesInterpolateKnownDistributionAndPreserveSubMillisecondPrecision() {
        val monitor = PerformanceMonitor()
        for (duration in listOf(40L, 0L, 20L, 30L, 10L)) {
            monitor.record(0L, 0L, duration * 1_000_000L, duration * 1_000_000L)
        }
        assertPercentiles(monitor.snapshot().inference!!, 20.0, 36.0, 38.0)
        monitor.reset()
        monitor.record(0L, 0L, 500_000L, 750_000L)
        assertEquals(0.5, monitor.snapshot().inference!!.p50Ms, 0.000001)
        assertEquals(0.75, monitor.snapshot().decision!!.p50Ms, 0.000001)
        assertEquals(0.25, monitor.snapshot().overlay!!.p50Ms, 0.000001)
    }

    @Test fun storageEvictsOldestSamplesAfterRepeatedWraparound() {
        val monitor = PerformanceMonitor(capacity = 3)
        for (duration in 1L..10L) {
            monitor.record(0L, 0L, duration * 1_000_000L, duration * 1_000_000L)
        }
        val snapshot = monitor.snapshot()
        assertEquals(3, snapshot.sampleCount)
        assertPercentiles(snapshot.inference!!, 9.0, 9.8, 9.9)
    }

    @Test fun capacityOneKeepsOnlyTheLatestSample() {
        val monitor = PerformanceMonitor(capacity = 1)
        monitor.record(0L, 0L, 10_000_000L, 10_000_000L)
        monitor.record(0L, 0L, 30_000_000L, 30_000_000L)
        assertEquals(1, monitor.snapshot().sampleCount)
        assertPercentiles(monitor.snapshot().inference!!, 30.0, 30.0, 30.0)
    }

    @Test fun monitorResetDiscardsAllEarlierStatistics() {
        val monitor = PerformanceMonitor(capacity = 2)
        monitor.record(0L, 0L, 100_000_000L, 100_000_000L)
        monitor.reset()
        assertEquals(0, monitor.snapshot().sampleCount)
        assertNull(monitor.snapshot().decision)
        monitor.record(0L, 0L, 2_000_000L, 2_000_000L)
        assertPercentiles(monitor.snapshot().inference!!, 2.0, 2.0, 2.0)
    }

    @Test fun invalidTimestampOrderAndOverflowDoNotCorruptExistingStatistics() {
        val monitor = PerformanceMonitor()
        monitor.record(0L, 1L, 2L, 3L)
        val before = monitor.snapshot()
        for (timestamps in listOf(
            longArrayOf(2L, 1L, 3L, 4L),
            longArrayOf(0L, 3L, 2L, 4L),
            longArrayOf(0L, 1L, 4L, 3L),
            longArrayOf(Long.MIN_VALUE, 0L, 1L, Long.MAX_VALUE),
        )) {
            assertThrows(IllegalArgumentException::class.java) {
                monitor.record(timestamps[0], timestamps[1], timestamps[2], timestamps[3])
            }
        }
        assertEquals(before, monitor.snapshot())
    }

    @Test fun zeroLengthAndNegativeOriginTimingSamplesAreValid() {
        val monitor = PerformanceMonitor()
        monitor.record(-4L, -3L, -2L, -1L)
        monitor.record(0L, 0L, 0L, 0L)
        assertEquals(2, monitor.snapshot().sampleCount)
        assertEquals(0.0000015, monitor.snapshot().decision!!.p50Ms, 0.0000000001)
    }

    @Test fun monitorAllowsOverlappingPipelinesAndImmutableSnapshots() {
        val monitor = PerformanceMonitor()
        monitor.record(10L, 20L, 30L, 40L)
        val before = monitor.snapshot()
        monitor.record(15L, 25L, 35L, 45L)
        assertEquals(1, before.sampleCount)
        assertEquals(2, monitor.snapshot().sampleCount)
    }

    @Test fun monitorValidatesBoundedCapacity() {
        for (capacity in listOf(-1, 0, PerformanceMonitor.MAX_CAPACITY + 1)) {
            assertThrows(IllegalArgumentException::class.java) { PerformanceMonitor(capacity) }
        }
    }

    private fun assertPercentiles(
        actual: PerformanceMonitor.Percentiles,
        p50: Double,
        p90: Double,
        p95: Double,
    ) {
        assertEquals(p50, actual.p50Ms, 0.000001)
        assertEquals(p90, actual.p90Ms, 0.000001)
        assertEquals(p95, actual.p95Ms, 0.000001)
    }
}
