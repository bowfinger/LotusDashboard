/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lotusbidashboard;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.Observable;
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
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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
    private Accordion accordion;
    
    @FXML
    private TitledPane chartsPane;

    private final SalesService salesService = new SalesService();
    private ObservableList<Sales> data = FXCollections.observableArrayList();
    private List<Sales> filteredData;
    private List<Integer> years;
    private List<String> vehicles;
    private List<String> regions;
    private final List<String> quarters = Arrays.asList("Q1","Q2","Q3","Q4");
    
    private List<CheckBox> yearCheckboxes = new ArrayList<CheckBox>();
    private List<CheckBox> vehicleCheckboxes = new ArrayList<CheckBox>();
    private List<CheckBox> regionCheckboxes = new ArrayList<CheckBox>();
    private List<CheckBox> quarterCheckboxes = new ArrayList<CheckBox>();
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //set expanded accordion
        accordion.setExpandedPane(chartsPane);
        
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
        
        data.addListener((ListChangeListener.Change<? extends Sales> c) -> {
            
            //checkbox filter setup
            setFilters();
            createFilterCheckboxes();
            addFiltersToUI();
                
            filterData();
            bindTable();
            buildBarChart();
        });
        
        buildTable();
    
        myProgressIndicator.progressProperty().bind(salesService.progressProperty());

        if(!salesService.isRunning()){
            salesService.reset();
            salesService.start();
        }
    }    
    
    private void buildBarChart() {
        //clear charts
        barChart.getData().clear();

        yearCheckboxes.stream().filter((year) -> (year.isSelected())).map((year) -> {
            XYChart.Series series = new XYChart.Series();
            series.setName(year.getText());
            data.stream().filter(o -> Integer.toString(o.getYear()).equals(year.getText())).forEach(o -> series.getData().add(new XYChart.Data<>(o.getVehicle(), o.getQuantity())));
            return series;
        }).forEach((series) -> {
            barChart.getData().add(series);
        });
    }


    private void bindTable() {
        dataTable.getItems().clear();
        dataTable.getItems().addAll(data);
    }

    private void setLastUpdated() {
        lastUpdatedLabel.setText(String.format("Last Updated: %s", new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(new Date())));
    }

    public void onAutoUpdateCheckChanged(ActionEvent actionEvent) {
        if(autoUpdateCheck.isSelected()){
            if(!salesService.isRunning()){
                salesService.reset();
                salesService.start();
            }
        }else{
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
        chartFilters.getChildren().add(filterHBox(yearCheckboxes));
        chartFilters.getChildren().add(filterHBox(quarterCheckboxes));
        chartFilters.getChildren().add(filterHBox(vehicleCheckboxes));
        chartFilters.getChildren().add(filterHBox(regionCheckboxes));
    }
    
    private HBox filterHBox(List<CheckBox> checkBoxes){
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10, 10, 10, 10));
        hbox.setSpacing(10);
        for(CheckBox cb : checkBoxes){
            hbox.getChildren().add(cb);
        }
        return hbox;
    }

    private List<CheckBox> buildCheckboxes(List<CheckBox> cbList, List list) {
        List<CheckBox> checkBoxes = new ArrayList<CheckBox>();

        for (byte index = 0; index < list.size(); index++) {
            CheckBox cb = new CheckBox(list.get(index).toString());
            cb.setSelected(true);
            cb.setOnAction(e ->{
                
                filterData();
                //add more here
                buildBarChart();
            });
            checkBoxes.add(cb);
        }
        
        //sets selected state after comparing to original state in list
        if(cbList.size() > 0){
            for(CheckBox origCb : cbList){
            for(CheckBox newCb : checkBoxes){
                if(newCb.getText().equals(origCb.getText())){
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
        
        dataTable.getColumns().addAll(yearCol,qtrCol,modelCol,regionCol,salesCol);
        //dataTable.itemsProperty().bind(salesService.valueProperty());
    }

    private void filterData() {
        
        List<Sales> list = new ArrayList<Sales>();
        
        for(CheckBox year : yearCheckboxes){
            if(year.isSelected()){
                list = data.stream().filter(o -> Integer.toString(o.getYear()).equals(year.getText())).collect(Collectors.toList());
            }
        }
        System.out.println("filtered data size " + list.size());
        
    }
    
    
}
