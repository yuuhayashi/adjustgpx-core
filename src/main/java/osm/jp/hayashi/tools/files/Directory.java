package osm.jp.hayashi.tools.files;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public abstract class Directory {

	public static Path getClassDirectory(Class<?> cls) throws URISyntaxException {
		ProtectionDomain pd = cls.getProtectionDomain();
		CodeSource cs = pd.getCodeSource();
		URL location = cs.getLocation();
		URI uri = location.toURI();
		Path path = Paths.get(uri);
		if (!Files.isDirectory(path)) {
			return path.getParent();
		}
		return path;
	}
	
	public static Path getClassDirectory() throws URISyntaxException {
		return getClassDirectory(Directory.class);
	}

	public static Path getCurrentDirectory() throws URISyntaxException {
		return Paths.get(System.getProperty("user.dir"));
	}
}
