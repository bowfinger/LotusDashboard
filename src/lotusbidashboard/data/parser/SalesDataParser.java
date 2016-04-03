package lotusbidashboard.data.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lotusbidashboard.Sales;

/**
 *
 * @author Jamie Deville
 */
public class SalesDataParser {
    public ObservableList<Sales> parseJSONData(String json) {
        Type listType = new TypeToken<ArrayList<Sales>>() {}.getType();
        List<Sales> dataList = new Gson().fromJson(json, listType);
       
        //test
        System.out.println(dataList.size());
        return FXCollections.observableArrayList(dataList);
    }
}
