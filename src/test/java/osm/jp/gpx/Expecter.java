package osm.jp.gpx;

public class Expecter {
    String value;
    boolean expect;
    String timeStr;
    double latD;
    double lonD;
    String magvar;
    String speed;

    /**
     * コンストラクタ
     * @param value		[0] 画像ファイルの相対パス
     * @param expect	[1] 存在するかどうか
     * @param timeStr	[2] 撮影時刻
     * @param latD		[3] 緯度
     * @param lonD		[4] 経度
     * @param magvar	[5] 方向(0-359),設定されていないことを期待する場合にはnullとする
     * @param speed		[6] SPEED(km/h)
     */
    public Expecter(String value, boolean expect, String timeStr, double latD, double lonD, String magvar, String speed) {
        this.value = value;
        this.expect = expect;
        this.timeStr = timeStr;
        this.latD = latD;
        this.lonD = lonD;
        this.magvar = magvar;
        this.speed = speed;
    }

    public static String comparePosition(double b) {
        return String.format("%.4f", b);
    }

}
