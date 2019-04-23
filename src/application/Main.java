package application;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import application.animation.AnimationTimer;
import application.language.Language;
import application.language.LanguageService;
import application.process.MonthProcessor;
import application.process.SingleProcessor;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {
	private final double INITIAL_HEIGHT = 600d;
	private final double INITIAL_WIDTH = 800d;
	private final Insets padding = new Insets(10d);
	private final Language[] langs = { new Language("en", "English"), new Language("es", "Espa�ol") };

	private VBox main = new VBox(10d);

	private File file;

	private FileChooser fileChooser;
	private Button selectFileButton;
	private Button startButton;
	private CheckBox checkBox;
	private ChoiceBox<Language> languageChoiceBox;

	private List<GridPane> grids;

	private Label key;
	private Label value;
	private Label fileLabel;

	private String errorTite;
	private String errorContent;

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

		Region region = new Region();
		HBox.setHgrow(region, Priority.ALWAYS);

		HBox firstRow = new HBox(10d, selectFileButton, fileLabel, region, languageChoiceBox);
		firstRow.setAlignment(Pos.CENTER_LEFT);

		HBox secondRow = new HBox(10d, startButton, checkBox);
		secondRow.setAlignment(Pos.CENTER_LEFT);

		main.getChildren().addAll(firstRow, secondRow, new Label());
		main.setPadding(padding);

		Scene scene = new Scene(main, INITIAL_WIDTH, INITIAL_HEIGHT);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		scene.setFill(Color.BEIGE);

		i18n();

		primaryStage.setTitle("Xpenser");
		primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));
		primaryStage.setScene(scene);

		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void initializeControls(Stage stage) {

		checkBox = new CheckBox();

		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xls", "*.xlsx"));

		selectFileButton = new Button();
		selectFileButton.setOnMouseClicked(getSelectFileEvent(stage));

		startButton = new Button();
		startButton.setDisable(true);
		startButton.setOnMouseClicked(getStartEvent());

		languageChoiceBox = new ChoiceBox<Language>(FXCollections.observableArrayList(langs));
		languageChoiceBox
			.getSelectionModel()
			.selectedIndexProperty()
			.addListener(getChangeListener());

		key = new Label();
		value = new Label();
		fileLabel = new Label();
		
		grids = new ArrayList<>();
	}

	private void setLanguage(String lang) {
		try {

			Map<String, String> translation = LanguageService.getWords(lang);

			selectFileButton.setText(translation.get("LOAD"));
			startButton.setText(translation.get("START"));
			errorContent = translation.get("ERROR_BODY");
			errorTite = translation.get("ERROR_TITLE");
			if(file == null) fileLabel.setText(translation.get("SELECTED"));
			checkBox.setText(translation.get("CHECKBOX"));
			key.setText(translation.get("KEY"));
			value.setText(translation.get("VALUE"));

		} catch (IOException e) {
			System.out.println("i18n error");
		}
	}

	private GridPane generateDatagrid(Map<String, Float> data, boolean legend) {

		Map<String, Float> sorted = data
				.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors
				.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap<String, Float>::new));

		GridPane gridpane = new GridPane();
		gridpane.setHgap(10d);
		gridpane.setVgap(10d);

		if (legend) {
			GridPane.setHalignment(key, HPos.RIGHT);
			gridpane.add(key, 0, 0);
			gridpane.add(value, 1, 0);
		}

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
	}

	private void showSingleData() {
		SingleProcessor p = new SingleProcessor(file);

		p.setOnFailed(onFailAlert());
		p.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				try {

					AnimationTimer fadeIn = new AnimationTimer(500);

					GridPane grid = generateDatagrid(p.get(), true);

					ScrollPane scrollPane = new ScrollPane();
					scrollPane.opacityProperty().set(0d);
					scrollPane.opacityProperty().bind(fadeIn.progressProperty());
					scrollPane.setContent(grid);

					main.getChildren().add(scrollPane);
					
					grids = new ArrayList<>();
					grids.add(grid);

					Thread animation = new Thread(fadeIn);
					animation.start();
					
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});

		Thread t = new Thread(p);
		t.start();
	}

	private void showMultipleData() {
		MonthProcessor p = new MonthProcessor(file);
		
		p.setOnFailed(onFailAlert());
		p.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				try {

					AnimationTimer fadeIn = new AnimationTimer(500);

					TabPane tabs = new TabPane();
					tabs.opacityProperty().set(0d);
					tabs.opacityProperty().bind(fadeIn.progressProperty());
					tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

					grids = new ArrayList<>();

					for (Entry<String, Map<String, Float>> map : p.get().entrySet()) {

						String tabName = LocalDate
								.parse(map.getKey())
								.format(DateTimeFormatter.ofPattern("MMMM - yyyy"));
						
						Tab tab = new Tab(tabName);

						ScrollPane scrollPane = new ScrollPane();
						GridPane grid = generateDatagrid(map.getValue(), false);
						grid.opacityProperty().set(0d);
						grid.opacityProperty().bind(fadeIn.progressProperty());

						scrollPane.setContent(grid);
						tab.setContent(scrollPane);

						tabs.getTabs().add(tab);
						grids.add(grid);
					}

					main.getChildren().add(tabs);

					Thread animation = new Thread(fadeIn);
					animation.start();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
		
		Thread t = new Thread(p);
		t.start();
	}

	private EventHandler<WorkerStateEvent> onFailAlert() {
		return new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText(errorContent);
				alert.setTitle(errorTite);
			}
		};
	}
	
	private void i18n() {
		String locale = Locale.getDefault().getLanguage();

		if (!LanguageService.existsLocale(locale)) {
			locale = "en";
		}

		int index = -1;
		boolean found = false;

		for (int i = 0; i < langs.length && !found; i++) {
			if (langs[i].getCode().equals(locale)) {
				index = i;
				found = true;
			}
		}
		
		languageChoiceBox.getSelectionModel().select(index);
	}
	
	private EventHandler<MouseEvent> getSelectFileEvent(Stage stage) {
		return new EventHandler<MouseEvent>() {
			
			public void handle(MouseEvent event) {
				File selectedFile = fileChooser.showOpenDialog(stage);
				if (selectedFile != null) {
					file = selectedFile;
					fileLabel.setText(file.getName());
					startButton.setDisable(false);
				}
			}
		};
	}

	private EventHandler<MouseEvent> getStartEvent() {
		return new EventHandler<MouseEvent>() {
			
			public void handle(MouseEvent event) {
				main.getChildren().remove(main.getChildren().size() - 1);

				if (!checkBox.isSelected()) {
					showSingleData();
				} else {
					showMultipleData();
				}
			}
		};
	}
	
	private ChangeListener<Number> getChangeListener() {
		return new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				setLanguage(langs[newValue.intValue()].getCode());
			}

		};
	}
}
