package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import application.language.Language;
import application.language.LanguageService;
import application.process.MonthProcessor;
import application.process.Processor;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {
	FileChooser fileChooser;
	TabPane tabs = new TabPane();
	private Button selectFileButton;
	private Button startButton;
	VBox main = new VBox(10d);
	private Label fileLabel;
	private File file;

	Insets padding = new Insets(10d);
	private List<GridPane> grids = new ArrayList<>();

	// private ProgressBar progressBar;

	private String errorTite;
	private String errorContent;

	private ChoiceBox<Language> languageChoiceBox;

	private CheckBox checkBox;

	private Label key;
	private Label value;

	Language[] langs = { new Language("en", "English"), new Language("es", "Español") };

	double width;

	@Override
	public void start(Stage primaryStage) {
		width = primaryStage.getWidth();

		primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
			width = newVal.doubleValue();

			for (GridPane gridpane : grids) {
				gridpane.getColumnConstraints().clear();
				gridpane.getColumnConstraints().addAll(new ColumnConstraints(width / 2d - 25d));
			}
		});

		initializeControls(primaryStage);

		HBox hbox = new HBox(10d, selectFileButton, fileLabel, languageChoiceBox, checkBox);
		hbox.setAlignment(Pos.CENTER_LEFT);

		tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		// main.getChildren().add(tabs);

		main.getChildren().addAll(hbox, startButton, new Label());
		main.setPadding(padding);

		Scene scene = new Scene(main, 800, 600);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		scene.setFill(Color.BEIGE);

		String locale = Locale.getDefault().getLanguage();
		setLanguage(locale);

		int index = -1;
		boolean found = false;

		for (int i = 0; i < langs.length && !found; i++) {
			if (langs[i].getCode().equals(locale)) {
				index = i;
				found = true;
			}
		}

		if (found) {
			languageChoiceBox.getSelectionModel().select(index);
		}

		primaryStage.setTitle("For Aiur!");
		primaryStage.setScene(scene);

		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void initializeControls(Stage stage) {

		checkBox = new CheckBox();

		fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xls", "*.xlsx"));

		selectFileButton = new Button();
		startButton = new Button();
		startButton.setDisable(true);

		EventHandler<MouseEvent> selectFileEvent = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {

				File selectedFile = fileChooser.showOpenDialog(stage);
				if (selectedFile != null) {
					file = selectedFile;
					fileLabel.setText(file.getName());
					startButton.setDisable(false);
				}
			}
		};

		EventHandler<MouseEvent> startEvent = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (!checkBox.isSelected()) {

					Processor p = new Processor(file);

//				progressBar.setStyle("visibility:visible;-fx-opacity:0.0");
//				progressBar.progressProperty().bind(p.progressProperty());
//				progressBar.opacityProperty().bind(fadeIn.progressProperty());

					p.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

						@Override
						public void handle(WorkerStateEvent event) {
							try {

								main.getChildren().remove(main.getChildren().size() - 1);

								if (!checkBox.isSelected()) {

									ScrollPane scrollPane = new ScrollPane();
									GridPane grid = buildDatagrid(p.get());

									scrollPane.setContent(grid);

									main.getChildren().add(scrollPane);

									grids = new ArrayList<>();
									grids.add(grid);
								} else {
									main.getChildren().add(new Label());
								}
							} catch (InterruptedException | ExecutionException e) {
								e.printStackTrace();
							}
						}
					});

					p.setOnFailed(new EventHandler<WorkerStateEvent>() {

						@Override
						public void handle(WorkerStateEvent event) {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setContentText(errorContent);
							alert.setTitle(errorTite);
						}
					});
					
					Thread t = new Thread(p);
					t.start();
				} else {
					MonthProcessor p = new MonthProcessor(file);
					
					Thread t = new Thread(p);
					t.start();
				}
			}
		};

		selectFileButton.setOnMouseClicked(selectFileEvent);
		startButton.setOnMouseClicked(startEvent);

		fileLabel = new Label();

		languageChoiceBox = new ChoiceBox<Language>(FXCollections.observableArrayList(langs));
		languageChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				setLanguage(langs[newValue.intValue()].getCode());
			}

		});

		key = new Label();
		value = new Label();
	}

	private void setLanguage(String lang) {
		try {
			Map<String, String> translation = LanguageService.getWords(lang);

			selectFileButton.setText(translation.get("LOAD"));
			startButton.setText(translation.get("START"));
			errorContent = translation.get("ERROR_BODY");
			errorTite = translation.get("ERROR_TITLE");
			fileLabel.setText(translation.get("SELECTED"));
			checkBox.setText(translation.get("CHECKBOX"));
			key.setText(translation.get("KEY"));
			value.setText(translation.get("VALUE"));

		} catch (IOException e) {
			System.out.println("i18n error");
		}
	}

	private GridPane buildDatagrid(Map<String, Float> data) {

		Map<String, Float> sorted = data.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors
				.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap<String, Float>::new));

		GridPane gridpane = new GridPane();
		gridpane.setHgap(10d);
		gridpane.setVgap(10d);

		GridPane.setHalignment(key, HPos.RIGHT);
		gridpane.add(key, 0, 0);
		gridpane.add(value, 1, 0);

		int rowCount = 1;
		for (Entry<String, Float> row : sorted.entrySet()) {

			Label key = new Label(row.getKey());
			Label value = new Label(Float.toString(row.getValue()));

			gridpane.add(key, 0, rowCount);
			GridPane.setHalignment(key, HPos.RIGHT);
			GridPane.setHgrow(key, Priority.ALWAYS);

			gridpane.add(value, 1, rowCount);
			GridPane.setHalignment(value, HPos.LEFT);
			GridPane.setHgrow(value, Priority.ALWAYS);
			rowCount++;
		}

		gridpane.getColumnConstraints().addAll(new ColumnConstraints(width / 2d - 25d));

		return gridpane;

//		Tab tab = new Tab("March - 2019");
//
//		ScrollPane scrollPane = new ScrollPane();
//		scrollPane.setContent(gridpane);
//		scrollPane.setPadding(padding);
//		tab.setContent(scrollPane);
//
//		tabs.getTabs().add(tab);
	}
}
