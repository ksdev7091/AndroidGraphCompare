# docs/benchmark-results/*-benchmarkData.json から記事用の Markdown の表を作る。
# 使い方: python3 docs/benchmark-results/make_tables.py docs/benchmark-results
import json, sys, os, statistics
root = sys.argv[1]
flavors = [("mp3", "MPAndroidChart 3.2.0"), ("mp4", "MPAndroidChart 4.0.1"), ("vico", "Vico 3.3.1")]
data = {f: json.load(open(os.path.join(root, f"{f}-benchmarkData.json"))) for f, _ in flavors}

def bench(f, cls, name_prefix):
    for b in data[f]["benchmarks"]:
        if b["className"].endswith(cls) and b["name"].startswith(name_prefix):
            return b
    return None

def startup_ms(b):
    m = b["metrics"].get("timeToInitialDisplayMs")
    return m["median"] if m else None

def jank_percent(b):
    runs = b["sampledMetrics"]["frameOverrunMs"]["runs"]
    frames = [v for run in runs for v in run]
    return 100.0 * sum(1 for v in frames if v > 0) / len(frames)

header = "| | " + " | ".join(n for _, n in flavors) + " |\n|---|" + "---:|" * len(flavors)
print("### 起動と画面表示（timeToInitialDisplay の中央値、ms）\n")
print(header)
row = [startup_ms(bench(f, "StartupBenchmark", "startupCold")) for f, _ in flavors]
print("| グラフ一覧までのコールドスタート | " + " | ".join(f"{v:.0f}" for v in row) + " |")
screens = [("Pie", "円"), ("Bar", "棒"), ("Line", "折れ線"), ("Combined", "複合"), ("LargeData", "大量データ"), ("Animation", "データ更新"), ("LazyList", "LazyColumn")]
for key, label in screens:
    row = [startup_ms(bench(f, "OpenChartBenchmark", f"openChart[{key}]")) for f, _ in flavors]
    print(f"| {label}の画面を直接開く | " + " | ".join(f"{v:.0f}" for v in row) + " |")

for cls, name, label in [("LargeDataBenchmark", "zoomAndScroll", "大量データ：ズームとスクロール"),
                         ("UpdateAnimationBenchmark", "updateTenTimes", "データ更新アニメーション：10 回"),
                         ("LazyListBenchmark", "flingDown", "LazyColumn：3 回フリック")]:
    print(f"\n### {label}\n")
    print(header)
    bs = [bench(f, cls, name) for f, _ in flavors]
    def r(title, fn, fmt):
        print(f"| {title} | " + " | ".join(fmt.format(fn(b)) for b in bs) + " |")
    r("フレームの CPU 時間 P50（ms）", lambda b: b["sampledMetrics"]["frameDurationCpuMs"]["P50"], "{:.1f}")
    r("フレームの CPU 時間 P90（ms）", lambda b: b["sampledMetrics"]["frameDurationCpuMs"]["P90"], "{:.1f}")
    r("フレームの CPU 時間 P99（ms）", lambda b: b["sampledMetrics"]["frameDurationCpuMs"]["P99"], "{:.1f}")
    r("締め切り超過 P99（ms、正なら遅延）", lambda b: b["sampledMetrics"]["frameOverrunMs"]["P99"], "{:+.1f}")
    r("遅延したフレームの割合", jank_percent, "{:.1f}%")
    r("フレーム数（中央値）", lambda b: b["metrics"]["frameCount"]["median"], "{:.0f}")

print("\n### 計測条件\n")
for f, n in flavors:
    c = data[f]["context"]
    print(f"- {n}: compilationMode={c['compilationMode']}, cpuLocked={c['cpuLocked']}")
