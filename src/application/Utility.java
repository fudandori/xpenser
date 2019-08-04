package application;

import java.io.File;
import java.util.Map.Entry;

import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Support functions and variables for Xpender
 */
public final class Utility {
	
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
	
	
}
