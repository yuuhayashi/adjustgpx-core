package osm.jp.gpx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.runner.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.*;

@RunWith(Enclosed.class)
public class ImportPictureUnitTest {

    public static class NoExist_outputDir {
    	
    	@Before
        public void setUp() throws Exception {
    		Fixture dataset = Fixture.datas[0];
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
            //outDir.mkdir();

            // カメラディレクトリを作成する
            UnZip.uncompress(new File(dataset.tarFilePath), new File("target/test-classes/cameradata"));
        }


        @Test
        public void test() throws Exception {
    		Fixture dataset = Fixture.datas[0];
            try {
                ImportPictureUnitTest.testdo(dataset.iniFilePath);
            }
            catch (Exception e) {
                e.printStackTrace();
                fail("Exceptionが発生した。");
            }

            AppParameters params = new AppParameters(dataset.iniFilePath);
            File outDir = new File(params.getProperty(AppParameters.IMG_OUTPUT_FOLDER));
            assertThat(outDir.exists(), is(true));
            
            dataset.check();
        }

        @Test
        public void testMAGVAR_ON() throws Exception {
    		Fixture dataset = Fixture.datas[1];
            try {
                ImportPictureUnitTest.testdo(dataset.iniFilePath);
            }
            catch (Exception e) {
                e.printStackTrace();
                fail("Exceptionが発生した。");
            }

            AppParameters params = new AppParameters(dataset.iniFilePath);
            File outDir = new File(params.getProperty(AppParameters.IMG_OUTPUT_FOLDER));
            assertThat(outDir.exists(), is(true));
            
            dataset.check();
        }

        static String comparePosition(double b) {
            return String.format("%.4f", b);
        }
    }

    /**
     * 出力ディレクトリがFILEの時
     */
    public static class OutputDirIsFile {
    	
    	@Before
        public void setUp() throws Exception {
    		Fixture dataset = Fixture.datas[0];
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
            
            // ファイルを生成
            outDir.createNewFile();

            // カメラディレクトリを作成する
            UnZip.uncompress(new File(dataset.tarFilePath), new File("target/test-classes/cameradata"));
        }


        @Test
        public void test() throws Exception {
    		Fixture dataset = Fixture.datas[0];
    		try {
                ImportPictureUnitTest.testdo(dataset.iniFilePath);
                fail("outDirがFILEなのに、例外が発生しなかった");	// 例外が発生しなかった
    		}
    		catch (Exception e) {
    			// 例外が発生する
    			assertThat(true, is(true));
    		}

            AppParameters params = new AppParameters(dataset.iniFilePath);
            File outDir = new File(params.getProperty(AppParameters.IMG_OUTPUT_FOLDER));
            assertThat(outDir.exists(), is(true));
        }

        static String comparePosition(double b) {
            return String.format("%.4f", b);
        }
    }

    
    /**
     * 実行する
     * @throws Exception
     */
    static void testdo(String iniFilePath) throws Exception {
        String[] argv = {iniFilePath};
        ImportPicture.main(argv);
    }
}