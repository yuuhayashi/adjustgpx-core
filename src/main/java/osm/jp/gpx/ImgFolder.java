package osm.jp.gpx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.xml.sax.SAXException;

public class ImgFolder extends ArrayList<ImgFile> {
    private static final long serialVersionUID = -1137199371724546343L;
    AppParameters params;
    Path imgDir;
    Path outDir;
	
	public ImgFolder(AppParameters params) {
		this.params = params;
		imgDir = params.getImgSourceFolder();
        File[] files = imgDir.toFile().listFiles(new ImgFileFilter());
        Arrays.sort(files, new FileSort());
        for (File file : files) {
        	this.add(new ImgFile(file));
        }
	}
	
	public void setOutDir(Path outDir) {
		this.outDir = outDir;
	}
	
	public Path getOutDir() {
		return this.outDir;
	}

	public Path getImgDir() {
		return this.imgDir;
	}
	
    /**
     * 個別のGPXファイルを処理する
     * 
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws ParseException 
     * @throws ImageWriteException 
     * @throws ImageReadException 
     * @throws TransformerException 
     */
    void procGPXfile(GpxFile gpxFile, long delta) throws ParserConfigurationException, SAXException, IOException, ParseException, ImageReadException, ImageWriteException, TransformerException {
    	ImgFile pre = null;
        for (ImgFile image : this) {
        	try {
        		if (!image.isDone()) {
                    if(image.procImageFile(params, delta, gpxFile, outDir.toFile())) {
                    	image.setDone(true);
                    	if (pre == null) {
                    		pre = image;
                    	}
                    	else {
                    		double simplify = params.getSimplifyMeters();
                    		if (simplify > 0) {
                    			GeoPoint prepoint = pre.getPoint();
                    			if (prepoint.getDistance(image.getPoint()) < simplify) {
                    				image.setEnable(false);
                    			}
                    			else {
                    				pre = image;
                    			}
                    		}
                    		else {
                    			pre = image;
                    		}
                    	}
                    }
        		}
        	}
        	catch(Exception e) {
                System.out.print(String.format("%s", e.toString()));
                continue;
        	}
        }
    }

    /**
     * ファイル名の順序に並び替えるためのソートクラス
     * 
     */
    static class FileSort implements Comparator<File> {
        @Override
        public int compare(File src, File target){
            int diff = src.getName().compareTo(target.getName());
            return diff;
        }
    }
    
    /**
     * imgDir内の画像ファイルを表示する
     */
    public void printinfo() {
        ImgFile.printheader();
        for (ImgFile image : this) {
        	image.printinfo();
        }
        ImgFile.printfooter();
    	
    }
}
