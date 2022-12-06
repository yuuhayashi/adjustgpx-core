package osm.jp.gpx;

import java.util.Date;
import java.util.TreeMap;

/**
 * GPXファイルをパースする
 * @param gpxFile
 * @code{
 * <gpx>
 *   <trk>
 *     <trkseg>
 *       <trkpt lat="35.32123832" lon="139.56965631">
 *         <ele>47.20000076293945</ele>
 *         <time>2012-06-15T03:00:29Z</time>
 *         <hdop>0.5</hdop>
 *       </trkpt>
 *     </trkseg>
 *   </trk>
 * </gpx>
 * }
 */
@SuppressWarnings("serial")
public class ElementMapTRKSEG extends TreeMap<Date, ElementMapTRKPT> {
    public ElementMapTRKSEG() {
        super(new TimeComparator());
    }

    /**
     * 拡張put value:ElementMapTRKPTをputするとElementMapTRKPT内の最初のエントリのtimeを読み取ってkeyとしてthis.put(key,value)する。
     * @param value 
     * @throws DOMException 
     */
    public void put(ElementMapTRKPT value) {
        for (Date key : value.keySet()) {
            this.put(key, value);
            return;
        }
    }
	
    public void printinfo() {
    	ElementMapTRKPT.printheader();
        for (java.util.Map.Entry<Date, ElementMapTRKPT> map : this.entrySet()) {
            ElementMapTRKPT mapTRKPT = map.getValue();
            mapTRKPT.printinfo();
        }
        ElementMapTRKPT.printfooter();
    }
}
