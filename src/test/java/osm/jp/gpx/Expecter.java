package osm.jp.gpx;

import static org.hamcrest.CoreMatchers.*;
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

public class Expecter {
    String value;
    boolean expect;
    String timeStr;
    double latD;
    double lonD;
    String magvar;

    public Expecter(String value, boolean expect, String timeStr, double latD, double lonD, String magvar) {
        this.value = value;
        this.expect = expect;
        this.timeStr = timeStr;
        this.latD = latD;
        this.lonD = lonD;
        this.magvar = magvar;
    }

    public static String comparePosition(double b) {
        return String.format("%.4f", b);
    }

    public static void check(Fixture dataset) {
    	Expecter[] es = dataset.expecters;
        AppParameters params;
		try {
			params = new AppParameters(dataset.iniFilePath);
	        File outDir = new File(params.getProperty(AppParameters.IMG_OUTPUT_FOLDER));
	        for (Expecter e : es) {
	            File file = new File(outDir, e.value);
	            System.out.println("[JUnit.debug] assert file='"+ file.getAbsolutePath() +"'");
	            assertThat(file.exists(), is(e.expect));
	            if (e.timeStr != null) {
	            	
	                // JPEG メタデータが存在すること
	                ImageMetadata meta = Imaging.getMetadata(file);

	                // メタデータは インスタンスJpegImageMetadata であること
	                assertThat((meta instanceof JpegImageMetadata), is(true));
	                JpegImageMetadata jpegMetadata = (JpegImageMetadata)meta;
	                assertNotNull(jpegMetadata);
	                
	                // EXIFデータが存在すること
	                TiffImageMetadata exif = jpegMetadata.getExif();
	                assertNotNull(exif);
	                
	                // EXIF-TIME が正しく設定されていること
	                String exifTime = ImportPicture.toEXIFString(ImportPicture.toEXIFDate(exif.getFieldValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)[0]));
	                System.out.println("[debug] exifTime = '"+ exifTime +"' <--> '" + e.timeStr + "'");
	                assertThat(exifTime, is(e.timeStr));
	                
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
	                
	                // MAGVAR
                	checkItem(exif, GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION.name, e.magvar);
	                
	                // SPEED
	                
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
     */
    static void checkItem(TiffImageMetadata exif, String keyword) {
    	boolean ismagvar = false;
    	List<? extends ImageMetadataItem> dirs = exif.getDirectories();
    	for (ImageMetadataItem dir : dirs) {
    		if (dir instanceof TiffImageMetadata.Directory) {
    			List<? extends ImageMetadataItem> items = ((TiffImageMetadata.Directory)dir).getItems();
    			for (ImageMetadataItem item : items) {
        			if (item instanceof TiffImageMetadata.TiffMetadataItem) {
        				String str = item.toString();
        				assertNotNull(str);
        				TiffImageMetadata.TiffMetadataItem tiffitem = (TiffImageMetadata.TiffMetadataItem)item;
        				if (tiffitem.getKeyword() == keyword) {
        					str = tiffitem.getText();
            				assertNotNull(str);
            				ismagvar = true;	// MAGVARが設定されている
        				}
        			}
    			}
    		}
    	}
    	if (!ismagvar) {
    		fail("MAGVARが設定されていない");
    	}
    	
    }
    
    /**
     * 指定のEXIFアイテムが設定されているかどうかのテスト
     * @param exif
     * @param keyword	例：GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION.name;
     * @param value		期待する値（nullの場合はアイテムが設定されていないことを期待する）
     */
    static void checkItem(TiffImageMetadata exif, String keyword, String value) {
    	boolean is = false;
    	List<? extends ImageMetadataItem> dirs = exif.getDirectories();
    	for (ImageMetadataItem dir : dirs) {
    		if (dir instanceof TiffImageMetadata.Directory) {
    			List<? extends ImageMetadataItem> items = ((TiffImageMetadata.Directory)dir).getItems();
    			for (ImageMetadataItem item : items) {
        			if (item instanceof TiffImageMetadata.TiffMetadataItem) {
        				String str = item.toString();
        				assertNotNull(str);
        				TiffImageMetadata.TiffMetadataItem tiffitem = (TiffImageMetadata.TiffMetadataItem)item;
        				if (tiffitem.getKeyword() == keyword) {
        					if (value == null) {
        			    		fail("MAGVARが設定されている");
        					}
        					str = tiffitem.getText();
            				assertNotNull(str);
            				assertThat(str, is(value));
            				is = true;	// MAGVARが設定されている
        				}
        			}
    			}
    		}
    	}
    	if (!is && (value != null)) {
    		fail("MAGVARが設定されていない");
    	}
    }
}
