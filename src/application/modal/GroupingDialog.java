package application.modal;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import application.Ctx;
import application.process.Group;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GroupingDialog extends Stage {

	private TextField t1 = new TextField();
	private TextField t2 = new TextField();
	private Button save;
	private ChoiceBox<String> combo;
	private HBox editLine;
	private Button add;
	private HBox header;
	private List<Group> groups = new ArrayList<>();
	private GridPane gridpane;
	private int row = 0;	
	
	public GroupingDialog(Stage owner) {
		super();
		setResizable(false);

		initOwner(owner);
		setTitle(Ctx.groups);
		initModality(Modality.APPLICATION_MODAL);

		gridpane = new GridPane();
		ColumnConstraints textCols = new ColumnConstraints();
		ColumnConstraints optionCol = new ColumnConstraints();
		ColumnConstraints col4 = new ColumnConstraints();
		textCols.setPercentWidth(35);
		optionCol.setPercentWidth(15);
		col4.setPercentWidth(15);

		gridpane.setHgap(10d);
		gridpane.setVgap(10d);

		gridpane.getColumnConstraints().addAll(textCols, optionCol, textCols, col4);

		t1.setPromptText(Ctx.name);
		t2.setPromptText(Ctx.value);

		String[] options = new String[] { Ctx.startsWith, Ctx.contains, Ctx.endsWith };
		combo = new ChoiceBox<>(FXCollections.observableArrayList(options));
		combo.getSelectionModel().selectFirst();

		add = new Button(Ctx.add);
		add.setOnMouseClicked(event -> {
			header.getChildren().remove(0);
			header.getChildren().add(editLine);
		});
		
		for(Group g : Ctx.config.getGroups()) {
			Label name = new Label(g.getName());
			Label value = new Label(g.getValue());
			Label option = new Label(options[g.getRegex().ordinal()]);
			Button remove = new Button(Ctx.remove);

			name.setMaxWidth(Double.MAX_VALUE);
			value.setMaxWidth(Double.MAX_VALUE);
			option.setMaxWidth(Double.MAX_VALUE);

			gridpane.add(name, 0, row);
			gridpane.add(option, 1, row);
			gridpane.add(value, 2, row);
			gridpane.add(remove, 3, row);
			row++;
			
			GridPane.setHalignment(remove, HPos.RIGHT);

			
			groups.add(g);
			
			remove.setOnMouseClicked(hi -> {
				name.setText(Ctx.removed);
				value.setText(Ctx.removed);
				option.setText(Ctx.removed);
				remove.setVisible(false);
				groups.remove(g);
			});
		}

		save = new Button(Ctx.save);
		save.setOnMouseClicked(event -> {

				Label name = new Label(t1.getText());
				Label value = new Label(t2.getText());
				Label option = new Label(combo.getSelectionModel().getSelectedItem());
				Button remove = new Button(Ctx.remove);

				name.setMaxWidth(Double.MAX_VALUE);
				value.setMaxWidth(Double.MAX_VALUE);
				option.setMaxWidth(Double.MAX_VALUE);

				gridpane.add(name, 0, row);
				gridpane.add(option, 1, row);
				gridpane.add(value, 2, row);
				gridpane.add(remove, 3, row);
				row++;
				
				GridPane.setHalignment(remove, HPos.RIGHT);

				Group g = new Group(t1.getText(), t2.getText(), combo.getSelectionModel().getSelectedIndex() + 1);
				groups.add(g);
				
				remove.setOnMouseClicked(hi -> {
					name.setText(Ctx.removed);
					value.setText(Ctx.removed);
					option.setText(Ctx.removed);
					remove.setVisible(false);
					groups.remove(g);
				});

				header.getChildren().remove(0);
				header.getChildren().add(add);

		});

		editLine = new HBox(10d, t1, t2, combo, save);
		editLine.setAlignment(Pos.CENTER_LEFT);

		header = new HBox(10d, add);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(Ctx.PADDING);

		HBox.setHgrow(editLine, Priority.ALWAYS);
		HBox.setHgrow(add, Priority.ALWAYS);
		HBox.setHgrow(save, Priority.ALWAYS);
		HBox.setHgrow(combo, Priority.ALWAYS);
		HBox.setHgrow(t1, Priority.ALWAYS);
		HBox.setHgrow(t2, Priority.ALWAYS);

		ScrollPane scrollPane = new ScrollPane();
		
		scrollPane.setMaxHeight(Double.MAX_VALUE);

		scrollPane.setStyle("-fx-background-color:transparent;");
		scrollPane.setContent(gridpane);
		scrollPane.setFitToWidth(true);
		VBox p = new VBox(10d, header, scrollPane);
		p.setPadding(Ctx.PADDING);

		Scene scene = new Scene(p, 600, 400, Color.WHITE);
		setScene(scene);
		
		setOnCloseRequest(event -> Ctx.config.setGroups(groups));
	}
}
