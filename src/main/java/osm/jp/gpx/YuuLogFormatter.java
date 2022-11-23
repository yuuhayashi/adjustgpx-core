package osm.jp.gpx;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * シンプルなサンプルログフォーマッタ
 */
public class YuuLogFormatter extends Formatter {
    private final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(final LogRecord argLogRecord) {
        final StringBuffer buf = new StringBuffer();

        buf.append(sdFormat.format(new Date(argLogRecord.getMillis()))).append(" ");

        if (argLogRecord.getLevel() == Level.FINEST) {
            buf.append("[FINEST]");
        }
        else if (argLogRecord.getLevel() == Level.FINER) {
            buf.append("[FINER]");
        }
        else if (argLogRecord.getLevel() == Level.FINE) {
            buf.append("[FINE]");
        }
        else if (argLogRecord.getLevel() == Level.CONFIG) {
            buf.append("[CONFIG]");
        }
        else if (argLogRecord.getLevel() == Level.INFO) {
            buf.append("[INFO]");
        }
        else if (argLogRecord.getLevel() == Level.WARNING) {
            buf.append("[WARN]");
        }
        else if (argLogRecord.getLevel() == Level.SEVERE) {
            buf.append("[SEVERE]");
        }
        else {
            buf.append(Integer.toString(argLogRecord.getLevel().intValue())).append(" ");
        }
        buf.append(" ").append(argLogRecord.getMessage()).append("\n");
        return buf.toString();
    }
}