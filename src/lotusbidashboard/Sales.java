package lotusbidashboard;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Jamie
 */
public class Sales {
    private IntegerProperty _year;
    
    @SerializedName("Year")
    private int year;
    public int getYear() {
        return (_year != null)? _year.get() : year;
    }
    public void setYear(int value) {
        if (_year != null) {
            _year.set(value);
        } else {
            year = value;
        }
    }
    public IntegerProperty yearProperty() {
        if (_year == null) {
            _year = new SimpleIntegerProperty(year);
        }
        return _year;
    }
    
    private IntegerProperty _quarter;
    
    @SerializedName("QTR")
    private int quarter;
    public int getQTR() {
        return (_quarter != null)? _quarter.get() : quarter;
    }
    public void setQTR(int value) {
        if (_quarter != null) {
            _quarter.set(value);
        } else {
            quarter = value;
        }
    }
    public IntegerProperty quarterProperty() {
        if (_quarter == null) {
            _quarter = new SimpleIntegerProperty(quarter);
        }
        return _quarter;
    }
    
    private StringProperty _region;
    
    @SerializedName("Region")
    private String region;
    public String getRegion() {
        return (_region != null)? _region.get() : region;
    }
    public void setRegion(String value) {
        if (_region != null) {
            _region.set(value);
        } else {
            region = value;
        }
    }
    public StringProperty regionProperty() {
        if (_region == null) {
            _region = new SimpleStringProperty(region);
        }
        return _region;
    }

    private StringProperty _vehicle;
    
    @SerializedName("Vehicle")
    private String vehicle;
    public String getVehicle() {
        return (_vehicle != null)? _vehicle.get() : vehicle;
    }
    public void setVehicle(String value) {
        if (_vehicle != null) {
            _vehicle.set(value);
        } else {
            vehicle = value;
        }
    }
    public StringProperty vehicleProperty() {
        if (_vehicle == null) {
            _vehicle = new SimpleStringProperty(vehicle);
        }
        return _vehicle;
    }
    
    private IntegerProperty _quantity;
    
    @SerializedName("Quantity")
    private int quantity;
    public int getQuantity() {
        return (_quantity != null)? _quantity.get() : quantity;
    }
    public void setQuantity(int value) {
        if (_quantity != null) {
            _quantity.set(value);
        } else {
            quantity = value;
        }
    }
    public IntegerProperty quantityProperty() {
        if (_quantity == null) {
            _quantity = new SimpleIntegerProperty(quantity);
        }
        return _quantity;
    }
}
