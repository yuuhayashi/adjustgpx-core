package osm.jp.gpx;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.experimental.runners.*;

@RunWith(Enclosed.class)
public class AppParametersTest {

    public static class 定義ファイルが存在しない場合 {

        @Before
        public void setUp() throws Exception {
        	delTestData("AdjustTime.ini");
        }

        @After
        public void tearDown() throws Exception {
        	delTestData("AdjustTime.ini");
        }

        @Test
        public void IMG_OUTPUT_ALLが定義されていない時() {
            try {
            	AppParameters params = new AppParameters("target/test-classes/AdjustTime.off.ini");
                String valueStr = params.getProperty(AppParameters.IMG_OUTPUT_ALL);
                assertFalse(Boolean.getBoolean(valueStr));
            }
            catch (Exception e) {
                fail("Exceptionが発生した。");
			}
        }
    }

    public static class 定義ファイルがtureに定義されているとき {

        @Before
        public void setUp() throws Exception {
        	delTestData("AdjustTime.ini");
        	setupTestData("AdjustTime.on.ini", "AdjustTime.ini");
        }

        @After
        public void tearDown() throws Exception {
        	delTestData("AdjustTime.ini");
        }

        @Test
        public void IMG_OUTPUT_ALLがtureに定義されているとき() {
            try {
                AppParameters params = new AppParameters();
                String valueStr = params.getProperty(AppParameters.IMG_OUTPUT_ALL);
                assertTrue(Boolean.valueOf(valueStr));
            }
            catch (Exception e) {
                fail("Exceptionが発生した。");
            }
        }

        @Test
        public void IMG_OUTPUT_ALLをfalseに書き換える() {
            try {
                AppParameters params = new AppParameters();
                params.setProperty(AppParameters.IMG_OUTPUT_ALL, "false");
                params.store();
                AppParameters newParams = new AppParameters();
                String valueStr = newParams.getProperty(AppParameters.IMG_OUTPUT_ALL);
                assertFalse(Boolean.valueOf(valueStr));
            }
            catch (Exception e) {
                fail("Exceptionが発生した。");
            }
        }
    }

    public static class 定義ファイルがfalseに定義されているとき {

        @Before
        public void setUp() throws Exception {
        	delTestData("AdjustTime.ini");
        	setupTestData("AdjustTime.off.ini", "AdjustTime.ini");
        }

        @After
        public void tearDown() throws Exception {
        	delTestData("AdjustTime.ini");
        }

        @Test
        public void IMG_OUTPUT_ALLがfalseに定義されているとき() {
            try {
                AppParameters params = new AppParameters();
                String valueStr = params.getProperty(AppParameters.IMG_OUTPUT_ALL);
                assertFalse(Boolean.valueOf(valueStr));
            }
            catch (Exception e) {
                fail("Exceptionが発生した。");
            }
        }

        @Test
        public void IMG_OUTPUT_ALLをtrueに書き換える() {
            try {
                AppParameters params = new AppParameters();
                params.setProperty(AppParameters.IMG_OUTPUT_ALL, "true");
                params.store();
                AppParameters newParams = new AppParameters();
                String valueStr = newParams.getProperty(AppParameters.IMG_OUTPUT_ALL);
                assertTrue(Boolean.valueOf(valueStr));
            }
            catch (Exception e) {
                fail("Exceptionが発生した。");
            }
        }
    }
    
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
