package lotusbidashboard;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 *
 * @author Jamie
 */
public class DashboardController implements Initializable {

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

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");

    private final SalesService salesService = new SalesService();
    private ObservableList<Sales> data = FXCollections.observableArrayList();
    private ObservableList<Sales> filteredData = FXCollections.observableArrayList();
    private List<Integer> years;
    private List<String> vehicles;
    private List<String> regions;
    private final List<String> quarters = Arrays.asList("Q1", "Q2", "Q3", "Q4");
    private final List<String> chartSeriesList = Arrays.asList("Vehicles", "Regions");

    private List<CheckBox> yearCheckboxes = new ArrayList<>();
    private List<CheckBox> vehicleCheckboxes = new ArrayList<>();
    private List<CheckBox> regionCheckboxes = new ArrayList<>();
    private List<CheckBox> quarterCheckboxes = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //system time
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
                pieCompareYearChoiceBox.getItems().setAll(years.stream()
                        .filter(o -> !o.equals(newValue))
                        .collect(Collectors.toList()));
                pieCompareYearChoiceBox.getSelectionModel().selectFirst();
                System.out.println("pieYearChoiceBoxChangedFired - build pie chart");
                //build
                buildPieChart();
            }
        });

        pieCompareYearChoiceBox.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue observable, Integer oldValue, Integer newValue) {
                //fire event
                System.out.println("pieCompareYearChoiceBoxChangedFired - build compare pie chart");
                buildComparePieChart();
            }
        });

        //set service change listener
        salesService.stateProperty().addListener((ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) -> {
            System.out.println(newState.toString());
            switch (newState) {
                case SCHEDULED:
                    break;
                case RUNNING:
                    veil.setVisible(true);
                    veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.25)");
                    veil.setOpacity(0.8);
                    myProgressIndicator.setVisible(true);
                    myProgressIndicator.progressProperty().bind(salesService.progressProperty());
                    break;
                case READY:
                case SUCCEEDED:
                case CANCELLED:
                case FAILED:
                    myProgressIndicator.progressProperty().unbind();
                    myProgressIndicator.setVisible(false);
                    veil.setVisible(false);
                    setLastUpdated();
                    break;
            }
        });

        salesService.setOnSucceeded(event -> {
            data.setAll(salesService.getValue());
        });

        data.addListener(new ListChangeListener<Sales>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Sales> c) {

                //checkbox filter setup
                setFilters();
                createFilterCheckboxes();
                addFiltersToUI();

                //do after filters set
                //set choice boxes
                bindPieChartChoiceBoxes();
                filterData();
            }
        });

        buildTable();

        myProgressIndicator.progressProperty().bind(salesService.progressProperty());

        salesService.start();
    }

    private void buildPieChart() {
        //clear chart
        mainPieChart.getData().clear();
        int year = (int) pieYearChoiceBox.getSelectionModel().getSelectedItem();
        if (year != 0) {
            Map<String, Integer> list = null;
            String series = pieSeriesChoiceBox.getSelectionModel().getSelectedItem().toString();
            System.out.println(series);

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
                mainPieChart.getData().add(pieData);
            });
        }
    }

    private void buildComparePieChart() {
        comparePieChart.getData().clear();
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
                comparePieChart.getData().add(pieData);
            });
        }
    }

    private void bindPieChartChoiceBoxes() {
        //first year selection
        //NB - might need to change order

        //get current selected value
        int selectedValue = 0;
        if (pieYearChoiceBox.getItems().size() > 0) {
            System.out.println(pieYearChoiceBox.getSelectionModel().getSelectedItem().getClass().getName());
            selectedValue = (int) pieYearChoiceBox.getSelectionModel().getSelectedItem();
        }

        //combine new data with old to get any additional values using hashset
        HashSet<Integer> combinedList = new HashSet<>();
        combinedList.addAll(pieYearChoiceBox.getItems());
        years.stream().forEach(o -> combinedList.add(o));

        pieYearChoiceBox.getItems().setAll(combinedList);
        if (selectedValue == 0) {
            pieYearChoiceBox.getSelectionModel().selectFirst();
        } else {
            pieYearChoiceBox.getSelectionModel().select(selectedValue);
        }

        //second year selection
        if (selectedValue != 0) {
            combinedList.remove(selectedValue);

            selectedValue = 0;
            if (pieCompareYearChoiceBox.getItems().size() > 0) {
                selectedValue = (int) pieCompareYearChoiceBox.getSelectionModel().getSelectedItem();
            }
            pieCompareYearChoiceBox.getItems().setAll(combinedList);
            if (selectedValue == 0) {
                pieCompareYearChoiceBox.getSelectionModel().selectFirst();
            } else {
                pieCompareYearChoiceBox.getSelectionModel().select(selectedValue);
            }
        }
    }

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
                chartData.setNode(new HoveredThresholdNode(entry.getValue()));
                return chartData;
            }).forEach((chartData) -> {
                series.getData().add(chartData);
            });
            return series;
        }).forEach((series) -> {
            barChart.getData().add(series);
        });
    }

    private void buildLineChart() {

        lineChart.getData().clear();
        lineChart.getXAxis().setLabel("Vehicle");

        years.stream().map((year) -> {
            XYChart.Series series = new XYChart.Series();
            series.setName(Integer.toString(year));
            Map<String, Integer> totalSalesByYear = filteredData.stream()
                    .filter(o -> o.getYear() == year)
                    .collect(Collectors.groupingBy(Sales::getVehicle, Collectors.reducing(0, Sales::getQuantity, Integer::sum)));

            totalSalesByYear.entrySet().stream().map((entry) -> {
                XYChart.Data chartData = new XYChart.Data<>(entry.getKey(), entry.getValue());
                chartData.setNode(new HoveredThresholdNode(entry.getValue()));
                return chartData;
            }).forEach((chartData) -> {
                series.getData().add(chartData);
            });
            return series;
        }).forEach((series) -> {
            lineChart.getData().add(series);
        });
    }

    private void bindTable() {
        dataTable.getItems().clear();
        dataTable.getItems().addAll(filteredData);
    }

    private void setLastUpdated() {
        lastUpdatedLabel.setText(String.format("Last Updated: %s", fullDateFormat.format(new Date())));
    }

    //alter this method to use .reset()
    public void onAutoUpdateCheckChanged(ActionEvent actionEvent) {
        if (autoUpdateCheck.isSelected()) {
            if (!salesService.isRunning()) {
                salesService.reset();
                salesService.start();
            }
        } else {
            salesService.cancel();
        }
    }

    private void setFilters() {
        years = data.stream().map(o -> o.getYear()).distinct().sorted().collect(Collectors.toList());
        vehicles = data.stream().map(o -> o.getVehicle()).distinct().sorted().collect(Collectors.toList());
        regions = data.stream().map(o -> o.getRegion()).distinct().sorted().collect(Collectors.toList());
    }

    private void createFilterCheckboxes() {
        /*  
            used to compare previous to new as checkboxes 
            rebuilt on receiving new data.
         */
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

    private void addFiltersToUI() {
        chartFilters.getChildren().clear();
        chartFilters.getChildren().add(new Label("Year:"));
        chartFilters.getChildren().add(filterHBox(yearCheckboxes));
        chartFilters.getChildren().add(new Label("Quater:"));
        chartFilters.getChildren().add(filterHBox(quarterCheckboxes));
        chartFilters.getChildren().add(new Label("Vehicle:"));
        chartFilters.getChildren().add(filterHBox(vehicleCheckboxes));
        chartFilters.getChildren().add(new Label("Region:"));
        chartFilters.getChildren().add(filterHBox(regionCheckboxes));
    }

    private HBox filterHBox(List<CheckBox> checkBoxes) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10, 10, 10, 10));
        hbox.setSpacing(10);
        for (CheckBox cb : checkBoxes) {
            hbox.getChildren().add(cb);
        }
        return hbox;
    }

    private List<CheckBox> buildCheckboxes(List<CheckBox> cbList, List list) {
        List<CheckBox> checkBoxes = new ArrayList<CheckBox>();

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
            for (CheckBox origCb : cbList) {
                for (CheckBox newCb : checkBoxes) {
                    if (newCb.getText().equals(origCb.getText())) {
                        newCb.setSelected(origCb.isSelected());
                    }
                }
            }
        }

        return checkBoxes;
    }

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
        //dataTable.itemsProperty().bind(salesService.valueProperty());
    }

//    private void filterData() {
//        
//        List<Sales> list = new ArrayList<Sales>();
//        
//        for(CheckBox year : yearCheckboxes){
//            if(year.isSelected()){
//                list.addAll(data.stream().filter(o -> Integer.toString(o.getYear()).equals(year.getText())).collect(Collectors.toList()));
//            }
//        }
//        
//        for(CheckBox region : regionCheckboxes){
//            if(region.isSelected()){
//                list = list.stream().filter(o -> o.getRegion().equals(region.getText())).collect(Collectors.toList());
//            }
//        }
//        
//        for(CheckBox quarter : quarterCheckboxes){
//            if(quarter.isSelected()){
//                list = list.stream().filter(o -> Integer.toString(o.getQTR()).equals(quarter.getText())).collect(Collectors.toList());
//            }
//        }
//        System.out.println("filtered data size " + list.size());
//        
//    }
    public void filterData() {
        //ObservableList<Sales> filteredData = FXCollections.observableArrayList(); 
        filteredData.clear();
        data.stream().forEach((Sales s) -> {
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
            //System.out.println(s + " = " + foundYear +" : "+foundVehicle +" : "+ foundRegion  +" : "+ foundQuarter); 
            if (foundYear && foundVehicle && foundRegion && foundQuarter) {
                filteredData.add(s);
            }
        });
        //updateStats(filteredData); 
        buildBarChart();
        buildLineChart();
        buildPieChart();
        buildComparePieChart();
        bindTable();
    }

}
