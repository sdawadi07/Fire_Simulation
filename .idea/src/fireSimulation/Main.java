package fireSimulation;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * The main entry point for the Forest Fire Simulation application using JavaFX.
 * Sets up the GUI, initializes the simulation model, and handles user interactions
 * through control buttons and a speed slider.
 */
public class Main extends Application {
    /** Default configuration file path. */
    private static final String DEFAULT_CONFIG = "example_config/sample.txt";
    private SimulationModel model;
    private SimulationPanel simulationPanel;
    private LogPanel logPanel;
    private Stage primaryStage;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Button loadButton;
    private Slider speedSlider;
    private ExecutorService executorService;
    private boolean simulationRunning = false;
    private Timeline updateTimer;

    /**
     * Launches the JavaFX application.
     * @param args Command-line arguments; if provided, the first argument is the config file path.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The main entry point for the JavaFX application.
     * Initializes the UI and simulation model.
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Forest Fire Simulation (JavaFX)");

        Parameters params = getParameters();
        String configFile = params.getRaw().isEmpty() ? DEFAULT_CONFIG : params.getRaw().get(0);

        model = new SimulationModel();
        try {
            model.loadConfiguration(configFile);
        } catch (Exception e) {
            showErrorDialog("Configuration Error", "Error loading configuration: " + e.getMessage());
            Platform.exit();
            return;
        }

        simulationPanel = new SimulationPanel(model);
        logPanel = new LogPanel(model);

        Node controlPanel = createControlPanel();

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        Canvas simCanvas = simulationPanel.getFxPanel();
        Pane canvasContainer = new Pane();
        canvasContainer.getChildren().add(simCanvas);
        simCanvas.widthProperty().bind(canvasContainer.widthProperty());
        simCanvas.heightProperty().bind(canvasContainer.heightProperty());
        canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> simulationPanel.updateFxPanel());
        canvasContainer.heightProperty().addListener((obs, oldVal, newVal) -> simulationPanel.updateFxPanel());


        splitPane.getItems().addAll(canvasContainer, logPanel.getFxPanel());
        splitPane.setDividerPositions(0.7);

        BorderPane root = new BorderPane();
        root.setCenter(splitPane);
        root.setBottom(controlPanel);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> shutdown());
        primaryStage.show();

        setupUpdateTimer();
    }

    /**
     * Creates the control panel with buttons and speed slider for user interaction.
     * @return The configured control panel Node (HBox).
     */
    private Node createControlPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");

        startButton = new Button("Start");
        pauseButton = new Button("Pause");
        resetButton = new Button("Reset");
        loadButton = new Button("Load Config");
        speedSlider = new Slider(1, 10, 1); // min, max, initial
        speedSlider.setMajorTickUnit(1);
        speedSlider.setMinorTickCount(0);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setSnapToTicks(true);

        Label speedLabel = new Label("Simulation Speed:");
        pauseButton.setDisable(true);

        startButton.setOnAction(e -> startSimulation());
        pauseButton.setOnAction(e -> pauseSimulation());
        resetButton.setOnAction(e -> resetSimulation());
        loadButton.setOnAction(e -> loadConfiguration());

        panel.getChildren().addAll(startButton, pauseButton, resetButton, loadButton, speedLabel, speedSlider);

        return panel;
    }

    /**
     * Sets up the Timeline for periodic UI updates.
     * Initializes a timer to periodically update the UI elements, including the simulation and log panels.
     */
    private void setupUpdateTimer() {
        updateTimer = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            simulationPanel.updateFxPanel();
            logPanel.updateFxLog();
        }));
        updateTimer.setCycleCount(Animation.INDEFINITE);
    }


    /**
     * Starts the simulation
     * Disabling start/load buttons and enabling pause.
     * Updates the timer play
     */
    private void startSimulation() {
        if (simulationRunning) return;

        simulationRunning = true;
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        loadButton.setDisable(true);

        executorService = Executors.newCachedThreadPool();
        model.startSimulation(executorService, (int) speedSlider.getValue());

        updateTimer.play();
    }

    /**
     * Pauses the simulation, re-enabling start/load buttons and disabling pause.
     */
    private void pauseSimulation() {
        if (!simulationRunning) return;

        simulationRunning = false;
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        loadButton.setDisable(false);

        updateTimer.pause();
        model.pauseSimulation();
        shutdownExecutor();
    }

    /**
     * Resets the simulation to its initial state based on the current configuration.
     */
    private void resetSimulation() {
        pauseSimulation();
        model.resetSimulation();
        simulationPanel.updateFxPanel();
        logPanel.resetFxLog();
    }

    /**
     * Opens a file chooser to load a new configuration file and resets the simulation.
     */
    private void loadConfiguration() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Configuration File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );


        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                pauseSimulation();
                model.loadConfiguration(selectedFile.getAbsolutePath());
                resetSimulation();
                showInfoDialog("Success", "Configuration loaded successfully!");
            } catch (Exception e) {
                showErrorDialog("Configuration Error", "Error loading configuration: " + e.getMessage());
            }
        }
    }

    /**
     * Shuts down the executor service if it's running.
     */
    private void shutdownExecutor() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow(); // Force shutdown
        }
        executorService = null;
    }

    /**
     * Performs cleanup actions when the application is closing.
     */
    private void shutdown() {
        pauseSimulation();
        if (updateTimer != null) {
            updateTimer.stop();
        }
        System.out.println("Application shutting down.");
    }

    /**
     * Helper method to show an error dialog.
     * @param title Dialog title.
     * @param message Dialog message.
     */
    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Helper method to show an information dialog.
     * @param title Dialog title.
     * @param message Dialog message.
     */
    private void showInfoDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }
}