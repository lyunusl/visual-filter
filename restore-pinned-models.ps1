param([Parameter(Mandatory=$true)][string]$ModelPath)
$ErrorActionPreference='Stop'
$expected='e202e26cf35d3445d00ac7af74183d1d962121b3a5327fc41afd63605d5c4780'
if((Get-FileHash -LiteralPath $ModelPath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expected){throw 'Wrong model; preserve the frozen fine-tuned reviewed MobileNetV3 Small224 export.'}
Copy-Item -LiteralPath $ModelPath -Destination (Join-Path $PSScriptRoot 'app/src/main/assets/review_mobilenetv3small224_224_rgb_fp32.onnx')
Write-Output 'Exact selected experimental binary model restored.'
