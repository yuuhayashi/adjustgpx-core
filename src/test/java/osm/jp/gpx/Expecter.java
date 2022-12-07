package osm.jp.gpx;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;

public class Expecter {
    String value;
    boolean expect;
    String timeStr;
    double latD;
    double lonD;
    String magvar;

    /**
     * コンストラクタ
     * @param value		[0] 画像ファイルの相対パス
     * @param expect	[1] 存在するかどうか
     * @param timeStr	[2] 撮影時刻
     * @param latD		[3] 緯度
     * @param lonD		[4] 経度
     * @param magvar	[5] 方向(0-359),設定されていないことを期待する場合にはnullとする
     */
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
    
}
