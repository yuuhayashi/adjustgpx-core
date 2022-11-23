package osm.jp.gpx;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public class UnZip {

	/**
     * *.tar.gz解凍
     * ファイル更新日時をオリジナルと同じにします。
     * @param tazFile 解凍する*.tar.gzファイル
     * @param dest 解凍先フォルダ
     * @throws IOException 
     */
    public static void uncompress(File tazFile, File dest) throws IOException {
        dest.mkdir();
        
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(tazFile))))) {
            TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
            while (tarEntry != null) {
                File destPath = new File(dest, tarEntry.getName());
                //System.out.println("uncompress: " + destPath.getCanonicalPath());
                if (tarEntry.isDirectory()) {
                    destPath.mkdirs();
                }
                else {
                    File dir = new File(destPath.getParent());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    destPath.createNewFile();
                    byte[] btoRead = new byte[1024];
                    try (BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destPath))) {
                        int len;
                        while ((len = tarIn.read(btoRead)) != -1) {
                            bout.write(btoRead, 0, len);
                        }
                    }
                    destPath.setLastModified(tarEntry.getLastModifiedDate().getTime());
                }
                tarEntry = tarIn.getNextTarEntry();
            }
        }
    }

    /**
     * Zipファイルを展開します
     * @param aZipFile zipファイル
     * @param aOutDir  出力先ディレクトリ
     * @throws java.io.IOException
     */
    public static void decode(File aZipFile, String aOutDir) throws IOException {
        FileInputStream  fileIn  = null;
        FileOutputStream fileOut = null;
        ZipInputStream zipIn = null;
        
        try {
            File outDir = new File(aOutDir);
            outDir.mkdirs();
            
            fileIn = new FileInputStream(aZipFile);
            zipIn = new ZipInputStream(fileIn);
            
            ZipEntry entry = null;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    String relativePath = entry.getName();
                    outDir = new File(outDir, relativePath);
                    outDir.mkdirs();
                }
                else {
                    String relativePath = entry.getName();
                    File outFile = new File( outDir, relativePath );
                    
                    File parentFile = outFile.getParentFile();
                    parentFile.mkdirs();
                    
                    fileOut = new FileOutputStream( outFile );
                    
                    byte[] buf = new byte[ 256 ];
                    int size = 0;
                    while ((size = zipIn.read(buf)) > 0){
                        fileOut.write(buf, 0, size);
                    }
                    fileOut.close();
                    fileOut = null;
                }
                zipIn.closeEntry();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (fileIn != null) {
                try {
                    fileIn.close();
                }
                catch (IOException e) {}
            }
            if (fileOut != null) {
                try {
                    fileOut.close();
                }
                catch(IOException e) {}
            }
            zipIn.close();
        }
    }
    

    public static void delete(File file) throws IOException {
        if (!file.exists()) {
            System.out.println("ERROR: ファイルまたはディレクトリが見つかりませんでした。");
            throw new IOException("File not found.");
        }
        
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    delete(file1); // 再帰呼び出し
                }
            }
        }
        if (!file.delete()) {
            System.out.println("ERROR: ファイルは削除できませんでした。 '" + file.getAbsolutePath() +"'");
        }
    }
}
