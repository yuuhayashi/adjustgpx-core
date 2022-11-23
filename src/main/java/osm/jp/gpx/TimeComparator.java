package osm.jp.gpx;

import java.util.Comparator;
import java.util.Date;

/**
 * java.util.Date型をコレクションのKEYにした時に、時間順に並べ替える
 * 
 */
public class TimeComparator implements Comparator<Date> 
{
    /**
     * 日付順にソート
     * @param arg0
     * @param arg1
     */
    @Override
    public int compare(Date arg0, Date arg1) {
        return arg0.compareTo(arg1);
    }
}
