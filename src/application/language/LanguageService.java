package application.language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LanguageService {

	public static Map<String, String> getWords(String locale) throws IOException {
		
		BufferedReader reader = getBuffer(locale);

		return getMap(reader);
	}

	private static BufferedReader getBuffer(String locale) {
		InputStream in = LanguageService.class.getClassLoader().getResourceAsStream(getPath(locale));
		
		return new BufferedReader(new InputStreamReader(in));
	}
	
	private static String getPath(String locale) {
		
		locale = existsLocale(locale) ? locale : "en";
		
		return "application/language/i18n/" + locale + ".lang";
	}
	
	private static Map<String, String> getMap(BufferedReader reader) throws IOException {
		Map<String, String> map = new HashMap<>();
		
		String line;
		
		while((line = reader.readLine()) != null) {
			map.put(line.split(":")[0], line.split(":")[1]);
		}

		return map;
	}
	
	public static boolean existsLocale(String locale) {
		return LanguageService.class.getClassLoader().getResource("application/language/i18n/" + locale + ".lang") != null;
	}
}
