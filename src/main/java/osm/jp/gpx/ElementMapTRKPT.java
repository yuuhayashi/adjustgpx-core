package osm.jp.gpx;

import java.text.ParseException;
import java.util.Date;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class ElementMapTRKPT extends TreeMap<Date, TagTrkpt> {
    public static final long DIFF_MAE_TIME = 3000L;	// before 3 secound
    AppParameters params;

    public ElementMapTRKPT(AppParameters params) {
        super(new TimeComparator());
        this.params = params;
    }

    /**
     * 拡張put value:ElementをputするとElement内のtimeを読み取ってkeyとしてthis.put(key,value)する。
     * @param tag
     * @return 
     * @throws java.text.ParseException
     * @code{
     * <trkpt lat="36.4260153752" lon="138.0117778201">
     *   <ele>614.90</ele>
     *   <time>2017-05-21T23:02:16Z</time>
     *   <hdop>0.5</hdop>
     * </trkpt>
     * }
     * @return	keyとして登録したtime:Date
     */
    public Date put(TagTrkpt tag) {
        this.put(tag.getTime(), tag);
    	return tag.getTime();
    }

    /**
     * [map]から指定した時刻の<trkpt>エレメントを取り出す。
     * 取り出すエレメントは、指定した時刻と同一時刻、もしくは、最直後の時刻のエレメントとする。
     * 指定した時刻以前のエレメントが存在しない場合は null を返す。
     * 
     * @param jptime
     * @return	<trkpt>エレメント。対象のエレメントが存在しなかった場合には null。
     * @throws ParseException
     */
    public TagTrkpt getValue(Date jptime) throws ParseException {
		try {
	    	Date keyTime = null;
	    	for (Date key : this.keySet()) {
                int flag = jptime.compareTo(key);
                if (flag < 0) {
                	// key以前にjptimeがある
                    return getValue(keyTime, key, jptime);
                }
                else if (flag == 0) {
                	// keyとjptimeは同時刻
                    keyTime = new Date(key.getTime());
                    return getValue(keyTime, key, jptime);
                }
                else if (flag > 0) {
                	// key以後にjptimeがある
                    keyTime = new Date(key.getTime());
                }
	        }
	        return null;
		}
		catch (Exception e) {
			throw new ParseException(e.toString(), 0);
		}
    }
    
    /**
     * 
     * @param maeTime
     * @param atoTime
     * @param imaTime
     * @return
     * @throws ParseException
     */
    private TagTrkpt getValue(Date maeTime, Date atoTime, Date imaTime) throws ParseException {
        if (maeTime != null) {
        	TagTrkpt mae = this.get(maeTime);
        	TagTrkpt ato = this.get(atoTime);
        	Complementation comp = new Complementation(ato, mae);

            // <MAGVAR>がなければ、
            // 直前の位置と、現在地から進行方向を求める
        	// 経度(longitude)と経度から進行方向を求める
            if (params.isGpxOverwriteMagvar()) {
                comp.complementationMagvar();
            }

            // 緯度・経度と時間差から速度(km/h)を求める
            if (params.isGpxOutputSpeed()) {
                comp.complementationSpeed();
            }
            
            TagTrkpt ima = (TagTrkpt)(comp.imaTag).clone();
            ima.setTime(imaTime);
        	double per = imaTime.getTime() - maeTime.getTime();
        	if ((atoTime.getTime() - maeTime.getTime()) != 0) {
        		per = per / (atoTime.getTime() - maeTime.getTime());
        	}
        	GeoPoint fPoint = (new GeoPoint()).set(ato.lat, ato.lon);
        	GeoPoint point = (new GeoPoint()).set(mae.lat, mae.lon).getPer(fPoint, per);
        	ima.lat = point.lat;
        	ima.lon = point.lng;
        	return ima;
        }
        return this.get(imaTime);
    }
    
    public void printinfo() {
    	Date firstTime = null;
    	Date lastTime = null;
        for (Date key : this.keySet()) {
            if (firstTime == null) {
                firstTime = new Date(key.getTime());
            }
            lastTime = new Date(key.getTime());
        }
        System.out.println(String.format("|                      <trkseg/> |%20s|%20s|", ImportPicture.toUTCString(firstTime), ImportPicture.toUTCString(lastTime)));
    }
}
