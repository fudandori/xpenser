package application.modal;

import application.Main;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfigDialog extends Stage {
	public ConfigDialog(Stage owner, Main context) {
		super();
		initOwner(owner);
		setTitle("Settings");
		initModality(Modality.APPLICATION_MODAL);

		Button b = new Button("PUSH");
		b.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				context.Test.setText("100");
				getScene().getWindow().hide();
			}
		});

		Scene scene = new Scene(b, 300, 200, Color.WHITE);
		setScene(scene);
	}
}
