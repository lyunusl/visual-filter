# Build and verify0.9.0

Pinned JDK17, SDK35/build-tools35.0.0 and Gradle wrapper. Restore the exact local FP32 AdamCodd epoch6 export via restore-pinned-models.ps1 -ModelPath <local ONNX>; SHA25640fa6c3c4e25dd4356f150ea737f6746e566fb040d530afffca22b6ccd10c38e. Never substitute upstream weights or failed INT8.

(0.9.0 notu) Release APK'si proje sorumlusu tarafından, bu depoda yer almayan bir yardımcı betikle derlenip imzalanmıştır. Kaynaktan derleme ve test komutları için CONTRIBUTING.md dosyasına bakın. 0.9.0 kaynak paketi model ağırlıklarını ve imzalama anahtarlarını içermiyordu; 0.9.2 modelinin bu depodaki yeri için README.md dosyasına bakın.

prepare_adam_parity_090.py produces21 exact pixel/probability references from10 existing licensed fixtures and11 deterministic RGB patterns. Direct384x384 bilinear stretch, no padding/normalization. ResearchProbe checks Android pixel SHA256, probabilities/decision, threshold controls and close/reject/reopen. It records PSS/native memory and inference timing using independent model-benchmark; live filter must be stopped with its test PIN first. Android/PC parity is not accuracy or physical-phone validation.

The live owned fixture test uses screenshot/compositor samples in RAM only and persists scalar differences/logs. It checks default SAFE/BLOCK, contiguous mask coverage, migration notice, preserved PIN/progress/old threshold, developer labels, repeated-frame count stability and PIN STOP. No Instagram pixels or customer dataset leave the PC. Physical A55 remains unmeasured unless separately connected and explicitly tested.
