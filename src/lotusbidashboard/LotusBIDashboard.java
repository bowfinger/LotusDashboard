package lotusbidashboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Jamie Deville, John Harvey and Sam Griffiths
 */
public class LotusBIDashboard extends Application {
    
    //private DashboardController dashboard;
    //private LoginController login;
            
    @Override
    public void start(Stage stage) throws Exception {
        //dashboard = new DashboardController();
        //login = new LoginController();
        
        Parent root = FXMLLoader.load(getClass().getResource("LoginFXML.fxml"));
        Scene scene = new Scene(root);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/lotusLogo.png")));
        stage.setTitle("Lotus BI Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
