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

## 計測メモ（途中の値）
- release APK（R8 あり、複合グラフだけ実装した時点）：土台だけで約 978KB、そこからの増加は 3.2.0 が約 +50KB、4.0.1 が約 +65KB、Vico が約 +98KB。
  全部のグラフを実装してから測り直す。
