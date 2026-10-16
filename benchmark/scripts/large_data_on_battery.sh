#!/system/bin/sh
# 大量データのベンチマークを、充電を外した状態で端末の中だけで回すスクリプト。
#
# 4 つの条件（3.2.0 / 4.0.1 / 4.0.1 + 間引き / Vico）を ROUNDS 周、交互に実行する。
# 各実行の前に電池の温度が TARGET_TEMP 以下になるまで待ち、時間とともに温度が変わる影響を均す。
# adb のケーブルを抜いても動き続けるよう、端末に置いてバックグラウンドで起動する。
#
# 準備：app の benchmark ビルド 3 つと、benchmark モジュールの APK を端末に入れておく。
#   adb push benchmark/scripts/large_data_on_battery.sh /data/local/tmp/
#   adb shell "nohup sh /data/local/tmp/large_data_on_battery.sh > /dev/null 2>&1 &"
# 結果：/data/local/tmp/largedata/ に <条件>-r<周>.json と run.log ができる。

ROUNDS=3
ITERATIONS=5
TARGET_TEMP=350      # 35.0℃（dumpsys battery の値は 0.1℃ 単位）
MAX_WAIT_STEPS=48    # 10 秒 × 48 = 最大 8 分待つ（画面が消える 10 分より短くする）

APP=io.github.ksdev7091.graphcompare
TEST_PKG=$APP.benchmark
MEDIA=/sdcard/Android/media/$TEST_PKG
OUT=/data/local/tmp/largedata

mkdir -p $OUT
rm -f $OUT/*

log() { echo "$(date +%H:%M:%S) $*" >> $OUT/run.log; }
temp() { dumpsys battery | grep temperature | awk '{print $2}'; }
plugged() { dumpsys battery | grep -E 'AC powered|USB powered' | tr -s ' ' | tr '\n' ' '; }

cool_down() {
    i=0
    while [ "$(temp)" -gt $TARGET_TEMP ] && [ $i -lt $MAX_WAIT_STEPS ]; do
        sleep 10
        i=$((i + 1))
    done
}

log "start rounds=$ROUNDS iterations=$ITERATIONS"
for round in $(seq 1 $ROUNDS); do
    for config in mp3 mp4 mp4dec vico; do
        case $config in
            mp4dec) flavor=mp4; decimation=true ;;
            *) flavor=$config; decimation=false ;;
        esac
        cool_down
        log "round $round $config start temp=$(temp) $(plugged)"
        rm -f $MEDIA/*benchmarkData.json $MEDIA/*.perfetto-trace
        am instrument -w -r \
            -e class $TEST_PKG.LargeDataBenchmark \
            -e targetAppId $APP.$flavor \
            -e decimation $decimation \
            -e largeDataIterations $ITERATIONS \
            -e androidx.benchmark.output.enable true \
            $TEST_PKG/androidx.test.runner.AndroidJUnitRunner > $OUT/$config-r$round.txt 2>&1
        cp $MEDIA/$TEST_PKG-benchmarkData.json $OUT/$config-r$round.json
        log "round $round $config end temp=$(temp) result=$(grep -E 'OK \(|FAILURES' $OUT/$config-r$round.txt)"
    done
done
log "DONE"
