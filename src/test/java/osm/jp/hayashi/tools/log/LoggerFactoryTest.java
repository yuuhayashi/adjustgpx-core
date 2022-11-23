package osm.jp.hayashi.tools.log;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class LoggerFactoryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Path path = Paths.get("test1.log");
		if (Files.exists(path)) {
			path.toFile().delete();
		}
		
		path = Paths.get("Logging%u.%g.log");
		if (Files.exists(path)) {
			path.toFile().delete();
		}
		
		try {
			Path dir = Paths.get(System.getProperty("user.dir"));
			Files.list(dir).forEach(new Consumer<Path>() {
				@Override
				public void accept(Path a) {
					if (!Files.isDirectory(a)) {
						String name = a.getFileName().toString();
						if (name.startsWith("Logging") && name.endsWith(".log")) {
							a.toFile().delete();
						}
					}
				}
			});
		} catch (IOException e) {}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() {
		Logger logger;
		try {
			logger = Logger.getLogger("log");
			FileHandler fHandler = new FileHandler("test1.log", true);
			fHandler.setFormatter(new YuuLogFormatter());
			logger.addHandler(fHandler);
			
			logger.finest("詳細レベル（高）");
			logger.finer("詳細レベル（中）");
			logger.fine("詳細レベル（小）");
			logger.config("設定");
			logger.info("情報");
			logger.warning("警告");
			logger.severe("致命的");
		}
		catch (Exception e) {
			fail();
		}
		Path path = Paths.get("test1.log");
		assertThat(Files.exists(path), is(true));
		assertThat(Files.isDirectory(path), is(false));
		try {
			final BufferedReader fileReader = Files.newBufferedReader(path);
			
			String data = fileReader.readLine();
			assertThat(data == null, is(false));
			assertThat(data.endsWith(" 詳細レベル（高）"), is(true));
			
			data = fileReader.readLine();
			assertThat(data == null, is(false));
			assertThat(data.endsWith(" 詳細レベル（中）"), is(true));
			
			data = fileReader.readLine();
			assertThat(data == null, is(false));
			assertThat(data.endsWith(" 詳細レベル（小）"), is(true));
			
			data = fileReader.readLine();
			assertThat(data == null, is(false));
			assertThat(data.endsWith(" 設定"), is(true));
			
			data = fileReader.readLine();
			assertThat(data == null, is(false));
			assertThat(data.endsWith(" 情報"), is(true));
			
			data = fileReader.readLine();
			assertThat(data == null, is(false));
			assertThat(data.endsWith(" 警告"), is(true));
			
			data = fileReader.readLine();
			assertThat(data == null, is(false));
			assertThat(data.endsWith(" 致命的"), is(true));
			
			data = fileReader.readLine();
			assertThat(data == null, is(true));
		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void test2() {
		Logger logger;
		try {
			logger = LoggerFactory.getInstance();
			logger.finest("詳細レベル（高）");
			logger.finer("詳細レベル（中）");
			logger.fine("詳細レベル（小）");
			logger.config("設定");
			logger.info("情報");
			logger.warning("警告");
			logger.severe("致命的");
		}
		catch (Exception e) {
			fail();
		}
		Path path = Paths.get("Logging0.0.log");
		assertThat(Files.exists(path), is(true));
		assertThat(Files.isDirectory(path), is(false));
		try {
			final BufferedReader fileReader = Files.newBufferedReader(path);
			String data = fileReader.readLine();
			assertThat(data == null, is(false));
			assertThat(data.endsWith(" 情報"), is(true));
			
			data = fileReader.readLine();
			assertThat(data == null, is(false));
			assertThat(data.endsWith(" 警告"), is(true));
			
			data = fileReader.readLine();
			assertThat(data == null, is(false));
			assertThat(data.endsWith(" 致命的"), is(true));
			
			data = fileReader.readLine();
			assertThat(data == null, is(true));
		} catch (IOException e) {
			fail();
		}
	}

}
