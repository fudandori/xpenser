package application;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;

import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Support functions and variables for Xpender
 */
public final class Utility {
	
	private Utility() {}
	
	/**
	 * Path pointing to the config file inside the user's home folder
	 */
	public static final String configPath = System.getProperty("user.home") + File.separator + "Xpenser" + File.separator + "config.xpr";
	
	/**
	 * Adds a row to the GridPane at the indicated index
	 * @param grid - GridPane where the row will be inserted
	 * @param row - data of the row. The key goes in the left column of the GridPane while the value will go inside the right column
	 * @param rowIndex - the line index at which the row will be inserted into the GridPane
	 */
	static void addRow(GridPane grid, Entry<String, Float> row, int rowIndex) {
		Label key = new Label(row.getKey());
		Label value = new Label(Float.toString(row.getValue()));

		grid.add(key, 0, rowIndex);
		GridPane.setHalignment(key, HPos.RIGHT);
		GridPane.setHgrow(key, Priority.ALWAYS);

		grid.add(value, 1, rowIndex);
		GridPane.setHalignment(value, HPos.LEFT);
		GridPane.setHgrow(value, Priority.ALWAYS);
	}
	
	static Config getConfig() {
		
		Config config = new Config();
		
		File configFile = new File(configPath);
		
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.serializeNulls()
				.create();

		if(configFile.exists()) {
			
			try {
				config = gson.fromJson(new JsonReader(new FileReader(configFile)), Config.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			config.setBalance((byte) 0);
			config.setConcept((byte) 0);
			config.setDate((byte) 0);
		}
		
		return config;
		
	}
	
	static void saveConfig(Config config) {
		
		File configFolder = new File(configPath).getParentFile();
		
		if(!configFolder.exists()) {
			configFolder.mkdirs();
		}
		
		try (FileWriter writer = new FileWriter(configPath)){
			new GsonBuilder()
					.setPrettyPrinting()
					.serializeNulls()
					.create()
					.toJson(config, writer);
		} catch (JsonIOException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveLang(String locale) {
		
		Config config = getConfig();
		
		config.setLang(locale);
		
		saveConfig(config);
	}
}
