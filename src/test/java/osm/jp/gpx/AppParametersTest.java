package osm.jp.gpx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.junit.runner.RunWith;
import org.junit.experimental.runners.*;

@RunWith(Enclosed.class)
public class AppParametersTest {

    public static void delTestData(String filename) {
        File iniFile = new File(filename);
        if (iniFile.exists()) {
            iniFile.delete();
        }
    }
    
    static void setupTestData(String sfilename, String dfilename) throws IOException {
        File testFile = new File("target/test-classes/ini", sfilename);
        FileInputStream inStream = new FileInputStream(testFile);
        FileOutputStream outStream = new FileOutputStream(new File(dfilename));
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(),outChannel);
        }
        finally {
            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();
            inStream.close();
            outStream.close();
        }
    }
}
