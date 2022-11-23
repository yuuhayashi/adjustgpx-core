package osm.jp.hayashi.tools.log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * ロギングファイルに動作ログを出力する簡単なモデル
 * "log"+日時+".log"ファイルに出力される。
 * 利用例：
 * (1) インスタンスを取得する。
 * 		Logger logger = LoggerFactory.getInstance();
 * (2) ログ出力例
 * 		logger.finest("[finest] 詳細レベル（高）");
 * 		logger.finer("[finer] 詳細レベル（中）");
 * 		logger.fine("[fine] 詳細レベル（小）");
 * 		logger.config("[config] 設定");
 * 		logger.info("[info] 情報");
 * 		logger.warning("[warning] 警告");
 * 		logger.severe("[severe] 致命的");
 * @author yuu
 * @version 2010/02/07
 * @since 2010/02/07
 */
public abstract class LoggerFactory
{
	public static void main(String[] args) {
		/*
		 * （設定例）
		 * 		handlers=java.util.logging.ConsoleHandler, java.util.logging.FileHandler
		 * 		.level=FINEST
		 *
		 * 		java.util.logging.ConsoleHandler.level=FINEST
		 * 		java.util.logging.ConsoleHandler.formatter=hayashi.yuu.tools.logger.YuuLogFormatter
		 *
		 * 		java.util.logging.FileHandler.level=WARNING
		 * 		java.util.logging.FileHandler.pattern=SampleLogging%u.%g.log
		 * 		java.util.logging.FileHandler.formatter=hayashi.yuu.tools.logger.YuuLogFormatter
		 * 		java.util.logging.FileHandler.count=10
		 *
		 *
		 * 標準設定時でのログ出力。
		 * info、warning、severeの３つのレベルのみ標準エラー出力に出力されます。
		 * また、同時にファイルへも出力します。
		 * 出力先ファイルは「Logging%u.%g.txt」。ログファイルは10個でローテーションする。
		 *
		 * 情報: [info] 情報
		 * 警告: [warning] 警告
		 * 致命的: [severe] 致命的
		 */
		Logger logger;
		try {
			logger = Logger.getLogger("log");
			FileHandler fHandler = new FileHandler("Sample.log", true);
			fHandler.setFormatter(new YuuLogFormatter());
			logger.addHandler(fHandler);
			
			/*
			logger = LoggerFactory.getInstance();
			*/
			logger.finest("[finest] 詳細レベル（高）");
			logger.finer("[finer] 詳細レベル（中）");
			logger.fine("[fine] 詳細レベル（小）");
			logger.config("[config] 設定");
			logger.info("[info] 情報");
			logger.warning("[warning] 警告");
			logger.severe("[severe] 致命的");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
	 * ログ設定プロパティファイルのファイル内容
	 *
	 */
	protected static final String LOGGING_PROPERTIES = "log.properties";

	/**
	 * 簡単な標準ロガーを得る
	 * @return	標準ロガー
	 */
	public static Logger getInstance() {
		final Logger logger = Logger.getLogger("log");							// Loggerオブジェクトの生成
		LogManager manager = LogManager.getLogManager();
		try {
			manager.readConfiguration(new FileInputStream(LOGGING_PROPERTIES));
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		return logger;
	}
}
