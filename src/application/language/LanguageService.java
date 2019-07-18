package application.language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LanguageService {

	public static Map<String, String> getWords(String locale) throws IOException {
		
		BufferedReader reader = getBuffer(locale);

		return getMap(reader);
	}

	private static BufferedReader getBuffer(String locale) {
		InputStreamReader reader = new InputStreamReader(LanguageService.class.getClass().getResourceAsStream(getPath(locale)));
		return new BufferedReader(reader);
	}
	
	private static String getPath(String locale) {
		
		locale = existsLocale(locale) ? locale : "en";
		
		return "/assets/i18n/" + locale + ".lang";
	}
	
	private static Map<String, String> getMap(BufferedReader reader) throws IOException {
		Map<String, String> map = new HashMap<>();
		
		String line;
		
		while((line = reader.readLine()) != null) {
			map.put(line.split(":")[0], line.split(":")[1]);
		}
		
		reader.close();

		return map;
	}
	
	public static boolean existsLocale(String locale) {
		return LanguageService.class.getClass().getResource("/assets/i18n/" + locale + ".lang") != null;
	}
}
