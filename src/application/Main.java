package application;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import application.animation.AnimationTimer;
import application.language.LanguageService;
import application.process.Processor;
import javafx.application.Application;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
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

	private Button selectFileButton;
	private Button startButton;

	private Label fileLabel;
	private File file;

	private GridPane gridpane;

	private ProgressBar progressBar;

	private String errorTite;
	private String errorContent;

	@Override
	public void start(Stage primaryStage) {

		primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
			gridpane.getColumnConstraints().clear();

			ColumnConstraints c1 = new ColumnConstraints(newVal.doubleValue() / 2d - 50d);
			ColumnConstraints c2 = new ColumnConstraints(newVal.doubleValue() / 2d - 50d);

			gridpane.getColumnConstraints().addAll(c1, c2);
		});

		Insets padding = new Insets(10d);

		initializeControls(primaryStage);

		HBox hbox = new HBox(10d, selectFileButton, fileLabel);
		hbox.setAlignment(Pos.CENTER_LEFT);

		gridpane = new GridPane();
		gridpane.setHgap(10d);
		gridpane.setVgap(10d);

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(gridpane);
		scrollPane.setPadding(padding);

		progressBar = new ProgressBar();
		progressBar.setProgress(.75);
		progressBar.setPrefWidth(750d);

		VBox main = new VBox(10d);
		main.getChildren().addAll(hbox, startButton, progressBar, scrollPane);
		main.setPadding(padding);

		Scene scene = new Scene(main, 800, 600);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		scene.setFill(Color.BEIGE);

		String locale = Locale.getDefault().getLanguage();
		setLanguage(locale);

		primaryStage.setTitle("For Aiur!");
		primaryStage.setScene(scene);

		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void initializeControls(Stage stage) {

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
				Processor p = new Processor(file);
				AnimationTimer fadeIn = new AnimationTimer(250);

				progressBar.setStyle("visibility:visible;-fx-opacity:0.0");

				progressBar.progressProperty().bind(p.progressProperty());
				progressBar.opacityProperty().bind(fadeIn.progressProperty());

				p.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

					@Override
					public void handle(WorkerStateEvent event) {
						try {
							buildDatagrid(p.get());
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

				Thread animateThread = new Thread(fadeIn);
				Thread barThread = new Thread(p);

				animateThread.start();
				barThread.start();
			}
		};

		selectFileButton.setOnMouseClicked(selectFileEvent);
		startButton.setOnMouseClicked(startEvent);

		fileLabel = new Label();
	}

	private void setLanguage(String lang) {
		try {
			Map<String, String> translation = LanguageService.getWords(lang);

			selectFileButton.setText(translation.get("LOAD"));
			startButton.setText(translation.get("START"));
			errorContent = translation.get("ERROR_BODY");
			errorTite = translation.get("ERROR_TITLE");
			fileLabel.setText(translation.get("SELECTED"));
		} catch (IOException e) {
			System.out.println("i18n error");
		}
	}

	private void buildDatagrid(Map<String, Float> data) {

		gridpane.getChildren().clear();

		Label c1 = new Label("KEY");
		GridPane.setHalignment(c1, HPos.RIGHT);

		Label c2 = new Label("VALUE");

		gridpane.add(c1, 0, 0);
		gridpane.add(c2, 1, 0);

		int rowCount = 1;
		for (Entry<String, Float> row : data.entrySet()) {

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
	}
}
