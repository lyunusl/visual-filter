#!/usr/bin/env python3
"""Summarize metadata-only VisualFilter logs. Never estimates unknown display latency."""
import re
import sys
import math
from pathlib import Path

def quantile(values, p):
    values = sorted(values)
    if not values:
        return None
    index = (len(values) - 1) * p
    low = math.floor(index)
    high = math.ceil(index)
    return values[low] + (values[high] - values[low]) * (index - low)

def main():
    if len(sys.argv) != 2:
        raise SystemExit("Usage: python3 tools/analyze_log.py metadata-logcat.txt")
    fields = ["inference_ms", "decision_after_acquire_ms", "overlay_dispatch_ms", "request_to_dispatch_ms"]
    records = []
    for line in Path(sys.argv[1]).read_text().splitlines():
        if "T4_dispatch=" not in line:
            continue
        row = {}
        for field in fields:
            match = re.search(r"\b" + field + r"=([0-9.Ee+-]+)", line)
            if match:
                value = float(match.group(1))
                if math.isfinite(value) and value >= 0:
                    row[field] = value
        if len(row) == len(fields):
            records.append(row)
    print(f"Applied MANUAL test decisions: {len(records)}")
    for field in fields:
        values = [row[field] for row in records]
        print(field, {f"P{p}": quantile(values, p/100) for p in (50,90,95)})
    print("Capture latency and actual pre-display TOTAL: UNMEASURED (T0 and T4 presentation unavailable)")
    print("Only applied manual decisions are summarized; cached-frame age affects after-acquire latency; stale/cancelled results are excluded. Do not combine device/sample-rate runs.")

if __name__ == "__main__":
    main()
