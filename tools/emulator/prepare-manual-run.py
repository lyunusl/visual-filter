"""Use observed Android UI nodes on the synthetic task emulator only."""
import sys
from emu import adb, tree, tap_text

fps=int(sys.argv[1]); assert fps in (3,5,8)
if 'null' not in adb('shell','dumpsys','media_projection'):
    print(adb('shell','input','tap','630','1072')) # STOP from observed fixed 720x1280 manual panel
assert 'null' in adb('shell','dumpsys','media_projection')
print(adb('shell','am','start','-n','com.augelab.visualfilter/.MainActivity'))
adb('shell','input','swipe','650','300','650','1100','800')
adb('shell','input','swipe','650','300','650','1100','800')
adb('shell','input','swipe','650','300','650','1100','800')
for _ in range(10):
    if any(n.get('text')==f'{fps} FPS' for n in tree().iter('node')):
        break
    adb('shell','input','swipe','650','1000','650','600','800')
print(tap_text(f'{fps} FPS'))
for _ in range(5):
    if any(n.get('text')=='START EXPERIMENT' for n in tree().iter('node')):
        break
    adb('shell','input','swipe','650','1000','650','400','800')
print(tap_text('START EXPERIMENT'))
for n in tree().iter('node'):
    if n.get('text'): print(n.get('text'))
