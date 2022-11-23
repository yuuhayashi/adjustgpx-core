package osm.jp.gpx;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class ElementMapTRKPTTest {

    public static class Keyのみ {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        ElementMapTRKPT map = null;
        long timeL;
        static String[] values = {
            "1970-01-01 08:59:59.999",
            "1970-01-01 09:00:00.000",
            "1970-01-01 09:00:00.001",
            "2018-10-25 07:59:59.999",
            "2018-10-25 08:00:00.000",
            "2018-10-25 08:00:00.001"
        };

        @Before
        public void setUp() throws Exception {
            timeL = (sdf.parse("2018-10-25 08:00:00.000")).getTime();
            map = new ElementMapTRKPT(new AppParameters(AppParameters.FILE_PATH));
            map.put(new Date(timeL), null);			// 5-6: 2018-10-25 08:00:00.000
            map.put(new Date(timeL + 1L), null);	// 7: 2018-10-25 08:00:00.001
            map.put(new Date(timeL - 1L), null);	// 4: 2018-10-25 07:59:59.999
            map.put(new Date(1L), null);			// 3: 1970-01-01 09:00:00.001
            map.put(new Date(0L), null);			// 2: 1970-01-01 09:00:00.000
            map.put(new Date(-1L), null);			// 1: 1970-01-01 08:59:59.999
            map.put(new Date(timeL), null);			// 5-6: 2018-10-25 08:00:00.000
        }

        @After
        public void tearDown() throws Exception {
        	AppParametersTest.delTestData("AdjustTime.ini");
        }

        @Test
        public void 同一キーをPUTした場合() {
            assertThat(map.size(), is(6));
        }

        @Test
        public void イテレータを使って読みだす() {
            assertThat(map.size(), is(6));

            int i = 0;
            for (Date key : map.keySet()) {
                assertThat(sdf.format(key), is(values[i++]));
            }
        }

        @Test
        public void 拡張FOR文を使って読みだす() {
            assertThat(map.size(), is(6));

            int i = 0;
            for (Date key : map.keySet()) {
                assertThat(sdf.format(key), is(values[i++]));
            }
        }
    }

    public static class Keyとvalueのセット {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        ElementMapTRKPT map = null;
        long timeL;

        /*
         * <trkpt lat="35.8812697884" lon="137.9952202085"><time>2017-05-29T01:23:18Z</time></trkpt>
         * <trkpt lat="35.8811769169" lon="137.9951928835"><time>2017-05-29T01:23:21Z</time><ele>614.90</ele></trkpt>
         * <trkpt lat="35.881112963" lon="137.9951796401"><time>2017-05-29T01:23:24Z</time><ele>615.00</ele></trkpt>
         * <trkpt lat="35.881072646" lon="137.9951728508"><time>2017-05-29T01:23:27Z</time><ele>615.03</ele></trkpt>
         */
        static String[][] values = {
            {"2017-05-29T01:23:18Z", "35.8812697884", "137.9952202085", null},
            {"2017-05-29T01:23:21Z", "35.8811769169", "137.9951928835", "614.90"},
            {"2017-05-29T01:23:24Z", "35.881112963", "137.9951796401", "615.00"},
            {"2017-05-29T01:23:27Z", "35.881072646", "137.9951728508", "615.03"}
        };

        TagTrkpt createElement(String[] values) throws ParseException {
        	TagTrkpt trkpt = new TagTrkpt(Double.valueOf(values[1]), Double.valueOf(values[2]));
            trkpt.setTime(ImportPicture.toUTCDate(values[0]));
            if (values[3] != null) {
                trkpt.setEle(values[3]);
            }
            return trkpt;
        }

        @Before
        public void setUp() throws Exception {
            AppParameters params = new AppParameters(AppParameters.FILE_PATH);
            params.setGpxOverwriteMagvar(true);

            map = new ElementMapTRKPT(params);
            for (int cnt = values.length; cnt > 0; cnt--) {
                map.put(createElement(values[cnt - 1]));
            }
        }

        @After
        public void tearDown() throws Exception {
        	AppParametersTest.delTestData("AdjustTime.ini");
        }

        @Test
        public void コンテンツの数をチェック() {
            assertThat(map.size(), is(4));
        }

        @Test
        public void KEYが時間順に取り出せるか() {
            int i = 0;
            for (Date key : map.keySet()) {
                try {
                    String s = sdf.format(ImportPicture.toUTCDate(values[i++][0]));
                    assertThat(sdf.format(key), is(s));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        @Test
        public void get_17() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:17Z"));
            assertThat(tag, is(nullValue()));
        }

        @Test
        public void get_18() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:18Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:18Z"));
            assertThat(tag.eleStr, is(nullValue()));
            assertThat(tag.lat, is(Double.valueOf(values[0][1])));
            assertThat(tag.lon, is(Double.valueOf(values[0][2])));
            assertNull(tag.magvarStr);
        }

        @Test
        public void get_19() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:19Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:18Z"));
            assertNull(tag.eleStr);
            assertThat(tag.lat, is(Double.valueOf(values[0][1])));
            assertThat(tag.lon, is(Double.valueOf(values[0][2])));
            assertNull(tag.magvarStr);
        }

        @Test
        public void get_20() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:20Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:18Z"));
            assertNull(tag.eleStr);
            assertThat(tag.lat, is(Double.valueOf(values[0][1])));
            assertThat(tag.lon, is(Double.valueOf(values[0][2])));
            assertNull(tag.magvarStr);
        }

        @Test
        public void get_21() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:21Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:21Z"));
            assertThat(tag.eleStr, is("614.90"));
            assertThat(tag.lat, is(Double.valueOf(values[1][1])));
            assertThat(tag.lon, is(Double.valueOf(values[1][2])));
            assertNotNull(tag.magvarStr);
        }

        @Test
        public void get_22() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:22Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:21Z"));
            assertThat(tag.eleStr, is("614.90"));
            assertThat(tag.lat, is(Double.valueOf(values[1][1])));
            assertThat(tag.lon, is(Double.valueOf(values[1][2])));
            assertNotNull(tag.magvarStr);
        }

        @Test
        public void get_23() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:23Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:21Z"));
            assertThat(tag.eleStr, is("614.90"));
            assertThat(tag.lat, is(Double.valueOf(values[1][1])));
            assertThat(tag.lon, is(Double.valueOf(values[1][2])));
            assertNotNull(tag.magvarStr);
        }

        @Test
        public void get_24() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:24Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:24Z"));
            assertThat(tag.eleStr, is("615.00"));
            assertThat(tag.lat, is(Double.valueOf(values[2][1])));
            assertThat(tag.lon, is(Double.valueOf(values[2][2])));
            assertThat(tag.magvarStr, is(notNullValue()));
        }

        @Test
        public void get_25() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:25Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:24Z"));
            assertThat(tag.eleStr, is("615.00"));
            assertThat(tag.lat, is(Double.valueOf(values[2][1])));
            assertThat(tag.lon, is(Double.valueOf(values[2][2])));
            assertThat(tag.magvarStr, is(notNullValue()));
        }

        @Test
        public void get_26() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:26Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:24Z"));
            assertThat(tag.eleStr, is("615.00"));
            assertThat(tag.lat, is(Double.valueOf(values[2][1])));
            assertThat(tag.lon, is(Double.valueOf(values[2][2])));
            assertThat(tag.magvarStr, is(notNullValue()));
        }

        @Test
        public void get_27() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:27Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:27Z"));
            assertThat(tag.eleStr, is("615.03"));
            assertThat(tag.lat, is(Double.valueOf(values[3][1])));
            assertThat(tag.lon, is(Double.valueOf(values[3][2])));
            assertNotNull(tag.magvarStr);
        }

        @Test
        public void get_28() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:28Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:27Z"));
            assertThat(tag.eleStr, is("615.03"));
            assertThat(tag.lat, is(Double.valueOf(values[3][1])));
            assertThat(tag.lon, is(Double.valueOf(values[3][2])));
            assertThat(tag.magvarStr, is(notNullValue()));
        }

        @Test
        public void get_30() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:30Z"));
            assertThat(sdf.format(tag.time), is("2017-05-29T10:23:27Z"));
            assertThat(tag.eleStr, is("615.03"));
            assertThat(tag.lat, is(Double.valueOf(values[3][1])));
            assertThat(tag.lon, is(Double.valueOf(values[3][2])));
            assertThat(tag.magvarStr, is(notNullValue()));
        }

        @Test
        public void get_31() throws ParseException {
            TagTrkpt tag = map.getValue(ImportPicture.toUTCDate("2017-05-29T01:23:31Z"));
            assertThat(tag, is(nullValue()));
        }
    }

    public static class タイムスタンプの書式 {
        @Test
        public void EXIF時刻書式テスト() throws Exception {
            String dateTimeOriginal = "2017:06:30 09:59:59";
            Date time = ImportPicture.toEXIFDate(dateTimeOriginal);
            assertThat(ImportPicture.toEXIFString(time), is("2017:06:30 09:59:59"));
            assertThat(ImportPicture.toUTCString(time), is("2017-06-30T00:59:59Z"));
            DateFormat dfUTC = new SimpleDateFormat(ImportPicture.TIME_FORMAT_STRING);
            assertThat(dfUTC.format(time), is("2017-06-30T09:59:59Z"));
        }
    }
}
