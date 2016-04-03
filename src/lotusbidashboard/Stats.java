package lotusbidashboard;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;

/**
 *
 * @author John Harvey - fixes by Jamie Deville
 *
 * Class holds additional stats gathered from Observable List of Sales objects.
 * This object is used in the BoxPlotDrawer Class
 */
public class Stats {

    private final DoubleProperty average = new SimpleDoubleProperty();
    private final DoubleProperty max = new SimpleDoubleProperty();
    private final DoubleProperty min = new SimpleDoubleProperty();
    private final DoubleProperty sdv = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();

    private double q1;
    private double q2;
    private double q3;
    private int dataPoints;

    public double getTotal() {
        return total.get();
    }

    public void setTotal(double value) {
        total.set(value);
    }

    public DoubleProperty totalProperty() {
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

    public double getQ1() {
        return q1;
    }

    public void setQ1(double q1) {
        this.q1 = q1;
    }

    public double getQ2() {
        return q2;
    }

    public void setQ2(double q2) {
        this.q2 = q2;
    }

    public double getQ3() {
        return q3;
    }

    public void setQ3(double q3) {
        this.q3 = q3;
    }

    public int getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(int dataPoints) {
        this.dataPoints = dataPoints;
    }

    public void calculateValues(ObservableList<Sales> filteredData) {
        setDataPoints(filteredData.size());
        DoubleSummaryStatistics stats = filteredData.stream().mapToDouble(Sales::getQuantity).summaryStatistics();
        total.set(stats.getSum());
        min.set(stats.getMin());
        max.set(stats.getMax());
        average.set(stats.getAverage());

        double totalVal = 0.0;
        totalVal = filteredData.stream().map((val) -> Math.pow(val.getQuantity() - average.get(), 2)).reduce(totalVal, (accumulator, _item) -> accumulator + _item);
        sdv.set(Math.sqrt((totalVal / filteredData.size())));

        double quartileTest = filteredData.size() > 0 ? filteredData.size() / 4 : 0;

        //sort
        List<Sales> sortedList = filteredData.stream().sorted((x, y) -> ((Integer) x.getQuantity()).compareTo((Integer) y.getQuantity())).collect(Collectors.toList());
        if (quartileTest > 0) {
            if (quartileTest % 1 != 0) {
                int q1Index = (int) Math.floor(quartileTest);
                int q2Index = (int) Math.floor(quartileTest * 2);
                int q3Index = (int) Math.floor(quartileTest * 3);
                setQ1((sortedList.get(q1Index).getQuantity() + sortedList.get(q1Index + 1).getQuantity()) / 2);
                setQ2((sortedList.get(q2Index).getQuantity() + sortedList.get(q2Index + 1).getQuantity()) / 2);
                setQ3((sortedList.get(q3Index).getQuantity() + sortedList.get(q3Index + 1).getQuantity()) / 2);
            } else {
                setQ1(sortedList.get((int) quartileTest).getQuantity());
                setQ2(sortedList.get((int) quartileTest * 2).getQuantity());
                setQ3(sortedList.get((int) quartileTest * 3).getQuantity());
            }
        }
    }
}
