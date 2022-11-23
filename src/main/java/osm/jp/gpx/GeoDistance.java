package osm.jp.gpx;

/**
 * The MIT License (MIT)
 * Copyright(C) 2007-2012   やまだらけ
 * http://yamadarake.jp/trdi/report000001.html
 * 「Cords.java」を改変
 *   2016-10-03
 * 
 * @author やまだらけ yama_darake@yahoo.co.jp
 *
 */
public class GeoDistance {

  public static final double GRS80_A = 6378137.000;				// 赤道半径（ｍ）
  public static final double GRS80_E2 = 0.00669438002301188;
  public static final double GRS80_MNUM = 6335439.32708317;		//

  public static final double WGS84_A = 6378137.000;
  public static final double WGS84_E2 = 0.00669437999019758;
  public static final double WGS84_MNUM = 6335439.32729246;

  /**
   *  角度（180度）をラジアン(2π）に変換する
   * @param deg
   * @return
   */
  public static double deg2rad(double deg){
    return deg * Math.PI / 180.0;
  }

  /**
   * 距離(m)を返す 
   * @param lat1
   * @param lng1
   * @param lat2
   * @param lng2
   * @return
   */
  public static double calcDistHubeny(double lat1, double lng1,
                                      double lat2, double lng2){
    double my = deg2rad((lat1 + lat2) / 2.0);	// 平均緯度
    double dy = deg2rad(lat1 - lat2);			// ２点間の緯度
    double dx = deg2rad(lng1 - lng2);			// ２点間の経度

    double sin = Math.sin(my);
    double w = Math.sqrt(1.0 - GRS80_E2 * sin * sin);
    double m = GRS80_MNUM / (w * w * w);
    double n = GRS80_A / w;
	
    double dym = dy * m;
    double dxncos = dx * n * Math.cos(my);

    return Math.sqrt(dym * dym + dxncos * dxncos);
  }


  public static void main(String[] args){
    System.out.println("Coords Test Program");
    double lat1, lng1, lat2, lng2;

    lat1 = Double.parseDouble(args[0]);
    lng1 = Double.parseDouble(args[1]);
    lat2 = Double.parseDouble(args[2]);
    lng2 = Double.parseDouble(args[3]);

    double d = calcDistHubeny(lat1, lng1, lat2, lng2);

    System.out.println("Distance = " + d + " m");
  }
}