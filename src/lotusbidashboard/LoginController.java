package lotusbidashboard;

import lotusbidashboard.services.LogInService;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author Jamie Deville
 */
public class LoginController implements Initializable {
        
    @FXML
    PasswordField passwordTextField;
    
    @FXML
    TextField usernameTextField;
    
    @FXML
    Button logInButton;
    
    @FXML
    Label errorLabel;
    
    @FXML
    ProgressIndicator progressIndicator;
    
    private final LogInService logInService = new LogInService();
    
    //DEBUG TO SKIP LOGIN - set to false for final
    private static final boolean ISDEBUG = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //set logInTask events
        logInButton.disableProperty().bind(progressIndicator.visibleProperty());
        usernameTextField.disableProperty().bind(progressIndicator.visibleProperty());
        passwordTextField.disableProperty().bind(progressIndicator.visibleProperty());
        logInService.setOnRunning(event ->{
            progressIndicator.setVisible(true);
        });
        logInService.setOnSucceeded(event ->{
            progressIndicator.setVisible(false);
            if(logInService.getValue().equals(true)){
                try {
                    doLogIn();
                } catch (IOException ex) {
                    Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                errorLabel.setText("There was an error with your credentials.");
            }
        });
    }

    public void onLogInButtonClick(ActionEvent event){
        
        String user = usernameTextField.getText();
        String pass = passwordTextField.getText();
        
        //auto set login no matter what
        if(ISDEBUG){
            user = "Lotus";
            pass = "Lotus";
        }
                
        if(checkLoginDetails(user, pass)){
            logInService.setCredentials(user, pass);
            logInService.reset();
            logInService.start();
        }else{
            errorLabel.setText("Please enter you credentials");
        }
    }

    //only checks length of text
    //will require extra validation for production
    private boolean checkLoginDetails(String user, String pass) {
        return user.length() > 0 && pass.length() > 0;
    }

    private void doLogIn() throws IOException {
        Stage stage =(Stage)this.logInButton.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("DashboardFXML.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        
        //get reference to current display device
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries to visible bounds of the main screen
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        
        stage.show();
    }
}
