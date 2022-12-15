package osm.jp.gpx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class ImgFile extends File {
	boolean done = false;
	boolean enable = true;		// 有効： デフォルトは有効、但し、SIMPLIFY_METERS以内の距離のIMGは無効にする。
	Date imgtime = null;
	Date gpstime = null;
	double latitude = 0.0D;
	double longitude = 0.0D;
    String eleStr = "-";
    String magvarStr = "-";
    String speedStr = "-";

    public ImgFile(File file) {
        super(file.getParentFile(), file.getName());
    }
    
    public void setDone(boolean done) {
    	this.done = done;
    }
    
    public boolean isDone() {
    	return this.done;
    }
    
    public void setEnable(boolean enable) {
    	this.enable = enable;
    }
    
    public boolean isEnable() {
    	return this.enable;
    }
    
    /**
     * 対象は '*.JPG' のみ対象とする
     * @return 
     */
    public boolean isImageFile() {
    	String name = this.getName();
        return ((name != null) && name.toUpperCase().endsWith(".JPG"));
    }
    
    /**
     * 
     * @param params :AppParameters
     * @param delta		:long
     * @param gpxFile	:GpxFile
     * @param preImg	:ImgFile
     * @return
     * @throws ParseException
     * @throws ImageReadException
     * @throws IOException
     * @throws ImageWriteException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public TagTrkpt procImageFile(AppParameters params, long delta, GpxFile gpxFile, ImgFile preImg) throws ParseException, ImageReadException, IOException, ImageWriteException, ParserConfigurationException, SAXException {
    	//ElementMapTRKSEG mapTRKSEG = gpxFile.parse();
        
        // itime <-- 画像ファイルの撮影時刻
        //			ファイルの更新日時／EXIFの撮影日時
        imgtime = getDate(params);
        
        // uktime <-- 画像撮影時刻に対応するGPX時刻(補正日時)
        gpstime = new Date(imgtime.getTime() + delta);

        // 時刻uktimeにおける<magver>をtrkptに追加する
        TagTrkpt trkptT = null;

        for (Map.Entry<Date,ElementMapTRKPT> map : gpxFile.getTrksegMap().entrySet()) {
            ElementMapTRKPT mapTRKPT = map.getValue();
            trkptT = mapTRKPT.getValue(gpstime);
            if (trkptT != null) {
                break;
            }
        }

        if (trkptT == null) {
            if (!params.isImgOutputAll()) {
            	this.setEnable(false);
                return null;
            }
        }
        else {
            latitude = trkptT.lat;
            longitude = trkptT.lon;
            
            if (trkptT.eleStr != null) {
            	eleStr = trkptT.eleStr;
            }
            
            if (preImg != null) {
                // simplify distance (m) 
    			GeoPoint prepoint = preImg.getPoint();
    			GeoPoint imapoint = (new GeoPoint()).set(this.latitude, this.longitude);
        		double simplify = params.getSimplifyMeters();
        		if (simplify > 0) {
        			if (prepoint.getDistance(imapoint) < simplify) {
        				this.setEnable(false);
        				return null;
        			}
        		}

        		// MAGVAR
        		if (params.isGpxOverwriteMagvar()) {
                    // 直前の位置と、現在地から進行方向を求める
                	// 経度(longitude)と経度から進行方向を求める
                    if (params.isGpxOverwriteMagvar()) {
                    	trkptT.magvarStr = imapoint.complementationMagvar(prepoint);
                    }

                    // 緯度・経度と時間差から速度(km/h)を求める
                    if (params.isGpxOutputSpeed()) {
						double speed = imapoint.complementationSpeed(prepoint, preImg.imgtime.getTime());
						trkptT.speedStr = String.format("%.1f", speed);
                    }
                }
                if (trkptT.magvarStr != null) {
                	magvarStr = trkptT.magvarStr;
                }
                
                if (trkptT.speedStr != null) {
                	speedStr = trkptT.speedStr;
                }
            }
        }
        return trkptT;
    }
    
    /**
     * ファイルに書き出す
     * @param params :AppParameters
     * @param trkptT
     * @param outDir
     * @return
     * @throws ImageReadException
     * @throws ImageWriteException
     * @throws IOException
     */
    public void exportToFile(AppParameters params, TagTrkpt trkptT, Path outPath) throws ImageReadException, ImageWriteException, IOException {
        File outDir = outPath.toFile();
        outDir.mkdir();
        if (trkptT == null) {
            if (!params.isImgOutputAll()) {
            	this.setEnable(false);
            }
        }
        else {
            if (params.isImgOutputExif()) {
            	// EXIF出力
                exifWrite(this, gpstime, trkptT, outDir);
            }
            else {
                // EXIFの変換を伴わない単純なファイルコピー
                FileInputStream sStream = new FileInputStream(this);
                FileInputStream dStream = new FileInputStream(new File(outDir, this.getName()));
                FileChannel srcChannel = sStream.getChannel();
                FileChannel destChannel = dStream.getChannel();
                try {
                    srcChannel.transferTo(0, srcChannel.size(), destChannel);
                	this.setEnable(true);
                }
                finally {
                    srcChannel.close();
                    destChannel.close();
                    sStream.close();
                    dStream.close();
                }
            }
        }
    }
    
    void exifWrite(File imageFile, Date correctedtime, TagTrkpt trkptT, File outDir) throws ImageReadException, IOException, ImageWriteException {
        DecimalFormat yearFormatter = new DecimalFormat("0000");
        DecimalFormat monthFormatter = new DecimalFormat("00");
        DecimalFormat dayFormatter = new DecimalFormat("00");
        
        TiffOutputSet outputSet = null;

        ImageMetadata meta = Imaging.getMetadata(imageFile);
        JpegImageMetadata jpegMetadata = (JpegImageMetadata)meta;
        if (jpegMetadata != null) {
            TiffImageMetadata exif = jpegMetadata.getExif();
            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }

        if (outputSet == null) {
            outputSet = new TiffOutputSet();
        }

        //---- EXIF_TAG_DATE_TIME_ORIGINAL / 「撮影日時/オリジナル画像の生成日時」----
        TiffOutputDirectory exifDir = outputSet.getOrCreateExifDirectory();
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            cal.setTime(correctedtime);
            exifDir.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            exifDir.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, ImportPicture.toEXIFString(cal.getTime()));
        }

        //---- EXIF GPS_TIME_STAMP ----
        TiffOutputDirectory gpsDir = outputSet.getOrCreateGPSDirectory();
        {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeZone(TimeZone.getTimeZone("GMT+00"));
            cal.setTime(correctedtime);
            final String yearStr = yearFormatter.format(cal.get(Calendar.YEAR));
            final String monthStr = monthFormatter.format(cal.get(Calendar.MONTH) + 1);
            final String dayStr = dayFormatter.format(cal.get(Calendar.DAY_OF_MONTH));
            final String dateStamp = yearStr +":"+ monthStr +":"+ dayStr;

            gpsDir.removeField(GpsTagConstants.GPS_TAG_GPS_TIME_STAMP);
            gpsDir.add(
                GpsTagConstants.GPS_TAG_GPS_TIME_STAMP,
                RationalNumber.valueOf(cal.get(Calendar.HOUR_OF_DAY)),
                RationalNumber.valueOf(cal.get(Calendar.MINUTE)),
                RationalNumber.valueOf(cal.get(Calendar.SECOND))
            );
            gpsDir.removeField(GpsTagConstants.GPS_TAG_GPS_DATE_STAMP);
            gpsDir.add(GpsTagConstants.GPS_TAG_GPS_DATE_STAMP, dateStamp);
        }

        if (trkptT != null) {
            //---- EXIF GPS elevation/ALTITUDE ----
            if (trkptT.eleStr != null) {
                final double altitude = Double.parseDouble(trkptT.eleStr);
                gpsDir.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
                gpsDir.add(GpsTagConstants.GPS_TAG_GPS_ALTITUDE, RationalNumber.valueOf(altitude));
            }

            //---- EXIF GPS magvar/IMG_DIRECTION ----
            if (trkptT.magvarStr != null) {
                final double magvar = Double.parseDouble(trkptT.magvarStr);
                gpsDir.removeField(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION);
                gpsDir.add(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION, RationalNumber.valueOf(magvar));
            }

            //---- EXIF GPS_ ----
            outputSet.setGPSInDegrees(trkptT.lon, trkptT.lat);
        }

        ExifRewriter rewriter = new ExifRewriter();
        try (FileOutputStream fos = new FileOutputStream(new File(outDir, imageFile.getName()))) {
            rewriter.updateExifMetadataLossy(imageFile, fos, outputSet);
        }
    }
    
    /**
     * 基準時刻ファイルの「更新日時」を取得する
     * @param baseFile = new File(this.imgDir, this.params.getProperty(AppParameters.IMG_BASE_FILE));
     * @return
     * @throws ImageReadException
     * @throws IOException
     * @throws ParseException
     */
    Date getDate(AppParameters params) throws ImageReadException, IOException, ParseException {
    	return getDate(params, this);
    }

    /**
     * 基準時刻ファイルの「更新日時」を取得する
     * @param baseFile = new File(this.imgDir, this.params.getProperty(AppParameters.IMG_BASE_FILE));
     * @return
     * @throws ImageReadException
     * @throws IOException
     * @throws ParseException
     */
    static Date getDate(AppParameters params, File baseFile) throws ImageReadException, IOException, ParseException {
        if (params.isExifBase()) {
            // 基準時刻（EXIF撮影日時)
            ImageMetadata meta = Imaging.getMetadata(baseFile);
            JpegImageMetadata jpegMetadata = (JpegImageMetadata)meta;
            if (jpegMetadata == null) {
                // "'%s'にEXIF情報がありません"
                throw new ImageReadException(
                    String.format(
                        ImportPicture.i18n.getString("msg.140"), 
                        baseFile.getAbsolutePath()
                    )
                );
            }
            TiffImageMetadata exif = jpegMetadata.getExif();
            if (exif == null) {
                // "'%s'にEXIF情報がありません"
                throw new ImageReadException(
                    String.format(
                    	ImportPicture.i18n.getString("msg.140"), 
                        baseFile.getAbsolutePath()
                    )
                );
            }
            String dateTimeOriginal = exif.getFieldValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)[0];
            return new Date(ImportPicture.toEXIFDate(dateTimeOriginal).getTime());
        }
        else {
            // 基準時刻（ファイル更新日時)
            return new Date(baseFile.lastModified());
        }
    }
    
    public GeoPoint getPoint() {
    	if (!isDone()) {
    		return null;
    	}
    	return (new GeoPoint()).set(this.latitude, this.longitude);
    }
    
    /**
     * ImgFileインスタンスの状態をTEXT化
     * @return	１行
     */
    String toText() {
    	String ret = "";
    	if (isDone()) {
            ret += (String.format("|%-32s|", this.getName()));
            ret += (String.format("%20s|", (imgtime==null ? "" : ImportPicture.toUTCString(imgtime))));
            ret += (String.format("%20s|", (gpstime==null ? "" : ImportPicture.toUTCString(gpstime))));
            ret += (String.format("%14.10f|%14.10f|", latitude, longitude));
            ret += (String.format("%8s|%6s|%6s|", eleStr, magvarStr, speedStr));
    	}
    	else {
            ret += (String.format("|%-32s|", this.getName()));
            ret += (String.format("%20s|", (imgtime==null ? "" : ImportPicture.toUTCString(imgtime))));
            ret += (String.format("%20s|", (gpstime==null ? "" : ImportPicture.toUTCString(gpstime))));
            ret += (String.format("%-14s|%-14s|", "", ""));
            ret += (String.format("%8s|%6s|%6s|", "", "", ""));
    	}
        return ret;
    }
    
    public void printinfo() {
        System.out.println(toText());
    }
    
    public static void printheader() {
        System.out.println("|--------------------------------|------------------------|------------------------|--------------|--------------|--------|------|------|");
        System.out.println("| name                           | Camera Time            | GPStime                |   Latitude   |   Longitude  | ele    |magvar| km/h |");
        System.out.println("|--------------------------------|------------------------|------------------------|--------------|--------------|--------|------|------|");
    }
    
    public static void printfooter() {
        System.out.println("|--------------------------------|------------------------|------------------------|--------------|--------------|--------|------|------|");
        System.out.println();
    }
}
