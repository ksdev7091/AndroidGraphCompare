#!/bin/zsh
# 記事用のスクリーンショットを撮る。debug ビルドの 3 flavor を端末に入れておく。
# 使い方: docs/scripts/capture_screens.sh <flavor> <dark|light> <出力フォルダ>
#
# - 円と複合グラフはスライス / 棒をタップした状態、棒はグループと積み上げの両方を撮る
# - 通知が画面の上に出ていたら、消えるまで待ってから撮る（個人の情報を写さないため）
# - ステータスバーとナビゲーションバーは切り落とし、幅 600px に縮める
# 座標は 1200 × 2608 の端末（POCO 26077PC53G）に合わせている
F=$1; T=$2; O=$3; mkdir -p $O
ADB=~/Library/Android/sdk/platform-tools/adb
P=io.github.winschneid.graphcompare.$F
TMP=$(mktemp -d)

open_screen() {
    $ADB shell am force-stop $P
    $ADB shell am start -W -n $P/io.github.winschneid.graphcompare.MainActivity --es theme $T ${1:+--es screen $1} >/dev/null
    sleep 2
}

# システム UI の要素がステータスバーより下に出ていれば、通知が表示されている
notification_shown() {
    $ADB exec-out uiautomator dump /dev/tty 2>/dev/null | python3 -I -c '
import re, sys
nodes = re.findall(r"<node [^>]*package=\"com.android.systemui\"[^>]*bounds=\"\[\d+,(\d+)\]\[\d+,(\d+)\]\"", sys.stdin.read())
sys.exit(0 if any(int(top) >= 140 and int(top) < 900 for top, _ in nodes) else 1)'
}

# shot <名前> <切り落とした後の高さ>
shot() {
    for i in {1..10}; do notification_shown || break; sleep 2; done
    $ADB exec-out screencap -p > $TMP/raw.png
    sips -c $2 1200 --cropOffset 140 0 $TMP/raw.png --out $TMP/crop.png >/dev/null
    sips --resampleWidth 600 $TMP/crop.png --out $O/$1-$F-$T.png >/dev/null
}

CHART=1560   # グラフの画面：タイトルからグラフの下の文字まで
FULL=2310    # 一覧と LazyColumn：ナビゲーションバーの上まで

open_screen; shot list $FULL
open_screen pie; $ADB shell input tap 826 710; sleep 1.2; shot pie $CHART
open_screen bar; shot bar-grouped $CHART; $ADB shell input tap 876 612; sleep 1.5; shot bar-stacked $CHART
open_screen line; shot line $CHART
open_screen combined; $ADB shell input tap 419 1174; sleep 1.2; shot combined $CHART
open_screen large_data; shot large-data $CHART
open_screen animation; shot animation $CHART
open_screen lazy_list; shot lazy-list $FULL
rm -rf $TMP
