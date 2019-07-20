package application.modal;

import java.io.File;
import java.io.PrintWriter;

import application.Main;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfigDialog extends Stage {

	private Label balance = new Label();
	private Label concept = new Label();
	private Label expenses = new Label();
	private Label date = new Label();

	private TextField t1 = new TextField();
	private TextField t2 = new TextField();
	private TextField t3 = new TextField();
	private TextField t4 = new TextField();

	public ConfigDialog(Stage owner, Main context) {
		super();
		setResizable(false);
		
		initOwner(owner);
		setTitle("Settings");
		initModality(Modality.APPLICATION_MODAL);

		Button b = new Button("Aceptar");
		b.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				
				File f = new File(getClass().getResource("/config.properties").getPath());

				try(PrintWriter writer = new PrintWriter(f)){
					
					writer.write("BALANCE_COLUMN=" + t1.getText() + System.lineSeparator());
					writer.write("CONCEPT_COLUMN=" + t2.getText() + System.lineSeparator());
					writer.write("EXPENSES_COLUMN=" + t3.getText() + System.lineSeparator());
					writer.write("DATE_COLUMN=" + t4.getText() + System.lineSeparator());
					
					writer.flush();
					
					context.updateConfig();
				} catch(Exception e) {
					
				}
				
				close();
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

		balance.setPrefWidth(100);
		balance.setText(context.getBalanceColumn());
		concept.setPrefWidth(100);
		concept.setText(context.getConceptColumn());
		expenses.setPrefWidth(100);
		expenses.setText(context.getExpensesColumn());
		date.setPrefWidth(100);
		date.setText(context.getDateColumn());
		
		HBox h1 = new HBox(10d, balance, t1);
		HBox h2 = new HBox(10d, concept, t2);
		HBox h3 = new HBox(10d, expenses, t3);
		HBox h4 = new HBox(10d, date, t4);
		HBox h5 = new HBox(10d, b, c);
		h5.setAlignment(Pos.CENTER);

		VBox v = new VBox(10d, h1, h2, h3, h4, h5);
		v.setPadding(new Insets(10d));
		v.setAlignment(Pos.CENTER);
		
		Scene scene = new Scene(v, 170, 200, Color.WHITE);
		setScene(scene);
		
	}
}
