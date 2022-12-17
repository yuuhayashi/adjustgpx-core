package osm.jp.gpx;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @code{
 * ＜trkpt lat="35.32123832" lon="139.56965631">
 *		<ele>47.20000076293945</ele>
 *		<time>2012-06-15T03:00:29Z</time>
 *		<magvar></magvar>
 *		<speed></speed>
 *	＜/trkpt>
 * }
 *
 */
public class TagTrkpt implements Cloneable {
    public Double lat = null;
    public Double lon = null;
    public String eleStr = null;
    public Date time = null;
    public String magvarStr = null;
    private String speedStr = null;

    public TagTrkpt(Double lat, Double lon) {
    	this.lat = lat;
    	this.lon = lon;
    }

    @Override
	public TagTrkpt clone() { //基本的にはpublic修飾子を付け、自分自身の型を返り値とする
    	TagTrkpt b = null;
		
		// ObjectクラスのcloneメソッドはCloneNotSupportedExceptionを投げる可能性があるので、try-catch文で記述(呼び出し元に投げても良い)
		try {
			//親クラスのcloneメソッドを呼び出す(親クラスの型で返ってくるので、自分自身の型でのキャストを忘れないようにする)
			b =(TagTrkpt)super.clone();
			//親クラスのcloneメソッドで深いコピー(複製先のクラス型変数と複製元のクラス型変数で指しているインスタンスの中身が違うコピー)がなされていないクラス型変数をその変数のcloneメソッドで複製し、複製先のクラス型変数に代入
			b.lat = this.lat;
			b.lon = this.lon;
			b.eleStr = (this.eleStr == null ? null : this.eleStr.toString());
			b.time = (Date) this.time.clone();
			b.magvarStr = (this.magvarStr==null ? null : this.magvarStr.toString());
			b.speedStr = (this.speedStr == null ? null : this.speedStr.toString());
		} catch (Exception e){
			e.printStackTrace();
		}
		return b;
	}
    
    public void setEle(String ele) {
    	this.eleStr = ele;
    }
    
    public void setTime(Date time) {
    	this.time = time;
    }
    
    public Date getTime() {
    	return this.time;
    }
    
    public void setMagvar(String magvar) {
    	this.magvarStr = magvar;
    }
    
    public void setSpeed(double speed) {
    	this.speedStr = rounding(1, speed);
    }
    
    public void setSpeed(String speed) {
    	try {
    		Double.valueOf(speed);
    		this.speedStr = speed;
    	}
    	catch (Exception e) {
    		clearSpeed();
    	}
    }
    
    public void clearSpeed() {
    	this.speedStr = null;
    }
    
    public String getSpeed() {
    	return this.speedStr;
    }
    
    public String toString() {
    	String ret = "<trkpt";
    	if (lat != null) {
    		ret += " lat="+ lat;
    	}
    	if (lon != null) {
    		ret += " lon="+ lon;
    	}
    	if (eleStr != null) {
    		ret += " ele="+ eleStr;
    	}
    	if (time != null) {
    		ret += " time="+ time;
    	}
    	if (this.magvarStr != null) {
    		ret += " magvar="+ magvarStr;
    	}
    	if (this.speedStr != null) {
    		ret += " speed="+ speedStr;
    	}
    	ret += ">";
    	System.out.println(ret);
    	return ret;
    }
    
    
    /**
     * 小数点以下scale桁の数値に丸めた値を返す
     * @param scale
     * @param str
     * @return
     */
    static String rounding(int scale, double d) {
    	try {
    		BigDecimal bd = BigDecimal.valueOf(d);
    		BigDecimal bd1 = bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
    		String str = bd1.toString();
    		return str;
    	}
    	catch(Exception e) {
    		return null;
    	}
    }
}
