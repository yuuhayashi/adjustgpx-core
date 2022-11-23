[ AdjustTerra ]

GPSログファイル(GPX)を元にして写真へ「位置情報(緯度経度)」と「方向」を追記します。(EXIF更新)

[概要]
GPSログの記録時刻とデジカメの撮影時刻とを見比べて、GPSログ内に写真へのリンク情報を付加した新しいGPSログファイルを作成します。

 ※ 対象とする画像ファイルは'*.jpg'のみです。
 ※ GPSログの形式は「GPX」形式に対応しています。
 * 画像ファイルの撮影日時をファイルの更新日時／EXIF撮影日時から選択することができます。
 	- ファイル更新日時： 高速処理が可能です。
 			一部のトイカメラ系のデジカメにはEXIF情報が正しく付加されないものがあります。そのような機種におすすめです。
 	- EXIF撮影日時: ファイル更新日時が利用できない場合はこちらを使ってください。
 			iPadなど直接ファイルを扱えないデバイスの場合はファイル更新日時が使えません。
 			うっかりファイルをコピーしてしまった場合は、ファイル更新日時が撮影日時を意味しなくなります。その時もEXIFにしてください。
 * 画像の精確な撮影時刻を入力することでGPSログとの時差を自動補正します。
 * 結果は、取り込み元のGPXファイルとは別に、元ファイル名にアンダーバー「_」を付加した.ファイルに出力します。
 	- SPEED(速度): 出力GPXに<speed>タグを付加することができます。
 	- MAGVAR(方向): 'MAGVAR'とは磁気方位のことです。直前のポイントとの２点間の位置関係を'MAGVAR'として出力できます。
 	- 出力先のGPXに写真へのリンク情報を付加する／付加しないを選択可能にしました。
 		[☑ 出力GPXにポイントマーカー<WPT>を書き出す]
 * 画像にEXIF情報を付加することができます。
 	- 緯度経度: GPSログから算出した緯度・経度情報をEXIFに書き出すことができます。
 	- 撮影方向: GPSログから移動方向を擬似撮影方向としてEXIFに書き出すことができます。（カメラの向きではありません）

http://sourceforge.jp/projects/importpicture/wiki/FrontPage

[起動]
下記のように'AdjustTerra'を起動するとGUIでパラメータを逐次設定可能です。（推奨起動方法）

> java -cp AdjustTerra.jar osm.jp.gpx.matchtime.gui.AdjustTerra


下記のコマンドラインによる起動方式は度重なる機能追加によりパラメーターが増大したため複雑になりすぎ作者でさえわけがわからなくなりました。
一応、過去の起動方法を記載しておきます。しかし、コマンドラインからの引数は2016-10-03版以降は正しく引き継がれません。
GUI版の'AdjustTerra.jar'を使ってください。

> java -jar importPicture.jar <outputfile> <targetDir> <time base image> <time> <gpx>

(パラメータ)
	 argv[0] = 画像リストの出力ファイル
	 argv[1] = 画像ファイルが格納されているディレクトリ
	 argv[2] = 時刻補正の基準とする画像ファイル
	 argv[3] = 基準画像ファイルの精確な撮影日時 "yyyy-mm-dd'T'HH:MM:ss"
	 argv[4] = 撮影位置をロギングしたGPXファイル	(省略可能：省略した場合は指定された画像ディレクトリ内のGPXファイルを対象とする（複数可能）)

exp)

> java -jar importPicture.jar list.csv . IMG_01234.JPG 2012-06-15T12:52:22 鎌倉宮_2012-06-15_12-00-16.gpx


[ GUIバージョン ]
撮影した画像を確認しながらパラメータを設定することができます。
また、補正した撮影時刻と位置情報を画像ファイルのEXIFに書き込むことも可能です。
EXIFへの書き込みには別途「Apache commons imaging」ライブラリが必要です。
commons_imaging ライブラリは下記から入手してください。
（version 1.0 以降が必要です）

-------------------------------------------------------------------

[Restamp]

動画から一定間隔で切り出したIMAGEのファイル更新日時を書き換える

 ・画像ファイルの更新日付を書き換えます。(Exi情報は無視します)
    ※ 指定されたディレクトリ内のすべての'*.jpg'ファイルを処理の対象とします

 ・画像は連番形式（名前順に並べられること）の名称となっていること

 ・一定の間隔（等間隔）で撮影された画像ファイルの中から２つの画像の撮影時刻を
 　入力することで、２つの画像の間の画像数から撮影間隔を割り出し、時刻を指定され
 　ていない残りの画像も含めて、画像ファイルの「ファイル更新日時」を書き換えます。
 
## パラメータ
 ・対象のフォルダ（ディレクトリ内のすべての'*.jpg'ファイルを処理の対象とします）
 ・基準となる画像（２つ）
 ・基準画像の正しい日時（２つ）

> java -cp ReStamp.jar osm.jp.gpx.Restamp <imgDir> <baseFile A> <baseTime A> <baseFile B> <baseTime B>

 argv[0] = 画像ファイルが格納されているディレクトリ		--> imgDir
 argv[1] = 時刻補正の基準とする画像ファイル			--> baseFile A
 argv[2] = 基準画像ファイルの精確な撮影日時 "yyyy-MM-dd_HH:mm:ss" --> baseTime A
 argv[3] = 時刻補正の基準とする画像ファイル			--> baseFile B
 argv[4] = 基準画像ファイルの精確な撮影日時 "yyyy-MM-dd_HH:mm:ss" --> baseTime B

## 使い方 
 1. 予め、動画から画像を切り出す
 　　ソースファイル（mp4ファイル）; 「-i 20160427_104154.mp4」
     出力先: 「-f image2 img/%06d.jpg」 imgフォルダに６桁の連番ファイルを差出力する
 　　切り出し開始秒数→ 「-ss 0」 （ファイルの０秒から切り出し開始）
 　　切り出し間隔； 「-r 30」 (１秒間隔=３０fps間隔)
 ```
 $ cd /home/yuu/Desktop/OSM/20180325_横浜新道
 $ ffmpeg -ss 0  -i 20160427_104154.mp4 -f image2 -r 15 img/%06d.jpg
 ```
 
 2. ファイルの更新日付を書き換える
 ```
 $ cd /home/yuu/Desktop/workspace/AdjustTime/importPicture/dist
 $ java -cp .:AdjustTime2.jar osm.jp.gpx.Restamp /home/yuu/Desktop/OSM/20180325_横浜新道/img 000033.jpg 2018-03-25_12:20:32 003600.jpg  2018-03-25_13:20:09
 ```


-------------------------------------------------------------------
AdjustTerra.jar
Copyright (c) 2014,2020 Yuu Hayashi
This software is released under the MIT License, see LICENSE.txt.

----------------
About 'commons-imaging-1.0-SNAPSHOT.jar'
'commons-imaging-1.0-SNAPSHOT.jar' is the work that is distributed in the Apache License 2.0

----------------
osm.jp.gpx.GeoDistance.java は'やまだらけ'様の著作物です。
	Copyright (C) 2007-2012   やまだらけ
	The MIT License (MIT)
	参照元: http://yamadarake.jp/trdi/report000001.html

	