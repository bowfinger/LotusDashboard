package lotusbidashboard;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;

/**
 *
 * @author cmpjharv - fixes by cmpjdevi
 */
public class Stats {
    private final DoubleProperty average = new SimpleDoubleProperty();
    private final DoubleProperty max = new SimpleDoubleProperty();
    private final DoubleProperty min = new SimpleDoubleProperty();
    private final DoubleProperty sdv = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();
 
    public double getTotal() {
        return total.get();
    }

    public void setTotal(double value) {
        total.set(value);
    }

    public DoubleProperty TotalProperty() {
        return total;
    }

    public double getAverage() {
        return average.get();
    }

    public void setAverage(double value) {
        average.set(value);
    }

    public DoubleProperty averageProperty() {
        return average;
    }

    public double getMin() {
        return min.get();
    }

    public void setMin(double value) {
        min.set(value);
    }

    public DoubleProperty minProperty() {
        return min;
    }

    public double getMax() {
        return max.get();
    }

    public void setMax(double value) {
        max.set(value);
    }

    public DoubleProperty maxProperty() {
        return max;
    }

    public double getSdv() {
        return sdv.get();
    }

    public void setSdv(double value) {
        sdv.set(value);
    }

    public DoubleProperty sdvProperty() {
        return sdv;
    }
    
    private void calculateValues(ObservableList<Sales> filteredData){
        total.set(filteredData.stream().map((val) -> val.getQuantity()).reduce(0, Integer::sum));
        min.set(filteredData.stream().map((val) -> val.getQuantity()).reduce(0, Integer::min));
        max.set(filteredData.stream().map((val) -> val.getQuantity()).reduce(0, Integer::max));        
        average.set(total.get()/ filteredData.size());
        double totalVal = 0.0;
        totalVal = filteredData.stream().map((val) -> Math.pow(val.getQuantity() - average.get(), 2)).reduce(totalVal, (accumulator, _item) -> accumulator + _item);
        sdv.set(Math.sqrt((totalVal/filteredData.size())));
    }
}
