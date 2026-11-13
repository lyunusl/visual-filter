"""Android CLI controls restricted to this task's synthetic emulator."""
from pathlib import Path
import subprocess
import re
import os
import sys
import xml.etree.ElementTree as ET

ADB = Path(os.environ.get('ANDROID_HOME', str(Path(__file__).resolve().parent / 'sdk'))) / 'platform-tools/adb.exe'
SERIAL = 'emulator-5554'
_verified = False

def adb(*arguments):
    global _verified
    if not _verified:
        avd = subprocess.check_output([str(ADB), '-s', SERIAL, 'emu', 'avd', 'name'], text=True)
        if avd.splitlines()[0] != 'VisualFilter_API34':
            raise RuntimeError('Refusing to control an emulator outside this task AVD')
        _verified = True
    return subprocess.check_output([str(ADB), '-s', SERIAL, *arguments], text=True, encoding='utf-8', errors='replace')

def tree():
    adb('shell', 'uiautomator', 'dump', '/sdcard/visualfilter-test-ui.xml')
    return ET.fromstring(adb('shell', 'cat', '/sdcard/visualfilter-test-ui.xml'))

def tap_text(label):
    matches = [node for node in tree().iter('node') if node.get('text') == label]
    if len(matches) != 1:
        raise RuntimeError(f'Expected one visible {label!r}; found {len(matches)}')
    x1, y1, x2, y2 = map(int, re.findall(r'\d+', matches[0].get('bounds')))
    return adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))

if __name__ == '__main__':
    if sys.argv[1] == 'tap-text':
        print(tap_text(sys.argv[2]))
    elif sys.argv[1] == 'synthetic-screenshot':
        resumed = adb('shell', 'dumpsys', 'activity', 'activities')
        if not re.search(r'(?:topResumedActivity|mResumedActivity):[^\n]*com\.augelab\.visualfilter\.synthetic/\.SyntheticActivity', resumed):
            raise RuntimeError('Synthetic target not present; screenshot refused')
        destination = Path(__file__).resolve().parent / 'synthetic-only.png'
        destination.write_bytes(subprocess.check_output([str(ADB), '-s', SERIAL, 'exec-out', 'screencap', '-p']))
        print(destination)
    elif sys.argv[1] == 'tree':
        for node in tree().iter('node'):
            text = node.get('text') or node.get('content-desc') or ''
            if text or node.get('checkable') == 'true':
                print(text, '|', node.get('resource-id'), '|', node.get('bounds'), '|', node.get('checkable'), node.get('checked'))
    else:
        print(adb(*sys.argv[1:]))
