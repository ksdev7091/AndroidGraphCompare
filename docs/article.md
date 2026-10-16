# MPAndroidChart 3.2.0 / 4.0.1（Compose）/ Vico を、同じグラフで比べてみた

> 下書きです。数値とスクリーンショットはリポジトリの `docs/` にあるものを使っています。

## はじめに

Android のグラフライブラリといえば長く MPAndroidChart が定番でしたが、2026 年の夏から秋にかけて大きな動きがありました。

- **MPAndroidChart 4.0**（9 月）が出ました。Kotlin で書き直され、Jetpack Compose 用のモジュール（MPChartCompose）が付きました。3.1.0 以来の安定版で、3.x 系の最終版として **3.2.0** も同時に出ています
- Compose 製のグラフライブラリ **Vico** は 3.3（8 月）で「Compose が主」の方針になり、View 版は重大な修正だけになりました。3.x では円グラフも加わっています

Compose で新しく作るなら、どれを選べばよいのでしょうか。View の 3.x から上げるなら、4.0 と Vico のどちらがよいのでしょうか。
これを確かめるため、**同じグラフを 3 つの書き方で実装し、コード・見た目・性能を比べました**。

| | ライブラリ | 書き方 |
|---|---|---|
| A | MPAndroidChart 3.2.0 | View を `AndroidView` で包む |
| B | MPAndroidChart 4.0.1 + MPChartCompose | 4.x の chart composable |
| C | Vico 3.3.1 | Compose |

コードはすべて GitHub に置いています：https://github.com/winschneid/AndroidGraphCompare

## 比べ方

### 作ったグラフ

| 画面 | 内容 | 主に比べること |
|---|---|---|
| 円 / ドーナツ | 中央の文字、割合、タップでスライスを強調 | 機能の有無 |
| 棒 | グループ / 積み上げの切り替え、角丸 | データの持ち方、切り替え |
| 折れ線 | 2 系列、グラデーションのエリア塗り、曲線 | 見た目の作り方 |
| 複合 | 棒（左軸）+ 折れ線（右軸）、タップでマーカーと文字 | マーカー、選択状態の受け取り方 |
| 大量データ | 10,000 点をピンチズーム・スクロール | 描画の性能 |
| データ更新 | ボタンで 12 か月分を差し替えてアニメーション | アニメーション |
| LazyColumn | 30 個の小さな折れ線を並べてスクロール | リストの中での性能 |

### 見た目を揃える

既定の見た目のまま比べると、どれが速いか・どれが書きやすいかより、既定値の違いばかりが目立ちます。
そこで、軸の範囲と目盛り、y 軸の幅、棒の太さ、凡例の形と位置、色、文字の大きさを 3 実装で揃えました。
実は**この「揃える」作業でいちばんライブラリの違いが見えた**ので、あとで詳しく書きます。

### 1 つのアプリで 3 つを比べる

MPAndroidChart 3.x と 4.x はパッケージ名（`com.github.mikephil.charting`）もクラス名も同じなので、1 つの APK には入れられません。
そこで product flavor を 3 つ（`mp3` / `mp4` / `vico`）作り、flavor ごとに依存と applicationId を分けました。

```kotlin
productFlavors {
    create("mp3") { dimension = "chart"; applicationIdSuffix = ".mp3" }
    create("mp4") { dimension = "chart"; applicationIdSuffix = ".mp4" }
    create("vico") { dimension = "chart"; applicationIdSuffix = ".vico" }
}
dependencies {
    "mp3Implementation"(libs.mpandroidchart3)
    "mp4Implementation"(libs.mpandroidchart4.lib)
    "mp4Implementation"(libs.mpandroidchart4.compose)
    "vicoImplementation"(libs.vico.compose)
}
```

データ（固定シードで生成）、見た目の基準（`ChartSpec`）、書式は `core` モジュールにまとめ、3 実装から同じものを使っています。

## 導入

| | 3.2.0 | 4.0.1 | Vico 3.3.1 |
|---|---|---|---|
| 配布 | JitPack | JitPack | Maven Central |
| minSdk | 14 | 23 | （今回は 24 で統一） |
| compileSdk | - | 35（Compose は 37） | - |
| release APK（全 7 画面） | 1,080 KB | 1,111 KB | 1,143 KB |

APK サイズの差は 60KB ほどで、選ぶ理由になるほどではありませんでした。

## グラフ別の比較

### 複合グラフ（棒 + 折れ線 + マーカー）

| 3.2.0 | 4.0.1 | Vico |
|---|---|---|
| ![](images/combined-mp3-light.png) | ![](images/combined-mp4-light.png) | ![](images/combined-vico-light.png) |

いちばん書き方の差が出たのがこのグラフです。

**マーカー**

- 3.2.0：XML レイアウトを用意し、`MarkerView` を継承して `refreshContent` で文字を入れます
- 4.0.1：マーカーが **Composable** になりました。`marker = { entry, _ -> MarkerBubble(...) }` と書くだけです
- Vico：マーカーは Canvas に描く `TextComponent` なので、Composable は置けません。文字は `DefaultCartesianMarker.ValueFormatter` で作ります

```kotlin
// 4.0.1：マーカーも選択状態も Compose の中で完結する
val state = rememberChartState()
CombinedChart(
    data = data,
    state = state,
    marker = { entry, _ -> (entry.data as? CombinedPoint)?.let { MarkerBubble(ChartFormat.markerText(it)) } },
    setup = { configure() },
)
Text("選択中: ${(state.selectedEntry?.data as? CombinedPoint)?.let(ChartFormat::markerText)}")
```

**選択状態の受け取り方**

- 3.2.0：`OnChartValueSelectedListener` から Compose の state に書き戻します
- 4.0.1：`ChartState.selectedEntry` を読むだけです
- Vico：`CartesianMarkerVisibilityListener` の `onShown` / `onHidden` で受け取ります

**元データの持たせ方**

4.0.1 の `Entry` は型付きの payload（`Entry<CombinedPoint>`）を持てます。3.2.0 も `data` に入れられますが `Object` なのでキャストが要ります。
Vico は数値しか渡せないので、マーカーでは x 座標から元のデータを引き直しました。

**タップしたあと**

MPAndroidChart はタップするとマーカーが出たままになりますが、Vico の既定は「押している間だけ」です。
`markerController = CartesianMarkerController.rememberToggleOnTap()` で揃えました。

#### 見た目を揃えるのに必要だったこと

| 揃えたいこと | MPAndroidChart | Vico |
|---|---|---|
| 12 か月を画面に収める | 何もしなくてよい | x の間隔が dp なので、はみ出した分はスクロールになる。`Zoom.Content` で全体が収まる倍率にする |
| 補助線を 1 組にする | 右軸の補助線を消す | 既定では全部の軸が点線の補助線を引く。右軸と x 軸の補助線を消し、左軸を実線にする |
| 軸の範囲と目盛り | 軸に `axisMinimum` / `axisMaximum`、目盛りは数で指定 | 範囲は**レイヤー**に `CartesianLayerRangeProvider`、目盛りは**間隔**で指定 |
| 棒の太さ | x の 1 区間に対する割合（`barWidth = 0.6`） | 太さと間隔を dp で指定して、比率から換算する |
| 描画領域の左右の端 | y 軸の幅を `minWidth` / `maxWidth` で固定 | `BaseAxis.Size.Fixed` で固定 |
| 凡例 | 既定で出る。描画領域の左端から並ぶ | 自分で並べる。Canvas の左端から並ぶので、y 軸の幅だけずらす |

特にハマったのが棒の太さです。比率を揃えたつもりでも Vico の棒だけ細いままでした。
原因は、**折れ線レイヤーの x 間隔が「点の大きさ + `pointSpacing`」で決まり、棒のレイヤーより広くなっていた**ことです。
全体がそちらに合わせて縮んでいました。さらに、x 軸の端のラベル用の余白（`addExtremeLabelPadding`）も切って、ようやく揃いました。

| 3.2.0 / 4.0.1 | Vico（調整前） | Vico（調整後） |
|---|---|---|
| 棒 42px、間隔 72px（0.58） | 棒 32px、間隔 70px（0.45） | 棒 42px、間隔 70px（0.60） |

残った 3% の差は、Vico が折れ線の点が切れないよう、点の半分を拡大縮小しない余白として必ず取るためで、設定では消せませんでした。

### 円グラフ

| 3.2.0 | 4.0.1 | Vico |
|---|---|---|
| ![](images/pie-mp3-light.png) | ![](images/pie-mp4-light.png) | ![](images/pie-vico-light.png) |

MPAndroidChart は、中央の文字・割合の計算・タップでのスライスの強調が**設定だけ**でできます。
Vico の PieChart にはどれもないので、Compose で作りました。

- 中央の文字：`PieChartHost` の上に `Text` を重ねる
- タップ：`pointerInput` でタップ位置の角度と中心からの距離を求め、どのスライスかを計算する。選んだスライスは `offsetFromCenter` で外へずらす

Compose なので重ねるのは簡単ですが、タップの判定まで自前で書くのは手間です。

もう 1 つの違いは**選択したときの円の大きさ**です。MPAndroidChart は最初から「ずらす量」の分だけ円を小さくしておくので、選択しても大きさは変わりません。
Vico は「指定した半径 − 一番大きいずらし量」で描くので、**スライスを選ぶたびに円全体が縮みます**。選択中だけ直径を大きく指定して揃えました。

なお、選択時にずらす量の既定値は 3.2.0 が 18（px のまま）、4.0.1 が 9dp と違います。

### 棒グラフ（グループ / 積み上げ）

| 3.2.0 | 4.0.1 | Vico |
|---|---|---|
| ![](images/bar-stacked-mp3-light.png) | ![](images/bar-stacked-mp4-light.png) | ![](images/bar-stacked-vico-light.png) |

- **データの持ち方**：MPAndroidChart はグループなら系列ごとに `BarDataSet` を作って `groupBars`、積み上げなら値を配列で持つ `BarEntry` 1 つ、と**表示方法でデータの形が変わります**。
  Vico はデータは同じで、`MergeMode`（`Grouped` / `Stacked`）を変えるだけです
- **角丸**：3.2.0 には設定がありません。4.0.1 は `barCornerRadius` で、グループは 4 隅、積み上げは一番上の区画の上側だけを丸めます。
  Vico は棒の形を Compose の `Shape` で渡しますが、区画ごとに当てるので、そのままだと積み上げの途中の区画まで丸くなります
- **切り替えの落とし穴（Vico）**：`MergeMode` と範囲を変えても、データが前回と同じだと y の範囲が計算し直されず、積み上げが 0〜100 の範囲に描かれて上が切れました。
  範囲は transaction を流したときに計算されるためです。表示方法を `ExtraStore` に入れて transaction を流し直すと直りました

### 折れ線

| 3.2.0 | 4.0.1 | Vico |
|---|---|---|
| ![](images/line-mp3-light.png) | ![](images/line-mp4-light.png) | ![](images/line-vico-light.png) |

- **グラデーション**：MPAndroidChart は 3.x も 4.x も `fillDrawable` に `GradientDrawable` を渡します（View の API のまま）。Vico は Compose の `Brush.verticalGradient` をそのまま使えます
- **曲線**：Vico の `Interpolator.cubic()` は点の間で角が残り、直線に近く見えました。MPAndroidChart の `CUBIC_BEZIER` に近いのは `catmullRom()` でした
- **端のラベル**：MPAndroidChart は左端の「1日」を左にはみ出して描きますが、Vico は描けません。ラベルの最大幅が「端までの距離の 2 倍」に制限されるためです

### ダークテーマ

| 3.2.0 | 4.0.1 | Vico |
|---|---|---|
| ![](images/combined-mp3-dark.png) | ![](images/combined-mp4-dark.png) | ![](images/combined-vico-dark.png) |

MPAndroidChart は中身が View なので、Compose の `MaterialTheme` の色は自動では反映されません。文字や補助線の色を `update` で塗り直しています（4.0.1 も同じ）。
Vico は Compose の色をそのまま渡せます。

## 操作とアニメーション

### データ更新アニメーション

動画：[3.2.0](videos/animation-mp3.mp4) / [4.0.1](videos/animation-mp4.mp4) / [Vico](videos/animation-vico.mp4)

| | 前の値から新しい値へ動かせるか | 書き方 |
|---|---|---|
| 3.2.0 | ✕ | 差し替えたあと `animateY` で 0 から伸ばし直す |
| 4.0.1 | ○ | chart composable に `animateChanges = true`（300ms 固定）。View なら `animateDataChange(data, ms)` |
| Vico | ○ | ModelProducer に新しいデータを流すだけ。`animationSpec` で時間と動き方を変えられる |

3.2.0 の「0 から伸び直す」動きは、データの変化が見づらくなります。4.0 で一番うれしい変化の 1 つです。

### LazyColumn

- MPAndroidChart は 3.2.0 も 4.0.1 も、行ごとに View を作ります（4.0.1 の chart composable も中身は `AndroidView`）。チャートがタッチを奪わないよう `isTouchEnabled = false` にしました
- Vico は Compose で描くので View は作りません。データを更新しないなら、ModelProducer を使わずに `CartesianChartModel` を直接渡せます

## 性能

Macrobenchmark で、release 相当のビルドを実機（POCO 26077PC53G、Android 16）で計測しました。

### 起動・画面表示

3 実装の差は**ばらつきの範囲**でした（どれも 120〜180ms ほどで、範囲が大きく重なる）。

### 大量データ（10,000 点のズームとスクロール）

いちばん差が出たのがここです。充電を外し、電池の温度を 35℃ に揃え、4 つの条件を 3 周交互に回して各 15 回測りました。

| | 3.2.0 | 4.0.1 | 4.0.1 + 間引き | Vico 3.3.1 |
|---|---:|---:|---:|---:|
| フレームの CPU 時間 P50（ms） | 6.5 | 6.0 | 6.0 | 8.4 |
| フレームの CPU 時間 P99（ms） | 16.8 | 16.8 | 10.4 | 30.4 |
| 締め切り超過 P99（ms、正なら遅延） | +6.6 | +6.6 | −1.0 | +21.3 |
| **遅延したフレームの割合** | 2.7% | 2.7% | **0.4%** | **24.5%** |

- Vico は 4 フレームに 1 フレームが遅延しました。3 周とも 23〜27% で安定していて、偶然ではありません
- 既定の設定では、3.2.0 と 4.0.1 に差はありませんでした
- 4.0.1 で**間引き（`isDecimationEnabled = true`）を on** にすると、遅延したフレームが 0.4% まで減りました。
  同じ画素の列に重なる点を間引く機能なので、10,000 点を 300dp ほどの幅に描くこのケースでは、見た目はほぼ変わりません

### データ更新アニメーションと LazyColumn

| | 3.2.0 | 4.0.1 | Vico |
|---|---:|---:|---:|
| データ更新：遅延したフレームの割合 | 0.1% | 0.1% | 1.6% |
| LazyColumn：遅延したフレームの割合 | 0.5% | 0.3% | 0.2% |

Vico はデータ更新のフレームの CPU 時間が MPAndroidChart の約 2 倍でしたが、遅延はわずかでした。LazyColumn は 3 実装とも問題ありませんでした。

### 計測の注意

- 1 台・各 5〜15 回の結果なので、数 % の差はばらつきの範囲として扱っています
- 3 実装とも事前コンパイルなし（`run-from-apk`、JIT のみ）の状態です
- 一部の端末では、USB 経由のインストールのたびに端末側の承認が要ります。承認されないと `INSTALL_FAILED_USER_RESTRICTED` で止まりますが、Gradle は BUILD SUCCESSFUL のまま終わるので注意してください
- `AndroidView` に直接付けた testTag は UiAutomator から見つかりませんでした。Compose の `Box` で包んでそこに付けています

## 実装量

空行・コメント・import を除いたコードの行数です。

| | 3.2.0 | 4.0.1 | Vico |
|---|---:|---:|---:|
| 円 | 49 | 42 | 76 |
| 棒 | 64 | 63 | 86 |
| 折れ線 | 49 | 50 | 58 |
| 複合 | 86 | 69 | 121 |
| 大量データ / データ更新 / LazyColumn | 139 | 130 | 162 |
| 共通のスタイル | 48 | 49 | 39 |
| XML（マーカー） | 14 | - | - |
| **合計** | **449** | **403** | **542** |

4.0.1 が一番短く書けました。Vico が長いのは、見た目を MPAndroidChart に揃えるためのコード（凡例を並べる、余白を dp で合わせる、円グラフのタップ判定）が多いためです。
Vico の既定の見た目で良ければ、もっと短くなります。

## 機能対応表

| | 3.2.0 | 4.0.1 | Vico 3.3.1 |
|---|:---:|:---:|:---:|
| 折れ線・棒・複合・ローソク足・円 | ○ | ○ | ○ |
| 横棒・散布図・バブル・レーダー | ○ | ○ | ✕ |
| 円の中央の文字・タップでの強調 | ○ | ○ | ✕（自前） |
| 棒の角丸 | ✕ | ○ | ○（Shape） |
| マーカー | View + XML | Composable | TextComponent |
| 凡例 | 自動 | 自動 | 手動 |
| 前の値からのアニメーション | ✕ | ○ | ○ |
| 大量データの間引き | ✕ | ○ | ✕ |
| 空状態・ローディングの表示 | 文字だけ | ○（新しい空状態、`isLoading`） | placeholder の枠 |
| Compose での使い方 | AndroidView で包む | chart composable（中身は View） | ネイティブ |
| Kotlin Multiplatform | ✕ | ✕ | ○ |

## 3.2.0 → 4.0.1 の移行

パッケージ名もクラス名も同じなので、import はほぼそのままで、主な書き換えは **getter / setter から Kotlin のプロパティへ**です。

```kotlin
// 3.2.0
xAxis.setDrawGridLines(false)
dataSet.setDrawValues(false)
axisLeft.setLabelCount(5, true)
dataSet.setCircleColor(color)

// 4.0.1
xAxis.isDrawGridLinesEnabled = false
dataSet.isDrawValuesEnabled = false
axisLeft.labelCount = 5; axisLeft.isForceLabelsEnabled = true
dataSet.circleColor = color
```

- 配列の引数はリストになりました（`drawOrder`、`stackLabels`、積み上げの `BarEntry`）
- `Entry` は型付きの payload を持てるようになりました（`Entry(x, y, data = point)`）
- **既定値が変わって見た目が変わる**ものがあります：軸線と補助線の太さ（1dp → 0.5dp）、円グラフの選択時にずらす量（18px → 9dp）
- Compose で使うなら、`AndroidView` の `factory` / `update` を chart composable の `setup` / `update` に移すだけで、設定の中身はほぼそのまま使えます

3.1.0 の `ValueFormatter` を使っていた場合は、3.2.0 の時点で `IAxisValueFormatter` に戻っています。4.0.1 も同じ書き方です。

## まとめ

| こんなとき | おすすめ |
|---|---|
| View の 3.x を使っている | **4.0.1 に上げる**。書き換えはプロパティ名が中心で、大量データなら間引きで速くなる |
| Compose で新しく作る・グラフは折れ線と棒が中心 | **Vico**。Compose らしく書け、アニメーションも自然。ただし大量データでは遅延が多かった |
| Compose で新しく作る・グラフの種類が多い（レーダー、散布図など）、大量データを扱う | **4.0.1**。中身は View だが、chart composable と Composable のマーカーで Compose から十分使いやすい |
| Kotlin Multiplatform で共有したい | **Vico** |

Compose ネイティブかどうかだけで選ぶと、Vico に寄りがちです。ただ実際に揃えて比べると、**4.0.1 は「中身は View」という点を除けば、Compose から十分使いやすく、機能も性能も強い**という結果になりました。
一方で Vico は、見た目を細かく作り込むほどコードが増え、大量データの描画には課題がありました。
