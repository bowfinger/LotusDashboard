package lotusbidashboard;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import lotusbidashboard.services.SalesService;
import lotusbidashboard.styling.StyleSupplier;

/**
 *
 * @author Jamie Deville
 */
public class SettingsController implements Initializable{
    
    private static int initialServiceDelay;
    
    @FXML
    private ChoiceBox stylesheetChoiceBox;
    
    @FXML
    private Slider serviceRefreshSlider;
    
    @FXML
    private Label timeLabel;
        
    private final SalesService salesService = SalesService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //set initial label
        int intVal = serviceRefreshSlider.valueProperty().intValue();
        timeLabel.setText(String.format("%d seconds", intVal));
        
        //bind label
        serviceRefreshSlider.valueProperty().addListener(new ChangeListener(){
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                int intVal = ((Double)newValue).intValue();
                timeLabel.setText(String.format("%d seconds", intVal));
            }
        });
        
        //set choice box
//        
//        Application.getUserAgentStylesheet() returns full URL of stylesheet.
//        to avoid excess trimming and additions of "bad" circular references,
//        the stylesheet is just checked for null. If null, the default is being
//        used else the other is being used.
//        
//        NB. this must be changed if more than 1 additional stylesheet is used!
        
        String current = Application.getUserAgentStylesheet();
        System.out.println(current);
        stylesheetChoiceBox.getItems().clear();
        stylesheetChoiceBox.getItems().add("Default");
        stylesheetChoiceBox.getItems().addAll(StyleSupplier.getStyles());
        if(current == null){
            stylesheetChoiceBox.getSelectionModel().selectFirst();
        }else{
            stylesheetChoiceBox.getSelectionModel().select(1);
        }
        int delay = salesService.getDelayAsInt();
        initialServiceDelay = delay;
        serviceRefreshSlider.valueProperty().set(delay);
    }
    
    @FXML
    public void onSaveSettingsButtonClick(ActionEvent event) {
        //get value from slider or label
        int newDelay = (int)serviceRefreshSlider.valueProperty().get();
        if(newDelay != initialServiceDelay){
            salesService.setNewDelay(newDelay);
            salesService.restart();
        }
        
        //reset service update checkbox????

        //set style
        String style = stylesheetChoiceBox.valueProperty().getValue().toString();
        String styleUrl = StyleSupplier.getStyle(style);
        if(styleUrl != null){
            
            //StyleManager adds css over the top of the original for all scenes
            //fails to return value in Application.getUserAgentStyleSheet();
            //StyleManager.getInstance().addUserAgentStylesheet(getClass().getResource(styleUrl).toExternalForm());
            Application.setUserAgentStylesheet(
                getClass().getResource(styleUrl).toExternalForm()
            );
        }else{
            //set to default - this calls static method in Control class
            Application.setUserAgentStylesheet(null);
        }
        
        //close this stage
        ((Node)(event.getSource())).getScene().getWindow().hide();
    }
    
    @FXML
    public void onCancelButtonClick(ActionEvent event){
        ((Node)(event.getSource())).getScene().getWindow().hide();
    }
}
