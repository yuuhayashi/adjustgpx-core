package osm.jp.gpx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata.GPSInfo;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.junit.experimental.theories.DataPoints;

public class Fixture {
    String comment;				// テスト概要（コメント）
    String tarFilePath;			// TARデータ
    String gpxSourcePath;		// GPXファイル（オリジナル）
    String gpxDestinationPath;	// GPXファイル（配置先）
    String iniFilePath;			// iniファイル
    Expecter[] expecters;

    public Fixture(
        String comment,
        String tarFilePath,
        String gpxSourcePath,
        String gpxDestinationPath,
        String iniFilePath,
        Expecter[] expecters
    ) {
        this.comment = comment;
        this.tarFilePath = tarFilePath;
        this.gpxSourcePath = gpxSourcePath;
        this.gpxDestinationPath = gpxDestinationPath;
        this.iniFilePath = iniFilePath;
        this.expecters = expecters;
    }

    @Override
    public String toString() {
        String msg = "テストパターン : "+ comment + "\n";
        msg += "\ttarFilePath = "+ tarFilePath +"\n";
        msg += "\tgpxSourcePath = "+ gpxSourcePath +"\n";
        msg += "\tgpxDestinationPath = "+ gpxDestinationPath +"\n";
        msg += "\tiniFilePath = "+ iniFilePath;
        return msg;
    }
    
    public void check() {
    	Expecter[] es = expecters;
        AppParameters params;
		try {
			params = new AppParameters(this.iniFilePath);
	        File outDir = new File(params.getProperty(AppParameters.IMG_OUTPUT_FOLDER));
	        for (Expecter e : es) {
	            File file = new File(outDir, e.value);
	            System.out.println("[JUnit.debug] assert file='"+ file.getAbsolutePath() +"'");
	            assertThat(file.exists(), is(e.expect));
	            if (e.expect) {
	            	
	                // JPEG メタデータが存在すること
	                ImageMetadata meta = Imaging.getMetadata(file);

	                // メタデータは インスタンスJpegImageMetadata であること
	                assertThat((meta instanceof JpegImageMetadata), is(true));
	                JpegImageMetadata jpegMetadata = (JpegImageMetadata)meta;
	                assertNotNull("メタデータは インスタンスJpegImageMetadata であること", jpegMetadata);
	                
	                // EXIFデータが存在すること
	                TiffImageMetadata exif = jpegMetadata.getExif();
	                assertNotNull("EXIFデータが存在すること", exif);
	                
	                // EXIF-TIME が正しく設定されていること
	                String exifTime = ImportPicture.toEXIFString(ImportPicture.toEXIFDate(exif.getFieldValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)[0]));
	                System.out.println("[debug] exifTime = '"+ exifTime +"' <--> '" + e.timeStr + "'");
	                assertThat("EXIF-TIME が正しく設定されていること", exifTime, is(e.timeStr));
	                
	                // LAT,LON
	                GPSInfo gpsInfo = exif.getGPS();
	                if (e.latD != 90.0D) {
	                    assertThat(Expecter.comparePosition(gpsInfo.getLatitudeAsDegreesNorth()), is(Expecter.comparePosition(e.latD)));
	                }
	                if (e.lonD != 180.0D) {
	                    assertThat(Expecter.comparePosition(gpsInfo.getLongitudeAsDegreesEast()), is(Expecter.comparePosition(e.lonD)));
	                }
	                
	                // ELE
	                //RationalNumber[] ele = (RationalNumber[]) exif.getFieldValue(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
	                
	                // MAGVAR, SPEED
	                checkItem(exif, e);
	            }
	        }
		} catch (Exception e1) {
			fail("予期しない例外: "+ e1.toString());
		}
    }
    
    /**
     * 指定のEXIFアイテムが設定されているかどうかのテスト
     * @param exif
     * @param keyword	例：GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION.name;
     * @param value		期待する値（nullの場合はアイテムが設定されていないことを期待する）
     */
    void checkItem(TiffImageMetadata exif, Expecter e) {
    	boolean isMagvar = false;
    	boolean isSpeed = false;
    	List<? extends ImageMetadataItem> dirs = exif.getDirectories();
    	for (ImageMetadataItem dir : dirs) {
    		if (dir instanceof TiffImageMetadata.Directory) {
    			List<? extends ImageMetadataItem> items = ((TiffImageMetadata.Directory)dir).getItems();
    			for (ImageMetadataItem item : items) {
        			if (item instanceof TiffImageMetadata.TiffMetadataItem) {
        				String str = item.toString();
        				assertNotNull(str);
        				TiffImageMetadata.TiffMetadataItem tiffitem = (TiffImageMetadata.TiffMetadataItem)item;
        				if (tiffitem.getKeyword().equals(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION.name)) {
        					if (e.magvar == null) {
        			    		fail("MAGVARが設定されている");
        					}
        					str = tiffitem.getText();
            				assertNotNull(str);
            				assertThat(str, is(e.magvar));
            				isMagvar = true;	// MAGVARが設定されている
        				}
        				if (tiffitem.getKeyword().equals(GpsTagConstants.GPS_TAG_GPS_SPEED.name)) {
        					if (e.speed == null) {
        			    		fail("SPEEDが設定されている");
        					}
        					str = tiffitem.getText();
            				assertNotNull(str);
            				System.out.println("GPS_TAG_GPS_SPEED "+ e.value +" : '"+ str +"' = '"+ e.speed +"'");
            				assertThat(str, is(e.speed));
            				isSpeed = true;	// SPEEDが設定されている
        				}
        			}
    			}
    		}
    	}
    	if (!isMagvar && (e.magvar != null)) {
    		fail("MAGVARが設定されていない");
    	}
    	if (!isSpeed && (e.speed != null)) {
    		fail("SPEEDが設定されていない");
    	}
    }
    

    /**
     * ユニットテスト用データ
     * [Canonカメラ]
     * 
    @DataPoints
    public static Fixture[] stddatas = {
		new Fixture(
		    "[std0].Canonカメラの場合.FILE_UPDATE時間を基準にして時間外のファイルはコピー対象外の時",
		    "target/test-classes/imgdata/Canon20200426-1.zip", 
		    "target/test-classes/cameradata/",
		    "target/test-classes/cameradata/",
		    "target/test-classes/cameradata/AdjustTime.ini",
		    new Expecter[] {
		        new Expecter("109_0426/IMG_0001.JPG", false, null, 90.0D, 180.0D, null),
		        new Expecter("109_0426/IMG_0004.JPG", true, "2020:04:26 10:58:18", 35.4393043555D, 139.4478441775D, null),
		        new Expecter("109_0426/IMG_0007.JPG", true, "2020:04:26 11:17:48", 35.4382312205D, 139.4584579300D, null),
		        new Expecter("109_0426/IMG_0010.JPG", true, "2020:04:26 11:20:42", 35.4374477640D, 139.4604294375D, null),
		        new Expecter("109_0426/IMG_0013.JPG", true, "2020:04:26 12:11:28", 35.4209551122D, 139.4677959569D, null),
		        new Expecter("109_0426/IMG_0016.JPG", true, "2020:04:26 12:19:42", 35.4202432372D, 139.4685635716D, null),
		        new Expecter("109_0426/IMG_0019.JPG", true, "2020:04:26 12:21:48", 35.4181452468D, 139.4684348255D, null),
		        new Expecter("109_0426/IMG_0022.JPG", false, null, 90.0D, 180.0D, null),
		        new Expecter("109_0426/IMG_0025.JPG", false, null, 90.0D, 180.0D, null),
		        new Expecter("109_0426/IMG_0028.JPG", false, null, 90.0D, 180.0D, null),
		        new Expecter("109_0426/IMG_0031.JPG", false, null, 90.0D, 180.0D, null),
		        new Expecter("109_0426/IMG_0034.JPG", false, null, 90.0D, 180.0D, null),
		    }
		),
		new Fixture(
			    "[std1].Canonカメラの場合.FILE_UPDATE時間を基準,MAGVARをONの時",
			    "target/test-classes/imgdata/Canon20200426-1.zip", 
			    "target/test-classes/cameradata/",
			    "target/test-classes/cameradata/",
			    "target/test-classes/cameradata/AdjustTime.magvar.ini",
			    new Expecter[] {
			        new Expecter("109_0426/IMG_0001.JPG", false, null, 90.0D, 180.0D, null),
			        new Expecter("109_0426/IMG_0004.JPG", true, "2020:04:26 10:58:18", 35.4393043555D, 139.4478441775D, "348"),
			        new Expecter("109_0426/IMG_0007.JPG", true, "2020:04:26 11:17:48", 35.4382312205D, 139.4584579300D, "126"),
			        new Expecter("109_0426/IMG_0010.JPG", true, "2020:04:26 11:20:42", 35.4374477640D, 139.4604294375D, "115"),
			        new Expecter("109_0426/IMG_0013.JPG", true, "2020:04:26 12:11:28", 35.4209551122D, 139.4677959569D, "101"),
			        new Expecter("109_0426/IMG_0016.JPG", true, "2020:04:26 12:19:42", 35.4202432372D, 139.4685635716D, "189"),
			        new Expecter("109_0426/IMG_0019.JPG", true, "2020:04:26 12:21:48", 35.4181452468D, 139.4684348255D, "173"),
			        new Expecter("109_0426/IMG_0022.JPG", false, null, 90.0D, 180.0D, null),
			        new Expecter("109_0426/IMG_0025.JPG", false, null, 90.0D, 180.0D, null),
			        new Expecter("109_0426/IMG_0028.JPG", false, null, 90.0D, 180.0D, null),
			        new Expecter("109_0426/IMG_0031.JPG", false, null, 90.0D, 180.0D, null),
			        new Expecter("109_0426/IMG_0034.JPG", false, null, 90.0D, 180.0D, null),
			    }
			),
    };
     */

    /**
     * 各種カメラGPXファイル
     */
    @DataPoints
    public static Fixture[] datas = {
        // datas[0] speed=on
        new Fixture(
            "[A1].SONYカメラの場合.FILE_UPDATE時間を基準にして時間外のファイルはコピー対象外の時",
            "target/test-classes/imgdata/Sony20170518-5.tar.gz", 
            "target/test-classes/gpx/20170518.gpx",
            "target/test-classes/cameradata/20170518.gpx",
            "target/test-classes/ini/AdjustTime.20170518.A1.ini",
            new Expecter[] {
                new Expecter("10170518/DSC05183.JPG", false, null, 90.0D, 180.0D, null, null),
                new Expecter("10170518/DSC05184.JPG", true, "2017:05:18 09:34:44", 35.4367520000D, 139.4082730000D, null, "0"),
                new Expecter("10170518/DSC05196.JPG", true, "2017:05:18 09:37:32", 35.4376820000D, 139.4085150000D, "383/10 (38.3)", "11/10 (1.1)"),
                new Expecter("10170518/DSC05204.JPG", true, "2017:05:18 09:46:48", 35.4368560000D, 139.4082190000D, "1131/5 (226.2)", "1/2 (0.5)"),
                new Expecter("10170518/DSC05205.JPG", false, null, 90.0D, 180.0D, null, null),
            }
        ),
        // datas[1] speed=on
        new Fixture(        		
            "[A2].SONYカメラの場合.FILE_UPDATE時間を基準にして時間外のファイルもコピーする時",
            "target/test-classes/imgdata/Sony20170518-5.tar.gz", 
            "target/test-classes/cameradata/20170518.gpx",
            "target/test-classes/output/20170518.gpx",
            "target/test-classes/ini/AdjustTime.20170518.A2.ini",
            new Expecter[] {
                new Expecter("10170518/DSC05183.JPG", false, null, 90.0D, 180.0D, null, null),
                new Expecter("10170518/DSC05184.JPG", true, "2017:05:18 09:34:44", 35.4367520000D, 139.4082730000D, null, "0"),
                new Expecter("10170518/DSC05196.JPG", true, "2017:05:18 09:37:32", 35.4376820000D, 139.4085150000D, "383/10 (38.3)", "11/10 (1.1)"),
                new Expecter("10170518/DSC05204.JPG", true, "2017:05:18 09:46:48", 35.4368560000D, 139.4082190000D, "1131/5 (226.2)", "1/2 (0.5)"),
                new Expecter("10170518/DSC05205.JPG", false, null, 90.0D, 180.0D, null, null),
            }
        ),
        // datas[2] speed=on
        new Fixture(
            "[B1].WiMiUSカメラの場合.FILE_UPDATE時間を基準にして時間外のファイルはコピー対象外の時",
            "target/test-classes/imgdata/WiMiUS20170518-5.tar.gz", 
            "target/test-classes/gpx/20170518.gpx",
            "target/test-classes/output/20170518.gpx",
            "target/test-classes/ini/AdjustTime.20170518.B1.ini",
            new Expecter[] {
                new Expecter("cameradata/20170518_092031A.jpg", false, null, 90.0D, 180.0D, null, null),
                new Expecter("cameradata/20170518_094226A_snap.jpg", true, "2017:05:18 09:42:26", 35.4366860000D, 139.4082650000D, null, "0"),
                new Expecter("cameradata/20170518_094737A.jpg", true, "2017:05:18 09:47:36", 35.4368200000D, 139.4082810000D, "813/10 (81.3)", "7/10 (0.7)"),
                new Expecter("cameradata/20170518_094827A.jpg", false, null, 90.0D, 180.0D, null, null),
            }
        ),
        // datas[3] speed=off
        new Fixture(
            "[B2].WiMiUSカメラの場合.FILE_UPDATE時間を基準にして時間外のファイルもコピーする時",
            "target/test-classes/imgdata/WiMiUS20170518-5.tar.gz", 
            "target/test-classes/gpx/20170518.gpx",
            "target/test-classes/cameradata/20170518.gpx",
            "target/test-classes/ini/AdjustTime.20170518.B2.ini",
            new Expecter[] {
                new Expecter("cameradata/20170518_092031A.jpg", false, "2017:05:18 09:20:30", 90.0D, 180.0D, null, null),
                new Expecter("cameradata/20170518_094226A_snap.jpg", true, "2017:05:18 09:42:26", 35.4366860000D, 139.4082650000D, null, "0"),
                new Expecter("cameradata/20170518_094737A.jpg", true, "2017:05:18 09:47:36", 35.4368200000D, 139.4082810000D, "813/10 (81.3)", "7/10 (0.7)"),
                new Expecter("cameradata/20170518_094827A.jpg", false, "2017:05:18 09:48:26", 90.0D, 180.0D, null, null),
            }
        ),
        /*
        // 5.
        new Fixture(
            "[M1a].GPXが複数のTRKSEGに分割している場合.FILE_UPDATE時間を基準.GarminColorado",
            "target/test-classes/imgdata/separate-5.tar.gz",
            "target/test-classes/gpx/muiltiTRK.GarminColorado.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M1a.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:04", 35.8808881603D, 137.9979396332D, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:18", 35.8339846227D, 138.0625408050D, "344"),
            }
        ),

        new Fixture(
            "[M1b].GPXが複数のTRKSEGに分割している場合.FILE_UPDATE時間を基準.GarminColorado",
            "target/test-classes/imgdata/separate-5.tar.gz",
            "target/test-classes/gpx/muiltiTRK.GarminColorado.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M1b.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", true, "2017:05:29 10:23:06", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", true, "2017:05:29 10:23:14", 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:04", 35.8808641881D, 137.9979D, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", true, "2017:05:29 10:24:10", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", true, "2017:05:29 10:24:18", 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", true, "2017:05:29 10:33:14", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", true, "2017:05:29 10:35:44", 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:18", 35.8339846227D, 138.0625408050D, "344"),
            }
        ),

        new Fixture(
            "[M1c].GPXが複数のTRKSEGに分割している場合.EXIF時間を基準.GarminColorado",
            "target/test-classes/imgdata/separate-5.tar.gz",
            "target/test-classes/gpx/muiltiTRK.GarminColorado.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M1c.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:05", 35.8808641881D, 137.9981065169D, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:19", 35.8339846227D, 138.0625408050D, "345"),
            }
        ),

        new Fixture(
            "[M1d].GPXが複数のTRKSEGに分割している場合.EXIF時間を基準.GarminColorado",
            "target/test-classes/imgdata/separate-5.tar.gz", 
            "target/test-classes/gpx/muiltiTRK.GarminColorado.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M1d.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", true, "2017:05:29 10:23:05", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", true, "2017:05:29 10:23:14", 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:05", 35.8808641881D, 137.9981065169D, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", true, "2017:05:29 10:24:09", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", true, "2017:05:29 10:24:18", 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", true, "2017:05:29 10:33:15", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", true, "2017:05:29 10:35:45", 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:19", 35.8339846227D, 138.0625408050D, "345"),
            }
        ),


        new Fixture(
            "[M2a].GPXが複数のTRKSEGに分割している場合.FILE_UPDATE時間を基準.eTrex_20J",
            "target/test-classes/imgdata/separate-5.tar.gz", 
            "target/test-classes/gpx/multiTRKSEG.eTrex_20J.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M2a.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:04", 35.8808641881D, 137.9979, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:18", 35.8339846227D, 138.0625408050D, "344"),
            }
        ),

        new Fixture(
            "[M2b].GPXが複数のTRKSEGに分割している場合.FILE_UPDATE時間を基準.eTrex_20J",
            "target/test-classes/imgdata/separate-5.tar.gz", 
            "target/test-classes/gpx/multiTRKSEG.eTrex_20J.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M2b.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", true, "2017:05:29 10:23:06", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", true, "2017:05:29 10:23:14", 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:04", 35.8808641881D, 137.9979, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", true, "2017:05:29 10:24:10", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", true, "2017:05:29 10:24:18", 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", true, "2017:05:29 10:33:14", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", true, "2017:05:29 10:35:44", 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:18", 35.8339846227D, 138.0625408050D, "344"),
            }
        ),

        new Fixture(
            "[M2c].GPXが複数のTRKSEGに分割している場合.EXIF時間を基準.eTrex_20J",
            "target/test-classes/imgdata/separate-5.tar.gz", 
            "target/test-classes/gpx/multiTRKSEG.eTrex_20J.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M2c.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:05", 35.8808641881D, 137.9981065169D, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:19", 35.8339846227D, 138.0625408050D, "345"),
            }
        ),

        new Fixture(
            "[M2d].GPXが複数のTRKSEGに分割している場合.EXIF時間を基準.eTrex_20J",
            "target/test-classes/imgdata/separate-5.tar.gz", 
            "target/test-classes/gpx/multiTRKSEG.eTrex_20J.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M2d.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", true, "2017:05:29 10:23:05", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", true, "2017:05:29 10:23:14", 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:05", 35.8808641881D, 137.9981065169D, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", true, "2017:05:29 10:24:09", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", true, "2017:05:29 10:24:18", 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", true, "2017:05:29 10:33:15", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", true, "2017:05:29 10:35:45", 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:19", 35.8339846227D, 138.0625408050D, "345"),
            }
        ),

        new Fixture(
            "[M3a].GPXが複数のTRKSEGに分割している場合.FILE_UPDATE時間を基準.eTrex_20Jreverse",
            "target/test-classes/imgdata/separate-5.tar.gz", 
            "target/test-classes/gpx/multiTRKSEGreverse.eTrex_20J.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M2a.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:04", 35.8808641881D, 137.9979, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:18", 35.8339846227D, 138.0625408050D, "344"),
            }
        ),

        new Fixture(
            "[M3b].GPXが複数のTRKSEGに分割している場合.FILE_UPDATE時間を基準.eTrex_20Jreverse",
            "target/test-classes/imgdata/separate-5.tar.gz", 
            "target/test-classes/gpx/multiTRKSEGreverse.eTrex_20J.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M2b.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", true, "2017:05:29 10:23:06", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", true, "2017:05:29 10:23:14", 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:04", 35.8808641881D, 137.9979D, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", true, "2017:05:29 10:24:10", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", true, "2017:05:29 10:24:18", 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", true, "2017:05:29 10:33:14", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", true, "2017:05:29 10:35:44", 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:18", 35.8339846227D, 138.0625408050D, "344"),
            }
        ),

        new Fixture(
            "[M3c].GPXが複数のTRKSEGに分割している場合.EXIF時間を基準.eTrex_20Jreverse",
            "target/test-classes/imgdata/separate-5.tar.gz", 
            "target/test-classes/gpx/multiTRKSEGreverse.eTrex_20J.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M2c.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:05", 35.8808641881D, 137.9981065169D, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", false, null, 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", false, null, 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:19", 35.8339846227D, 138.0625408050D, "345"),
            }
        ),

        new Fixture(
            "[M3d].GPXが複数のTRKSEGに分割している場合.EXIF時間を基準.eTrex_20Jreverse",
            "target/test-classes/imgdata/separate-5.tar.gz", 
            "target/test-classes/gpx/multiTRKSEGreverse.eTrex_20J.gpx.xml",
            "target/test-classes/cameradata/separate.gpx",
            "target/test-classes/ini/AdjustTime.M2d.separate.ini",
            new Expecter[] {
                // out of time ( - 2017-05-29T01:23:18)
                new Expecter("separate/20170529_102305A.jpg", true, "2017:05:29 10:23:05", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102314A.jpg", true, "2017:05:29 10:23:14", 90.0D, 180.0D, null),

                // in TRKSEG(1) (2017-05-29T01:23:18 - 2017-05-29T01:24:05)
                new Expecter("separate/20170529_102318A.jpg", true, "2017:05:29 10:23:18", 35.8812697884D, 137.9952202085D, null),
                new Expecter("separate/20170529_102322A.jpg", true, "2017:05:29 10:23:22", 35.8810500987D, 137.9951669835D, "191"),
                new Expecter("separate/20170529_102405A.jpg", true, "2017:05:29 10:24:05", 35.8808641881D, 137.9981065169D, "100"),

                // out of time (2017-05-29T01:24:05 - 2017-05-29T01:24:37)
                new Expecter("separate/20170529_102409A.jpg", true, "2017:05:29 10:24:09", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_102418A.jpg", true, "2017:05:29 10:24:18", 90.0D, 180.0D, null),

                // in TRKSEG(2) (2017-05-29T01:24:37 - 2017-05-29T01:33:03)
                new Expecter("separate/20170529_102448A.jpg", true, "2017:05:29 10:24:48", 35.8788877353D, 138.0039562471D, "178"),
                new Expecter("separate/20170529_103246A.jpg", true, "2017:05:29 10:32:46", 35.8405660931D, 138.0353022180D, "95"),

                // out of time (2017-05-29T01:33:03 - 2017-05-29T01:35:53)
                new Expecter("separate/20170529_103315A.jpg", true, "2017:05:29 10:33:15", 90.0D, 180.0D, null),
                new Expecter("separate/20170529_103545A.jpg", true, "2017:05:29 10:35:45", 90.0D, 180.0D, null),

                // in TRKSEG(3) (2017-05-29T01:35:53 - 2017-05-29T01:47:35)
                new Expecter("separate/20170529_103615A.jpg", true, "2017:05:29 10:36:14", 35.8359798510D, 138.0600296706D, "111"),
                new Expecter("separate/20170529_104119A.jpg", true, "2017:05:29 10:41:19", 35.8339846227D, 138.0625408050D, "345"),
            }
        ),
        */
    };
	//public static Fixture[] stddatas;

}
