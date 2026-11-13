# 0.9.2-mobilenetv3small-ft (versionCode25)

The active export is reviewed MobileNetV3 Small224 FP32, SHA256 e202e26cf35d3445d00ac7af74183d1d962121b3a5327fc41afd63605d5c4780, 6,094,846 bytes. This supersedes older model/version descriptions in README and BUILDING. Default block threshold is 0.4477543234825134, inclusive. Old Large, AdamCodd, Marqo and legacy thresholds are retained but never imported into this model's preference key. Existing PIN and encounter count remain.

Input: float32 NHWC[1,224,224,3], raw RGB0..255. Use Pillow-compatible antialiased bilinear letterbox, gray128 padding, ties-even rounded dimensions, centered with odd padding right/bottom. Division by255 and ImageNet normalization are embedded; do not normalize externally. Output classes are nsfw_suggestive, normal. Failed INT8 export is excluded.

Preserves PIN-protected stop, sticky contiguous visual-area masks, animations and debug scores. Pending content stays visible by user choice. Hair/arms/legs are not separate classes. Manual diagnostic recall6/37 and normal false blocks24/152; this export does not establish reliable Islamic coverage.

Build from VisualFilter-PC: build-scroll.ps1 -Probe for x86_64 debug, benchmark and debug tests; build-scroll.ps1 for signed ARM64 release, release tests and lint. Keep research-debug.keystore private. Model assets must match the pinned hash. No Internet permission or screenshot persistence.

Verification reports are in VisualFilter-PC/scroll-evaluation/small-092. Emulator timing is not physical Galaxy A55 or source-to-compositor latency. CPU runtime uses intra2/inter1 threads, one in-flight pipeline, no user FPS cap; Android capture restrictions/backoff still apply.
