package osm.jp.gpx;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import osm.jp.hayashi.tools.files.Directory;

@SuppressWarnings("serial")
public class AppParameters extends Properties {
    static final String FILE_PATH = "AdjustTime.ini";
    
    // GPX: 時間的に間隔が開いたGPXログを別の<trkseg>セグメントに分割する。 {ON | OFF}
    public static String GPX_GPXSPLIT = "GPX.gpxSplit";

    // GPX: <trkseg>セグメントの最初の１ノードは無視する。 {ON | OFF}
    public static String GPX_NO_FIRST_NODE = "GPX.noFirstNode";

    // GPX: 基準時刻 {FILE_UPDATE | EXIF_TIME}
    public static String GPX_BASETIME = "GPX.BASETIME";

    // GPX: ファイル更新時刻 yyyy:MM:dd HH:mm:ss
    public static String IMG_TIME = "IMG.TIME";

    // 対象IMGフォルダ:(位置情報を付加したい画像ファイルが格納されているフォルダ)
    public static String IMG_SOURCE_FOLDER = "IMG.SOURCE_FOLDER";

    // 基準時刻画像(正確な撮影時刻が判明できる画像)
    public static String IMG_BASE_FILE = "IMG.BASE_FILE";

    // 対象GPXフォルダ:(GPXファイルが格納されているフォルダ)
    public static String GPX_SOURCE_FOLDER = "GPX.SOURCE_FOLDER";

    // 出力フォルダ:(変換した画像ファイルを出力するフォルダ)
    public static String IMG_OUTPUT_FOLDER = "IMG.OUTPUT_FOLDER";

    // 出力GPX: <SPEED>を上書き出力する {ON | OFF}
    public static String GPX_OUTPUT_SPEED = "GPX.OUTPUT_SPEED";

    // 出力GPX: ソースGPXの<MAGVER>を無視する {ON | OFF}
    public static String GPX_OVERWRITE_MAGVAR = "GPX.OVERWRITE_MAGVAR";
    
    // "*_.GPX"も対象にする
    public static String GPX_REUSE = "GPX.REUSE";

    // simplify distance (m) 
    public static String SIMPLIFY_METERS = "IMG.SIMPLIFY_METERS";
    
    Path file;

    public AppParameters() throws FileNotFoundException, IOException, URISyntaxException {
        this(FILE_PATH);
    }

    public AppParameters(Properties defaults) throws FileNotFoundException, IOException, URISyntaxException {
        super(defaults);
        this.file = Paths.get(FILE_PATH);
        syncFile();
    }

    public AppParameters(String iniFileName) throws FileNotFoundException, IOException, URISyntaxException {
        super();
        this.file = Paths.get(iniFileName);
        syncFile();
    }

    private void syncFile() throws FileNotFoundException, IOException, URISyntaxException {
        boolean update = false;

        if (Files.exists(this.file)) {
            // ファイルが存在すれば、その内容をロードする。
            this.load(new FileInputStream(file.toFile()));
        }
        else {
            update = true;
        }

        //------------------------------------------------
        // 対象フォルダ:(位置情報を付加したい画像ファイルが格納されているフォルダ)
        String valueStr = this.getProperty(IMG_SOURCE_FOLDER);
        if (valueStr == null) {
            update = true;
            this.setProperty(IMG_SOURCE_FOLDER, Directory.getCurrentDirectory().toString());
        }

        //------------------------------------------------
        // 対象フォルダ:(GPXファイルが格納されているフォルダ)
        valueStr = this.getProperty(GPX_SOURCE_FOLDER);
        if (valueStr == null) {
            update = true;
            this.setProperty(GPX_SOURCE_FOLDER, Directory.getCurrentDirectory().toString());
        }

        //------------------------------------------------
        // 基準時刻画像(正確な撮影時刻が判明できる画像)
        valueStr = this.getProperty(IMG_BASE_FILE);
        if (valueStr == null) {
            update = true;
            this.setProperty(IMG_BASE_FILE, "");
        }

        //------------------------------------------------
        // 出力フォルダ:(変換した画像ファイルとGPXファイルを出力するフォルダ)
        valueStr = this.getProperty(IMG_OUTPUT_FOLDER);
        if (valueStr == null) {
            update = true;
            this.setProperty(IMG_OUTPUT_FOLDER, Directory.getCurrentDirectory().toString());
        }

        //------------------------------------------------
        // GPX出力: 時間的に間隔が開いたGPXログを別の<trkseg>セグメントに分割する。 {ON | OFF}
        valueStr = this.getProperty(GPX_GPXSPLIT);
        if (valueStr == null) {
            update = true;
            this.setProperty(GPX_GPXSPLIT, String.valueOf(true));
        }

        //------------------------------------------------
        // GPX出力: <trkseg>セグメントの最初の１ノードは無視する。 {ON | OFF}
        valueStr = this.getProperty(GPX_NO_FIRST_NODE);
        if (valueStr == null) {
            update = true;
            this.setProperty(GPX_NO_FIRST_NODE, String.valueOf(true));
        }

        //------------------------------------------------
        // GPX出力: ソースGPXの<MAGVAR>を無視する {ON | OFF}
        valueStr = this.getProperty(GPX_OVERWRITE_MAGVAR);
        if (valueStr == null) {
            update = true;
            this.setProperty(GPX_OVERWRITE_MAGVAR, String.valueOf(true));
        }

        //------------------------------------------------
        // GPX出力: <SPEED>を上書き出力する {ON | OFF}
        valueStr = this.getProperty(GPX_OUTPUT_SPEED);
        if (valueStr == null) {
            update = true;
            this.setProperty(GPX_OUTPUT_SPEED, String.valueOf(true));
        }

        //------------------------------------------------
        //  GPX: 基準時刻 {FILE_UPDATE | EXIF}
        valueStr = this.getProperty(GPX_BASETIME);
        if (valueStr == null) {
            update = true;
            setProperty(AppParameters.GPX_BASETIME, "FILE_UPDATE");
        }

        //------------------------------------------------
        // simplify distance (m) 
    	valueStr = getProperty(AppParameters.SIMPLIFY_METERS);
    	if (valueStr == null) {
            update = true;
            setProperty(AppParameters.SIMPLIFY_METERS, String.valueOf("0.0"));
    	}
        
        // その他のパラメータを読み取る
    	valueStr = getProperty(AppParameters.GPX_GPXSPLIT);
    	if (valueStr == null) {
            update = true;
            setProperty(AppParameters.GPX_GPXSPLIT, Boolean.toString(false));
    	}

    	valueStr = getProperty(AppParameters.GPX_NO_FIRST_NODE);
    	if (valueStr == null) {
            update = true;
            setProperty(AppParameters.GPX_NO_FIRST_NODE, Boolean.toString(false));
    	}
    	
    	valueStr = getProperty(AppParameters.GPX_OVERWRITE_MAGVAR);
    	if (valueStr == null) {
            update = true;
            setProperty(AppParameters.GPX_OVERWRITE_MAGVAR, Boolean.toString(false));
    	}

    	
    	valueStr = getProperty(AppParameters.GPX_OUTPUT_SPEED);
    	if (valueStr == null) {
            update = true;
            setProperty(AppParameters.GPX_OUTPUT_SPEED, Boolean.toString(false));
    	}
    	
    	valueStr = getProperty(AppParameters.GPX_REUSE);
    	if (valueStr == null) {
            update = true;
            setProperty(AppParameters.GPX_REUSE, Boolean.toString(false));
    	}
        
        if (update) {
            // ・ファイルがなければ新たに作る
            // ・項目が足りない時は書き足す。
            this.store(new FileOutputStream(this.file.toFile()), "defuilt settings");
        }
    }

    public void store() throws FileNotFoundException, IOException {
        this.store(new FileOutputStream(this.file.toFile()), "by AdjustGpx");
    }
    
    public void printout() {
        System.out.println(" - param： "+ AppParameters.IMG_TIME +"="+ getProperty(AppParameters.IMG_TIME) );
        System.out.println(" - param： "+ AppParameters.IMG_BASE_FILE +"="+ getProperty(AppParameters.IMG_BASE_FILE) );
        System.out.println(" - param： "+ AppParameters.GPX_BASETIME +"="+ getProperty(AppParameters.GPX_BASETIME) );
        System.out.println(" - param： "+ AppParameters.IMG_SOURCE_FOLDER +"="+ getProperty(AppParameters.IMG_SOURCE_FOLDER) );
        System.out.println(" - param： "+ AppParameters.IMG_OUTPUT_FOLDER +"="+ getProperty(AppParameters.IMG_OUTPUT_FOLDER) );
        System.out.println(" - param： "+ AppParameters.GPX_SOURCE_FOLDER +"="+ getProperty(AppParameters.GPX_SOURCE_FOLDER));
        System.out.println(" - param： "+ AppParameters.GPX_OVERWRITE_MAGVAR +"="+ getProperty(AppParameters.GPX_OVERWRITE_MAGVAR));
        System.out.println(" - param： "+ AppParameters.GPX_OUTPUT_SPEED +"="+ getProperty(AppParameters.GPX_OUTPUT_SPEED));
        System.out.println(" - param： "+ AppParameters.GPX_GPXSPLIT +"="+ isGpxSplit());
        System.out.println(" - param： "+ AppParameters.GPX_NO_FIRST_NODE +"="+ isGpxNoFirstNode());        
        System.out.println(" - param： "+ AppParameters.GPX_REUSE +"="+ isGpxReuse());
        System.out.println(" - param： "+ AppParameters.SIMPLIFY_METERS +"="+ getSimplifyMeters());
    }
    
    /**
     * 基準時刻（ファイル更新日時 | EXIF撮影日時)
     * @return boolean exifBase = false;
     */
    public boolean isExifBase() {
    	return (getProperty(AppParameters.GPX_BASETIME).equals("EXIF_TIME"));
    }
    
    /**
     * AppParameters.IMG_SOURCE_FOLDER
     * @return new Path(getProperty(AppParameters.IMG_SOURCE_FOLDER));
     */
    public Path getImgSourceFolder() {
    	return Paths.get(getProperty(AppParameters.IMG_SOURCE_FOLDER));
    }
    
    public Path getGpxSourceFolder() {
    	String str = getProperty(AppParameters.GPX_SOURCE_FOLDER);
    	if (str == null) {
    		return null;
    	}
    	if (str.isEmpty()) {
    		return null;
    	}
    	return Paths.get(str);
    }

    //------------------------------------------------
    // simplify distance (m) 
    public double getSimplifyMeters() {
    	String str = getProperty(AppParameters.SIMPLIFY_METERS);
    	if (str == null) {
    		return 0.0D;
    	}
    	return Double.parseDouble(str);
    }

    /**
     * AppParameters.GPX_GPXSPLIT
     * @return
     */
    public boolean isGpxSplit() {
    	return isParam(AppParameters.GPX_GPXSPLIT);
    }

    public boolean isGpxNoFirstNode() {
    	return isParam(AppParameters.GPX_NO_FIRST_NODE);
    }
	
	public boolean isGpxOverwriteMagvar() {
    	return isParam(AppParameters.GPX_OVERWRITE_MAGVAR);
	}

	public void setGpxOverwriteMagvar(boolean v) {
        this.setProperty(GPX_OVERWRITE_MAGVAR, String.valueOf(v));
	}

	public boolean isGpxOutputSpeed() {
    	return isParam(AppParameters.GPX_OUTPUT_SPEED);
	}
    
	public void setGpxOutputSpeed(boolean v) {
        this.setProperty(GPX_OUTPUT_SPEED, String.valueOf(v));
	}

	public boolean isGpxReuse() {
    	return isParam(AppParameters.GPX_REUSE);
	}
    
	boolean isParam(String item) {
    	String valueStr = getProperty(item);
    	if ((valueStr != null) && valueStr.equals(Boolean.toString(true))) {
    		return true;
    	}
    	return false;
	}
}
