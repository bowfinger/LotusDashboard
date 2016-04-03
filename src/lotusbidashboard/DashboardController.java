package lotusbidashboard;

import lotusbidashboard.services.SalesService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lotusbidashboard.canvasworker.BoxPlotDrawer;
import lotusbidashboard.canvasworker.ResizableCanvas;
import lotusbidashboard.data.parser.ImportExportParser;

/**
 *
 * @author Jamie Deville, John Harvey and Sam Griffiths
 */
public class DashboardController implements Initializable {

    @FXML
    private BorderPane rootNode;

    @FXML
    private ProgressIndicator myProgressIndicator;

    @FXML
    private TableView dataTable;

    @FXML
    private Region veil;

    @FXML
    private LineChart lineChart;

    @FXML
    private BarChart barChart;

    @FXML
    private CheckBox autoUpdateCheck;

    @FXML
    private Label lastUpdatedLabel;

    @FXML
    private VBox chartFilters;

    @FXML
    private Label sysTimeLabel;

    @FXML
    private PieChart mainPieChart;

    @FXML
    private PieChart comparePieChart;

    @FXML
    private ChoiceBox pieSeriesChoiceBox;

    @FXML
    private ChoiceBox pieYearChoiceBox;

    @FXML
    private ChoiceBox pieCompareYearChoiceBox;

    @FXML
    private CheckBox pieCompareCheckBox;

    @FXML
    private AnchorPane canvasWrapper;

    @FXML
    private ComboBox addYearComboBox;

    @FXML
    private ComboBox addQuarterComboBox;

    @FXML
    private ComboBox addRegionComboBox;

    @FXML
    private TextField addQuantityTextField;

    @FXML
    private ComboBox addVehicleComboBox;

    @FXML
    private Label importDataMessageLabel;
    
    @FXML
    private Label statsMinLabel;
    
    @FXML
    private Label statsMaxLabel;
    
    @FXML
    private Label statsAverageLabel;
    
    @FXML
    private Label statsTotalLabel;
    
    @FXML
    private Label statsSdLabel;

    private final ResizableCanvas boxPlotCanvas = new ResizableCanvas();
    private final BoxPlotDrawer bpd = new BoxPlotDrawer();

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");

    private final List<ExtensionFilter> fileSaveFilters = new ArrayList<>(Arrays.asList(
            new FileChooser.ExtensionFilter("CSV", "*.csv"),
            new FileChooser.ExtensionFilter("XLS", "*.xls")
    ));

    private final FileChooser fileChooser = new FileChooser();

    private final SalesService salesService = SalesService.getInstance();
    private final ObservableList<Sales> data = FXCollections.observableArrayList();
    private final ObservableList<Sales> filteredData = FXCollections.observableArrayList();
    private final ObservableList<Sales> projectedData = FXCollections.observableArrayList();
    private final Stats statsObject = new Stats();
    
    private List<Integer> years;
    private List<String> vehicles;
    private List<String> regions;
    private final List<String> quarters = Arrays.asList("Q1", "Q2", "Q3", "Q4");
    private final List<String> chartSeriesList = Arrays.asList("Vehicles", "Regions");

    private List<CheckBox> yearCheckboxes = new ArrayList<>();
    private List<CheckBox> vehicleCheckboxes = new ArrayList<>();
    private List<CheckBox> regionCheckboxes = new ArrayList<>();
    private List<CheckBox> quarterCheckboxes = new ArrayList<>();
    
    private final static String DECIMAL_FORMAT_2DP = "%.2f";

    /**
     * All bindings and static UI collections set here.
     * ScheduledService events hooked into.
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
                
        //bind stats - uses 2 decimal places (optional) as format
        statsMinLabel.textProperty().bind(statsObject.minProperty().asString(DECIMAL_FORMAT_2DP));
        statsMaxLabel.textProperty().bind(statsObject.maxProperty().asString(DECIMAL_FORMAT_2DP));
        statsAverageLabel.textProperty().bind(statsObject.averageProperty().asString(DECIMAL_FORMAT_2DP));
        statsTotalLabel.textProperty().bind(statsObject.totalProperty().asString(DECIMAL_FORMAT_2DP));
        statsSdLabel.textProperty().bind(statsObject.sdvProperty().asString(DECIMAL_FORMAT_2DP));
        
        //set quarters
        addQuarterComboBox.getItems().addAll(quarters);

        //callback
        boxPlotCanvas.setRedraw(() -> {
            bpd.drawBoxPlot(boxPlotCanvas, statsObject);
        });
        //bind canvas size to wrapper
        boxPlotCanvas.widthProperty().bind(canvasWrapper.widthProperty());
        boxPlotCanvas.heightProperty().bind(canvasWrapper.heightProperty());
        canvasWrapper.getChildren().add(boxPlotCanvas);

        //set file chooser defaults
        fileChooser.getExtensionFilters().addAll(fileSaveFilters);
        fileChooser.setSelectedExtensionFilter(fileSaveFilters.get(0));

        //system time displayed in bottom right corner
        final Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            final Calendar cal = Calendar.getInstance();
            sysTimeLabel.setText(timeFormat.format(cal.getTime()));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        //Pie Chart onchange listeners
        pieSeriesChoiceBox.getItems().setAll(chartSeriesList);
        pieSeriesChoiceBox.getSelectionModel().selectFirst();

        pieSeriesChoiceBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                buildPieChart();
                buildComparePieChart();
            }
        });

        pieCompareYearChoiceBox.disableProperty().bind(pieCompareCheckBox.selectedProperty().not());
        comparePieChart.visibleProperty().bind(pieCompareCheckBox.selectedProperty());

        pieYearChoiceBox.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue observable, Integer oldValue, Integer newValue) {
                //null check as lists cleared when updating data source
                if (newValue != null) {
                    pieCompareYearChoiceBox.getItems().setAll(years.stream()
                            .filter(o -> !o.equals(newValue))
                            .collect(Collectors.toList()));
                    pieCompareYearChoiceBox.getSelectionModel().selectFirst();
                    //build
                    buildPieChart();
                }
            }
        });

        pieCompareYearChoiceBox.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue observable, Integer oldValue, Integer newValue) {
                //null check as lists cleared when updating data source
                if (newValue != null) {
                    //fire event
                    buildComparePieChart();
                }
            }
        });

        //set service change listener
        //ready->failed will fall through
        salesService.stateProperty().addListener((ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) -> {
            System.out.println("SERVICE_STATUS - " + newState.toString());
            switch (newState) {
                case SCHEDULED:
                    break;
                case RUNNING:
                    veil.setVisible(true);
                    veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.25)");
                    veil.setOpacity(0.8);
                    myProgressIndicator.setVisible(true);
                    break;
                case READY:
                case SUCCEEDED:
                case CANCELLED:
                case FAILED:
                    myProgressIndicator.setVisible(false);
                    veil.setVisible(false);
                    setLastUpdated();
                    break;
            }
        });

        //listener for succeeded event
        salesService.setOnSucceeded(event -> {
            data.setAll(salesService.getValue());
        });

        //listener for failed event
        salesService.setOnFailed(event -> {
            //display choice box stating error
            //and whether to retry or quit
            System.out.println(event.getSource().getException().getMessage());
        });

        data.addListener(new ListChangeListener<Sales>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Sales> c) {
                buildUIWrapper();
            }
        });
        projectedData.addListener(new ListChangeListener<Sales>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Sales> c) {
                buildUIWrapper();
            }
        });

        buildTable();

        myProgressIndicator.progressProperty().bind(salesService.progressProperty());

        //start the service to connect to web service
        salesService.start();
    }

    /**
     * Wrapper to call multiple methods
     */
    private void buildUIWrapper() {
        //checkbox filter setup
        setFilters();
        createFilterCheckboxes();
        addFiltersToUI();

        //do after filters set
        //set choice boxes
        bindPieChartChoiceBoxes();
        bindProjectedChoiceBoxes();
        filterData();
    }

    /**
     * Builds first Pie Chart based on globally filtered data
     */
    private void buildPieChart() {
        //clear chart
        mainPieChart.getData().clear();
        if (pieYearChoiceBox.getItems().size() > 0) {
            int year = (int) pieYearChoiceBox.getSelectionModel().getSelectedItem();
            if (year != 0) {
                Map<String, Integer> list = null;
                String series = pieSeriesChoiceBox.getSelectionModel().getSelectedItem().toString();

                mainPieChart.setTitle(String.format("Total sales by %s - %d", series, year));
                switch (series) {
                    case "Vehicles":
                        list = filteredData.stream().filter(o -> o.getYear() == year).collect(Collectors.groupingBy(Sales::getVehicle, Collectors.reducing(0, Sales::getQuantity, Integer::sum)));
                        break;
                    case "Regions":
                        list = filteredData.stream().filter(o -> o.getYear() == year).collect(Collectors.groupingBy(Sales::getRegion, Collectors.reducing(0, Sales::getQuantity, Integer::sum)));
                        break;
                }
                list.entrySet().stream().forEach((entry) -> {
                    PieChart.Data pieData = new PieChart.Data(entry.getKey(), entry.getValue());
                    pieData.setName(entry.getKey() + ": " + entry.getValue().toString());
                    mainPieChart.getData().add(pieData);
                });
            }
        }
    }

    /**
     * Builds second Pie Chart based on globally filtered data.
     * Only visible if "compare" option is checked.
     */
    private void buildComparePieChart() {
        comparePieChart.getData().clear();
        if (pieCompareYearChoiceBox.getItems().size() > 0) {
            int year = (int) pieCompareYearChoiceBox.getSelectionModel().getSelectedItem();
            if (year != 0) {
                Map<String, Integer> list = null;
                String series = pieSeriesChoiceBox.getSelectionModel().getSelectedItem().toString();

                comparePieChart.setTitle(String.format("Total sales by %s - %d", series, year));
                switch (series) {
                    case "Vehicles":
                        list = filteredData.stream().filter(o -> o.getYear() == year).collect(Collectors.groupingBy(Sales::getVehicle, Collectors.reducing(0, Sales::getQuantity, Integer::sum)));
                        break;
                    case "Regions":
                        list = filteredData.stream().filter(o -> o.getYear() == year).collect(Collectors.groupingBy(Sales::getRegion, Collectors.reducing(0, Sales::getQuantity, Integer::sum)));
                        break;
                }
                list.entrySet().stream().forEach((entry) -> {
                    PieChart.Data pieData = new PieChart.Data(entry.getKey(), entry.getValue());
                    pieData.setName(entry.getKey() + ": " + entry.getValue().toString());
                    comparePieChart.getData().add(pieData);
                });
            }
        }
    }

    /**
     * Sets choice boxes for local Pie Chart filters.
     * Utilises HashMap to ensure no duplicates are in list.
     */
    private void bindPieChartChoiceBoxes() {
        //get current selected value
        int selectedIndex = -1;
        if (pieYearChoiceBox.getItems().size() > 0) {
            selectedIndex = pieYearChoiceBox.getSelectionModel().getSelectedIndex();
        }

        //combine new data with old to get any additional values using hashset
        HashSet<Integer> combinedList = new HashSet<>();
        combinedList.addAll(pieYearChoiceBox.getItems());
        years.stream().forEach(o -> combinedList.add(o));

        pieYearChoiceBox.getItems().setAll(combinedList);
        if (selectedIndex == -1) {
            pieYearChoiceBox.getSelectionModel().selectFirst();
        } else {
            pieYearChoiceBox.getSelectionModel().select(selectedIndex);
        }
    }

    /**
     * Builds Bar Chart based on filtered data.
     * Iterates over all year values to ensure chart data and series
     * are consistent.
     * Also adds custom hover label for data nodes.
     */
    private void buildBarChart() {
        //clear charts 
        barChart.getData().clear();

        years.stream().map((year) -> {
            XYChart.Series series = new XYChart.Series();
            series.setName(Integer.toString(year));
            Map<String, Integer> totalSalesByYear = filteredData.stream()
                    .filter(o -> o.getYear() == year)
                    .collect(Collectors.groupingBy(Sales::getVehicle, Collectors.reducing(0, Sales::getQuantity, Integer::sum)));

            totalSalesByYear.entrySet().stream().map((entry) -> {
                XYChart.Data chartData = new XYChart.Data<>(entry.getKey(), entry.getValue());
                chartData.setNode(new ChartDataHoverLabel(entry.getValue()));
                return chartData;
            }).forEach((chartData) -> {
                series.getData().add(chartData);
            });
            return series;
        }).forEach((series) -> {
            barChart.getData().add(series);
        });
    }

    /**
     * Builds Line Chart based on filtered data.
     * Iterates over all vehicle values to ensure chart data and series
     * are consistent.
     * Also adds custom hover label for data nodes.
     */
    private void buildLineChart() {
        lineChart.getData().clear();
        lineChart.getXAxis().setLabel("Vehicle");

        vehicles.stream().map((vehicle) -> {
            XYChart.Series series = new XYChart.Series();
            series.setName(vehicle);
            Map<Integer, Integer> totalSalesByYear = filteredData.stream()
                    .filter(o -> o.getVehicle().equals(vehicle))
                    .collect(Collectors.groupingBy(Sales::getYear, Collectors.reducing(0, Sales::getQuantity, Integer::sum)));
            
            totalSalesByYear.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map((entry) -> {
                XYChart.Data chartData = new XYChart.Data<>(entry.getKey().toString(), entry.getValue());
                chartData.setNode(new ChartDataHoverLabel(entry.getValue()));
                return chartData;
            }).forEach((chartData) -> {
                series.getData().add(chartData);
            });
            return series;
        }).forEach((series) -> {
            lineChart.getData().add(series);
        });
    }

    /**
     * Populates projected data choice boxes using values populated by
     * ScheduledService.
     */
    private void bindProjectedChoiceBoxes() {
        addYearComboBox.getItems().clear();
        addYearComboBox.getItems().add(null);
        addYearComboBox.getItems().addAll(years);
        addRegionComboBox.getItems().clear();
        addRegionComboBox.getItems().add(null);
        addRegionComboBox.getItems().addAll(regions);
        addVehicleComboBox.getItems().clear();
        addVehicleComboBox.getItems().add(null);
        addVehicleComboBox.getItems().addAll(vehicles);
    }

    /**
     * Refreshes data in TableView
     */
    private void bindTable() {
        dataTable.getItems().clear();
        dataTable.getItems().addAll(filteredData);
    }

    /**
     * Called from SalesService to set last contact time
     */
    private void setLastUpdated() {
        lastUpdatedLabel.setText(String.format("Last Updated: %s", fullDateFormat.format(new Date())));
    }

    @FXML
    public void onAutoUpdateCheckChanged(ActionEvent actionEvent) {
        if (autoUpdateCheck.isSelected()) {
            salesService.restart();
        } else {
            salesService.cancel();
        }
    }

    /**
     * Sets raw data for filters
     */
    private void setFilters() {
        ObservableList<Sales> tempData = FXCollections.observableArrayList();
        tempData.setAll(data);
        tempData.addAll(projectedData);
        years = tempData.stream().map(o -> o.getYear()).distinct().sorted().collect(Collectors.toList());
        vehicles = tempData.stream().map(o -> o.getVehicle()).distinct().sorted().collect(Collectors.toList());
        regions = tempData.stream().map(o -> o.getRegion()).distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Used to compare previous to new as CheckBoxs 
     * rebuilt on receiving new data.
     */
    private void createFilterCheckboxes() {
        List<CheckBox> temp;
        temp = yearCheckboxes;
        yearCheckboxes = buildCheckboxes(temp, years);

        //quarters
        temp = quarterCheckboxes;
        quarterCheckboxes = buildCheckboxes(temp, quarters);

        //vehicles
        temp = vehicleCheckboxes;
        vehicleCheckboxes = buildCheckboxes(temp, vehicles);

        //regions
        temp = regionCheckboxes;
        regionCheckboxes = buildCheckboxes(temp, regions);
    }

    /**
     * Dynamically builds global filter controls
     * and adds to static VBox defined by FXML
     */
    private void addFiltersToUI() {
        chartFilters.getChildren().clear();
        chartFilters.getChildren().add(new Label("Year:"));
        chartFilters.getChildren().add(filterHBox(yearCheckboxes));
        chartFilters.getChildren().add(new Label("Quarter:"));
        chartFilters.getChildren().add(filterHBox(quarterCheckboxes));
        chartFilters.getChildren().add(new Label("Vehicle:"));
        chartFilters.getChildren().add(filterHBox(vehicleCheckboxes));
        chartFilters.getChildren().add(new Label("Region:"));
        chartFilters.getChildren().add(filterHBox(regionCheckboxes));
    }

    /**
     * Accepts list of CheckBox to build a HBox of filters for this
     * filter type.
     * @param checkBoxes
     * @return HBox
     */
    private HBox filterHBox(List<CheckBox> checkBoxes) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10, 10, 10, 10));
        hbox.setSpacing(10);
        checkBoxes.stream().forEach((cb) -> {
            hbox.getChildren().add(cb);
        });
        return hbox;
    }

    /**
     * Compares new CheckBox list to existing to preserve selected states.
     * @param cbList
     * @param list
     * @return 
     */
    private List<CheckBox> buildCheckboxes(List<CheckBox> cbList, List list) {
        List<CheckBox> checkBoxes = new ArrayList<>();

        for (byte index = 0; index < list.size(); index++) {
            CheckBox cb = new CheckBox(list.get(index).toString());
            cb.setSelected(true);
            cb.setOnAction(e -> {
                filterData();
            });
            checkBoxes.add(cb);
        }

        //sets selected state after comparing to original state in list
        if (cbList.size() > 0) {
            cbList.stream().forEach((origCb) -> {
                checkBoxes.stream().filter((newCb) -> (newCb.getText().equals(origCb.getText()))).forEach((newCb) -> {
                    newCb.setSelected(origCb.isSelected());
                });
            });
        }

        return checkBoxes;
    }

    /**
     * Sets the columns and CellValueFactory for TableView
     */
    private void buildTable() {
        TableColumn yearCol = new TableColumn();
        yearCol.setText("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory("Year"));
        TableColumn qtrCol = new TableColumn();
        qtrCol.setText("Quarter");
        qtrCol.setCellValueFactory(new PropertyValueFactory<>("QTR"));
        TableColumn modelCol = new TableColumn();
        modelCol.setText("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory("Vehicle"));
        TableColumn regionCol = new TableColumn();
        regionCol.setText("Region");
        regionCol.setCellValueFactory(new PropertyValueFactory("Region"));
        TableColumn salesCol = new TableColumn();
        salesCol.setText("Sales");
        salesCol.setCellValueFactory(new PropertyValueFactory("Quantity"));

        dataTable.getColumns().addAll(yearCol, qtrCol, modelCol, regionCol, salesCol);
    }

    /**
     * Main method to push data to the UI.
     * Data is filtered based on selected CheckBoxs.
     * Nested loop with checks to reduce processing.
     */
    public void filterData() {
        filteredData.clear();
        ObservableList<Sales> tempData = FXCollections.observableArrayList();
        tempData.setAll(data);
        tempData.addAll(projectedData);
        //filters all data received from service using global checkboxes
        tempData.stream().forEach((Sales s) -> {
            boolean foundYear = false;
            boolean foundVehicle = false;
            boolean foundRegion = false;
            boolean foundQuarter = false;
            for (CheckBox box : yearCheckboxes) {
                if (box.isSelected() && box.getText().equals(Integer.toString(s.getYear()))) {
                    foundYear = true;
                }
            }
            if (foundYear) {
                for (CheckBox box : quarterCheckboxes) {
                    if (box.isSelected() && box.getText().equals('Q' + Integer.toString(s.getQTR()))) {
                        foundQuarter = true;
                    }
                }
                if (foundQuarter) {
                    for (CheckBox box : regionCheckboxes) {
                        if (box.isSelected() && box.getText().equals(s.getRegion())) {
                            foundRegion = true;
                        }
                    }
                    if (foundRegion) {
                        for (CheckBox box : vehicleCheckboxes) {
                            if (box.isSelected() && box.getText().equals(s.getVehicle())) {
                                foundVehicle = true;
                            }
                        }
                    }
                }
            }
            if (foundYear && foundVehicle && foundRegion && foundQuarter) {
                filteredData.add(s);
            }
        });
        statsObject.calculateValues(filteredData);
        buildBarChart();
        buildLineChart();
        buildPieChart();
        buildComparePieChart();
        bindTable();
        buildBoxPlot();
    }

    //MODALS
    @FXML
    public void showAbout(ActionEvent event) throws IOException {
        showModal("Lotus Dashboard About", "AboutFXML.fxml");
    }

    @FXML
    public void showSettings(ActionEvent event) throws IOException {
        showModal("Lotus Dashboard Settings", "SettingsFXML.fxml");
    }

    private void showModal(String windowTitle, String fxmlFile) throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(windowTitle);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/lotusLogo.png")));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(rootNode.getScene().getWindow());
        stage.show();
    }

    //export data
    @FXML
    public void onExportDataClick(ActionEvent event) {
        fileChooser.setTitle("Export Data");
        File file = fileChooser.showSaveDialog(rootNode.getScene().getWindow());
        if (file != null) {
            try {
                if (!file.getName().isEmpty()) {
                    ImportExportParser eParser = new ImportExportParser();
                    switch (fileChooser.getSelectedExtensionFilter().getDescription()) {
                        //excel can handle both
                        case "CSV":
                        case "XLS":
                            eParser.toCsv(file, filteredData);
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    //import data
    @FXML
    public void onImportDataClick(ActionEvent event) {
        openImportWindow();
    }

    //separated out as functionality is used elsewhere
    private void openImportWindow() {
        fileChooser.setTitle("Import Data");

        File file = fileChooser.showOpenDialog(rootNode.getScene().getWindow());
        if (file != null) {
            //disable autoupdate of data to stop overwriting
            autoUpdateCheck.selectedProperty().set(false);

            try {
                if (!file.getName().isEmpty()) {
                    ImportExportParser eParser = new ImportExportParser();
                    switch (fileChooser.getSelectedExtensionFilter().getDescription()) {
                        //parser detects file type and delimiter
                        case "CSV":
                        case "XLS":
                            //projectedData.setAll(eParser.fromCsv(file));
                            addImportedData(eParser.fromCsv(file));
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    //adds list of imported data ensuring there are no duplicates
    private void addImportedData(List<Sales> importedData) {
        List<Sales> nonDupes = new ArrayList<>();

        for (Sales s1 : importedData) {
            boolean found = false;
            for (Sales s2 : data) {
                if (s1.isEqualTo(s2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                nonDupes.add(s1);
            }
        }
        projectedData.setAll(nonDupes);
    }

    @FXML
    public void onExitClick(ActionEvent event) {
        Platform.exit();
    }

    private void buildBoxPlot() {
        bpd.drawBoxPlot(boxPlotCanvas, statsObject);
    }

    @FXML
    public void onAddProjectedButtonClick(ActionEvent event) {
        String outputMessage = "";
        boolean errored = false;
        Sales newSalesData = new Sales();
        //check data
        if (!addYearComboBox.getEditor().getText().isEmpty()
                && addYearComboBox.getEditor().getText().length() == 4) {
            try {
                newSalesData.setYear(Integer.parseInt(addYearComboBox.getEditor().getText()));
            } catch (Exception e) {
                errored = true;
                outputMessage += "* Year in wrong format\n";
            }
        } else if (addYearComboBox.getValue() != null
                && !addYearComboBox.getValue().toString().isEmpty()
                && addYearComboBox.getValue().toString().length() == 4) {
            try {
                newSalesData.setYear(Integer.parseInt(addYearComboBox.getValue().toString()));
            } catch (Exception e) {
                errored = true;
                outputMessage += "* Year in wrong format\n";
            }
        } else {
            errored = true;
        }

        if (addQuarterComboBox.getValue() != null) {
            try {
                String quarter = addQuarterComboBox.getValue().toString();
                newSalesData.setQTR(Integer.parseInt(quarter.substring(quarter.length() - 1)));
            } catch (Exception e) {
                errored = true;
                outputMessage += "* Quarter in wrong format\n";
            }
        } else {
            errored = true;
        }
        
        if (!addVehicleComboBox.getEditor().getText().equals("")) {
            try {
                newSalesData.setVehicle(addVehicleComboBox.getEditor().getText());
            } catch (Exception e) {
                errored = true;
                outputMessage += "* Year in wrong format\n";
            }
        }else if (addVehicleComboBox.getValue() != null
                && !addVehicleComboBox.getValue().toString().isEmpty()) {
            try {
                newSalesData.setVehicle(addVehicleComboBox.getValue().toString());
            } catch (Exception e) {
                errored = true;
                outputMessage += "* Vehicle in wrong format\n";
            }
        } else {
            errored = true;
        }
        
        if (addRegionComboBox.getValue() != null) {
            try {
                newSalesData.setRegion(addRegionComboBox.getValue().toString());
            } catch (Exception e) {
                errored = true;
                outputMessage += "* Region in wrong format\n";
            }
        } else {
            errored = true;
        }
        
        if (!addQuantityTextField.getText().equals("")) {
            try {
                newSalesData.setQuantity(Integer.parseInt(addQuantityTextField.getText()));
            } catch (Exception e) {
                errored = true;
                outputMessage += "* Quantity in wrong format\n";
            }
        } else {
            errored = true;
        }

        if (!errored) {
            //add to data
            projectedData.add(newSalesData);
            outputMessage = "Sales Data Added!";

            //clear
            addYearComboBox.getEditor().setText(null);
            addYearComboBox.setValue(null);
            addQuarterComboBox.setValue(null);
            addVehicleComboBox.setValue(null);
            addVehicleComboBox.getEditor().setText(null);
            addRegionComboBox.setValue(null);
            addQuantityTextField.setText(null);
        }
        importDataMessageLabel.setText(outputMessage);
    }

    @FXML
    public void onImportProjectedButtonClick(ActionEvent event) {
        openImportWindow();
    }

    @FXML
    public void onClearProjectedButtonClick(ActionEvent event) {
        projectedData.clear();
        importDataMessageLabel.setText("Projected Data Cleared");
    }
}
