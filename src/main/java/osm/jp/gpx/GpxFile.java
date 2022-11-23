package osm.jp.gpx;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@SuppressWarnings("serial")
public class GpxFile extends File {
	GpxParser gpx;	
    AppParameters params;

    public GpxFile(AppParameters params, File file) throws ParserConfigurationException, SAXException, IOException, ParseException {
        super(file.getParentFile(), file.getName());
        this.params = params;
        this.gpx = new GpxParser(params);
    }
    
    /**
     * XMLパースを実行する
     * 
     */
    public void parse() throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        
        SAXParser parser;
		parser = factory.newSAXParser();
        try {
			parser.parse(this, gpx);
		} catch (SAXParseException e) {}

        // XMLが閉じられなかったデータを救出する
		if (gpx.tag != null) {
			if (gpx.tag.getTime() != null) {
				gpx.trkpt.put(gpx.tag.clone());
			}
			gpx.tag = null;
		}
		if (gpx.trkpt.size() > 0) {
			gpx.trkseg.put((ElementMapTRKPT)gpx.trkpt.clone());
			gpx.trkpt.clear();
		}
    }
    
    public ElementMapTRKSEG getTrksegMap() {
    	return this.gpx.trkseg;
    }
    
    /**
     * インスタンス状態の表示（parse()実行後に有効になる）
     * 
     */
    public void printinfo() {
		// 表示
    	System.out.println(String.format("GPX file: '%s'", getName()));
		gpx.trkseg.printinfo();
    }
}
