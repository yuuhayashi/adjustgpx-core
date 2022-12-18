# adjustgpx-core

GPSログファイル(GPX)を元にして写真へ「位置情報(緯度経度)」と「方向」を追記します。(EXIF更新)

## 概要

GPSログの記録時刻とデジカメの撮影時刻とを見比べて、GPSログ内に写真へのリンク情報を付加した新しいGPSログファイルを作成します。

- 対象とする画像ファイルは'*.jpg'のみです。
- GPSログの形式は「GPX」形式に対応しています。
- 画像ファイルの撮影日時をファイルの更新日時／EXIF撮影日時から選択することができます。
  - ファイル更新日時： 高速処理が可能です。
    - 一部のトイカメラ系のデジカメにはEXIF情報が正しく付加されないものがあります。そのような機種におすすめです。
  - EXIF撮影日時: ファイル更新日時が利用できない場合はこちらを使ってください。
    - iPadなど直接ファイルを扱えないデバイスの場合はファイル更新日時が使えません。
    - うっかりファイルをコピーしてしまった場合は、ファイル更新日時が撮影日時を意味しなくなります。その時もEXIFにしてください。
- 画像の精確な撮影時刻を入力することでGPSログとの時差を自動補正します。
- 結果は、取り込み元のGPXファイルとは別に、元ファイル名にアンダーバー「_」を付加した.ファイルに出力します。
  - SPEED(速度): 出力GPXに<speed>タグを付加することができます。
  - MAGVAR(方向): 'MAGVAR'とは磁気方位のことです。直前のポイントとの２点間の位置関係を'MAGVAR'として出力できます。
  - 出力先のGPXに写真へのリンク情報を付加する／付加しないを選択可能にしました。
    - [☑ 出力GPXにポイントマーカー<WPT>を書き出す]
- 画像にEXIF情報を付加することができます。
  - 緯度経度: GPSログから算出した緯度・経度情報をEXIFに書き出すことができます。
  - 撮影方向: GPSログから移動方向を擬似撮影方向としてEXIFに書き出すことができます。（カメラの向きではありません）

http://sourceforge.jp/projects/importpicture/wiki/FrontPage

## 起動

下記のように'AdjustGpx'を起動するとGUIでパラメータを逐次設定可能です。（推奨起動方法）

```
> java -cp adjustgpx-core.jar adjustgpx-core.ini
```

下記のコマンドラインによる起動方式は度重なる機能追加によりパラメーターが増大したため複雑になりすぎ作者でさえわけがわからなくなりました。

一応、過去の起動方法を記載しておきます。しかし、コマンドラインからの引数は2016-10-03版以降は正しく引き継がれません。

GUI版の'AdjustTerra.jar'を使ってください。

```
> java -jar adjustgpx-core.jar <parameter file>
```

| (パラメータ) |    |
| ----------- | --------------------------- |
| argv[0]     | パラメータファイル(adjustgpx-core.ini)  |

| argv[0]  | 画像リストの出力ファイル  |
| argv[1] | 画像ファイルが格納されているディレクトリ |
| argv[2] | 時刻補正の基準とする画像ファイル |
| argv[3] | 基準画像ファイルの精確な撮影日時 "yyyy-mm-dd'T'HH:MM:ss" |
| argv[4] | 撮影位置をロギングしたGPXファイル	(省略可能：省略した場合は指定された画像ディレクトリ内のGPXファイルを対象とする（複数可能）) |

```
exp) java -jar AdjustGpx.jar list.csv . IMG_01234.JPG 2012-06-15T12:52:22 鎌倉宮_2012-06-15_12-00-16.gpx
```

## パラメータファイル(adjustgpx-core.ini)

| key               | 設定例       | コメント    |
| ----------------- | ------------ | ------------------------------------------- |
| IMG.BASE_FILE     |              | 基準時刻画像(正確な撮影時刻が判明できる画像) |
| IMG.SOURCE_FOLDER | img          | 対象IMGフォルダ:(位置情報を付加したい画像ファイルが格納されているフォルダ) |
| IMG.OUTPUT_FOLDER | output       | 変換された画像ファイルを出力するフォルダ |
| IMG.TIME          |              | GPX: ファイル更新時刻 yyyy:MM:dd HH:mm:ss.SSS |
| GPX.SOURCE_FOLDER | gpx          | GPXファイルが格納されたフォルダ |
| GPX.BASETIME      | FILE_UPDATE  | 基準時刻をEXIF/ファイル更新時刻の何方にするか {FILE_UPDATE | EXIF_TIME}  |
| GPX.noFirstNode   | true         | GPX: <trkseg>セグメントの最初の１ノードは無視する。(最初のノードはゴミデータになっていることが多いため) |
| GPX.OVERWRITE_MAGVAR | true      | ソース画像のEXIFにあるMAGVARを無視して新たに書き換える |
| GPX.gpxSplit      | true         |  GPX: 時間的に間隔が開いたGPXログを別の<trkseg>セグメントに分割する。 {ON | OFF}  |
| IMG.OUTPUT_EXIF   | true         | [廃止] 出力IMG: EXIFを変換する    |
| IMG.OUTPUT        | true         | [廃止] IMG出力をする  {ON | OFF}    |
| GPX.OUTPUT_SPEED  | true         | 出力GPX: <SPEED>を上書き出力する {ON | OFF}    |
| GPX.OUTPUT_WPT    | true         | ?    |
| GPX.REUSE         | true         | ON にすると、"*_.GPX" ファイルもGPXの対象とする |
| IMG.SIMPLIFY_METERS | 0.0        | simplify distance (m)  |



## GUIバージョン

撮影した画像を確認しながらパラメータを設定することができます。

また、補正した撮影時刻と位置情報を画像ファイルのEXIFに書き込むことも可能です。

EXIFへの書き込みには別途「Apache commons imaging」ライブラリが必要です。

commons_imaging ライブラリは下記から入手してください。（version 1.0 以降が必要です）

- About 'commons-imaging-1.0-SNAPSHOT.jar'
  - 'commons-imaging-1.0-SNAPSHOT.jar' is the work that is distributed in the Apache License 2.0

## Repository

Source repository

| name           | url                                                      |
| -------------- | -------------------------------------------------------- |
| adjustgpx-core | [http://surveyor.mydns.jp/archiva/#artifact~haya4/osm.surveyor/adjustgpx-gui](http://surveyor.mydns.jp/archiva/#artifact~haya4/osm.surveyor/adjustgpx-gui) |
| osdn   | `yuuhayashi@git.osdn.net:/gitroot/importpicture/importpicture.git` |

Binary repository : `http://surveyor.mydns.jp/archiva/#artifact/osm.surveyor/adjustgpx-core/`

```
    <!-- http://surveyor.mydns.jp/archiva/#artifact/osm.surveyor/adjustgpx-core/ -->
    <dependency>
      <groupId>osm.surveyor</groupId>
      <artifactId>adjustgpx-core</artifactId>
      <type>jar</type>
    </dependency>
```
