# グラフごとの実装量（コードの行数）を flavor ごとに数え、Markdown の表にする。
# 空行・コメント・package・import の行は数えない。
# 使い方: python3 docs/scripts/count_lines.py
import os
import re

ROOT = os.path.join(os.path.dirname(__file__), "..", "..")
SRC = "app/src/{flavor}/java/io/github/ksdev7091/graphcompare/"
FLAVORS = [("mp3", "MPAndroidChart 3.2.0"), ("mp4", "MPAndroidChart 4.0.1"), ("vico", "Vico 3.3.1")]
CHARTS = [
    ("円", ["pie/PieChartScreen.kt"]),
    ("棒", ["bar/BarChartScreen.kt"]),
    ("折れ線", ["line/LineChartScreen.kt"]),
    ("複合", ["combined/CombinedChartScreen.kt", "combined/CombinedMarkerView.kt"]),
    ("大量データ", ["performance/LargeDataScreen.kt"]),
    ("データ更新", ["performance/UpdateAnimationScreen.kt"]),
    ("LazyColumn", ["performance/LazyListScreen.kt"]),
    ("共通のスタイル", ["common/ChartStyle.kt"]),
]
# MPAndroidChart 3.x のマーカーは XML レイアウトが要る
XML = {"mp3": ["res/layout/marker_combined.xml", "res/drawable/marker_background.xml"]}


def count_kotlin(path):
    if not os.path.exists(path):
        return 0
    count, in_block = 0, False
    for line in open(path, encoding="utf-8"):
        s = line.strip()
        if in_block:
            in_block = "*/" not in s
            continue
        if s.startswith("/*"):
            in_block = "*/" not in s
            continue
        if not s or s.startswith("//") or re.match(r"(package|import) ", s):
            continue
        count += 1
    return count


def count_xml(path):
    if not os.path.exists(path):
        return 0
    text = re.sub(r"<!--.*?-->", "", open(path, encoding="utf-8").read(), flags=re.S)
    return sum(1 for line in text.splitlines() if line.strip() and not line.strip().startswith("<?xml"))


print("| | " + " | ".join(name for _, name in FLAVORS) + " |")
print("|---|" + "---:|" * len(FLAVORS))
totals = {f: 0 for f, _ in FLAVORS}
for label, files in CHARTS:
    cells = []
    for flavor, _ in FLAVORS:
        n = sum(count_kotlin(os.path.join(ROOT, SRC.format(flavor=flavor), f)) for f in files)
        totals[flavor] += n
        cells.append(str(n))
    print(f"| {label} | " + " | ".join(cells) + " |")
xml_cells = []
for flavor, _ in FLAVORS:
    n = sum(count_xml(os.path.join(ROOT, f"app/src/{flavor}", f)) for f in XML.get(flavor, []))
    totals[flavor] += n
    xml_cells.append(str(n) if n else "-")
print("| XML（マーカー） | " + " | ".join(xml_cells) + " |")
print("| **合計** | " + " | ".join(f"**{totals[f]}**" for f, _ in FLAVORS) + " |")
