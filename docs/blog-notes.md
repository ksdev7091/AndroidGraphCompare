# ブログ用メモ：実装して見えた違い

実装しながら気づいた、ライブラリごとの違いのメモ。記事を書くときの材料にする。

## 複合グラフ（棒 + 折れ線、左右の軸、タップでマーカー）

### 作り方
| | MPAndroidChart 3.2.0 | MPAndroidChart 4.0.1 + Compose | Vico 3.3.1 |
|---|---|---|---|
| グラフ本体 | View の `CombinedChart` を `AndroidView` で包む | Composable の `CombinedChart`。中身は View を `AndroidView` で包んだもの | `CartesianChartHost` に棒と折れ線のレイヤーを渡す |
| 設定 | Java の setter | `setup` / `update` の中で View のプロパティに書く | `remember*` 関数の引数 |
| マーカー | XML レイアウト + `MarkerView` の継承 | Composable | `TextComponent`（Canvas に描く文字。Composable は置けない） |
| 選択状態 | リスナーから state に書き戻す | `ChartState.selectedEntry` | `CartesianMarkerVisibilityListener` |
| 元データ | `Entry.data`（Object、キャストが要る） | 型付きの payload（`Entry<CombinedPoint>`） | 数値しか渡せない。x 座標から元データを引き直す |
| 凡例 | 既定で出る | 既定で出る | 自分で並べる |
| タップ後の挙動 | マーカーが出たまま | マーカーが出たまま | 既定は押している間だけ（`rememberToggleOnTap()` で揃えた） |

### 見た目を揃えるのに必要だった設定
既定のままでは見た目がかなり違う。揃えるのに使った設定と、そこから読み取れる設計の違い。

- **軸の範囲と目盛り**：自動計算に任せるとライブラリごとに区切りが変わる。
  Vico は右軸のラベルが左軸の補助線の高さに置かれ、-10%, -5%, 0%, 4%, 8%… のような半端な値になった。
  範囲は MPAndroidChart が軸（`axisMinimum` / `axisMaximum`）、Vico がレイヤー（`CartesianLayerRangeProvider.fixed`）に設定する。
  目盛りは MPAndroidChart が数（`setLabelCount(n, force = true)`）、Vico が間隔（`VerticalAxis.ItemPlacer.step`）で指定する。
- **補助線**：どちらも既定では各軸が補助線を引く（MPAndroidChart は実線、Vico は点線）。
  左右の軸の補助線が別々の高さに引かれて二重に見えるので、左軸の横線だけ残し、同じ色・太さの実線にした。
- **12 か月が画面に収まらない**：Vico は x の間隔を dp で決め、はみ出した分はスクロールで見せる。
  スクロールを止めると右側が見えなくなるので、`Zoom.Content` で全体が収まる倍率を指定した。
  MPAndroidChart は範囲を画面幅に合わせて描くので、この問題は起きない。
- **棒の太さ**：MPAndroidChart は「x の 1 区間に対する割合」（`barWidth = 0.6`）、Vico は「太さと間隔の dp」。
  Vico で同じ比率にするには次の 3 つが必要だった。
  - 太さと間隔を比率から計算する（太さ 16dp、間隔 10.7dp）
  - 折れ線レイヤーの x 間隔は「点の大きさ + `pointSpacing`」で、棒より広いとそちらに合わせて全体が縮み、棒が細くなる。
    `pointSpacing` から点の大きさを引く
  - x 軸の `addExtremeLabelPadding`（端のラベルが切れないよう余白を足す）を切る
  - それでも Vico は点の半分（4dp）を拡大縮小しない余白として両端に取るので、x の間隔は約 3% 狭い（70px と 72px）
- **軸の幅と凡例の位置**：
  - y 軸の幅はどちらも既定ではラベルの長さで決まる。両方で 40dp に固定し、描画領域の左右の端を揃えた
    （MPAndroidChart は `minWidth` / `maxWidth`、Vico は `BaseAxis.Size.Fixed`）
  - 凡例の起点が違う。MPAndroidChart の横並びの凡例は「描画領域の左端」から、Vico は「Canvas の左端」から並ぶ。
    Vico は中央寄せもできない。y 軸の幅を固定したうえで、Vico の凡例の左余白を y 軸の幅と同じにして揃えた
  - 凡例の並び順：MPAndroidChart は `CombinedData` の既定で 折れ線 → 棒 の順になる。Vico もこの順に揃えた
- **グラフの高さ**：外側を同じ 300dp にしても、Vico はグラフの上に 3 行のマーカーを出す余白を取るので、描画領域が低い。
- **選択の見せ方**：MPAndroidChart はタップした棒を濃い色にし、マーカーを点の上に出す。
  Vico は縦の点線を引き、マーカーはグラフ上部の決まった位置に出す。

## 折れ線（2 系列、エリア塗り、曲線）

- **エリアのグラデーション**：MPAndroidChart は 3.x も 4.x も `fillDrawable` に `GradientDrawable` を渡す（View の API のまま）。
  Vico は Compose の `Brush.verticalGradient` を `Fill` にそのまま渡せる。
- **曲線**：MPAndroidChart の `Mode.CUBIC_BEZIER` に対し、Vico の `Interpolator.cubic()` は点の間で角が残り、直線に近く見えた。
  点を通るなめらかな曲線になる `Interpolator.catmullRom()` のほうが MPAndroidChart に近い。
- **右側の余白**：右に軸がないとき、MPAndroidChart は最小余白（`minOffset`、既定 15dp）を取り、Vico は端まで描く。
  Vico 側に `CartesianLayerPadding(unscalableEnd = 15.dp)` を足して揃えた。
- **端の x ラベル**：MPAndroidChart は描画領域の左端の「1日」を、左にはみ出して中央揃えで描く。
  Vico は描けない。ラベルの最大幅が「端までの距離の 2 倍」に制限され、描画領域で切り取られるため。
  `shiftExtremeLabels`（端のラベルを内側に寄せる）を使うと最後のラベル（26日）の位置まで変わってしまうので、揃えるのはやめた。
- **選択**：MPAndroidChart は既定でタップした点に十字線を出す（`isHighlightPerTapEnabled = false` で止めた）。
  Vico はマーカーを渡さなければ何も出ない。

## 棒（グループ / 積み上げの切り替え、角丸）

- **グループと積み上げの作り方**：
  - MPAndroidChart：グループは系列ごとに `BarDataSet` を作り、`groupBars(fromX, groupSpace, barSpace)` で並べ直す。
    積み上げは 1 つの `BarDataSet` に、値を配列（3.x）/ リスト（4.x）で持つ `BarEntry` を入れ、凡例の文字は `stackLabels` で付ける。
    切り替えるとデータの形そのものが変わる
  - Vico：データは同じ（系列 3 つ）で、`ColumnCartesianLayer` の `MergeMode`（`Grouped` / `Stacked`）を変えるだけ
- **切り替えの反映**：
  - 3.2.0：View は Compose の state を知らないので、`AndroidView` の `update` でモードの変化を見て、データと軸を差し替え、
    `notifyDataSetChanged()` で目盛りを計算し直させる
  - 4.0.1：`remember(mode)` で新しい `BarData` を作って渡すだけ。ただし軸の範囲を変えたら `update` で `notifyDataSetChanged()` が要る
  - Vico：`MergeMode` と範囲を変えても、**データが前回と同じだと y の範囲が計算し直されず、積み上げが 0〜100 の範囲に描かれて上が切れた**。
    y の範囲は transaction を流したときに計算されるため。表示方法を `ExtraStore` に入れて transaction を流し直すと直った
    （データが同じだと、transaction を流し直すだけでは足りなかった）
- **角丸**：
  - 3.2.0：設定がない（角丸にするには `BarChartRenderer` を自作する）
  - 4.0.1：`barCornerRadius`。グループでは 4 隅とも丸め、積み上げでは一番上の区画の上側だけを丸める
  - Vico：棒の形を Compose の `Shape` で渡す。区画ごとに形を当てるので、そのままだと積み上げの途中の区画まで丸くなる。
    4.0.1 に合わせ、表示方法と系列ごとに形を変えた
- **太さと間隔**：複合グラフと同じく、MPAndroidChart は x の 1 区間に対する割合、Vico は dp。
  1 区間を 64dp とみなして換算し、全体が収まる倍率に任せた。

## 円（ドーナツ、中央の文字、割合、タップで強調）

- **機能の有無**：中央の文字・割合の計算・タップでのスライスの強調は、MPAndroidChart では設定だけでできる。
  Vico（3.x で PieChart を追加）には中央の文字もタップ判定もない。
  - 中央の文字：`PieChartHost` の上に Compose の `Text` を重ねた（Compose なので簡単）
  - タップ：`pointerInput` でタップ位置の角度と中心からの距離を求め、どのスライスかを自前で計算した（`PieChartSpec.sliceIndexAt`）。
    選んだスライスは `Slice.offsetFromCenter` で外へずらす。もう一度タップすると外す、穴の中や円の外で外す、も MPAndroidChart に合わせて自前で書いた
  - 割合：Vico は値をそのまま渡すので、合計から自分で計算して `PieValueFormatter` で文字にした
- **スライスの文字**：MPAndroidChart は値（割合）と項目名を別々に描く（値の下に項目名）。Vico は 1 つの文字列なので、2 行の文字にした。
- **選択時にずらす量**：既定値が 3.x は 18（px のまま）、4.x は 9dp と違う。8dp に揃えた。
- **円の大きさ**：MPAndroidChart は最初から「ずらす量」の分だけ円を小さくしておくので、選択しても大きさが変わらない。
  Vico は「指定した半径 − 一番大きいずらし量」で描くので、**スライスを選ぶたびに円全体が縮む**。
  選択中は指定する直径をずらす分だけ大きくして揃えた。
- **凡例**：位置合わせが難しいうえ、項目名をスライスに描けば足りるので、円グラフでは 3 実装とも出さなかった。

## 大量データ（10,000 点、ピンチズームとスクロール）

- **描き方を揃えた条件**：点の印なし、直線でつなぐ（曲線は計算が重く、ライブラリごとの差が性能に混ざる）、x 方向だけズーム。
- **ズームの有効化**：MPAndroidChart は `isScaleXEnabled` / `isDragEnabled`。Vico は `rememberVicoZoomState` / `rememberVicoScrollState`。
  Vico は点の間隔を dp で持つので、最初は全体が収まる倍率（`Zoom.Content`）から始める。
- **4.0.1 の間引き**：`isDecimationEnabled`（同じ画素の列に重なる点を間引く）があるが、既定は off。まず既定のまま比べる。
- **x 軸のラベル**：MPAndroidChart は 0 / 2,000 / 4,000…ときりのよい値を選ぶ。
  Vico は拡大率に応じて「重ならない最小の点数おき」に置くので、0 / 1,056 / 2,112…のような半端な値になる。

## データ更新アニメーション（ボタンで 12 か月分を差し替え）

- **前の値から新しい値へ動かせるか**：
  - 3.2.0：できない。差し替えたあと `animateY` で 0 から伸ばし直す（見た目がまったく違う）
  - 4.0.1：chart composable に `animateChanges = true` を渡すと、新しいデータを渡すたびに前の値から動く。
    時間は 300ms 固定（View の `animateDataChange(data, durationMillis)` なら時間を指定できる）
  - Vico：ModelProducer に新しいデータを流すと自動で動く。`animationSpec` で時間と動き方を指定できる（既定は約 500ms で、だんだん遅くなる動き）
- 3 実装とも 300ms・等速に揃え、最初の表示ではアニメーションさせないようにした（Vico は `initialAnimationSpec = null`）。

## LazyColumn（30 個の小さな折れ線）

- **行ごとに何が作られるか**：
  - 3.2.0：`AndroidView` で包んだ `LineChart`（View）。`onReset` を渡さないので、View は使い回されず行ごとに作られる
  - 4.0.1：chart composable も中身は `AndroidView` で包んだ View で、`onReset` は渡していない。3.2.0 と同じく行ごとに View を作る
  - Vico：Compose で描くので View は作らない。データを更新しないので、ModelProducer を使わず `CartesianChartModel` を直接渡せる
- **リストのスクロールとの兼ね合い**：MPAndroidChart はチャートがタッチを受け取るので `isTouchEnabled = false` にした。
  Vico はマーカーもスクロールも無効にすれば、指の動きはそのままリストに渡る。

## Macrobenchmark のシナリオ

`benchmark` モジュール。flavor ごとに `./gradlew :benchmark:connected<Flavor>BenchmarkAndroidTest` で実行する。

| テスト | 内容 | 指標 |
|---|---|---|
| `StartupBenchmark` | グラフ一覧までのコールドスタート | 起動時間 |
| `OpenChartBenchmark` | 各グラフ画面をコールドスタートで直接開く（7 画面） | 起動時間、フレーム時間 |
| `LargeDataBenchmark` | ピンチで拡大 → 左右に 2 回ずつスワイプ → ピンチで縮小 | フレーム時間 |
| `UpdateAnimationBenchmark` | ボタンで 10 回差し替え、毎回アニメーションの終わりまで待つ | フレーム時間 |
| `LazyListBenchmark` | LazyColumn を下へ 3 回フリック | フレーム時間 |

### ベンチマークを書くときの注意
- `AndroidView`（MPAndroidChart の View、4.0.1 の chart composable の中身も同じ）に直接付けた testTag は、
  中の View のアクセシビリティ情報に隠れて UiAutomator から見つからなかった。Compose の `Box` で包んでそこに付ける。
- `connectedAndroidTest` は終わるとアプリとテスト用 APK をアンインストールする。
- 端末によっては（Xiaomi 系など）USB 経由のインストールのたびに端末側で承認が要る。承認されないと
  `INSTALL_FAILED_USER_RESTRICTED` になるが、Gradle は BUILD SUCCESSFUL のまま終わるので、ログを確認する。

## 計測結果

生の結果は `docs/benchmark-results/<flavor>-benchmarkData.json`。表の値はそこから作った。

### 計測条件
- 端末：POCO（26077PC53G）、Android 16、可変リフレッシュレート（60〜185Hz）
- release 相当（R8 あり、debug 署名）の `benchmark` ビルド。`compilationMode` は 3 実装とも `run-from-apk`（事前コンパイルなし、JIT のみ）
- 起動系は各 5〜10 回、操作系は各 5 回。CPU のクロックは Macrobenchmark が固定（`cpuLocked = true`）
- 充電しながら計測した。開始時の電池の温度は 3.2.0 が 35.5℃、4.0.1 が 37.5℃、Vico が 36.9℃（前の計測の熱が抜けきらなかった）
- 1 台・各 5 回なので、数 % の差はばらつきの範囲。はっきりした差だけを結論に使う

### 起動と画面表示（timeToInitialDisplay の中央値、ms）

| | MPAndroidChart 3.2.0 | MPAndroidChart 4.0.1 | Vico 3.3.1 |
|---|---:|---:|---:|
| グラフ一覧までのコールドスタート | 133 | 162 | 140 |
| 円の画面を直接開く | 140 | 154 | 150 |
| 棒の画面を直接開く | 169 | 172 | 164 |
| 折れ線の画面を直接開く | 139 | 137 | 131 |
| 複合の画面を直接開く | 147 | 134 | 131 |
| 大量データの画面を直接開く | 146 | 149 | 140 |
| データ更新の画面を直接開く | 138 | 136 | 131 |
| LazyColumnの画面を直接開く | 154 | 133 | 148 |

### 大量データ：ズームとスクロール

| | MPAndroidChart 3.2.0 | MPAndroidChart 4.0.1 | Vico 3.3.1 |
|---|---:|---:|---:|
| フレームの CPU 時間 P50（ms） | 6.4 | 6.1 | 8.4 |
| フレームの CPU 時間 P90（ms） | 8.6 | 8.5 | 12.4 |
| フレームの CPU 時間 P99（ms） | 13.7 | 16.4 | 23.4 |
| 締め切り超過 P99（ms、正なら遅延） | +6.6 | +6.8 | +21.7 |
| 遅延したフレームの割合 | 1.8% | 3.3% | 22.1% |
| フレーム数（中央値） | 89 | 90 | 83 |

### データ更新アニメーション：10 回

| | MPAndroidChart 3.2.0 | MPAndroidChart 4.0.1 | Vico 3.3.1 |
|---|---:|---:|---:|
| フレームの CPU 時間 P50（ms） | 2.1 | 2.3 | 4.9 |
| フレームの CPU 時間 P90（ms） | 2.4 | 2.6 | 6.2 |
| フレームの CPU 時間 P99（ms） | 3.7 | 4.2 | 7.7 |
| 締め切り超過 P99（ms、正なら遅延） | -8.1 | -3.1 | +0.8 |
| 遅延したフレームの割合 | 0.1% | 0.1% | 1.6% |
| フレーム数（中央値） | 413 | 406 | 406 |

### LazyColumn：3 回フリック

| | MPAndroidChart 3.2.0 | MPAndroidChart 4.0.1 | Vico 3.3.1 |
|---|---:|---:|---:|
| フレームの CPU 時間 P50（ms） | 2.3 | 2.3 | 2.0 |
| フレームの CPU 時間 P90（ms） | 2.7 | 2.7 | 2.6 |
| フレームの CPU 時間 P99（ms） | 8.0 | 4.9 | 9.4 |
| 締め切り超過 P99（ms、正なら遅延） | -4.0 | -7.0 | -1.9 |
| 遅延したフレームの割合 | 0.5% | 0.3% | 0.2% |
| フレーム数（中央値） | 160 | 160 | 159 |

### APK サイズ（release、R8 あり、全 7 画面を実装した時点）

| | MPAndroidChart 3.2.0 | MPAndroidChart 4.0.1 | Vico 3.3.1 |
|---|---:|---:|---:|
| APK | 1,080 KB | 1,111 KB（+31） | 1,143 KB（+63） |

### 結果から読めること
- **起動・画面表示**：3 実装の差はばらつきの範囲（どれも 120〜180ms ほどで、範囲が大きく重なる）。
  4.0.1 のコールドスタートの中央値が高いのは、1 回だけ 243ms の外れ値があったため。
- **大量データ（10,000 点のズームとスクロール）**：はっきり差が出た。MPAndroidChart は 3.x・4.x とも遅延したフレームが 2〜3% なのに対し、
  Vico は 22%。フレームの CPU 時間も Vico が 1.3〜1.7 倍長い。
  条件を揃えて測り直しても同じ結果で、4.0.1 の間引きを on にするとさらに減った（次の節）。
- **データ更新アニメーション**：Vico はフレームの CPU 時間が MPAndroidChart の約 2 倍だが、遅延したフレームは 1.6% で、目に見える差は小さい。
  ただし 3.2.0 は 0 から伸ばし直すだけなので、見た目の動きは同じではない。
- **LazyColumn**：3 実装とも遅延したフレームは 1% 未満で、差はほとんどない。
  View を行ごとに作る MPAndroidChart でも、この規模（30 行、小さな折れ線）なら問題にならなかった。
- **APK サイズ**：Vico が一番大きいが、差は 60KB ほど。

### 大量データの測り直し（電池駆動・温度を揃えて・各 15 回）

最初の計測は充電しながらで、開始温度も揃わなかったので、大量データだけ測り直した。

- **やり方**：`benchmark/scripts/large_data_on_battery.sh` を端末に置いてバックグラウンドで実行し、USB を抜いて電池駆動で回した
  （`adb shell am instrument` を端末の中で順に実行するので、ケーブルを抜いても続く）。
  4 つの条件を 3 周交互に回し（各 5 回 × 3 周 = 15 回）、時間とともに温度が変わる影響を均した。
  各計測の前に、電池の温度が 35.0℃ 以下になるまで待った（最大 8 分）
- **温度**：12 回中 10 回は 34.9〜35.0℃ で開始。1 周目の 3.2.0（36.9℃）と 4.0.1（35.9℃）だけ、8 分待っても下がりきらずに始まった
- 生の結果は `docs/benchmark-results/large-data-on-battery/`（条件ごと・周ごとの JSON と run.log）

| | 3.2.0 | 4.0.1 | 4.0.1 + 間引き | Vico 3.3.1 |
|---|---:|---:|---:|---:|
| フレームの CPU 時間 P50（ms） | 6.5 | 6.0 | 6.0 | 8.4 |
| フレームの CPU 時間 P90（ms） | 8.5 | 8.3 | 7.6 | 12.2 |
| フレームの CPU 時間 P99（ms） | 16.8 | 16.8 | 10.4 | 30.4 |
| 締め切り超過 P99（ms、正なら遅延） | +6.6 | +6.6 | -1.0 | +21.3 |
| 遅延したフレームの割合（15 回の全フレーム） | 2.7% | 2.7% | 0.4% | 24.5% |
| 遅延したフレームの割合（周ごと） | 2.2 / 3.1 / 2.7% | 1.8 / 3.5 / 2.6% | 0.8 / 0.2 / 0.0% | 23.7 / 22.7 / 27.1% |
| 1 回あたりのフレーム数（中央値） | 89 | 91 | 90 | 83 |

- **Vico の遅さは条件を揃えても変わらない**：遅延したフレームは 3 周とも 23〜27% で、1 回目の計測（22.1%）ともほぼ同じ。
  同じ操作で描けたフレーム数も 1 割ほど少ない。
- **3.2.0 と 4.0.1 は同じくらい**：既定の設定のままでは、4.0.1 の Kotlin 書き直しによる差はこの操作では見えなかった。
- **4.0.1 の間引き（`isDecimationEnabled = true`）は大きく効く**：遅延したフレームが 2.7% → 0.4% に減り、P99 も締め切り内に収まった。
  10,000 点を 300dp ほどの幅に描くので、同じ画素の列に重なる点がほとんどで、間引いても見た目はほぼ変わらない。
  大量データを扱うなら、4.0.1 に上げて間引きを on にするのがいちばん効果がある。

### 測り直すときの注意
- 計測の合間に端末を冷ます（充電を外す、十分な時間を置く）。今回は温度が揃わなかった。
- 気になる差（大量データ）は、回数を増やす・順番を入れ替える・4.0.1 の間引きを on にした場合も測る。
