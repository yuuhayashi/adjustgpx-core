package osm.jp.gpx;

import java.text.ParseException;

public class Complementation {
    public static final Double R = (6378137D + 6356752.314D)/2D;	// 6367444.657m
    
    public TagTrkpt imaTag = null;
    public TagTrkpt maeTag = null;
    //public static boolean param_GpxOutputSpeed = false;
    //public static boolean param_GpxOverwriteMagvar = false;
    
    /**
     * @param imaE
     * @param maeE
     * @throws java.text.ParseException
     * @code{
     * 	<trkpt lat="34.976635" lon="138.466228">
     * 		<ele>267.291</ele>
     * 		<magvar>359</magvar>
     * 		<speed></speed>
     * 		<time>2016-07-02T08:25:18Z</time>
     * 	</trkpt>
     * }
     *
     *
     * @throws ParseException
     */
    public Complementation(TagTrkpt imaE, TagTrkpt maeE) throws ParseException {
        this.imaTag = imaE.clone();
        if (maeE != null) {
    		this.maeTag = maeE.clone();
        }
    }
    
    /**
     * 緯度・経度と時間差から速度(km/h)を求める
     * 
     */
    public void complementationSpeed() {
    	if (imaTag.speedStr != null)  {
            try {
            	Double.parseDouble(imaTag.speedStr);
            }
            catch (NumberFormatException e) {
                // 数字以外なら<speed>エレメントを削除する
                imaTag.speedStr = null;
            }
    	}
    	
    	if (imaTag.speedStr == null)  {
            double d = GeoDistance.calcDistHubeny(imaTag.lat, imaTag.lon, maeTag.lat, maeTag.lon);
            String str = Double.toString((d * 3600) / (imaTag.time.getTime() - maeTag.time.getTime()));
            int iDot = str.indexOf('.');
            if (iDot > 0) {
                str = str.substring(0, iDot+2);
            }
            imaTag.speedStr = str;
    	}
    }

    /**
     *  経度(longitude)と経度から進行方向を求める
     * @throws ParseException
     */
    public void complementationMagvar() throws ParseException {
    	if (imaTag.magvarStr != null) {
            try {
            	Double.parseDouble(imaTag.magvarStr);
            }
            catch (NumberFormatException e) {
                // 数字以外なら<magvar>エレメントを削除する
                imaTag.magvarStr = null;
            }
    	}
    	
    	if (imaTag.magvarStr == null) {
            Double r = Math.cos(Math.toRadians((imaTag.lat + maeTag.lat) / 2)) * R;
            Double x = Math.toRadians(imaTag.lon - maeTag.lon) * r;
            Double y = Math.toRadians(imaTag.lat - maeTag.lat) * R;
            double rad = Math.toDegrees(Math.atan2(y, x));
            
            if (y >= 0) {
                if (x >= 0) {
                    rad = 0 - (rad - 90);
                }
                else {
                    rad = 360 - (rad - 90);
                }
            }
            else {
                if (x >= 0) {
                    rad = 90 - rad;
                }
                else {
                    rad = 90 - rad;
                }
            }

            String str = Double.toString(rad);
            int iDot = str.indexOf('.');
            if (iDot > 0) {
                str = str.substring(0, iDot);
            }
            imaTag.magvarStr = str;
    	}
    }
}
