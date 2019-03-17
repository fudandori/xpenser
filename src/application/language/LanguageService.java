package application.language;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LanguageService {

	public static Map<String, String> getWords(String locale) throws IOException {
		String path = System.getProperty("user.dir") + File.separatorChar + "i18n" + File.separatorChar + locale
				+ ".lang";
		Map<String, String> map = new HashMap<>();

		File file = new File(path);

		if (file.exists()) {
			Files.lines(Paths.get(path), Charset.forName("ISO-8859-1"))
					.forEach(line -> map.put(line.split(":")[0], line.split(":")[1]));
		}

		return map;
	}
}
