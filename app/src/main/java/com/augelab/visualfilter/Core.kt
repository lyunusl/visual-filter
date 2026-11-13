package com.augelab.visualfilter

import kotlin.math.abs
import kotlin.math.floor

/**
 * Detects visual changes using one bounded, in-memory 32 x 32 luminance baseline.
 *
 * [threshold] is mean absolute luminance difference, normalized to 0..1. The
 * comparison baseline changes only when [update] returns true. Consequently,
 * gradual changes accumulate and frames suppressed by the cooldown are not lost.
 * Timestamps must come from one monotonic nanosecond clock, such as nanoTime().
 * The first valid frame after construction or [reset] is always accepted.
 */
class ContentChangeDetector(
    val threshold: Double = 0.06,
    val cooldownNs: Long = DEFAULT_COOLDOWN_NS,
) {
    init {
        require(threshold.isFinite() && threshold > 0.0 && threshold <= 1.0) {
            "threshold must be finite and in (0, 1]"
        }
        require(cooldownNs >= 0L) { "cooldownNs must be non-negative" }
    }

    private var baseline: IntArray? = null
    private var lastAcceptedNs: Long = 0L
    private var lastObservedNs: Long? = null

    /**
     * Returns true when inference should run. Input is copied only on acceptance;
     * callers may reuse or mutate their buffer after this method returns.
     */
    @Synchronized
    fun update(samples: IntArray, nowNs: Long): Boolean {
        require(samples.size == SAMPLE_COUNT) { "Expected a 32 x 32 luminance sample" }
        require(samples.all { it in 0..255 }) { "Luminance values must be in 0..255" }
        lastObservedNs?.let { previous ->
            require(nowNs >= previous) { "Timestamps must be monotonic" }
        }
        lastObservedNs = nowNs

        val reference = baseline
        if (reference == null) {
            accept(samples, nowNs)
            return true
        }

        // An overflowing positive span exceeds every valid configured cooldown.
        val elapsed = nowNs - lastAcceptedNs
        if (elapsed >= 0L && elapsed < cooldownNs) return false

        var difference = 0L
        for (index in samples.indices) {
            difference += abs(samples[index] - reference[index]).toLong()
        }
        val meanDifference = difference.toDouble() / (SAMPLE_COUNT * 255.0)
        if (meanDifference < threshold) return false

        accept(samples, nowNs)
        return true
    }

    @Synchronized
    fun reset() {
        // Discard the internal thumbnail immediately when a capture session ends.
        baseline?.fill(0)
        baseline = null
        lastAcceptedNs = 0L
        lastObservedNs = null
    }

    private fun accept(samples: IntArray, nowNs: Long) {
        val reference = baseline
        if (reference == null) baseline = samples.copyOf() else samples.copyInto(reference)
        lastAcceptedNs = nowNs
    }

    companion object {
        const val SAMPLE_WIDTH = 32
        const val SAMPLE_HEIGHT = 32
        const val SAMPLE_COUNT = SAMPLE_WIDTH * SAMPLE_HEIGHT
        const val DEFAULT_COOLDOWN_NS = 350_000_000L
    }
}

/**
 * Bounded, in-memory duration statistics. No screen pixels, sample contents,
 * classification labels, wall-clock timestamps or synthetic capture T0 are stored.
 *
 * T1: frame acquired; T2: inference starts; T3: inference ends; T4: overlay update
 * finishes. All four must come from the same monotonic nanosecond clock. T4 is an
 * application callback timestamp, not a claim about physical display scanout.
 */
class PerformanceMonitor(val capacity: Int = 256) {
    init {
        require(capacity in 1..MAX_CAPACITY) { "capacity must be in 1..$MAX_CAPACITY" }
    }

    private val inferenceDurations = LongArray(capacity)
    private val decisionDurations = LongArray(capacity)
    private val overlayDurations = LongArray(capacity)
    private var nextIndex = 0
    private var count = 0

    data class Percentiles(val p50Ms: Double, val p90Ms: Double, val p95Ms: Double)

    data class Snapshot(
        val sampleCount: Int,
        val inference: Percentiles?,
        val decision: Percentiles?,
        val overlay: Percentiles?,
    )

    /**
     * Adds one complete sample; older samples are evicted at [capacity]. Invalid
     * ordering or an unrepresentable duration is rejected without mutating state.
     * Overlapping pipeline samples are allowed, so ordering is per sample.
     */
    @Synchronized
    fun record(t1Ns: Long, t2Ns: Long, t3Ns: Long, t4Ns: Long) {
        require(t1Ns <= t2Ns && t2Ns <= t3Ns && t3Ns <= t4Ns) {
            "Expected monotonic timestamps T1 <= T2 <= T3 <= T4"
        }
        val decisionNs = t4Ns - t1Ns
        require(decisionNs >= 0L) { "Duration exceeds the nanosecond range" }

        inferenceDurations[nextIndex] = t3Ns - t2Ns
        decisionDurations[nextIndex] = decisionNs
        overlayDurations[nextIndex] = t4Ns - t3Ns
        nextIndex = (nextIndex + 1) % capacity
        if (count < capacity) count++
    }

    /**
     * Percentiles use linear interpolation at rank (n - 1) * p, in milliseconds.
     * An empty monitor returns null statistics rather than manufactured zeroes.
     */
    @Synchronized
    fun snapshot(): Snapshot = Snapshot(
        sampleCount = count,
        inference = percentiles(inferenceDurations),
        decision = percentiles(decisionDurations),
        overlay = percentiles(overlayDurations),
    )

    @Synchronized
    fun reset() {
        inferenceDurations.fill(0L)
        decisionDurations.fill(0L)
        overlayDurations.fill(0L)
        nextIndex = 0
        count = 0
    }

    private fun percentiles(values: LongArray): Percentiles? {
        if (count == 0) return null
        val sorted = values.copyOf(count).apply { sort() }
        fun percentile(fraction: Double): Double {
            val rank = (sorted.size - 1) * fraction
            val lowerIndex = floor(rank).toInt()
            val upperIndex = (lowerIndex + 1).coerceAtMost(sorted.lastIndex)
            val lower = sorted[lowerIndex].toDouble()
            val upper = sorted[upperIndex].toDouble()
            return (lower + (upper - lower) * (rank - lowerIndex)) / 1_000_000.0
        }
        return Percentiles(percentile(0.50), percentile(0.90), percentile(0.95))
    }

    companion object {
        const val MAX_CAPACITY = 4096
    }
}
