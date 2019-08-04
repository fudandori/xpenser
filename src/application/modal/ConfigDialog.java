package application.modal;

import java.io.File;
import java.io.PrintWriter;

import application.Main;
import application.Utility;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfigDialog extends Stage {

	private Label balance = new Label();
	private Label concept = new Label();
	private Label expenses = new Label();
	private Label date = new Label();
	private Label start = new Label();

	private TextField t1 = new TextField();
	private TextField t2 = new TextField();
	private TextField t3 = new TextField();
	private TextField t4 = new TextField();
	private TextField t5 = new TextField();

	private int balanceValue;
	private int conceptValue;
	private int expensesValue;
	private int dateValue;
	private int startValue;

	private String selected;

	public ConfigDialog(Stage owner, Main context) {
		super();
		setResizable(false);

		initOwner(owner);
		setTitle(context.getSettings());
		initModality(Modality.APPLICATION_MODAL);

		Button b = new Button("Aceptar");
		b.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {

				boolean skip = false;

				if ("Manual".equals(selected)) {

					try {

						balanceValue = Integer.parseInt(t1.getText());
						conceptValue = Integer.parseInt(t2.getText());
						expensesValue = Integer.parseInt(t3.getText());
						dateValue = Integer.parseInt(t4.getText());

					} catch (NumberFormatException e) {
						skip = true;

						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");
						alert.setContentText("Número no válido");
						alert.setHeaderText(null);

						Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();

						stage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/icon.png")));

						alert.show();
					}
				}

				if (!skip) {

					File f = new File(Utility.configPath);

					try (PrintWriter writer = new PrintWriter(f)) {

						writer.write("BALANCE_COLUMN=" + balanceValue + System.lineSeparator());
						writer.write("CONCEPT_COLUMN=" + conceptValue + System.lineSeparator());
						writer.write("EXPENSES_COLUMN=" + expensesValue + System.lineSeparator());
						writer.write("DATE_COLUMN=" + dateValue + System.lineSeparator());
						writer.write("START_ROW=" + startValue + System.lineSeparator());
						writer.write("LABEL=" + selected + System.lineSeparator() );
						writer.flush();

						context.updateConfig();
						context.selectedLabel.setText(selected);
					} catch (Exception e) {

					}

					close();
				}
			}
		});

		Button c = new Button("Cerrar");
		c.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				close();
			}
		});

		t1.setPrefWidth(50);
		t2.setPrefWidth(50);
		t3.setPrefWidth(50);
		t4.setPrefWidth(50);
		t5.setPrefWidth(50);

		balance.setPrefWidth(100);
		balance.setText(context.getBalanceColumn());
		concept.setPrefWidth(100);
		concept.setText(context.getConceptColumn());
		expenses.setPrefWidth(100);
		expenses.setText(context.getExpensesColumn());
		date.setPrefWidth(100);
		date.setText(context.getDateColumn());
		start.setPrefWidth(100);
		start.setText(context.getFirstRow());
		
		t1.setEditable(false);
		t2.setEditable(false);
		t3.setEditable(false);
		t4.setEditable(false);
		t5.setEditable(false);

		RadioButton r1 = new RadioButton("EVO");
		RadioButton r2 = new RadioButton("Caixabank");
		RadioButton r3 = new RadioButton("Manual");

		ToggleGroup toggleGroup = new ToggleGroup();

		r1.setToggleGroup(toggleGroup);
		r2.setToggleGroup(toggleGroup);
		r3.setToggleGroup(toggleGroup);

		toggleGroup.selectedToggleProperty().addListener((observable, oldVal, newVal) -> {
			selected = ((RadioButton) newVal).getText();

			switch (selected) {

			case "EVO":

				balanceValue = 5;
				conceptValue = 2;
				expensesValue = 3;
				dateValue = 0;
				startValue = 2;
				
				predefine();
				break;
				
			case "Caixabank":

				balanceValue = 5;
				conceptValue = 0;
				expensesValue = 4;
				dateValue = 1;
				startValue = 4;

				predefine();
				break;
				
			case "Manual":
				t1.setEditable(true);
				t2.setEditable(true);
				t3.setEditable(true);
				t4.setEditable(true);
				t5.setEditable(true);

				t1.setText("");
				t2.setText("");
				t3.setText("");
				t4.setText("");
				t5.setText("");
				break;
			}

		});

		HBox h1 = new HBox(10d, balance, t1);
		HBox h2 = new HBox(10d, concept, t2);
		HBox h3 = new HBox(10d, expenses, t3);
		HBox h4 = new HBox(10d, date, t4);
		HBox h6 = new HBox(10d, start, t5);
		
		HBox h5 = new HBox(10d, b, c);
		h5.setAlignment(Pos.BOTTOM_RIGHT);

		VBox bs = new VBox(h5);
		bs.setPadding(new Insets(10d));
		bs.setAlignment(Pos.BOTTOM_CENTER);
		VBox.setVgrow(bs, Priority.ALWAYS);

		VBox r = new VBox(10d, r1, r2, r3);
		r.setPadding(new Insets(10d));
		r.setAlignment(Pos.BASELINE_LEFT);

		VBox v = new VBox(10d, h1, h2, h3, h4, h6);
		v.setPadding(new Insets(10d));
		v.setAlignment(Pos.CENTER);

		VBox p = new VBox(r, v, bs);

		Scene scene = new Scene(p, 200, 350, Color.WHITE);
		setScene(scene);

	}

	private void predefine() {
		t1.setEditable(false);
		t2.setEditable(false);
		t3.setEditable(false);
		t4.setEditable(false);
		t5.setEditable(false);

		t1.setText(Integer.toString(balanceValue));
		t2.setText(Integer.toString(conceptValue));
		t3.setText(Integer.toString(expensesValue));
		t4.setText(Integer.toString(dateValue));
		t5.setText(Integer.toString(startValue));
	}
}
