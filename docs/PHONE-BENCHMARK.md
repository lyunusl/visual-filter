# Physical Instagram benchmark

The current scroll service has not yet been validated on a physical phone. Live Instagram Reels was checked on the emulator in0.7.2. Check scroll-evaluation/phone-model-probe.json, if present, for an actual physical model-only probe. Absence of this file means no physical timing result. Unit tests, compilation, emulator results, and model-only instrumentation do not establish physical-phone capture/overlay performance.

Phone: Samsung Galaxy A55 SM-A556E, Android16/API36, ARM64, previously observed through authorized ADB. Owner must reconnect an authorized device and manually enable Visual Filter Scroll Research Accessibility service. Open app, enable optional scalar metrics, start filter, and open Instagram. Existing phone settings/grants must not be changed automatically. Images remain in memory; do not save screenshots, record screen video, dump UI content, upload images, or export broad logcat. Collect only the VisualFilterScroll scalar tag.

In0.7.3 automatic maximum-throughput mode, manually perform10-20 Reel transitions with unchanged threshold/ROI, orientation and power mode. Include expected SAFE/BLOCK transitions and video-motion cases; repeat on feed cards with different image heights. Record observed gesture obstruction, geometry accuracy, stale results, automatic safe opening, false masks/misses and exposure qualitatively. No automatic classification of ordinary frame motion as NSFW. Record warmup separately. FPS selection,350ms frame spacing and120ms scroll quiet time have been removed. Count capture requests, acquired frames, Android errors and region decisions separately; multiple region decisions do not equal multiple frames.

| Metric | Clock/source | Meaning |
|---|---|---|
| T0_source | unknown | No Instagram decode/prerender producer is available. |
| T0_present | unknown | First source-content compositor presentation is unmeasured. |
| screenshot_uptime_ms | ScreenshotResult.timestamp, uptimeMillis milliseconds | Platform screenshot time; excludes sleep, not source creation. |
| scroll_event_uptime_ms | AccessibilityEvent.eventTime, uptimeMillis milliseconds | Latest actual scroll notification, not onset of touch or first image presentation. |
| T1_elapsed_ns | elapsedRealtimeNanos at callback receipt | Receipt of an already captured window. |
| T2_elapsed_ns | elapsedRealtimeNanos before classifier | Preprocessing/model/policy start after ROI preparation. |
| T3_elapsed_ns | elapsedRealtimeNanos after classifier | Result ready, before UI dispatch. |
| T4_dispatch_elapsed_ns | elapsedRealtimeNanos after mask update | Main-thread view dispatch, not compositor presentation. |
| T4_present | unknown | No compositor/fence timing is implemented. |

Compute T3-T2 and T4_dispatch-T1 on the same elapsed clock; summarize counts/P50/P90/P95. Screenshot-to-latest-scroll differences are both uptime milliseconds but require same-generation correlation and do not measure zero exposure. Never subtract uptime timestamps from elapsedRealtimeNanos. Completed region decisions are not identical to frame captures when multiple feed cards are visible. Record actual capture/decision rate and stale epochs separately. Missing timestamps must remain unknown; no source/exposure/compositor duration can be claimed from dispatch timing.

The isolated native instrumentation probe is separate: in0.7.1 only the frozen grouped-split local MobileNetV3Small binary model remains. Ten licensed PNG fixtures and eleven synthetic patterns check pixel hashes and ONNX probabilities against PC references, after warmup with three timed trials each. Older model results are historical. Setup/warmup, native inference and live end-to-end metrics must remain distinct. There is no A55 latency estimate until actual successful output is recorded.

Postrender capture, incomplete accessible media geometry and safe-video changes still prevent a zero-exposure guarantee even after successful physical tests.
