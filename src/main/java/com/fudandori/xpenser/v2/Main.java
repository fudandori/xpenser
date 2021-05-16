package com.fudandori.xpenser.v2;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fudandori.xpenser.v2.animation.AnimationTimer;
import com.fudandori.xpenser.v2.language.Language;
import com.fudandori.xpenser.v2.language.LanguageService;
import com.fudandori.xpenser.v2.modal.ConfigDialog;
import com.fudandori.xpenser.v2.modal.GroupingDialog;
import com.fudandori.xpenser.v2.process.Processor;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
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
	private static final double INITIAL_HEIGHT = 600d;
	private static final double INITIAL_WIDTH = 800d;
	
	private static final Language[] langs = { new Language("en", "English"), new Language("es", "Espa�ol") };

	private VBox mainPane;

	private File file;

	private FileChooser fileChooser;
	
	private Button selectFileButton;
	private Button startButton;
	private Button configButton;
	private Button groupingButton; 
	
	private CheckBox checkBox;
	private ChoiceBox<Language> languageChoiceBox;

	private List<GridPane> grids;

	private Label keyLabel;
	private Label valueLabel;
	private Label fileLabel;
	private Label selectedLabel;

	private String errorTite;
	private String errorContent;

	private double width;
	
	@Override
	public void start(Stage primaryStage) {
		
		Ctx.load();
		initControls(primaryStage);
		initLayout(primaryStage);
		
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	private void initLayout(Stage primaryStage) {
		
		Region region = new Region();
		HBox.setHgrow(region, Priority.ALWAYS);

		HBox firstRow = new HBox(10d, selectFileButton, fileLabel, region, selectedLabel, configButton, groupingButton, languageChoiceBox);
		firstRow.setAlignment(Pos.CENTER_LEFT);

		HBox secondRow = new HBox(10d, startButton, checkBox);
		secondRow.setAlignment(Pos.CENTER_LEFT);

		mainPane = new VBox(10d, firstRow, secondRow);
		mainPane.setPadding(Ctx.PADDING);
		
		Scene scene = new Scene(mainPane, INITIAL_WIDTH, INITIAL_HEIGHT);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		scene.setFill(Color.BEIGE);
		
		primaryStage.setScene(scene);
	}

	private void initControls(Stage primaryStage) {

		primaryStage.setTitle("Xpenser");
		primaryStage.getIcons().add(new Image("/assets/icon.png"));
		primaryStage.setOnCloseRequest(e -> Utility.saveConfig(Ctx.config));

		width = primaryStage.getWidth();

		checkBox = new CheckBox();

		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xls", "*.xlsx"));

		selectFileButton = new Button();
		selectFileButton.setOnMouseClicked(getSelectFileEvent(primaryStage));

		startButton = new Button();
		startButton.setDisable(true);
		startButton.setOnMouseClicked(getStartEvent());

		configButton = new Button();
		configButton.setOnMouseClicked(event -> {
			String selected = new ConfigDialog(primaryStage).test();
			
			if(selected != null) {
				Ctx.config.setBank(selected);
				selectedLabel.setText(selected);
			}
		});

		groupingButton = new Button();
		groupingButton.setOnMouseClicked(event -> new GroupingDialog(primaryStage).show());
		
		keyLabel = new Label();
		valueLabel = new Label();
		fileLabel = new Label();

		final String locale = Ctx.config.getLang() != null
				? Ctx.config.getLang()
				: LanguageService.getLocale();
		
		int localeIndex = IntStream
				.range(0, langs.length)
				.filter(i -> langs[i].getCode().equals(locale))
				.findFirst()
				.getAsInt();
		
		String selectedLabelText = Ctx.config.getBank() != null
				? Ctx.config.getBank() 
				: LanguageService.getWords(locale).get("NO_BANK");
		
		selectedLabel = new Label(selectedLabelText);
		
		languageChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(langs));
		languageChoiceBox.getSelectionModel().selectedIndexProperty().addListener(getChangeListener());
		languageChoiceBox.getSelectionModel().select(localeIndex);
		
		grids = new ArrayList<>();

		primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
			width = newVal.doubleValue();

			for (GridPane gridpane : grids) {
				gridpane.getColumnConstraints().clear();
				gridpane.getColumnConstraints().addAll(new ColumnConstraints(width / 2d - 25d));
			}
		});
	}
	
	private void translate(String lang) {

		Map<String, String> translation = LanguageService.getWords(lang);

		if (!translation.isEmpty()) {

			if (!Ctx.config.hasBank()) selectedLabel.setText(translation.get("NO_BANK"));
			
			Ctx.groups = translation.get("GROUPS");
			Ctx.balanceColumn = translation.get("BALANCE_COLUMN");
			Ctx.conceptColumn = translation.get("CONCEPT_COLUMN");
			Ctx.dateColumn = translation.get("DATE_COLUMN");
			Ctx.expensesColumn = translation.get("EXPENSES_COLUMN");
			Ctx.settings = translation.get("SETTINGS");
			Ctx.firstRowText = translation.get("FIRST_ROW");
			Ctx.save = translation.get("SAVE");
			Ctx.add = translation.get("ADD");
			Ctx.contains = translation.get("CONTAINS");
			Ctx.startsWith = translation.get("STARTS");
			Ctx.endsWith = translation.get("ENDS");
			Ctx.remove= translation.get("REMOVE");
			Ctx.removed= translation.get("REMOVED");
			
			selectFileButton.setText(translation.get("LOAD"));
			startButton.setText(translation.get("START"));
			configButton.setText(translation.get("SETTINGS"));
			groupingButton.setText(Ctx.groups);
			errorContent = translation.get("ERROR_BODY");
			errorTite = translation.get("ERROR_TITLE");
			if (file == null) fileLabel.setText(translation.get("SELECTED"));
			checkBox.setText(translation.get("CHECKBOX"));
			keyLabel.setText(translation.get("KEY"));
			valueLabel.setText(translation.get("VALUE"));
		}
	}

	private GridPane generateDatagrid(Map<String, Float> data, boolean legend) {

		Map<String, Float> sorted = data
				.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(
						Entry::getKey,
						Entry::getValue,
						(e1, e2) -> e2,
						LinkedHashMap<String, Float>::new));

		GridPane gridpane = new GridPane();
		gridpane.setHgap(10d);
		gridpane.setVgap(10d);

		if (legend) {
			GridPane.setHalignment(keyLabel, HPos.RIGHT);
			gridpane.add(keyLabel, 0, 0);
			gridpane.add(valueLabel, 1, 0);
		}

		int rowIndex = 1;
		for (Entry<String, Float> row : sorted.entrySet()) {

			Utility.addRow(gridpane, row, rowIndex);
			rowIndex++;
		}

		gridpane.getColumnConstraints().addAll(new ColumnConstraints(width / 2d - 25d));

		return gridpane;
	}

	private void showSingleData(Processor p, AnimationTimer fadeIn) {

		try {

			GridPane grid = generateDatagrid(p.process(), true);

			ScrollPane scrollPane = new ScrollPane();
			scrollPane.opacityProperty().set(0d);
			scrollPane.opacityProperty().bind(fadeIn.progressProperty());
			scrollPane.setContent(grid);
			scrollPane.setMaxHeight(Double.MAX_VALUE);
			
			mainPane.getChildren().add(scrollPane);

			grids = new ArrayList<>();
			grids.add(grid);

			Thread animation = new Thread(fadeIn);
			animation.start();

		} catch (Exception e) {
			onFailAlert();
		}
	}

	private void showMultipleData(Processor p, AnimationTimer fadeIn) {

		try {

			TabPane tabs = new TabPane();
			tabs.opacityProperty().set(0d);
			tabs.opacityProperty().bind(fadeIn.progressProperty());
			tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

			grids = new ArrayList<>();

			for (Entry<String, Map<String, Float>> map : p.processMonthly().entrySet()) {

				String tabName = LocalDate.parse(map.getKey()).format(DateTimeFormatter.ofPattern("MMMM - yyyy"));

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

			mainPane.getChildren().add(tabs);

			Thread animation = new Thread(fadeIn);
			animation.start();
		} catch (Exception e) {
			onFailAlert();
		}
	}

	private void onFailAlert() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setContentText(errorContent);
		alert.setTitle(errorTite);
		alert.show();
	}

	private EventHandler<MouseEvent> getSelectFileEvent(Stage stage) {
		return event -> {
			File selectedFile = fileChooser.showOpenDialog(stage);
			if (selectedFile != null) {
				file = selectedFile;
				fileLabel.setText(file.getName());
				startButton.setDisable(false);
			}
		};
	}

	private EventHandler<MouseEvent> getStartEvent() {
		return event -> {

			if (Ctx.config.hasBank()) {

				if (mainPane.getChildren().size() > 2) {
					mainPane.getChildren().remove(mainPane.getChildren().size() - 1);
				}

				int balance = Ctx.config.getBalance();
				int concept = Ctx.config.getConcept();
				int expenses = Ctx.config.getExpenses();
				int date = Ctx.config.getDate();
				int start = Ctx.config.getStart();

				Processor p = new Processor(file, balance, concept, expenses, date, start);
				AnimationTimer fadeIn = new AnimationTimer(500);

				if (checkBox.isSelected()) {

					showMultipleData(p, fadeIn);

				} else {

					showSingleData(p, fadeIn);

				}
			} else {
				onFailAlert();
			}
		};
	}

	private ChangeListener<Number> getChangeListener() {
		return (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			
			String locale =  langs[newValue.intValue()].getCode();
			
			translate(locale);
			Ctx.config.setLang(locale);
		};
	}
}
