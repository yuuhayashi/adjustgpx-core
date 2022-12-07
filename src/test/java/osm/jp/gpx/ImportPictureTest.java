package osm.jp.gpx;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.junit.runner.*;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;

@RunWith(Theories.class)
public class ImportPictureTest {

    @DataPoints
    public static Fixture[] datas = Fixture.datas;

    /**
     * パラメータテスト
     * @param dataset
     * @throws Exception
     */
    @Theory
    public void testParameter(Fixture dataset) throws Exception {
        setup(dataset);
        testdo(dataset.iniFilePath);
        dataset.check();
    }

    void setup(Fixture dataset) throws IOException {
        System.out.println(dataset.toString());

        // カメラディレクトリを削除する
        File dir = new File("target/test-classes/cameradata");
        if (dir.exists()) {
            UnZip.delete(dir);
        }
        File outDir = new File("target/test-classes/output");
        if (outDir.exists()) {
        	UnZip.delete(outDir);
        }
        outDir.mkdir();

        // カメラディレクトリを作成する
        UnZip.uncompress(new File(dataset.tarFilePath), new File("target/test-classes/cameradata"));

        // GPXファイルをセット
        //copy(new File(dataset.gpxSourcePath), new File(dataset.gpxDestinationPath));
    }
    
    void copy(File source, File dest) throws IOException {
    	if (source.isDirectory()) {
    		File[] files = source.listFiles();
    		for (int i = 0; i < files.length; i++) {
				File file = files[i];
				copy(file, dest);
			}
    	}
    	else {
            try (FileInputStream inStream = new FileInputStream(source);
                FileOutputStream outStream = new FileOutputStream(dest);
                FileChannel inChannel = inStream.getChannel();
                FileChannel outChannel = outStream.getChannel())
            {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
    	}
    }

    /**
     * 実行する
     * @throws Exception
     */
    void testdo(String iniFilePath) {
        try {
            String[] argv = {iniFilePath};
            ImportPicture.main(argv);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Exceptionが発生した。");
        }
    }
}