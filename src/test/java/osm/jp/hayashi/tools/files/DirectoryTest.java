package osm.jp.hayashi.tools.files;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DirectoryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
	public void testCurrent() {
		try {
			Path path = Directory.getCurrentDirectory();
			System.out.println("CurrentDirectory: "+ path.toAbsolutePath().toString());
            assertThat(path.isAbsolute(), is(true));
            assertThat(Files.isDirectory(path), is(true));
            assertThat(Files.exists(path), is(true));
		}
		catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testClass() {
		try {
			Path path = Directory.getClassDirectory();
			System.out.println("ClassDirectory: " + path.toAbsolutePath().toString());
            assertThat(path.isAbsolute(), is(true));
            assertThat(Files.isDirectory(path), is(true));
            assertThat(Files.exists(path), is(true));
		}
		catch (Exception e) {
			fail();
		}
	}

}
