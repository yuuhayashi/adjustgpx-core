package osm.jp.gpx;

import java.text.ParseException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

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
 * 
 */
public class GpxParser extends DefaultHandler {
	StringBuffer outSb;
	int segCnt = 0;
	int kptCnt = 0;
	boolean kpt = false;
	TagTrkpt tag = null;
	public ElementMapTRKPT trkpt;
	public ElementMapTRKSEG trkseg;
	
    public GpxParser(AppParameters params) {
        super();
        trkseg = new ElementMapTRKSEG();
        trkpt = new ElementMapTRKPT(params);
    }
    
	/**
     * ドキュメント開始
     */
    public void startDocument() {
        outSb = new StringBuffer();
    }
 
    /**
     * ドキュメント終了
     */
    public void endDocument() {
    }
    
    public void startElement(String uri,String localName, String qName, Attributes atts) {
		if(qName.equals("trkseg")){
			segCnt++;
			kptCnt = 0;
			if (trkpt.size() > 0) {
				trkpt.printinfo();
				trkseg.put((ElementMapTRKPT) trkpt.clone());
				trkpt.clear();
			}
		}
		if(qName.equals("trkpt")){
			kpt = true;
			kptCnt++;
			if (tag != null) {
				if (tag.getTime() != null) {
					trkpt.put(tag.clone());
				}
				tag = null;
			}

			Double lat = null;
		    Double lon = null;

			for (int i = 0; i < atts.getLength(); i++) {
				String aname = atts.getQName(i);
				if (aname.equals("lat")) {
					lat = Double.valueOf(atts.getValue(i));
				}
				if (aname.equals("lon")) {
					lon = Double.valueOf(atts.getValue(i));
				}
			}
			
			if ((lat != null) && (lon != null)) {
				tag = new TagTrkpt(lat, lon);
			}
		}
		if(qName.equals("ele")){
			outSb = new StringBuffer();
		}
		if(qName.equals("time")){
			outSb = new StringBuffer();
		}
		if(qName.equals("magvar")){
			outSb = new StringBuffer();
		}
		if(qName.equals("speed")){
			outSb = new StringBuffer();
		}
	}

    /**
     * 要素の終了タグ読み込み時に毎回呼ばれる
     */
    public void endElement(String uri,String localName,String qName) {
        if(qName.equals("trkseg")){
			if (trkpt.size() > 0) {
				trkseg.put((ElementMapTRKPT) trkpt.clone());
				trkpt.clear();
			}
        }
        if(qName.equals("trkpt")){
        	kpt = false;
			if (tag != null) {
				if (tag.getTime() != null) {
					trkpt.put(tag);
				}
				tag = null;
			}
        }
		if(qName.equals("ele")){
			tag.setEle(outSb.toString());
		}
		if(qName.equals("time")){
			try {
				tag.setTime(ImportPicture.toUTCDate(outSb.toString()));
			} catch (ParseException e) {}
		}
		if(qName.equals("magvar")){
			tag.setMagvar(outSb.toString());
		}
		if(qName.equals("speed")){
			tag.setSpeed(outSb.toString());
		}
		outSb = new StringBuffer();
    }

    /**
     * テキストデータ読み込み時に毎回呼ばれる
     */
    public void characters(char[] ch, int offset, int length) {
    	if (kpt) {
            outSb.append(new String(ch, offset, length));
    	}
    }
    
 }
