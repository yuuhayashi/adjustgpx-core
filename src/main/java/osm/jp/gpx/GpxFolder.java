package osm.jp.gpx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GpxFolder extends ArrayList<GpxFile> {
	private static final long serialVersionUID = 6178901459948163548L;
    AppParameters params;
	
	public GpxFolder(AppParameters params) throws ParserConfigurationException, SAXException, IOException, ParseException {
		this.params = params;

		File[] gpxfiles;
		File gpxDir = params.getGpxSourceFolder().toFile();
    	if (gpxDir == null) {
        	// GPXファイルまたはディレクトリが存在しません。('%s')
    		throw new FileNotFoundException(String.format(ImportPicture.i18n.getString("msg.100"), "null"));
    	}
        if (!gpxDir.exists()) {
        	// GPXファイルまたはディレクトリが存在しません。('%s')
    		throw new FileNotFoundException(String.format(ImportPicture.i18n.getString("msg.100"), gpxDir.getAbsolutePath()));
        }

        if (gpxDir.isFile()) {
        	if (accept(params, gpxDir.getName())) {
            	List<File> fileList = new ArrayList<>();
            	fileList.add(gpxDir);
            	gpxfiles = fileList.toArray(new File[fileList.size()]);
        	}
        	else {
            	// GPXファイルまたはディレクトリが存在しません。('%s')
        		throw new FileNotFoundException(String.format(ImportPicture.i18n.getString("msg.100"), gpxDir.getAbsolutePath()));
        	}
        }
        else if (gpxDir.isDirectory()) {
        	// 指定されたディレクトリ内のGPXファイルすべてを対象とする
            gpxfiles = gpxDir.listFiles(new GpxFileFilter());
            if ((gpxfiles == null) || (gpxfiles.length < 1)) {
            	// 対象となるGPXファイルがありませんでした。('%s')
            	throw new FileNotFoundException(
                    String.format(ImportPicture.i18n.getString("msg.110"), gpxDir.getAbsolutePath())
                );
            }
            if (params.isImgOutputAll() && (gpxfiles.length > 1)) {
                // "複数のGPXファイルがあるときには、'IMG.OUTPUT_ALL'オプションは指定できません。"
            	throw new FileNotFoundException(
                    String.format(ImportPicture.i18n.getString("msg.120"))
                );
            }
        }
        else {
        	// GPXファイルまたはディレクトリが存在しません。('%s')
    		throw new FileNotFoundException(String.format(ImportPicture.i18n.getString("msg.100"), gpxDir.getAbsolutePath()));
        }

        Arrays.sort(gpxfiles, new FileSort());
        for (File file : gpxfiles) {
        	GpxFile gpx = new GpxFile(params, file);
        	gpx.parse();
        	this.add(gpx);
        }
	}
	
	public void printinfo() {
        for (GpxFile gpxFile : this) {
        	gpxFile.printinfo();
            System.out.println();
        }
	}
	
	/**
	 * 対象は '*.GPX' のみ対象とする
	 */
	public static boolean accept(AppParameters params, String name) {
		String filename = name.toUpperCase();
        if (filename.endsWith(".GPX")) {
            if (!filename.endsWith("_.GPX") || params.isGpxReuse()) {
            	return true;
            }
        }
        return false;
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
     * GPXファイルフィルター
     */
    class GpxFileFilter implements FilenameFilter {
    	@Override
        public boolean accept(File dir, String name) {
    		return GpxFolder.accept(params, name);
    	}
    }
}
