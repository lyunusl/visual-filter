"""Run manual dummy commands against geometric content; never measures Reel safety."""
from pathlib import Path
import importlib.util
import json
import re
import sys
import time
from emu import adb

fps = int(sys.argv[1])
assert fps in (3, 5, 8)
assert '720x1280' in adb('shell', 'wm', 'size')
assert 'com.augelab.visualfilter.synthetic/.SyntheticActivity' in adb('shell', 'dumpsys', 'activity', 'activities')
adb('shell', 'logcat', '-c')
for _ in range(20):
    adb('shell', 'input', 'swipe', '690', '900', '690', '300', '100')
    adb('shell', 'input', 'tap', '90', '1072')
    time.sleep(.55)
time.sleep(.4)
raw = adb('shell', 'logcat', '-d', '-s', 'VisualFilter:I', 'VisualFilterSynthetic:I', '*:S')
project = Path(__file__).resolve().parents[1] / 'source/visual-filter-android'
if not project.exists():
    project = next(p for p in Path(__file__).resolve().parents if (p/'settings.gradle.kts').is_file())
output = project / 'verification' / f'pc-manual-{fps}fps-metadata.txt'
output.write_text(raw, encoding='utf-8')
spec=importlib.util.spec_from_file_location('analyzer', project / 'tools/analyze_log.py')
analyzer=importlib.util.module_from_spec(spec); spec.loader.exec_module(analyzer)
records = [line for line in raw.splitlines() if 'T4_dispatch=' in line]
summary = {'environment':'AOSP API34 x86_64 emulator, WHPX, software graphics, 720x1280',
           'fps_setting':fps, 'manual_commands_sent':20, 'applied_manual_decisions':len(records),
           'synthetic_draw_events':raw.count('T0_draw_start='), 't0_instagram':None, 't4_presentation':None}
for field in ('inference_ms','overlay_dispatch_ms','request_to_dispatch_ms','decision_after_acquire_ms','cached_frame_age_ms'):
    values=[float(re.search(r'\b'+field+r'=([0-9.Ee+-]+)', line).group(1)) for line in records]
    summary[field] = {f'p{p}':analyzer.quantile(values,p/100) for p in (50,90,95)}
(output.with_suffix('.json')).write_text(json.dumps(summary,indent=2),encoding='utf-8')
print(json.dumps(summary,indent=2))
