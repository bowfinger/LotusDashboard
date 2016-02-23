/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lotusbidashboard;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Jamie
 */
public class Sales {
    
    //private final IntegerProperty year = new SimpleIntegerProperty();
    //private final IntegerProperty quarter = new SimpleIntegerProperty();
    //private final StringProperty region = new SimpleStringProperty();
    //private final StringProperty vehicle = new SimpleStringProperty();
    //private final IntegerProperty quantity = new SimpleIntegerProperty();
    
    /*
    public Sales(int QTR, int Quantity, String Region, String Vehicle, int Year){
        this.quarter.set(QTR);
        this.quantity.set(Quantity);
        this.region.set(Region);
        this.vehicle.set(Vehicle);
        this.year.set(Year);
    }
    */
    private IntegerProperty year;
    private int Year;
    public int getYear() {
        return (year != null)? year.get() : Year;
    }
    public void setYear(int value) {
        if (year != null) {
            year.set(value);
        } else {
            Year = value;
        }
    }
    public IntegerProperty yearProperty() {
        if (year == null) {
            year = new SimpleIntegerProperty(Year);
        }
        return year;
    }
    
    private IntegerProperty quarter;
    private int QTR;
    public int getQTR() {
        return (quarter != null)? quarter.get() : QTR;
    }
    public void setQTR(int value) {
        if (quarter != null) {
            quarter.set(value);
        } else {
            QTR = value;
        }
    }
    public IntegerProperty quarterProperty() {
        if (quarter == null) {
            quarter = new SimpleIntegerProperty(QTR);
        }
        return quarter;
    }
    
    private StringProperty region;
    private String Region;
    public String getRegion() {
        return (region != null)? region.get() : Region;
    }
    public void setRegion(String value) {
        if (region != null) {
            region.set(value);
        } else {
            Region = value;
        }
    }
    public StringProperty regionProperty() {
        if (region == null) {
            region = new SimpleStringProperty(Region);
        }
        return region;
    }

    private StringProperty vehicle;
    private String Vehicle;
    public String getVehicle() {
        return (vehicle != null)? vehicle.get() : Vehicle;
    }
    public void setVehicle(String value) {
        if (vehicle != null) {
            vehicle.set(value);
        } else {
            Vehicle = value;
        }
    }
    public StringProperty vehicleProperty() {
        if (vehicle == null) {
            vehicle = new SimpleStringProperty(Vehicle);
        }
        return vehicle;
    }
    
    private IntegerProperty quantity;
    private int Quantity;
    public int getQuantity() {
        return (quantity != null)? quantity.get() : Quantity;
    }
    public void setQuantity(int value) {
        if (quantity != null) {
            quantity.set(value);
        } else {
            Quantity = value;
        }
    }
    public IntegerProperty quantityProperty() {
        if (quantity == null) {
            quantity = new SimpleIntegerProperty(Quantity);
        }
        return quantity;
    }
    
    /*
    public void setYear(int year){
        this.year.set(year);
    }

    public int getYear(){
        return this.year.get();
    }
    
    public IntegerProperty yearProperty(){
        return this.year;
    }
    

    public void setQuarter(byte quarter){
        this.quarter.set(quarter);
    }

    public int getQuarter(){
        return this.quarter.get();
    }
    
    public IntegerProperty quarterProperty(){
        return this.quarter;
    }
    
    public void setRegion(String region){
        this.region.set(region);
    }

    public String getRegion(){
        return this.region.get();
    }
    
    public StringProperty regionProperty(){
        return this.region;
    }
    
    public void setVehicle(String vehicle){
        this.vehicle.set(vehicle);
    }

    public String getVehicle(){
        return this.vehicle.get();
    }
    
    public StringProperty vehicleProperty(){
        return this.vehicle;
    }

    public void setQuantity(int sales){
        this.quantity.set(sales);
    }

    public int getQuantity(){
        return this.quantity.get();
    }
    
    public IntegerProperty quantityProperty(){
        return this.quantity;
    }
    */
}
