package osm.jp.gpx;

import java.text.ParseException;

public class GeoPoint implements Cloneable {
	public double lat;
	public double lng;
	
	public GeoPoint set(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
		return this;
	}
	
	public GeoPoint set(Double dlat, Double dlng) {
		set(dlat.doubleValue(), dlng.doubleValue());
		return this;
	}
	
    @Override
	public GeoPoint clone() { //基本的にはpublic修飾子を付け、自分自身の型を返り値とする
    	GeoPoint b = null;
		
		// ObjectクラスのcloneメソッドはCloneNotSupportedExceptionを投げる可能性があるので、try-catch文で記述(呼び出し元に投げても良い)
		try {
			//親クラスのcloneメソッドを呼び出す(親クラスの型で返ってくるので、自分自身の型でのキャストを忘れないようにする)
			b =(GeoPoint)super.clone();
			//親クラスのcloneメソッドで深いコピー(複製先のクラス型変数と複製元のクラス型変数で指しているインスタンスの中身が違うコピー)がなされていないクラス型変数をその変数のcloneメソッドで複製し、複製先のクラス型変数に代入
			b.lat = this.lat;
			b.lng = this.lng;
		} catch (Exception e){
			e.printStackTrace();
		}
		return b;
	}
    
	public boolean equals(GeoPoint obj) {
		if ((this.lat - obj.lat) >= 0.00000001d) {
			return false;
		}
		if ((this.lng - obj.lng) >= 0.00000001d) {
			return false;
		}
		return true;
	}
	
	public double getDistance(GeoPoint obj) {
		return GeoDistance.calcDistHubeny(this.lat, this.lng, obj.lat, obj.lng);
	}
	
	/**
	 * 経度(longitude)と経度から進行方向を求める
	 * @param pre
	 * @return	MAGVARの値。"0.0".."359.9", ２点間の移動がない場合はNULL
	 * @throws ParseException
	 */
    public String complementationMagvar(GeoPoint pre) throws ParseException {
        Double r = Math.cos(Math.toRadians((this.lat + pre.lat) / 2)) * Complementation.R;
        Double x = Math.toRadians(this.lng - pre.lng) * r;
        Double y = Math.toRadians(this.lat - pre.lat) * Complementation.R;
        double rad = Math.toDegrees(Math.atan2(y, x));
        
        if ((x == 0) && (y == 0)) {
        	return null;
        }
        
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

        return String.format("%.1f", rad);
    }

    /**
     * 緯度・経度と時間差から速度(km/h)を求める
     * 
     */
    public double complementationSpeed(GeoPoint obj, long time) {
    	return Math.abs(getDistance(obj) * 3600 / time);
    }
    
    /**
     * ここからobjへのper比率の中間点を取得する
     * @param obj
     * @param per
     * @return
     */
    public GeoPoint getPer(GeoPoint obj, double per) {
    	double lati = ((obj.lat - this.lat) * per) + this.lat;
    	double lngi = ((obj.lng - this.lng) * per) + this.lng;
    	return (new GeoPoint()).set(lati, lngi);
    }


}
