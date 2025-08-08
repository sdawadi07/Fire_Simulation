package fireSimulation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Name:Priya Dahal and Swastika Dawadi
 * CS 351L
 * Displays the base station log and simulation statistics using JavaFX components.
 * Supports filtering log entries by agent ID.
 */
public class LogPanel {
    private final SimulationModel model;
    private final TableView<LogEntryWrapper> logTable;
    private final ObservableList<LogEntryWrapper> logData = FXCollections.observableArrayList();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private Label activeAgentsValue;
    private Label burnedSensorsValue;
    private Label totalAgentsValue;
    private Label activeAreasValue;
    private TextField filterField;
    private final BorderPane mainPanel;
    private Timeline statsTimeline;
    private int currentFilterAgentId = -1;

    /**
     * Constructs a new log panel using JavaFX.
     * @param model The simulation model to monitor.
     */
    public LogPanel(SimulationModel model) {
        this.model = model;

        mainPanel = new BorderPane();
        mainPanel.setPadding(new Insets(5));
        mainPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-insets: 5; -fx-padding: 5;");

        logTable = createLogTable();
        Node filterPanel = createFilterPanel();
        mainPanel.setTop(filterPanel);

        mainPanel.setCenter(logTable);

        Node statsPanel = createStatsPanel();
        mainPanel.setBottom(statsPanel);
        startStatsUpdates();
    }

    /**
     * Returns the main JavaFX Node for this panel.
     * @return The BorderPane containing all log panel elements.
     */
    public Node getFxPanel() {
        return mainPanel;
    }

    /**
     * Creates the TableView for displaying log entries.
     * @return The configured TableView.
     */
    private TableView<LogEntryWrapper> createLogTable() {
        TableView<LogEntryWrapper> table = new TableView<>(logData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Columns fit width

        TableColumn<LogEntryWrapper, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setPrefWidth(80);
        timeCol.setSortable(false);

        TableColumn<LogEntryWrapper, Integer> agentIdCol = new TableColumn<>("Agent ID");
        agentIdCol.setCellValueFactory(new PropertyValueFactory<>("agentId"));
        agentIdCol.setPrefWidth(60);
        agentIdCol.setSortable(false);


        TableColumn<LogEntryWrapper, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(80);
        locationCol.setSortable(false);


        TableColumn<LogEntryWrapper, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        reasonCol.setPrefWidth(200);
        reasonCol.setSortable(false);


        table.getColumns().addAll(timeCol, agentIdCol, locationCol, reasonCol);

        return table;
    }

    /**
     * Creates the filter panel (HBox) for filtering log entries by agent ID.
     * @return The configured filter panel Node.
     */
    private Node createFilterPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(5));
        panel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 5; -fx-border-radius: 5;");


        Label filterLabel = new Label("Agent ID:");
        filterField = new TextField();
        filterField.setPromptText("Enter ID");
        filterField.setPrefWidth(100);

        Button applyFilter = new Button("Apply");
        Button clearFilter = new Button("Clear");

        panel.getChildren().addAll(filterLabel, filterField, applyFilter, clearFilter);

        applyFilter.setOnAction(e -> {
            String filterText = filterField.getText().trim();
            if (!filterText.isEmpty()) {
                try {
                    currentFilterAgentId = Integer.parseInt(filterText);
                    updateFxLog();
                } catch (NumberFormatException ex) {
                    showErrorDialog("Filter Error", "Please enter a valid Agent ID (number).");
                    currentFilterAgentId = -1;
                    updateFxLog();
                }
            } else {
                currentFilterAgentId = -1;
                updateFxLog();
            }
        });

        clearFilter.setOnAction(e -> {
            filterField.clear();
            currentFilterAgentId = -1;
            updateFxLog();
        });

        return panel;
    }

    /**
     * Builds the GridPane that shows the simulation statistics.
     * @return The configured stats panel Node.
     */
    private Node createStatsPanel() {
        GridPane panel = new GridPane();
        panel.setHgap(10);
        panel.setVgap(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 5; -fx-border-radius: 5;");


        totalAgentsValue = new Label("0");
        activeAgentsValue = new Label("0");
        burnedSensorsValue = new Label("0");
        activeAreasValue = new Label("0");

        panel.add(new Label("Total Agents:"), 0, 0); panel.add(totalAgentsValue, 1, 0);
        panel.add(new Label("Active Agents:"), 0, 1); panel.add(activeAgentsValue, 1, 1);
        panel.add(new Label("Burned Sensors:"), 0, 2); panel.add(burnedSensorsValue, 1, 2);
        panel.add(new Label("Active Areas:"), 0, 3); panel.add(activeAreasValue, 1, 3);

        return panel;
    }

    /**
     * Starts the periodic update of the statistics labels.
     */
    private void startStatsUpdates() {
        statsTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateStats()));
        statsTimeline.setCycleCount(Animation.INDEFINITE);
        statsTimeline.play();
    }

    /**
     * Updates the statistics labels with current data from the model.
     * Ensures UI updates happen on the JavaFX Application Thread.
     */
    private void updateStats() {
        int totalAgents = model.getAgentLog().size();
        int activeAgents = model.getActiveAgentCount();
        int burnedSensors = model.getBurnedSensorCount();
        long uniqueLocations = model.getAgentLog().stream()
                .map(SimulationModel.LogEntry::getLocation)
                .distinct()
                .count();

        Platform.runLater(() -> {
            totalAgentsValue.setText(Integer.toString(totalAgents));
            activeAgentsValue.setText(Integer.toString(activeAgents));
            burnedSensorsValue.setText(Integer.toString(burnedSensors));
            activeAreasValue.setText(Long.toString(uniqueLocations));
        });
    }

    /**
     * Refreshes the log table, applying the current agent-ID filter if set.
     *  Scrolls to the newest entry if the table is not empty.
     */
    public void updateFxLog() {
        List<SimulationModel.LogEntry> rawLog = model.getAgentLog();
        List<LogEntryWrapper> filteredLog;

        if (currentFilterAgentId != -1) {
            filteredLog = rawLog.stream()
                    .filter(entry -> entry.getAgentId() == currentFilterAgentId)
                    .map(this::createLogEntryWrapper)
                    .collect(Collectors.toList());
        } else {

            filteredLog = rawLog.stream()
                    .map(this::createLogEntryWrapper)
                    .collect(Collectors.toList());
        }

        Platform.runLater(() -> {
            logData.setAll(filteredLog);
            if (!logData.isEmpty()) {
                logTable.scrollTo(logData.size() - 1);
            }
        });
    }

    /**
     * Helper to convert SimulationModel.LogEntry to LogEntryWrapper.
     */
    private LogEntryWrapper createLogEntryWrapper(SimulationModel.LogEntry entry) {
        return new LogEntryWrapper(
                entry.getTimestamp(),
                entry.getAgentId(),
                entry.getLocation().toString(),
                entry.getReason()
        );
    }


    /**
     * Resets the log table
     * Clearing all entries and the filter.
     */
    public void resetFxLog() {
        currentFilterAgentId = -1;
        Platform.runLater(() -> {
            logData.clear();
            filterField.clear();
            updateStats();
        });
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
     * Wrapper class for SimulationModel.LogEntry to be used in the JavaFX TableView.
     * Formatted time, agent ID, location, and reason properties.
     */
    public static class LogEntryWrapper {
        private final SimpleStringProperty time;
        private final SimpleIntegerProperty agentId;
        private final SimpleStringProperty location;
        private final SimpleStringProperty reason;
        private final SimpleLongProperty timestamp; // Keep original timestamp if needed

        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");


        public LogEntryWrapper(long timestamp, int agentId, String location, String reason) {
            this.timestamp = new SimpleLongProperty(timestamp);
            this.time = new SimpleStringProperty(DATE_FORMAT.format(new Date(timestamp)));
            this.agentId = new SimpleIntegerProperty(agentId);
            this.location = new SimpleStringProperty(location);
            this.reason = new SimpleStringProperty(reason);
        }

        public String getTime() { return time.get(); }
        public SimpleStringProperty timeProperty() { return time; }

        public int getAgentId() { return agentId.get(); }
        public SimpleIntegerProperty agentIdProperty() { return agentId; }

        public String getLocation() { return location.get(); }
        public SimpleStringProperty locationProperty() { return location; }

        public String getReason() { return reason.get(); }
        public SimpleStringProperty reasonProperty() { return reason; }

        public long getTimestamp() { return timestamp.get(); }
        public SimpleLongProperty timestampProperty() { return timestamp; }
    }
}